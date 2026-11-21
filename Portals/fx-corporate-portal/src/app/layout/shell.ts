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
    { path: '/dashboard', label: 'Treasury Home', icon: 'bi-speedometer2' },
    { path: '/blotter', label: 'Multi-LP Blotter', icon: 'bi-bar-chart-steps' },
    { path: '/execution-copilot', label: 'Execution Copilot', icon: 'bi-chat-dots' },
    { path: '/exposures', label: 'Exposures & Hedge', icon: 'bi-pie-chart' },
    { path: '/tca', label: 'Best Exec / TCA', icon: 'bi-receipt' },
    { path: '/alerts', label: 'Regime Alerts', icon: 'bi-bell' },
  ];

  readonly topLinks = [
    { path: '/blotter', label: 'Blotter' },
    { path: '/execution-copilot', label: 'Copilot' },
    { path: '/tca', label: 'TCA' },
  ];
}
