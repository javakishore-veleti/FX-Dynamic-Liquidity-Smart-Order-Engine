from __future__ import annotations

from datetime import datetime, timezone
from uuid import UUID

from sqlalchemy import create_engine
from sqlalchemy.orm import Session, sessionmaker

from fx_topology_mcp.persistence.dtos.audit_record_dtos import (
    InvocationIdDto,
    PersistCompleteDto,
    PersistFailDto,
    PersistInvocationDto,
)
from fx_topology_mcp.persistence.db_migration import AlembicMigrationRunner
from fx_topology_mcp.persistence.entities.mcp_tool_invocation_log import McpToolInvocationLog


class McpInvocationRepository:
    """Dedicated local audit store (SQLite by default; mirrors fx_topology_mcp_db table)."""

    def __init__(self, database_url: str) -> None:
        AlembicMigrationRunner(database_url).upgrade_to_head()
        self._engine = create_engine(database_url, future=True)
        self._session_factory = sessionmaker(bind=self._engine, expire_on_commit=False)

    def begin_invocation(self, dto: PersistInvocationDto) -> InvocationIdDto:
        with Session(self._engine) as session:
            row = McpToolInvocationLog(
                tool_name=dto.tool_name,
                request_payload=dto.request_payload,
                status="RUNNING",
                safety_tier=dto.safety_tier,
                dry_run=dto.dry_run,
                triggered_by=dto.triggered_by,
                started_at=datetime.now(timezone.utc),
            )
            session.add(row)
            session.commit()
            return InvocationIdDto(invocation_id=UUID(row.invocation_id))

    def complete_invocation(self, dto: PersistCompleteDto) -> InvocationIdDto:
        with Session(self._engine) as session:
            row = session.get(McpToolInvocationLog, str(dto.invocation_id))
            if row is not None:
                row.status = "SUCCESS"
                row.response_payload = dto.response_payload
                row.duration_ms = dto.duration_ms
                row.completed_at = datetime.now(timezone.utc)
                session.commit()
            return InvocationIdDto(invocation_id=dto.invocation_id)

    def fail_invocation(self, dto: PersistFailDto) -> InvocationIdDto:
        with Session(self._engine) as session:
            row = session.get(McpToolInvocationLog, str(dto.invocation_id))
            if row is not None:
                row.status = "FAILED"
                row.error_details = dto.error_details
                row.response_payload = dto.response_payload
                row.duration_ms = dto.duration_ms
                row.completed_at = datetime.now(timezone.utc)
                session.commit()
            return InvocationIdDto(invocation_id=dto.invocation_id)
