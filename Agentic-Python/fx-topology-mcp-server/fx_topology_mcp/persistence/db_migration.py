from __future__ import annotations

from pathlib import Path

from alembic import command
from alembic.config import Config


class AlembicMigrationRunner:
    """Mandatory schema migrations (Python equivalent of Liquibase) — no create_all."""

    def __init__(self, database_url: str, alembic_ini: Path | None = None) -> None:
        root = Path(__file__).resolve().parents[2]
        self._ini = alembic_ini or (root / "alembic.ini")
        self._database_url = database_url

    def upgrade_to_head(self) -> None:
        cfg = Config(str(self._ini))
        cfg.set_main_option("sqlalchemy.url", self._database_url)
        command.upgrade(cfg, "head")
