import { Component, inject } from '@angular/core';
import { AuthService } from '../core/auth.service';

@Component({
  selector: 'app-home',
  template: `
    @if (auth.user(); as user) {
      <h1>Olá, {{ user.name }}</h1>
      <p>Os seus projetos aparecerão aqui.</p>
    } @else {
      <p>A carregar…</p>
    }
  `,
})
export class Home {
  protected readonly auth = inject(AuthService);
}
