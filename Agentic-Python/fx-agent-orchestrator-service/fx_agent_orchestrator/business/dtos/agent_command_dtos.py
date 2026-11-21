from __future__ import annotations

from datetime import datetime
from typing import List, Optional
from uuid import UUID

from pydantic import BaseModel, ConfigDict


class BlastRadiusCommandDto(BaseModel):
    model_config = ConfigDict(frozen=True)

    service_name: Optional[str] = None
    ccy_pair: Optional[str] = None
    from_ts: Optional[str] = None
    to_ts: Optional[str] = None
    depth: int = 3
    dry_run: bool = True
    approval_token: Optional[str] = None


class BackpressureCommandDto(BaseModel):
    model_config = ConfigDict(frozen=True)

    consumer_group: Optional[str] = None
    service_name: Optional[str] = None
    apply_scaling: bool = False
    dry_run: bool = True
    approval_token: Optional[str] = None


class IncidentRcaCommandDto(BaseModel):
    model_config = ConfigDict(frozen=True)

    service_name: Optional[str] = None
    min_latency_ms: int = 10
    limit: int = 50
    ccy_pair: Optional[str] = None
    lp_id: Optional[str] = None
    reason: Optional[str] = None
    dry_run: bool = True
    approval_token: Optional[str] = None


class ChaosSimCommandDto(BaseModel):
    model_config = ConfigDict(frozen=True)

    num_batches: int = 10
    batch_size: int = 1000
    datasets: Optional[List[str]] = None
    target_rps: int = 50_000
    duration_sec: int = 60
    ccy_pair: str = "EURUSD"
    dry_run: bool = True
    approval_token: Optional[str] = None


class AgentAcceptedResultDto(BaseModel):
    model_config = ConfigDict(frozen=True)

    wf_exec_id: UUID
    agent_type: str
    status: str
    dry_run: bool
    triggered_by: str
    accepted_at: datetime
