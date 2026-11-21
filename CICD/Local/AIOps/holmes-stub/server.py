#!/usr/bin/env python3
"""Minimal AIOps triage stub — forwards prompts to LiteLLM OpenAI-compatible API."""
from __future__ import annotations

import json
import os
import urllib.error
import urllib.request
from http.server import BaseHTTPRequestHandler, HTTPServer


GATEWAY = os.environ.get("LLM_GATEWAY_URL", "http://host.docker.internal:4000").rstrip("/")
API_KEY = os.environ.get("LITELLM_MASTER_KEY", "sk-local-fx-sor")


class Handler(BaseHTTPRequestHandler):
    def log_message(self, fmt: str, *args) -> None:  # quieter docker logs
        pass

    def _json(self, code: int, payload: dict) -> None:
        body = json.dumps(payload).encode()
        self.send_response(code)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def do_GET(self) -> None:  # noqa: N802
        if self.path in ("/health", "/"):
            self._json(200, {"status": "UP", "service": "holmes-stub", "gateway": GATEWAY})
            return
        self._json(404, {"error": "not_found"})

    def do_POST(self) -> None:  # noqa: N802
        if self.path != "/investigate":
            self._json(404, {"error": "not_found"})
            return
        length = int(self.headers.get("Content-Length", "0"))
        raw = self.rfile.read(length) if length else b"{}"
        try:
            req = json.loads(raw.decode() or "{}")
        except json.JSONDecodeError:
            self._json(400, {"error": "invalid_json"})
            return
        prompt = req.get("prompt") or req.get("question") or "Summarize local FX SOR AIOps health."
        payload = {
            "model": req.get("model", "fx-fast-triage-tier"),
            "messages": [
                {
                    "role": "system",
                    "content": "You are HolmesGPT-style AIOps for FX SOR. Be concise; cite signals.",
                },
                {"role": "user", "content": prompt},
            ],
            "temperature": 0,
        }
        try:
            http_req = urllib.request.Request(
                f"{GATEWAY}/v1/chat/completions",
                data=json.dumps(payload).encode(),
                headers={
                    "Content-Type": "application/json",
                    "Authorization": f"Bearer {API_KEY}",
                },
                method="POST",
            )
            with urllib.request.urlopen(http_req, timeout=60) as resp:
                data = json.loads(resp.read().decode())
            content = (
                data.get("choices", [{}])[0].get("message", {}).get("content")
                or json.dumps(data)
            )
            self._json(200, {"investigation": content, "gateway": GATEWAY, "stubbed": False})
        except urllib.error.URLError as exc:
            self._json(
                200,
                {
                    "investigation": f"Gateway unreachable ({exc}). Check npm run local:llm-gateway:status.",
                    "gateway": GATEWAY,
                    "stubbed": True,
                },
            )


if __name__ == "__main__":
    HTTPServer(("0.0.0.0", 8088), Handler).serve_forever()
