from __future__ import annotations

from typing import List, Optional

from pydantic import BaseModel, ConfigDict, Field, field_validator


class ServiceDependencyQueryDto(BaseModel):
    model_config = ConfigDict(frozen=True)

    service_name: str = Field(alias="serviceName")
    layer: str = "network"
    depth: int = 3

    @field_validator("depth")
    @classmethod
    def _depth_positive(cls, value: int) -> int:
        return value if value >= 1 else 3


class BlastRadiusRequestDto(BaseModel):
    model_config = ConfigDict(frozen=True)

    service_name: str = Field(alias="serviceName")
    ccy_pair: Optional[str] = Field(default=None, alias="ccyPair")


class TopologyDiffRequestDto(BaseModel):
    model_config = ConfigDict(frozen=True)

    from_ts: str = Field(alias="fromTs")
    to_ts: str = Field(alias="toTs")


class TraceOutlierQueryDto(BaseModel):
    model_config = ConfigDict(frozen=True)

    service_name: str = Field(alias="serviceName")
    min_latency_ms: int = Field(default=10, alias="minLatencyMs")
    limit: int = 50

    @field_validator("min_latency_ms")
    @classmethod
    def _latency_nonneg(cls, value: int) -> int:
        return value if value >= 0 else 10

    @field_validator("limit")
    @classmethod
    def _limit_positive(cls, value: int) -> int:
        return value if value >= 1 else 50


class ConsumerLagQueryDto(BaseModel):
    model_config = ConfigDict(frozen=True)

    consumer_group: str = Field(alias="consumerGroup")


class SyntheticSeedRequestDto(BaseModel):
    model_config = ConfigDict(frozen=True)

    num_batches: int = Field(default=10, alias="numBatches")
    batch_size: int = Field(default=1000, alias="batchSize")
    datasets: List[str] = Field(default_factory=list)

    @field_validator("num_batches", "batch_size")
    @classmethod
    def _positive(cls, value: int) -> int:
        return value if value >= 1 else 1


class SkewedLoadSimRequestDto(BaseModel):
    model_config = ConfigDict(frozen=True)

    target_rps: int = Field(default=1000, alias="targetRps")
    duration_sec: int = Field(default=60, alias="durationSec")
    ccy_pair: str = Field(default="EURUSD", alias="ccyPair")


class LpVenueHaltRequestDto(BaseModel):
    model_config = ConfigDict(frozen=True)

    lp_id: str = Field(alias="lpId")
    reason: str
    dry_run: bool = Field(default=True, alias="dryRun")
    approval_token: Optional[str] = Field(default=None, alias="approvalToken")
