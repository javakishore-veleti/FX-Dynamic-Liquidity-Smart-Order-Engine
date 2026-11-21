import { Component, signal } from '@angular/core';
import { API } from '../api';

@Component({
  selector: 'app-tca',
  template: `
    <div class="d-flex flex-wrap justify-content-between align-items-end gap-3 mb-4">
      <div>
        <h1 class="h3 mb-1">Best Execution &amp; TCA</h1>
        <p class="mb-0" style="color: var(--fx-ink-soft)">
          Plain-English provenance · {{ api.routeExec }}/api/v1/fx/orders/&#123;id&#125;/trace
        </p>
      </div>
    </div>

    <div class="row g-3">
      <div class="col-lg-5">
        <div class="fx-card">
          <h2 class="h5 mb-3">Completed fills</h2>
          @for (f of fills; track f.id) {
            <button
              class="btn btn-fx-outline w-100 text-start mb-2"
              (click)="select(f)"
              [class.active]="selected()?.id === f.id"
            >
              <div class="fw-bold">{{ f.id }} · {{ f.pair }}</div>
              <div class="small">{{ f.notional }} · saved {{ f.saved }}</div>
            </button>
          }
        </div>
      </div>
      <div class="col-lg-7">
        <div class="fx-card h-100">
          <h2 class="h5 mb-3">AI TCA explainer</h2>
          @if (selected()) {
            <p class="mb-3" style="color: var(--fx-ink-soft)">{{ selected()!.summary }}</p>
            <div class="d-flex flex-wrap gap-2 mb-3">
              <span class="badge badge-fx">Spread saved {{ selected()!.saved }}</span>
              <span class="badge badge-fx">Fill certainty {{ selected()!.certainty }}</span>
              <span class="badge badge-fx">MiFID II / FXGC</span>
            </div>
            <div class="small" style="color: var(--fx-ink-soft)">
              Trace endpoint: {{ api.routeExec }}/api/v1/fx/orders/{{ selected()!.id }}/trace
            </div>
          } @else {
            <p class="mb-0" style="color: var(--fx-ink-soft)">Select a fill to view the receipt.</p>
          }
        </div>
      </div>
    </div>
  `,
})
export class TcaPage {
  readonly api = API;
  readonly fills = [
    {
      id: 'CO-88410',
      pair: 'USD/JPY',
      notional: '$8M',
      saved: '2.1 bps',
      certainty: '97%',
      summary:
        'Your order swept three sanitized liquidity bands during London/NY overlap. Versus arrival mid, you saved 2.1 bps with high fill certainty and full FX Global Code provenance attached for audit.',
    },
    {
      id: 'CO-88392',
      pair: 'EUR/USD',
      notional: '$22M',
      saved: '1.4 bps',
      certainty: '93%',
      summary:
        'TWAP child clips reduced impact ahead of a macro print. Spread capture remained positive versus the retail-tier reference while credit utilization stayed within CORP-441 limits.',
    },
    {
      id: 'CO-88371',
      pair: 'GBP/USD',
      notional: '$5M',
      saved: '0.9 bps',
      certainty: '99%',
      summary:
        'Near-touch sweep completed in under 800 ms. Best-execution narrative cites composite depth quality and zero venue concentration breach.',
    },
  ];
  readonly selected = signal<(typeof this.fills)[0] | null>(null);

  select(f: (typeof this.fills)[0]): void {
    this.selected.set(f);
  }
}
