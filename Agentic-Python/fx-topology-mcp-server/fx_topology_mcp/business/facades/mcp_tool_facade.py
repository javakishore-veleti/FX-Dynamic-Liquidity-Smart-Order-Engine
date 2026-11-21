from __future__ import annotations

import json
import time
from typing import Any, Callable
from uuid import UUID

from fx_topology_mcp.business.dtos.invocation_dtos import AuditBeginDto, AuditCompleteDto, AuditFailDto
from fx_topology_mcp.business.dtos.tool_contract_dtos import (
    BlastRadiusRequestDto,
    ConsumerLagQueryDto,
    LpVenueHaltRequestDto,
    McpToolResultDto,
    ServiceDependencyQueryDto,
    SkewedLoadSimRequestDto,
    SyntheticSeedRequestDto,
    TopologyDiffRequestDto,
    TraceOutlierQueryDto,
)
from fx_topology_mcp.business.facades.mcp_tool_catalog import McpToolCatalog
from fx_topology_mcp.persistence.dtos.audit_record_dtos import (
    PersistCompleteDto,
    PersistFailDto,
    PersistInvocationDto,
)
from fx_topology_mcp.persistence.repositories.mcp_invocation_repository import McpInvocationRepository
from fx_topology_mcp.services.adapters.admin_service_adapter import AdminServiceAdapter
from fx_topology_mcp.services.adapters.topology_service_adapter import TopologyServiceAdapter
from fx_topology_mcp.services.dtos.upstream_dtos import (
    AdminHaltCallDto,
    AdminSeedCallDto,
    AdminSkewCallDto,
    TopologyBlastCallDto,
    TopologyDepsCallDto,
    TopologyDiffCallDto,
    TopologyTracesCallDto,
    UpstreamCallResultDto,
)


