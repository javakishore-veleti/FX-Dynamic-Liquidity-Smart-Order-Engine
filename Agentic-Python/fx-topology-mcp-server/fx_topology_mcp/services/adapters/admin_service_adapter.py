from __future__ import annotations

import httpx

from fx_topology_mcp.services.dtos.upstream_dtos import (
    AdminHaltCallDto,
    AdminSeedCallDto,
    AdminSkewCallDto,
    UpstreamCallResultDto,
)


class AdminServiceAdapter:
    def __init__(self, base_url: str, timeout_seconds: float = 5.0) -> None:
        self._base_url = base_url.rstrip("/")
        self._timeout = timeout_seconds

    def trigger_synthetic_seed(self, dto: AdminSeedCallDto) -> UpstreamCallResultDto:
        return self._post(
            "/api/v1/admin/synthetic/seed",
            {
                "numBatches": dto.num_batches,
                "batchSize": dto.batch_size,
                "datasets": dto.datasets,
            },
        )

    def trigger_skewed_load_sim(self, dto: AdminSkewCallDto) -> UpstreamCallResultDto:
        return self._post(
            "/api/v1/admin/load-sim/skewed",
            {
                "targetRps": dto.target_rps,
                "durationSec": dto.duration_sec,
                "ccyPair": dto.ccy_pair,
            },
        )

    def propose_or_execute_lp_halt(self, dto: AdminHaltCallDto) -> UpstreamCallResultDto:
        body: dict = {
            "lpId": dto.lp_id,
            "reason": dto.reason,
            "dryRun": dto.dry_run,
        }
        if dto.approval_token:
            body["approvalToken"] = dto.approval_token
        return self._post("/api/v1/admin/lp/halt", body)

    def _post(self, path: str, body: dict) -> UpstreamCallResultDto:
        try:
            with httpx.Client(base_url=self._base_url, timeout=self._timeout) as client:
                response = client.post(path, json=body)
                if response.is_success:
                    payload = response.json() if response.content else {}
                    return UpstreamCallResultDto(success=True, data=payload, status=response.status_code)
                return UpstreamCallResultDto(
                    success=False,
                    status=response.status_code,
                    error=response.text[:500] or f"upstream HTTP {response.status_code}",
                )
        except httpx.HTTPError as exc:
            return UpstreamCallResultDto(success=False, status=503, error=f"admin-service unavailable: {exc}")
