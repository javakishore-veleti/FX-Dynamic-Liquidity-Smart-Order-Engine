from __future__ import annotations

from fastapi import APIRouter
from fastapi.responses import JSONResponse

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
from fx_topology_mcp.business.facades.mcp_tool_facade import McpToolFacade


class McpToolsController:
    def __init__(self, facade: McpToolFacade) -> None:
        self._facade = facade
        self.router = APIRouter(prefix="/mcp/tools", tags=["mcp-tools"])
        self.router.add_api_route(
            "/get_service_dependencies", self.get_service_dependencies, methods=["POST"]
        )
        self.router.add_api_route(
            "/calculate_blast_radius", self.calculate_blast_radius, methods=["POST"]
        )
        self.router.add_api_route(
            "/diff_topology_snapshots", self.diff_topology_snapshots, methods=["POST"]
        )
        self.router.add_api_route(
            "/query_iceberg_outlier_traces", self.query_iceberg_outlier_traces, methods=["POST"]
        )
        self.router.add_api_route(
            "/get_kafka_consumer_lag", self.get_kafka_consumer_lag, methods=["POST"]
        )
        self.router.add_api_route(
            "/trigger_synthetic_seed", self.trigger_synthetic_seed, methods=["POST"]
        )
        self.router.add_api_route(
            "/trigger_skewed_load_sim", self.trigger_skewed_load_sim, methods=["POST"]
        )
        self.router.add_api_route(
            "/propose_or_execute_lp_halt", self.propose_or_execute_lp_halt, methods=["POST"]
        )

    def get_service_dependencies(self, dto: ServiceDependencyQueryDto) -> JSONResponse:
        return self._to_response(self._facade.get_service_dependencies(dto))

    def calculate_blast_radius(self, dto: BlastRadiusRequestDto) -> JSONResponse:
        return self._to_response(self._facade.calculate_blast_radius(dto))

    def diff_topology_snapshots(self, dto: TopologyDiffRequestDto) -> JSONResponse:
        return self._to_response(self._facade.diff_topology_snapshots(dto))

    def query_iceberg_outlier_traces(self, dto: TraceOutlierQueryDto) -> JSONResponse:
        return self._to_response(self._facade.query_iceberg_outlier_traces(dto))

    def get_kafka_consumer_lag(self, dto: ConsumerLagQueryDto) -> JSONResponse:
        return self._to_response(self._facade.get_kafka_consumer_lag(dto))

    def trigger_synthetic_seed(self, dto: SyntheticSeedRequestDto) -> JSONResponse:
        return self._to_response(self._facade.trigger_synthetic_seed(dto))

    def trigger_skewed_load_sim(self, dto: SkewedLoadSimRequestDto) -> JSONResponse:
        return self._to_response(self._facade.trigger_skewed_load_sim(dto))

    def propose_or_execute_lp_halt(self, dto: LpVenueHaltRequestDto) -> JSONResponse:
        return self._to_response(self._facade.propose_or_execute_lp_halt(dto))

    def _to_response(self, result: McpToolResultDto) -> JSONResponse:
        payload = result.model_dump(mode="json")
        if result.success:
            return JSONResponse(status_code=200, content=payload)
        if result.upstream_status == "400":
            return JSONResponse(status_code=400, content=payload)
        if result.upstream_status == "503":
            return JSONResponse(status_code=503, content=payload)
        return JSONResponse(status_code=502, content=payload)
