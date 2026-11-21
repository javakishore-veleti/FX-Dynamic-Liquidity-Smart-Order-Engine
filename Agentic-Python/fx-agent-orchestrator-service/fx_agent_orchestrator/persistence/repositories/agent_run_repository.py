from __future__ import annotations

from datetime import datetime, timezone
from uuid import UUID

from sqlalchemy import create_engine
from sqlalchemy.orm import Session, sessionmaker

from fx_agent_orchestrator.persistence.dtos.run_record_dtos import (
    AgentRunIdDto,
    PersistAgentRunDto,
    PersistRunCompleteDto,
    PersistRunFailDto,
)
from fx_agent_orchestrator.persistence.db_migration import AlembicMigrationRunner
from fx_agent_orchestrator.persistence.entities.agent_run_log import AgentRunLog


class AgentRunRepository:
    """Dedicated local audit store (SQLite by default; mirrors fx_agent_orchestrator_db.agent_run_log)."""

    def __init__(self, database_url: str) -> None:
        AlembicMigrationRunner(database_url).upgrade_to_head()
        self._engine = create_engine(database_url, future=True)
        self._session_factory = sessionmaker(bind=self._engine, expire_on_commit=False)

    def begin_run(self, dto: PersistAgentRunDto) -> AgentRunIdDto:
        now = datetime.now(timezone.utc)
        with Session(self._engine) as session:
            row = AgentRunLog(
                agent_type=dto.agent_type,
                dry_run=dto.dry_run,
                status="QUEUED",
                request_params=dto.request_params,
                approval_token=dto.approval_token,
                triggered_by=dto.triggered_by,
                started_at=now,
                updated_at=now,
            )
            session.add(row)
            session.commit()
            return AgentRunIdDto(run_id=UUID(row.run_id))

    def mark_running(self, dto: AgentRunIdDto) -> AgentRunIdDto:
        with Session(self._engine) as session:
            row = session.get(AgentRunLog, str(dto.run_id))
            if row is not None:
                row.status = "RUNNING"
                row.updated_at = datetime.now(timezone.utc)
                session.commit()
            return dto

    def complete_run(self, dto: PersistRunCompleteDto) -> AgentRunIdDto:
        now = datetime.now(timezone.utc)
        with Session(self._engine) as session:
            row = session.get(AgentRunLog, str(dto.run_id))
            if row is not None:
                row.status = "COMPLETED"
                row.mcp_tools_invoked = dto.mcp_tools_invoked
                row.result_summary = dto.result_summary
                row.duration_ms = dto.duration_ms
                row.completed_at = now
                row.updated_at = now
                session.commit()
            return AgentRunIdDto(run_id=dto.run_id)

    def fail_run(self, dto: PersistRunFailDto) -> AgentRunIdDto:
        now = datetime.now(timezone.utc)
        with Session(self._engine) as session:
            row = session.get(AgentRunLog, str(dto.run_id))
            if row is not None:
                row.status = "FAILED"
                row.error_details = dto.error_details
                row.duration_ms = dto.duration_ms
                row.completed_at = now
                row.updated_at = now
                session.commit()
            return AgentRunIdDto(run_id=dto.run_id)
