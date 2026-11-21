from __future__ import annotations

from typing import List, Optional

from pydantic import BaseModel, ConfigDict, Field


class BlastRadiusAnalyzeRequestDto(BaseModel):
    model_config = ConfigDict(frozen=True, populate_by_name=True)

    service_name: Optional[str] = Field(default=None, alias="serviceName")
    ccy_pair: Optional[str] = Field(default=None, alias="ccyPair")
    from_ts: Optional[str] = Field(default=None, alias="fromTs")
    to_ts: Optional[str] = Field(default=None, alias="toTs")
    depth: int = 3
    dry_run: bool = Field(default=True, alias="dryRun")
    approval_token: Optional[str] = Field(default=None, alias="approvalToken")


class BackpressureAssessRequestDto(BaseModel):
    model_config = ConfigDict(frozen=True, populate_by_name=True)

    consumer_group: Optional[str] = Field(default=None, alias="consumerGroup")
    service_name: Optional[str] = Field(default=None, alias="serviceName")
    apply_scaling: bool = Field(default=False, alias="applyScaling")
    dry_run: bool = Field(default=True, alias="dryRun")
    approval_token: Optional[str] = Field(default=None, alias="approvalToken")


class IncidentRcaInvestigateRequestDto(BaseModel):
    model_config = ConfigDict(frozen=True, populate_by_name=True)

    service_name: Optional[str] = Field(default=None, alias="serviceName")
    min_latency_ms: int = Field(default=10, alias="minLatencyMs")
    limit: int = 50
    ccy_pair: Optional[str] = Field(default=None, alias="ccyPair")
    lp_id: Optional[str] = Field(default=None, alias="lpId")
    reason: Optional[str] = None
    dry_run: bool = Field(default=True, alias="dryRun")
    approval_token: Optional[str] = Field(default=None, alias="approvalToken")


class ChaosSimExecuteRequestDto(BaseModel):
    model_config = ConfigDict(frozen=True, populate_by_name=True)

    num_batches: int = Field(default=10, alias="numBatches")
    batch_size: int = Field(default=1000, alias="batchSize")
    datasets: Optional[List[str]] = None
    target_rps: int = Field(default=50_000, alias="targetRps")
    duration_sec: int = Field(default=60, alias="durationSec")
    ccy_pair: str = Field(default="EURUSD", alias="ccyPair")
    dry_run: bool = Field(default=True, alias="dryRun")
    approval_token: Optional[str] = Field(default=None, alias="approvalToken")
