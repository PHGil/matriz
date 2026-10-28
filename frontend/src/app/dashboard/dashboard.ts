import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../core/auth.service';
import { ProjectSummary } from '../core/models';
import { ProjectsService } from '../core/projects.service';
import { errorMessage, roleLabel } from '../core/ui-helpers';

@Component({
  selector: 'app-dashboard',
  imports: [RouterLink],
  template: `
    <h1>Olá, {{ auth.user()?.name }}</h1>
    <div class="toolbar">
      <h2>Os meus projetos</h2>
      <a routerLink="/projetos" class="button">Gerir projetos</a>
    </div>

    @if (loading()) {
      <p class="muted">A carregar…</p>
    } @else if (error()) {
      <p class="notice error" role="alert">
        {{ error() }} <button type="button" (click)="load()">Tentar novamente</button>
      </p>
    } @else if (projects().length === 0) {
      <p class="muted">
        Ainda não participa em nenhum projeto.
        <a routerLink="/projetos">Crie o primeiro projeto.</a>
      </p>
    } @else {
      <ul class="cards">
        @for (p of projects(); track p.id) {
          <li class="card">
            <a [routerLink]="['/projetos', p.id]" class="card-title">{{ p.name }}</a>
            <span class="badge">{{ roleLabel[p.myRole] }}</span>
            <dl class="counts">
              <div>
                <dt>Por fazer</dt>
                <dd>{{ p.counts.todo }}</dd>
              </div>
              <div>
                <dt>Em curso</dt>
                <dd>{{ p.counts.inProgress }}</dd>
              </div>
              <div>
                <dt>Concluídas</dt>
                <dd>{{ p.counts.done }}</dd>
              </div>
            </dl>
            <p class="muted">
              @if (p.counts.total === 0) {
                Sem tarefas
              } @else {
                {{ percent(p) }}% concluído
              }
            </p>
          </li>
        }
      </ul>
    }
  `,
})
export class Dashboard implements OnInit {
  protected readonly auth = inject(AuthService);
  private readonly api = inject(ProjectsService);
  protected readonly roleLabel = roleLabel;

  protected readonly projects = signal<ProjectSummary[]>([]);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.load();
  }

  protected load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.api.list().subscribe({
      next: (projects) => {
        this.projects.set(projects);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(errorMessage(err));
        this.loading.set(false);
      },
    });
  }

  protected percent(p: ProjectSummary): number {
    return Math.round((100 * p.counts.done) / p.counts.total);
  }
}
