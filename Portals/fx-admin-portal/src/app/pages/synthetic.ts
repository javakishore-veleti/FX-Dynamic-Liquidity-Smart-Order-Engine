import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-synthetic',
  imports: [FormsModule],
  template: `
    <div class="d-flex flex-wrap justify-content-between align-items-end gap-3 mb-4">
      <div>
        <h1 class="h3 mb-1">Synthetic &amp; Chaos</h1>
        <p class="mb-0" style="color: var(--fx-ink-soft)">
          Dataset generation and chaos presets polling wf_exec_log (admin :8084).
        </p>
      </div>
      <button class="btn btn-fx btn-sm" (click)="launch()" [disabled]="running()">
        <i class="bi bi-play-fill me-1"></i>{{ running() ? 'Running…' : 'Launch scenario' }}
      </button>
    </div>

    <div class="row g-3 mb-3">
      <div class="col-md-4">
        <div class="fx-card h-100">
          <h2 class="h5 mb-3">Scenario preset</h2>
          <select class="form-select mb-3" [(ngModel)]="preset">
            @for (p of presets; track p) {
              <option [value]="p">{{ p }}</option>
            }
          </select>
          <label class="form-label fw-bold">Multiplier</label>
          <input type="number" class="form-control mb-3" [(ngModel)]="multiplier" min="1" max="200" />
          <div class="small" style="color: var(--fx-ink-soft)">
            POST /api/v1/admin/synthetic/datasets/generate
          </div>
        </div>
      </div>
      <div class="col-md-8">
        <div class="fx-card h-100">
          <div class="d-flex justify-content-between align-items-center mb-3">
            <h2 class="h5 mb-0">Progress</h2>
            <span class="badge badge-fx">{{ progress() }}%</span>
          </div>
          <div class="progress mb-3" style="height: 0.75rem; background: #dff4ef">
            <div
              class="progress-bar"
              role="progressbar"
              [style.width.%]="progress()"
              style="background: var(--fx-accent)"
            ></div>
          </div>
          <ul class="list-unstyled mb-0">
            @for (log of logs(); track log) {
              <li class="mb-2">
                <i class="bi bi-check-circle-fill me-2" style="color: var(--fx-success)"></i>{{ log }}
              </li>
            }
          </ul>
        </div>
      </div>
    </div>

    <div class="fx-card">
      <h2 class="h5 mb-3">Recent wf_exec_log</h2>
      <div class="table-responsive">
        <table class="table align-middle mb-0">
          <thead>
            <tr>
              <th>Workflow</th>
              <th>Status</th>
              <th>Started</th>
            </tr>
          </thead>
          <tbody>
            @for (row of history; track row.id) {
              <tr>
                <td class="fw-semibold">{{ row.name }}</td>
                <td><span class="badge" [class]="row.badge">{{ row.status }}</span></td>
                <td>{{ row.started }}</td>
              </tr>
            }
          </tbody>
        </table>
      </div>
    </div>
  `,
})
export class SyntheticPage {
  presets = [
    'Simulate FOMC 100x EUR/USD Skew',
    'LP failover storm · 5 venues',
    'Partition skew · Kafka lag injection',
    'Credit headroom exhaustion CORP-*',
  ];
  preset = this.presets[0];
  multiplier = 100;
  readonly progress = signal(0);
  readonly running = signal(false);
  readonly logs = signal<string[]>([]);

  readonly history = [
    { id: 1, name: 'FOMC skew x50', status: 'Complete', badge: 'badge-fx', started: '2026-03-20 09:12' },
    { id: 2, name: 'Chaos · LP halt cascade', status: 'Complete', badge: 'badge-fx', started: '2026-03-19 22:04' },
    { id: 3, name: 'Synthetic retail netting', status: 'Failed', badge: 'badge-danger', started: '2026-03-18 14:41' },
  ];

  launch(): void {
    this.running.set(true);
    this.progress.set(0);
    this.logs.set([`Queued: ${this.preset} ×${this.multiplier}`]);
    const ticks = [25, 55, 80, 100];
    ticks.forEach((p, i) => {
      setTimeout(() => {
        this.progress.set(p);
        this.logs.update((l) => [
          ...l,
          p < 100 ? `Stage ${i + 1}: synthetic ticks injected` : 'Chaos agent finished · wf_exec_log updated',
        ]);
        if (p === 100) this.running.set(false);
      }, (i + 1) * 400);
    });
  }
}
