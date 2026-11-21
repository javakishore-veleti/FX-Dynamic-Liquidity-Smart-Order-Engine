from __future__ import annotations

import os

from fastapi import FastAPI


class ObservabilitySetup:
    """Micrometer-equivalent: Prometheus /metrics + optional OTLP export."""

    def __init__(self, service_name: str) -> None:
        self._service_name = service_name
        self._otlp_endpoint = os.getenv(
            "OTEL_EXPORTER_OTLP_ENDPOINT",
            "http://localhost:4318",
        )
        self._otel_enabled = os.getenv("OTEL_SDK_DISABLED", "false").lower() not in {
            "1",
            "true",
            "yes",
        }
        self._metrics_enabled = os.getenv("PROMETHEUS_METRICS_ENABLED", "true").lower() in {
            "1",
            "true",
            "yes",
        }

    def configure(self, app: FastAPI) -> None:
        if self._metrics_enabled:
            self._mount_prometheus(app)
        if self._otel_enabled:
            self._configure_otel()

    def _mount_prometheus(self, app: FastAPI) -> None:
        try:
            from prometheus_fastapi_instrumentator import Instrumentator
        except Exception:  # noqa: BLE001
            @app.get("/metrics")
            def metrics_unavailable() -> dict:
                return {"status": "prometheus_fastapi_instrumentator_not_installed"}

            return
        Instrumentator(
            should_group_status_codes=True,
            excluded_handlers=["/metrics", "/health", "/docs", "/openapi.json", "/redoc"],
        ).instrument(app).expose(app, endpoint="/metrics", include_in_schema=False)

    def _configure_otel(self) -> None:
        try:
            from opentelemetry import trace
            from opentelemetry.exporter.otlp.proto.http.trace_exporter import OTLPSpanExporter
            from opentelemetry.instrumentation.fastapi import FastAPIInstrumentor
            from opentelemetry.instrumentation.httpx import HTTPXClientInstrumentor
            from opentelemetry.sdk.resources import Resource
            from opentelemetry.sdk.trace import TracerProvider
            from opentelemetry.sdk.trace.export import BatchSpanProcessor
        except Exception:  # noqa: BLE001
            return

        resource = Resource.create({"service.name": self._service_name})
        provider = TracerProvider(resource=resource)
        endpoint = self._otlp_endpoint.rstrip("/")
        if not endpoint.endswith("/v1/traces"):
            endpoint = f"{endpoint}/v1/traces"
        provider.add_span_processor(BatchSpanProcessor(OTLPSpanExporter(endpoint=endpoint)))
        trace.set_tracer_provider(provider)
        HTTPXClientInstrumentor().instrument()
        # FastAPIInstrumentor applied after app creation by caller via instrument_app
        self._fastapi_instrumentor = FastAPIInstrumentor

    def instrument_app(self, app: FastAPI) -> None:
        instrumentor = getattr(self, "_fastapi_instrumentor", None)
        if instrumentor is not None:
            instrumentor.instrument_app(app)
