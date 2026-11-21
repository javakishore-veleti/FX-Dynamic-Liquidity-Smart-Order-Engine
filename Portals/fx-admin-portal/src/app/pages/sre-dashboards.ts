import { Component } from '@angular/core';

@Component({
  selector: 'app-sre-dashboards',
  template: `
    <div class="d-flex flex-wrap justify-content-between align-items-end gap-3 mb-4">
      <div>
        <h1 class="h3 mb-1">SRE Dashboards</h1>
        <p class="mb-0" style="color: var(--fx-ink-soft)">
          Golden signals · LP toxicity · ZGC · audit provenance (demo panels).
        </p>
      </div>
      <span class="badge badge-fx">Micrometer + OTEL</span>
    </div>

    <div class="row g-3 mb-3">
      <div class="col-lg-6">
        <div class="fx-card h-100">
          <h2 class="h5 mb-3">Golden signals</h2>
          <div class="row g-2">
            @for (s of golden; track s.label) {
              <div class="col-6">
                <div class="fx-kpi fx-card" style="padding: 0.85rem; box-shadow: none">
                  <span class="label">{{ s.label }}</span>
                  <span class="value" style="font-size: 1.35rem">{{ s.value }}</span>
                  <span class="small" style="color: var(--fx-accent)">{{ s.hint }}</span>
                </div>
              </div>
            }
          </div>
        </div>
      </div>
      <div class="col-lg-6">
        <div class="fx-card h-100">
          <h2 class="h5 mb-3">LP toxicity heatmap</h2>
          <div class="table-responsive">
            <table class="table align-middle mb-0">
              <thead>
                <tr>
                  <th>Venue</th>
                  <th>Score</th>
                  <th>Trend</th>
                </tr>
              </thead>
              <tbody>
                @for (lp of toxicity; track lp.venue) {
                  <tr>
                    <td class="fw-semibold">{{ lp.venue }}</td>
                    <td>{{ lp.score }}</td>
                    <td>
                      <span class="badge" [class]="lp.score > 0.65 ? 'badge-danger' : 'badge-fx'">{{
                        lp.trend
                      }}</span>
                    </td>
                  </tr>
                }
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>

    <div class="row g-3">
      <div class="col-lg-6">
        <div class="fx-card h-100">
          <h2 class="h5 mb-3">ZGC / JVM</h2>
          @for (m of zgc; track m.label) {
            <div class="d-flex justify-content-between border-bottom py-2" style="border-color: var(--fx-border) !important">
              <span>{{ m.label }}</span>
              <span class="fw-bold" style="color: var(--fx-accent-deep)">{{ m.value }}</span>
            </div>
          }
        </div>
      </div>
      <div class="col-lg-6">
        <div class="fx-card h-100">
          <h2 class="h5 mb-3">Audit trail (recent)</h2>
          @for (a of audit; track a.id) {
            <div class="mb-2">
              <div class="fw-bold">{{ a.action }}</div>
              <div class="small" style="color: var(--fx-ink-soft)">{{ a.detail }}</div>
            </div>
          }
        </div>
      </div>
    </div>
  `,
})
export class SreDashboardsPage {
  readonly golden = [
    { label: 'Latency p99', value: '8.4 ms', hint: 'route-exec' },
    { label: 'Traffic', value: '182k rps', hint: 'quote edge' },
    { label: 'Errors', value: '0.02%', hint: '5xx / total' },
    { label: 'Saturation', value: '61%', hint: 'CPU · quote' },
  ];

  readonly toxicity = [
    { venue: 'LP-CITI-ECN', score: 0.82, trend: 'Hot' },
    { venue: 'LP-BARX-STREAM', score: 0.41, trend: 'Stable' },
    { venue: 'LP-JPM-FAST', score: 0.28, trend: 'Cool' },
    { venue: 'LP-HSBC-ESP', score: 0.55, trend: 'Watch' },
  ];

  readonly zgc = [
    { label: 'GC pause p99', value: '0.8 ms' },
    { label: 'Heap used', value: '4.2 / 8 GB' },
    { label: 'Allocation rate', value: '1.1 GB/s' },
    { label: 'Threads live', value: '214' },
  ];

  readonly audit = [
    { id: 1, action: 'Venue halt proposed', detail: 'LP-CITI-ECN · RCA agent · pending checker' },
    { id: 2, action: 'DMN v12 shadow', detail: 'Policy studio · +2.1 bps · ready' },
    { id: 3, action: 'Synthetic FOMC x50', detail: 'Chaos complete · wf_exec_log #88421' },
  ];
}
