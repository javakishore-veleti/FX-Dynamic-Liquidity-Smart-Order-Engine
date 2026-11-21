import { Component } from '@angular/core';

@Component({
  selector: 'app-agents',
  template: `
    <div class="d-flex flex-wrap justify-content-between align-items-end gap-3 mb-4">
      <div>
        <h1 class="h3 mb-1">Agent Orchestrator</h1>
        <p class="mb-0" style="color: var(--fx-ink-soft)">
          Autonomous SRE / RCA / blast-radius / synthetic agents via MCP (:8091 / :8192).
        </p>
      </div>
      <span class="badge badge-fx">SSE stream ready</span>
    </div>

    <div class="row g-3 mb-4">
      @for (agent of agents; track agent.id) {
        <div class="col-md-6 col-xl-3">
          <div class="fx-card h-100">
            <div class="d-flex align-items-center gap-2 mb-2">
              <i [class]="'bi ' + agent.icon" style="font-size: 1.35rem; color: var(--fx-accent)"></i>
              <h2 class="h6 mb-0">{{ agent.name }}</h2>
            </div>
            <p class="small mb-2" style="color: var(--fx-ink-soft)">{{ agent.desc }}</p>
            <div class="d-flex justify-content-between align-items-center">
              <span class="badge" [class]="agent.badge">{{ agent.state }}</span>
              <span class="small fw-bold" style="color: var(--fx-accent-deep)">{{ agent.runs }} runs</span>
            </div>
          </div>
        </div>
      }
    </div>

    <div class="fx-card">
      <h2 class="h5 mb-3">Recent agent invocations</h2>
      <div class="table-responsive">
        <table class="table align-middle mb-0">
          <thead>
            <tr>
              <th>Agent</th>
              <th>Trigger</th>
              <th>Latency</th>
              <th>Outcome</th>
            </tr>
          </thead>
          <tbody>
            @for (row of invocations; track row.id) {
              <tr>
                <td class="fw-semibold">{{ row.agent }}</td>
                <td>{{ row.trigger }}</td>
                <td>{{ row.latency }}</td>
                <td><span class="badge badge-fx">{{ row.outcome }}</span></td>
              </tr>
            }
          </tbody>
        </table>
      </div>
    </div>
  `,
})
export class AgentsPage {
  readonly agents = [
    {
      id: 'rca',
      name: 'Incident RCA',
      icon: 'bi-shield-exclamation',
      desc: 'Correlates traces, toxicity, and LP health into halt cards.',
      state: 'Idle',
      badge: 'badge-fx',
      runs: 18,
    },
    {
      id: 'blast',
      name: 'Blast Radius',
      icon: 'bi-radar',
      desc: 'Maps downstream STP impact from any node failure.',
      state: 'Warm',
      badge: 'badge-fx',
      runs: 12,
    },
    {
      id: 'chaos',
      name: 'Synthetic Chaos',
      icon: 'bi-lightning-charge',
      desc: 'Injects skew, failover, and partition-skew scenarios.',
      state: 'Idle',
      badge: 'badge-fx',
      runs: 9,
    },
    {
      id: 'policy',
      name: 'Policy Shadow',
      icon: 'bi-journal-code',
      desc: 'Replays DMN candidates against historical fills.',
      state: 'Busy',
      badge: 'badge-warn',
      runs: 8,
    },
  ];

  readonly invocations = [
    { id: 1, agent: 'Incident RCA', trigger: 'p99 alert EUR/USD', latency: '1.2 s', outcome: 'Halt proposed' },
    { id: 2, agent: 'Blast Radius', trigger: 'Manual · LP-CITI-ECN', latency: '480 ms', outcome: '3 paths' },
    { id: 3, agent: 'Synthetic Chaos', trigger: 'FOMC preset x100', latency: '42 s', outcome: 'Complete' },
    { id: 4, agent: 'Policy Shadow', trigger: 'DMN v12 draft', latency: '8.1 s', outcome: '+2.1 bps' },
  ];
}
