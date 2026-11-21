from __future__ import annotations

import json
from typing import Any

import httpx
from pydantic import BaseModel, ConfigDict


class A2aTaskRequestDto(BaseModel):
    model_config = ConfigDict(frozen=True)

    skill_id: str
    payload: dict[str, Any]
    peer_base_url: str


class A2aPeerClient:
    """Minimal A2A-style HTTP handoff to a peer agent's REST skill endpoint."""

    def __init__(self, timeout_sec: float = 30.0) -> None:
        self._timeout = timeout_sec

    def fetch_agent_card(self, card_url: str) -> dict[str, Any]:
        with httpx.Client(timeout=self._timeout) as client:
            response = client.get(card_url)
            response.raise_for_status()
            return response.json()

    def invoke_skill(self, request: A2aTaskRequestDto) -> dict[str, Any]:
        card = self.fetch_agent_card(f"{request.peer_base_url.rstrip('/')}/.well-known/agent.json")
        skill = next((s for s in card.get("skills", []) if s.get("id") == request.skill_id), None)
        if not skill or not skill.get("endpoint"):
            return {"error": f"skill_not_found:{request.skill_id}", "card": card.get("name")}
        endpoint = skill["endpoint"]
        # endpoint like "POST /api/v1/agents/..."
        method, path = endpoint.split(" ", 1)
        url = f"{request.peer_base_url.rstrip('/')}{path}"
        with httpx.Client(timeout=self._timeout) as client:
            response = client.request(method.upper(), url, json=request.payload)
            try:
                body = response.json()
            except json.JSONDecodeError:
                body = {"raw": response.text}
            return {"status_code": response.status_code, "body": body}
