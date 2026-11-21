from __future__ import annotations

from typing import Any, List, Optional

from pydantic import BaseModel, ConfigDict


class UpstreamCallResultDto(BaseModel):
    model_config = ConfigDict(frozen=True)

    success: bool
    data: Optional[Any] = None
    status: int = 200
    error: Optional[str] = None


class TopologyDepsCallDto(BaseModel):
    model_config = ConfigDict(frozen=True)

    service_name: str
    layer: str
    depth: int


class TopologyBlastCallDto(BaseModel):
    model_config = ConfigDict(frozen=True)

    service_name: str
    ccy_pair: Optional[str] = None


class TopologyDiffCallDto(BaseModel):
    model_config = ConfigDict(frozen=True)

    from_ts: str
    to_ts: str


class TopologyTracesCallDto(BaseModel):
    model_config = ConfigDict(frozen=True)

    service_name: str
    min_latency_ms: int
    limit: int


class AdminSeedCallDto(BaseModel):
    model_config = ConfigDict(frozen=True)

    num_batches: int
    batch_size: int
    datasets: List[str]


class AdminSkewCallDto(BaseModel):
    model_config = ConfigDict(frozen=True)

    target_rps: int
    duration_sec: int
    ccy_pair: str


class AdminHaltCallDto(BaseModel):
    model_config = ConfigDict(frozen=True)

    lp_id: str
    reason: str
    dry_run: bool
    approval_token: Optional[str] = None
