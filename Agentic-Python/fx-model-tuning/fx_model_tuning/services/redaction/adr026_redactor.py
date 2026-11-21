from __future__ import annotations

import re
from typing import Any

from pydantic import BaseModel, ConfigDict


class RedactionReportDto(BaseModel):
    model_config = ConfigDict(frozen=True)

    fields_touched: tuple[str, ...]
    replacements: int


class Adr026Redactor:
    """Strip counterparty account IDs and raw wholesale LP spreads (ADR-026)."""

    _ACCOUNT_RE = re.compile(
        r"\b(?:acct|account|client[_-]?id)[-_:]?\s*[A-Z0-9-]{6,}\b",
        re.IGNORECASE,
    )
    _SPREAD_RE = re.compile(
        r"\b(?:raw[_-]?spread|wholesale[_-]?spread|lp[_-]?spread)\s*[:=]\s*[-+]?\d+(?:\.\d+)?\b",
        re.IGNORECASE,
    )
    _SENSITIVE_KEYS = {
        "accountId",
        "account_id",
        "clientAccount",
        "counterpartyAccount",
        "rawSpread",
        "raw_spread",
        "wholesaleSpread",
        "wholesale_spread",
        "lpSpreadBps",
        "lp_spread_bps",
        "bidAsk",
        "creditBalance",
        "nopLimit",
    }

    def redact_mapping(self, payload: dict[str, Any]) -> tuple[dict[str, Any], RedactionReportDto]:
        touched: list[str] = []
        replacements = 0
        cleaned: dict[str, Any] = {}
        for key, value in payload.items():
            if key in self._SENSITIVE_KEYS:
                cleaned[key] = "[REDACTED]"
                touched.append(key)
                replacements += 1
                continue
            if isinstance(value, str):
                new_value, count = self._redact_text(value)
                cleaned[key] = new_value
                replacements += count
                if count:
                    touched.append(key)
            elif isinstance(value, dict):
                nested, report = self.redact_mapping(value)
                cleaned[key] = nested
                replacements += report.replacements
                touched.extend(f"{key}.{t}" for t in report.fields_touched)
            else:
                cleaned[key] = value
        return cleaned, RedactionReportDto(
            fields_touched=tuple(dict.fromkeys(touched)),
            replacements=replacements,
        )

    def _redact_text(self, text: str) -> tuple[str, int]:
        count = 0
        out, n = self._ACCOUNT_RE.subn("[REDACTED_ACCOUNT]", text)
        count += n
        out, n = self._SPREAD_RE.subn("raw_spread=[REDACTED]", out)
        count += n
        return out, count