class McpToolFacade:
    def __init__(
        self,
        topology_adapter: TopologyServiceAdapter,
        admin_adapter: AdminServiceAdapter,
        audit_repository: McpInvocationRepository,
        kafka_enabled: bool = False,
    ) -> None:
        self._topology = topology_adapter
        self._admin = admin_adapter
        self._audit = audit_repository
        self._kafka_enabled = kafka_enabled

    def get_service_dependencies(self, dto: ServiceDependencyQueryDto) -> McpToolResultDto:
        return self._invoke(
            "get_service_dependencies",
            dto,
            McpToolCatalog.SAFETY_READ_ONLY,
            True,
            lambda: self._topology.get_service_dependencies(
                TopologyDepsCallDto(service_name=dto.service_name, layer=dto.layer, depth=dto.depth)
            ),
        )

    def calculate_blast_radius(self, dto: BlastRadiusRequestDto) -> McpToolResultDto:
        return self._invoke(
            "calculate_blast_radius",
            dto,
            McpToolCatalog.SAFETY_READ_ONLY,
            True,
            lambda: self._topology.calculate_blast_radius(
                TopologyBlastCallDto(service_name=dto.service_name, ccy_pair=dto.ccy_pair)
            ),
        )

    def diff_topology_snapshots(self, dto: TopologyDiffRequestDto) -> McpToolResultDto:
        return self._invoke(
            "diff_topology_snapshots",
            dto,
            McpToolCatalog.SAFETY_READ_ONLY,
            True,
            lambda: self._topology.diff_snapshots(
                TopologyDiffCallDto(from_ts=dto.from_ts, to_ts=dto.to_ts)
            ),
        )

    def query_iceberg_outlier_traces(self, dto: TraceOutlierQueryDto) -> McpToolResultDto:
        return self._invoke(
            "query_iceberg_outlier_traces",
            dto,
            McpToolCatalog.SAFETY_READ_ONLY,
            True,
            lambda: self._topology.query_outlier_traces(
                TopologyTracesCallDto(
                    service_name=dto.service_name,
                    min_latency_ms=dto.min_latency_ms,
                    limit=dto.limit,
                )
            ),
        )

    def get_kafka_consumer_lag(self, dto: ConsumerLagQueryDto) -> McpToolResultDto:
        def _lag() -> UpstreamCallResultDto:
            data = {
                "consumerGroup": dto.consumer_group,
                "kafkaEnabled": self._kafka_enabled,
                "lagTotal": 0,
                "partitions": [],
                "note": (
                    "kafka feature toggle OFF — returning empty lag snapshot"
                    if not self._kafka_enabled
                    else "Kafka Admin lag query not configured; empty snapshot returned"
                ),
            }
            return UpstreamCallResultDto(success=True, data=data, status=200)

        return self._invoke("get_kafka_consumer_lag", dto, McpToolCatalog.SAFETY_READ_ONLY, True, _lag)

    def trigger_synthetic_seed(self, dto: SyntheticSeedRequestDto) -> McpToolResultDto:
        return self._invoke(
            "trigger_synthetic_seed",
            dto,
            McpToolCatalog.SAFETY_CONTROLLED_WRITE,
            False,
            lambda: self._admin.trigger_synthetic_seed(
                AdminSeedCallDto(
                    num_batches=dto.num_batches,
                    batch_size=dto.batch_size,
                    datasets=dto.datasets,
                )
            ),
        )

    def trigger_skewed_load_sim(self, dto: SkewedLoadSimRequestDto) -> McpToolResultDto:
        return self._invoke(
            "trigger_skewed_load_sim",
            dto,
            McpToolCatalog.SAFETY_CONTROLLED_WRITE,
            False,
            lambda: self._admin.trigger_skewed_load_sim(
                AdminSkewCallDto(
                    target_rps=dto.target_rps,
                    duration_sec=dto.duration_sec,
                    ccy_pair=dto.ccy_pair,
                )
            ),
        )

    def propose_or_execute_lp_halt(self, dto: LpVenueHaltRequestDto) -> McpToolResultDto:
        dry_run = True if dto.dry_run is None else dto.dry_run
        if not dry_run and not dto.approval_token:
            start = time.perf_counter()
            begin = AuditBeginDto(
                tool_name="propose_or_execute_lp_halt",
                request_payload=dto.model_dump(by_alias=True),
                safety_tier=McpToolCatalog.SAFETY_HIGH_RISK,
                dry_run=False,
            )
            invocation_id = self._begin(begin)
            duration_ms = int((time.perf_counter() - start) * 1000)
            error = "approvalToken required when dryRun=false (ADR-032 maker-checker)"
            self._fail(AuditFailDto(invocation_id=invocation_id, error_details=error, duration_ms=duration_ms))
            return McpToolResultDto(
                success=False,
                tool_name="propose_or_execute_lp_halt",
                error=error,
                upstream_status="400",
                duration_ms=duration_ms,
            )
        return self._invoke(
            "propose_or_execute_lp_halt",
            dto,
            McpToolCatalog.SAFETY_HIGH_RISK,
            dry_run,
            lambda: self._admin.propose_or_execute_lp_halt(
                AdminHaltCallDto(
                    lp_id=dto.lp_id,
                    reason=dto.reason,
                    dry_run=dry_run,
                    approval_token=dto.approval_token,
                )
            ),
        )

    def _invoke(
        self,
        tool_name: str,
        request_dto: Any,
        safety_tier: str,
        dry_run: bool,
        action: Callable[[], UpstreamCallResultDto],
    ) -> McpToolResultDto:
        start = time.perf_counter()
        begin = AuditBeginDto(
            tool_name=tool_name,
            request_payload=request_dto.model_dump(by_alias=True),
            safety_tier=safety_tier,
            dry_run=dry_run,
        )
        invocation_id = self._begin(begin)
        try:
            result = action()
            duration_ms = int((time.perf_counter() - start) * 1000)
            if result.success:
                self._complete(
                    AuditCompleteDto(
                        invocation_id=invocation_id,
                        response_payload=result.data,
                        duration_ms=duration_ms,
                    )
                )
                return McpToolResultDto(
                    success=True,
                    tool_name=tool_name,
                    data=result.data,
                    upstream_status=str(result.status),
                    duration_ms=duration_ms,
                )
            self._fail(
                AuditFailDto(
                    invocation_id=invocation_id,
                    error_details=result.error or "upstream failure",
                    duration_ms=duration_ms,
                )
            )
            return McpToolResultDto(
                success=False,
                tool_name=tool_name,
                error=result.error,
                upstream_status=str(result.status),
                duration_ms=duration_ms,
            )
        except Exception as exc:  # noqa: BLE001 — surface graceful MCP error envelope
            duration_ms = int((time.perf_counter() - start) * 1000)
            self._fail(
                AuditFailDto(invocation_id=invocation_id, error_details=str(exc), duration_ms=duration_ms)
            )
            return McpToolResultDto(
                success=False,
                tool_name=tool_name,
                error=str(exc),
                upstream_status="500",
                duration_ms=duration_ms,
            )

    def _begin(self, dto: AuditBeginDto) -> UUID:
        return self._audit.begin_invocation(
            PersistInvocationDto(
                tool_name=dto.tool_name,
                request_payload=json.dumps(dto.request_payload),
                safety_tier=dto.safety_tier,
                dry_run=dto.dry_run,
                triggered_by=dto.triggered_by,
            )
        ).invocation_id

    def _complete(self, dto: AuditCompleteDto) -> None:
        self._audit.complete_invocation(
            PersistCompleteDto(
                invocation_id=dto.invocation_id,
                response_payload=json.dumps(dto.response_payload),
                duration_ms=dto.duration_ms,
            )
        )

    def _fail(self, dto: AuditFailDto) -> None:
        self._audit.fail_invocation(
            PersistFailDto(
                invocation_id=dto.invocation_id,
                error_details=dto.error_details,
                duration_ms=dto.duration_ms,
                response_payload=None if dto.response_payload is None else json.dumps(dto.response_payload),
            )
        )
