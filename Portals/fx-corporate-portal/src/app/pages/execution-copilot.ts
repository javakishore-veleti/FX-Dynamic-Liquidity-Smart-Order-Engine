import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { API } from '../api';

@Component({
  selector: 'app-execution-copilot',
  imports: [FormsModule],
  template: `
    <div class="d-flex flex-wrap justify-content-between align-items-end gap-3 mb-4">
      <div>
        <h1 class="h3 mb-1">Execution Copilot</h1>
        <p class="mb-0" style="color: var(--fx-ink-soft)">
          Natural-language block splitting · qualify {{ api.qualify }} · exec {{ api.routeExec }}
        </p>
      </div>
    </div>

    <div class="row g-3">
      <div class="col-lg-7">
        <div class="fx-card" style="min-height: 420px">
          <h2 class="h5 mb-3">Conversation</h2>
          @for (m of messages(); track $index) {
            <div class="chat-bubble" [class.user]="m.role === 'user'" [class.bot]="m.role === 'bot'">
              {{ m.text }}
            </div>
          }
          <div class="d-flex gap-2 mt-3">
            <input
              class="form-control"
              [(ngModel)]="draft"
              (keyup.enter)="send()"
              placeholder="Ask how to execute a block…"
            />
            <button class="btn btn-fx" (click)="send()">Send</button>
          </div>
          <div class="d-flex flex-wrap gap-2 mt-2">
            @for (q of quick; track q) {
              <button class="btn btn-fx-outline btn-sm" (click)="ask(q)">{{ q }}</button>
            }
          </div>
        </div>
      </div>
      <div class="col-lg-5">
        <div class="fx-card h-100">
          <h2 class="h5 mb-3">Sweep vs TWAP preview</h2>
          @if (preview()) {
            <div class="mb-3">
              <div class="fw-bold mb-1">Multi-LP Sweep</div>
              <p class="small mb-2" style="color: var(--fx-ink-soft)">{{ preview()!.sweep }}</p>
              <div class="fw-bold mb-1">TWAP (30m)</div>
              <p class="small mb-0" style="color: var(--fx-ink-soft)">{{ preview()!.twap }}</p>
            </div>
            <button class="btn btn-fx btn-sm">Submit qualify</button>
          } @else {
            <p class="mb-0" style="color: var(--fx-ink-soft)">Ask the copilot to generate a preview card.</p>
          }
        </div>
      </div>
    </div>
  `,
})
export class ExecutionCopilotPage {
  readonly api = API;
  draft = '';
  readonly quick = [
    'Lowest-slippage way to buy $45M EUR/USD before NY close?',
    'Compare sweep vs TWAP for GBP/USD $12M',
  ];
  readonly messages = signal<{ role: 'user' | 'bot'; text: string }[]>([
    {
      role: 'bot',
      text: 'I can simulate composite LP depth and markout without revealing wholesale venue names. What are you hedging?',
    },
  ]);
  readonly preview = signal<{ sweep: string; twap: string } | null>(null);

  send(): void {
    if (!this.draft.trim()) return;
    this.ask(this.draft.trim());
    this.draft = '';
  }

  ask(text: string): void {
    this.messages.update((m) => [...m, { role: 'user', text }]);
    this.messages.update((m) => [
      ...m,
      {
        role: 'bot',
        text: 'Recommended: 60% multi-LP sweep in London/NY overlap, 40% TWAP over 25 minutes to limit impact. Credit headroom OK on CORP-441.',
      },
    ]);
    this.preview.set({
      sweep: 'Est. slippage 0.6 bps · fill certainty 94% · 4 sanitized LPs',
      twap: 'Est. slippage 1.1 bps · lower impact · qualify POST /api/v1/fx/orders/qualify',
    });
  }
}
