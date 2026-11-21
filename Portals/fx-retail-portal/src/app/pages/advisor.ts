import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { API } from '../api';

@Component({
  selector: 'app-advisor',
  imports: [FormsModule],
  template: `
    <div class="d-flex flex-wrap justify-content-between align-items-end gap-3 mb-4">
      <div>
        <h1 class="h3 mb-1">Smart Transfer &amp; Rate Advisor</h1>
        <p class="mb-0" style="color: var(--fx-ink-soft)">
          Conversational timing tips · rates from {{ api.quote }}
        </p>
      </div>
    </div>

    <div class="fx-card" style="max-width: 720px">
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
          placeholder="Ask about converting or locking a rate…"
        />
        <button class="btn btn-fx" (click)="send()">Send</button>
      </div>
      <div class="d-flex flex-wrap gap-2 mt-2">
        @for (q of quick; track q) {
          <button class="btn btn-fx-outline btn-sm" (click)="ask(q)">{{ q }}</button>
        }
      </div>
    </div>
  `,
})
export class AdvisorPage {
  readonly api = API;
  draft = '';
  readonly quick = [
    'Send $2,500 to Mumbai this week — convert now or auto-lock?',
    'When is the best window for USD/MXN?',
  ];
  readonly messages = signal<{ role: 'user' | 'bot'; text: string }[]>([
    {
      role: 'bot',
      text: 'Hi! I explain mid-market vs all-in rates in plain language and can help you set a target-rate auto-convert.',
    },
  ]);

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
        text: 'London/NY overlap (today ~12:00–16:00 ET) usually tightens spreads. For $2,500 USD→INR, converting now saves ~$18 vs a typical retail bank. Or set an auto-lock at 83.60 if you can wait 2–3 days.',
      },
    ]);
  }
}
