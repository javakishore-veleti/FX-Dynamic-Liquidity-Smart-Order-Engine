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
    {
      path: '/workflows',
      label: 'Workflows',
      icon: 'bi-diagram-2',
      children: [
        { path: '/workflows/master', label: 'Master List', icon: 'bi-list-ul' },
        { path: '/workflows/executions', label: 'Executions', icon: 'bi-play-circle' },
      ],
    },
    { path: '/synthetic', label: 'Synthetic & Chaos', icon: 'bi-lightning-charge' },
    { path: '/agents', label: 'Agent Orchestrator', icon: 'bi-robot' },
    { path: '/sre-dashboards', label: 'SRE Dashboards', icon: 'bi-graph-up' },
  ];

  readonly topLinks = [
    { path: '/topology', label: 'Live Topology' },
    { path: '/workflows/master', label: 'Workflows' },
    { path: '/agents', label: 'Agents' },
  ];
}
