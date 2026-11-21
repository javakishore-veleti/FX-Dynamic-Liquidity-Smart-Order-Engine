from __future__ import annotations

import httpx

from fx_topology_mcp.services.dtos.upstream_dtos import (
    TopologyBlastCallDto,
    TopologyDepsCallDto,
    TopologyDiffCallDto,
    TopologyTracesCallDto,
    UpstreamCallResultDto,
)


class TopologyServiceAdapter:
    def __init__(self, base_url: str, timeout_seconds: float = 5.0) -> None:
        self._base_url = base_url.rstrip("/")
        self._timeout = timeout_seconds

    def get_service_dependencies(self, dto: TopologyDepsCallDto) -> UpstreamCallResultDto:
        layer = (dto.layer or "network").lower()
        if layer == "ipc":
            path = "/api/v1/topology/graph/ipc"
        elif layer in {"trace", "traces"}:
            path = "/api/v1/topology/traces/outliers"
        else:
            path = "/api/v1/topology/graph/network"
        return self._get(path, {"service": dto.service_name, "depth": dto.depth})

    def calculate_blast_radius(self, dto: TopologyBlastCallDto) -> UpstreamCallResultDto:
        return self._post(
            "/api/v1/topology/blast-radius",
            {"serviceName": dto.service_name, "ccyPair": dto.ccy_pair or ""},
        )

    def diff_snapshots(self, dto: TopologyDiffCallDto) -> UpstreamCallResultDto:
        return self._get("/api/v1/topology/diff", {"from": dto.from_ts, "to": dto.to_ts})

    def query_outlier_traces(self, dto: TopologyTracesCallDto) -> UpstreamCallResultDto:
        return self._get(
            "/api/v1/topology/traces/outliers",
            {
                "service": dto.service_name,
                "minLatencyMs": dto.min_latency_ms,
                "limit": dto.limit,
            },
        )

    def _get(self, path: str, params: dict) -> UpstreamCallResultDto:
        try:
            with httpx.Client(base_url=self._base_url, timeout=self._timeout) as client:
                response = client.get(path, params=params)
                if response.is_success:
                    return UpstreamCallResultDto(success=True, data=response.json(), status=response.status_code)
                return UpstreamCallResultDto(
                    success=False,
                    status=response.status_code,
                    error=response.text[:500] or f"upstream HTTP {response.status_code}",
                )
        except httpx.HTTPError as exc:
            return UpstreamCallResultDto(success=False, status=503, error=f"topology-service unavailable: {exc}")

    def _post(self, path: str, body: dict) -> UpstreamCallResultDto:
        try:
            with httpx.Client(base_url=self._base_url, timeout=self._timeout) as client:
                response = client.post(path, json=body)
                if response.is_success:
                    return UpstreamCallResultDto(success=True, data=response.json(), status=response.status_code)
                return UpstreamCallResultDto(
                    success=False,
                    status=response.status_code,
                    error=response.text[:500] or f"upstream HTTP {response.status_code}",
                )
        except httpx.HTTPError as exc:
            return UpstreamCallResultDto(success=False, status=503, error=f"topology-service unavailable: {exc}")
