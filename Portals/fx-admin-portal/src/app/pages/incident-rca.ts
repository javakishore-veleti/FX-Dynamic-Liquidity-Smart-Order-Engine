import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-incident-rca',
  imports: [FormsModule],
  template: `
    <div class="d-flex flex-wrap justify-content-between align-items-end gap-3 mb-4">
      <div>
        <h1 class="h3 mb-1">Incident RCA Copilot</h1>
        <p class="mb-0" style="color: var(--fx-ink-soft)">
          3:00 AM autonomous triage · SSE reasoning · maker-checker halt cards (admin :8084).
        </p>
      </div>
      <button class="btn btn-fx btn-sm" (click)="runRca()">
        <i class="bi bi-magic me-1"></i>Run RCA agent
      </button>
    </div>

    <div class="row g-3">
      <div class="col-lg-5">
        <div class="fx-card h-100">
          <h2 class="h5 mb-3">Incident context</h2>
          <label class="form-label fw-bold">Symptom</label>
          <textarea class="form-control mb-3" rows="3" [(ngModel)]="symptom"></textarea>
          <label class="form-label fw-bold">Suspect LP</label>
          <select class="form-select mb-3" [(ngModel)]="lpId">
            <option>LP-CITI-ECN</option>
            <option>LP-BARX-STREAM</option>
            <option>LP-JPM-FAST</option>
            <option>LP-HSBC-ESP</option>
          </select>
          <div class="small" style="color: var(--fx-ink-soft)">
            POST /api/v1/agents/incident-rca/analyze · halt via /api/v1/admin/venues/{{ '{' }}lpId{{ '}' }}/halt
          </div>
        </div>
      </div>
      <div class="col-lg-7">
        <div class="fx-card mb-3">
          <h2 class="h5 mb-3">Reasoning stream</h2>
          @for (step of steps(); track step) {
            <div class="d-flex gap-2 align-items-start mb-2">
              <i class="bi bi-chevron-right" style="color: var(--fx-accent)"></i>
              <span>{{ step }}</span>
            </div>
          }
          @if (!steps().length) {
            <p class="mb-0" style="color: var(--fx-ink-soft)">Awaiting agent run…</p>
          }
        </div>
        @if (approval()) {
          <div class="fx-card">
            <div class="d-flex justify-content-between align-items-start mb-2">
              <div>
                <h2 class="h5 mb-1">Maker-checker approval</h2>
                <p class="mb-0 small" style="color: var(--fx-ink-soft)">{{ approval() }}</p>
              </div>
              <span class="badge badge-warn">Pending 2nd eye</span>
            </div>
            <div class="d-flex gap-2 mt-3">
              <button class="btn btn-fx btn-sm" (click)="approve()">Approve halt</button>
              <button class="btn btn-fx-outline btn-sm" (click)="reject()">Reject</button>
            </div>
          </div>
        }
      </div>
    </div>
  `,
})
export class IncidentRcaPage {
  symptom = 'EUR/USD p99 spike + toxicity on LP-CITI-ECN after 02:58 UTC';
  lpId = 'LP-CITI-ECN';
  readonly steps = signal<string[]>([]);
  readonly approval = signal<string | null>(null);

  runRca(): void {
    this.steps.set([
      'Correlated Iceberg outlier traces on route-exec span batch…',
      `LP toxicity score for ${this.lpId} crossed 0.82 (threshold 0.65).`,
      'Blast radius: 3 STP desks, qualify credit cache warm path, admin kill-switch ready.',
      'Recommended action: temporary venue halt with shadow replay for 15 minutes.',
    ]);
    this.approval.set(`Halt ${this.lpId} — toxicity spike · proposed by Incident RCA agent`);
  }

  approve(): void {
    this.approval.set(`Approved · ${this.lpId} halt logged in wf_exec_log (demo)`);
  }

  reject(): void {
    this.approval.set(null);
    this.steps.update((s) => [...s, 'Halt rejected by checker — continuing observation.']);
  }
}
