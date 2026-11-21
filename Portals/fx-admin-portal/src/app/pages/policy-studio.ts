import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-policy-studio',
  imports: [FormsModule],
  template: `
    <div class="d-flex flex-wrap justify-content-between align-items-end gap-3 mb-4">
      <div>
        <h1 class="h3 mb-1">Policy Studio</h1>
        <p class="mb-0" style="color: var(--fx-ink-soft)">
          Natural-language Drools / DMN authoring with shadow-mode P&amp;L replay.
        </p>
      </div>
      <button class="btn btn-fx btn-sm" (click)="generate()">
        <i class="bi bi-stars me-1"></i>Generate DMN
      </button>
    </div>

    <div class="row g-3">
      <div class="col-lg-6">
        <div class="fx-card h-100">
          <h2 class="h5 mb-3">Policy intent</h2>
          <textarea
            class="form-control mb-3"
            rows="8"
            [(ngModel)]="intent"
            placeholder="Describe the dealing rule in plain English…"
          ></textarea>
          <div class="small" style="color: var(--fx-ink-soft)">
            Validated against admin :8084 maker-checker queue before promote.
          </div>
        </div>
      </div>
      <div class="col-lg-6">
        <div class="fx-card mb-3">
          <h2 class="h5 mb-3">Generated decision table</h2>
          @if (table().length) {
            <div class="table-responsive">
              <table class="table align-middle mb-0">
                <thead>
                  <tr>
                    <th>Condition</th>
                    <th>Action</th>
                    <th>Priority</th>
                  </tr>
                </thead>
                <tbody>
                  @for (row of table(); track row.condition) {
                    <tr>
                      <td>{{ row.condition }}</td>
                      <td class="fw-semibold">{{ row.action }}</td>
                      <td><span class="badge badge-fx">{{ row.priority }}</span></td>
                    </tr>
                  }
                </tbody>
              </table>
            </div>
          } @else {
            <p class="mb-0" style="color: var(--fx-ink-soft)">Generate a rule to preview the DMN diff.</p>
          }
        </div>
        @if (replay()) {
          <div class="fx-card">
            <h2 class="h5 mb-2">Shadow replay</h2>
            <p class="mb-2" style="color: var(--fx-ink-soft)">{{ replay() }}</p>
            <button class="btn btn-fx-outline btn-sm">Submit for maker-checker</button>
          </div>
        }
      </div>
    </div>
  `,
})
export class PolicyStudioPage {
  intent =
    'Widen EUR/USD retail markup by 1.5 bps outside London/NY overlap; tighten 0.5 bps when LP toxicity < 0.3.';
  readonly table = signal<{ condition: string; action: string; priority: string }[]>([]);
  readonly replay = signal<string | null>(null);

  generate(): void {
    this.table.set([
      {
        condition: 'Session ≠ London∩NY',
        action: 'markup += 1.5 bps',
        priority: 'P1',
      },
      {
        condition: 'toxicity < 0.3',
        action: 'markup -= 0.5 bps',
        priority: 'P2',
      },
      {
        condition: 'pair = EUR/USD',
        action: 'apply tier retail-A',
        priority: 'P0',
      },
    ]);
    this.replay.set('Shadow replay on last 7d: simulated P&L +2.1 bps · 0 rejects · ready for four-eyes.');
  }
}
