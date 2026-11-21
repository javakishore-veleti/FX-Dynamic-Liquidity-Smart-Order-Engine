import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-shell',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './shell.html',
  styleUrl: './shell.scss',
})
export class Shell {
  readonly nav = [
    { path: '/dashboard', label: 'Command Center', icon: 'bi-speedometer2' },
    { path: '/topology', label: 'Topology Canvas', icon: 'bi-diagram-3' },
    { path: '/incident-rca', label: 'Incident RCA', icon: 'bi-shield-exclamation' },
    { path: '/policy-studio', label: 'Policy Studio', icon: 'bi-journal-code' },
    { path: '/synthetic', label: 'Synthetic & Chaos', icon: 'bi-lightning-charge' },
    { path: '/agents', label: 'Agent Orchestrator', icon: 'bi-robot' },
    { path: '/sre-dashboards', label: 'SRE Dashboards', icon: 'bi-graph-up' },
  ];

  readonly topLinks = [
    { path: '/topology', label: 'Live Topology' },
    { path: '/synthetic', label: 'Workflows' },
    { path: '/agents', label: 'Agents' },
  ];
}
