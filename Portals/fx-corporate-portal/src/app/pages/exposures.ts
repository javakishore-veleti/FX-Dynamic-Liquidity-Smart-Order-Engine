import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { API } from '../api';

@Component({
  selector: 'app-exposures',
  imports: [FormsModule],
  template: `
    <div class="d-flex flex-wrap justify-content-between align-items-end gap-3 mb-4">
      <div>
        <h1 class="h3 mb-1">Exposures &amp; Hedge Recommender</h1>
        <p class="mb-0" style="color: var(--fx-ink-soft)">
          Cash-flow grouping by value date · credit check {{ api.qualify }}
        </p>
      </div>
      <button class="btn btn-fx btn-sm" (click)="recommend()">
        <i class="bi bi-magic me-1"></i>Recommend hedges
      </button>
    </div>

    <div class="row g-3 mb-3">
      <div class="col-lg-7">
        <div class="fx-card">
          <h2 class="h5 mb-3">Multi-currency exposures</h2>
          <div class="table-responsive">
            <table class="table align-middle mb-0">
              <thead>
                <tr>
                  <th>CCY</th>
                  <th>Value date</th>
                  <th>Amount</th>
                  <th>Type</th>
                </tr>
              </thead>
              <tbody>
                @for (e of exposures; track e.id) {
                  <tr>
                    <td class="fw-semibold">{{ e.ccy }}</td>
                    <td>{{ e.vd }}</td>
                    <td>{{ e.amt }}</td>
                    <td><span class="badge badge-fx">{{ e.type }}</span></td>
                  </tr>
                }
              </tbody>
            </table>
          </div>
        </div>
      </div>
      <div class="col-lg-5">
        <div class="fx-card h-100">
          <h2 class="h5 mb-3">Upload / feed</h2>
          <label class="form-label fw-bold">ERP payables CSV (demo)</label>
          <input type="file" class="form-control mb-3" />
          <label class="form-label fw-bold">Client ID</label>
          <input class="form-control mb-2" [(ngModel)]="clientId" />
          <div class="small" style="color: var(--fx-ink-soft)">
            GET {{ api.qualify }}/api/v1/fx/credit/utilization/{{ clientId }}
          </div>
        </div>
      </div>
    </div>

    @if (hedges().length) {
      <div class="fx-card">
        <h2 class="h5 mb-3">Recommended tranches</h2>
        <div class="row g-3">
          @for (h of hedges(); track h.id) {
            <div class="col-md-4">
              <div class="fx-card h-100" style="box-shadow: none; background: var(--fx-surface-2)">
                <div class="fw-bold mb-1">{{ h.instrument }} · {{ h.pair }}</div>
                <div class="mb-2">{{ h.notional }} · {{ h.tenor }}</div>
                <span class="badge badge-fx">{{ h.rationale }}</span>
              </div>
            </div>
          }
        </div>
      </div>
    }
  `,
})
export class ExposuresPage {
  readonly api = API;
  clientId = 'CORP-441';
  readonly exposures = [
    { id: 1, ccy: 'EUR', vd: '2026-04-02', amt: '€28.4M', type: 'Payable' },
    { id: 2, ccy: 'GBP', vd: '2026-04-08', amt: '£9.1M', type: 'Receivable' },
    { id: 3, ccy: 'JPY', vd: '2026-04-15', amt: '¥1.2B', type: 'Payable' },
    { id: 4, ccy: 'USD', vd: '2026-04-02', amt: '$12.0M', type: 'Payroll' },
  ];
  readonly hedges = signal<{ id: number; instrument: string; pair: string; notional: string; tenor: string; rationale: string }[]>([]);

  recommend(): void {
    this.hedges.set([
      {
        id: 1,
        instrument: 'Spot',
        pair: 'EUR/USD',
        notional: '$15M',
        tenor: 'T+2',
        rationale: 'Near VD · tight spread',
      },
      {
        id: 2,
        instrument: 'Forward',
        pair: 'EUR/USD',
        notional: '$16M',
        tenor: '1W',
        rationale: 'Ladder remainder',
      },
      {
        id: 3,
        instrument: 'Forward',
        pair: 'USD/JPY',
        notional: '$8M',
        tenor: '2W',
        rationale: 'Credit OK',
      },
    ]);
  }
}
