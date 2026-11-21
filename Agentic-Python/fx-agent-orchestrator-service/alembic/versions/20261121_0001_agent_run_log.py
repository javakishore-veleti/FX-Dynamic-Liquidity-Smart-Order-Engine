"""create agent_run_log

Revision ID: 20261121_0001
Revises:
Create Date: 2026-11-21
"""
from __future__ import annotations

from typing import Sequence, Union

import sqlalchemy as sa
from alembic import op

revision: str = "20261121_0001"
down_revision: Union[str, None] = None
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    op.create_table(
        "agent_run_log",
        sa.Column("run_id", sa.String(length=36), primary_key=True),
        sa.Column("agent_type", sa.String(length=50), nullable=False),
        sa.Column("dry_run", sa.Boolean(), nullable=False, server_default=sa.true()),
        sa.Column("status", sa.String(length=20), nullable=False),
        sa.Column("request_params", sa.Text(), nullable=False),
        sa.Column("mcp_tools_invoked", sa.Text(), nullable=True),
        sa.Column("result_summary", sa.Text(), nullable=True),
        sa.Column("approval_token", sa.String(length=200), nullable=True),
        sa.Column("triggered_by", sa.String(length=100), nullable=False, server_default="LANGGRAPH_AGENT"),
        sa.Column("started_at", sa.DateTime(timezone=True), nullable=False),
        sa.Column("updated_at", sa.DateTime(timezone=True), nullable=False),
        sa.Column("completed_at", sa.DateTime(timezone=True), nullable=True),
        sa.Column("duration_ms", sa.Integer(), nullable=True),
        sa.Column("error_details", sa.Text(), nullable=True),
    )


def downgrade() -> None:
    op.drop_table("agent_run_log")
