import { Component, DOCUMENT, OnInit, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { NgIcon, provideIcons } from '@ng-icons/core';
import {
  lucideChevronDown,
  lucideCircleAlert,
  lucideLayoutGrid,
  lucideLogOut,
} from '@ng-icons/lucide';
import { HlmAlertImports } from '@spartan-ng/helm/alert';
import { HlmAvatarImports } from '@spartan-ng/helm/avatar';
import { HlmButtonImports } from '@spartan-ng/helm/button';
import { HlmDropdownMenuImports } from '@spartan-ng/helm/dropdown-menu';
import { AuthService } from './core/auth.service';
import { initials } from './core/ui-helpers';

@Component({
  imports: [
    RouterOutlet,
    RouterLink,
    RouterLinkActive,
    NgIcon,
    HlmAlertImports,
    HlmAvatarImports,
    HlmButtonImports,
    HlmDropdownMenuImports,
  ],
  providers: [
    provideIcons({ lucideChevronDown, lucideCircleAlert, lucideLayoutGrid, lucideLogOut }),
  ],
  selector: 'app-root',
  styleUrl: './app.css',
  templateUrl: './app.html',
})
export class App implements OnInit {
  protected readonly title = 'Matriz';
  protected readonly auth = inject(AuthService);
  protected readonly initials = initials;
  protected readonly navLinks = [
    { path: '/', label: 'Início', exact: true },
    { path: '/projetos', label: 'Projetos', exact: false },
  ];

  constructor() {
    const root = inject(DOCUMENT).documentElement;
    const media = root.ownerDocument.defaultView?.matchMedia?.('(prefers-color-scheme: dark)');
    if (media) {
      const apply = () => root.classList.toggle('dark', media.matches);
      apply();
      media.addEventListener('change', apply);
    }
  }

  ngOnInit(): void {
    this.auth.loadCurrentUser();
  }
}
