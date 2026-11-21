import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-topology',
  imports: [FormsModule],
  template: `
    <div class="d-flex flex-wrap justify-content-between align-items-end gap-3 mb-4">
      <div>
        <h1 class="h3 mb-1">Topology &amp; Blast Radius</h1>
        <p class="mb-0" style="color: var(--fx-ink-soft)">
          Network · IPC · Trace layers with time-travel scrubber (topology :8085 + blast-radius agent).
        </p>
      </div>
      <div class="d-flex gap-2">
        <select class="form-select form-select-sm" style="width: auto" [(ngModel)]="layer">
          <option>Network</option>
          <option>IPC</option>
          <option>Trace</option>
        </select>
        <button class="btn btn-fx btn-sm" (click)="runBlast()">
          <i class="bi bi-radar me-1"></i>Analyze blast radius
        </button>
      </div>
    </div>

    <div class="fx-card mb-3">
      <div class="topology-canvas">
        @for (n of nodes; track n.id) {
          <div class="node" [style.left.px]="n.x" [style.top.px]="n.y" [class.border-danger]="n.hot">
            <i [class]="'bi ' + n.icon"></i> {{ n.label }}
          </div>
        }
      </div>
      <div class="mt-3 d-flex flex-wrap gap-3 align-items-center">
        <label class="small fw-bold mb-0" style="color: var(--fx-ink-soft)">Time travel</label>
        <input type="range" class="form-range flex-grow-1" min="0" max="100" [(ngModel)]="scrub" />
        <span class="badge badge-fx">T-{{ scrub }}m</span>
        <span class="badge badge-fx">Layer: {{ layer }}</span>
      </div>
    </div>

    @if (blastResult()) {
      <div class="fx-card">
        <h2 class="h5">Blast-radius summary</h2>
        <p class="mb-2" style="color: var(--fx-ink-soft)">{{ blastResult() }}</p>
        <div class="d-flex gap-2">
          <span class="badge badge-warn">3 STP paths impacted</span>
          <span class="badge badge-fx">Desk: eFX London</span>
          <span class="badge badge-danger">p99 heat: elevated</span>
        </div>
      </div>
    }
  `,
})
export class TopologyPage {
  layer = 'Network';
  scrub = 15;
  readonly blastResult = signal<string | null>(null);

  readonly nodes = [
    { id: 1, label: 'quote :8081', icon: 'bi-broadcast', x: 40, y: 60, hot: false },
    { id: 2, label: 'qualify :8082', icon: 'bi-shield-check', x: 220, y: 40, hot: false },
    { id: 3, label: 'route-exec :8083', icon: 'bi-signpost-split', x: 400, y: 70, hot: true },
    { id: 4, label: 'admin :8084', icon: 'bi-gear', x: 120, y: 200, hot: false },
    { id: 5, label: 'topology :8085', icon: 'bi-diagram-3', x: 300, y: 220, hot: false },
    { id: 6, label: 'MCP :8091', icon: 'bi-plug', x: 480, y: 200, hot: false },
  ];

  runBlast(): void {
    this.blastResult.set(
      'Agent SPRING_AI_AGENT: LP-CITI-ECN failure fans out to route-exec failover lane, qualify credit cache, and admin halt workflow within 480 ms.',
    );
  }
}
