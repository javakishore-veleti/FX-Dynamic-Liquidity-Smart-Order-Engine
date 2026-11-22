import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

/** Legacy route — Policy Studio now lives under Reference Data → Client markup schedules. */
@Component({
  selector: 'app-policy-studio',
  template: `<p class="text-muted">Redirecting to client markup schedules…</p>`,
})
export class PolicyStudioPage implements OnInit {
  constructor(private readonly router: Router) {}

  ngOnInit(): void {
    void this.router.navigateByUrl('/reference/schedules');
  }
}
