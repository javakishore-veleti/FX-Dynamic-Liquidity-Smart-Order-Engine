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
    { path: '/dashboard', label: 'Home', icon: 'bi-house-heart' },
    { path: '/convert', label: 'Convert', icon: 'bi-currency-exchange' },
    { path: '/advisor', label: 'Rate Advisor', icon: 'bi-chat-heart' },
    { path: '/planner', label: 'Recurring Planner', icon: 'bi-calendar-check' },
    { path: '/tracker', label: 'Transfer Tracker', icon: 'bi-geo-alt' },
  ];

  readonly topLinks = [
    { path: '/convert', label: 'Convert' },
    { path: '/advisor', label: 'Advisor' },
    { path: '/tracker', label: 'Tracker' },
  ];
}
