from __future__ import annotations

from datetime import datetime, timezone
from uuid import uuid4

from sqlalchemy import Boolean, DateTime, Integer, String, Text
from sqlalchemy.orm import DeclarativeBase, Mapped, mapped_column


class Base(DeclarativeBase):
    pass


class McpToolInvocationLog(Base):
    __tablename__ = "mcp_tool_invocation_log"

    invocation_id: Mapped[str] = mapped_column(String(36), primary_key=True, default=lambda: str(uuid4()))
    tool_name: Mapped[str] = mapped_column(String(100), nullable=False)
    request_payload: Mapped[str] = mapped_column(Text, nullable=False)
    response_payload: Mapped[str | None] = mapped_column(Text, nullable=True)
    status: Mapped[str] = mapped_column(String(20), nullable=False)
    safety_tier: Mapped[str] = mapped_column(String(40), nullable=False)
    dry_run: Mapped[bool] = mapped_column(Boolean, nullable=False, default=True)
    error_details: Mapped[str | None] = mapped_column(Text, nullable=True)
    duration_ms: Mapped[int | None] = mapped_column(Integer, nullable=True)
    triggered_by: Mapped[str] = mapped_column(String(100), nullable=False, default="MCP_CLIENT")
    started_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), nullable=False, default=lambda: datetime.now(timezone.utc)
    )
    completed_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True), nullable=True)
