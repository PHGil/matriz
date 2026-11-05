import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { NgIcon, provideIcons } from '@ng-icons/core';
import { lucideArrowRight, lucideFolderOpen, lucideRefreshCw } from '@ng-icons/lucide';
import { HlmAlertImports } from '@spartan-ng/helm/alert';
import { HlmBadgeImports } from '@spartan-ng/helm/badge';
import { HlmButtonImports } from '@spartan-ng/helm/button';
import { HlmCardImports } from '@spartan-ng/helm/card';
import { HlmEmptyImports } from '@spartan-ng/helm/empty';
import { HlmProgressImports } from '@spartan-ng/helm/progress';
import { HlmSkeletonImports } from '@spartan-ng/helm/skeleton';
import { AuthService } from '../core/auth.service';
import { ProjectSummary } from '../core/models';
import { ProjectsService } from '../core/projects.service';
import { errorMessage, roleLabel } from '../core/ui-helpers';

@Component({
  selector: 'app-dashboard',
  imports: [
    RouterLink,
    NgIcon,
    HlmAlertImports,
    HlmBadgeImports,
    HlmButtonImports,
    HlmCardImports,
    HlmEmptyImports,
    HlmProgressImports,
    HlmSkeletonImports,
  ],
  providers: [provideIcons({ lucideArrowRight, lucideFolderOpen, lucideRefreshCw })],
  template: `
    <h1 class="text-3xl font-semibold tracking-tight">Olá, {{ auth.user()?.name }}</h1>
    <div class="mt-6 mb-4 flex flex-wrap items-center justify-between gap-4">
      <h2 class="text-xl font-semibold tracking-tight">Os meus projetos</h2>
      <a hlmBtn variant="outline" routerLink="/projetos">
        Gerir projetos
        <ng-icon name="lucideArrowRight" />
      </a>
    </div>

    @if (loading()) {
      <div class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3" aria-busy="true">
        <span class="sr-only">A carregar…</span>
        @for (i of [1, 2, 3]; track i) {
          <hlm-skeleton class="h-48 rounded-xl" />
        }
      </div>
    } @else if (error()) {
      <div hlmAlert variant="destructive">
        <p hlmAlertDescription>{{ error() }}</p>
        <div hlmAlertAction>
          <button type="button" hlmBtn variant="outline" size="sm" (click)="load()">
            <ng-icon name="lucideRefreshCw" />
            Tentar novamente
          </button>
        </div>
      </div>
    } @else if (projects().length === 0) {
      <hlm-empty class="border border-dashed">
        <hlm-empty-header>
          <hlm-empty-media variant="icon"><ng-icon name="lucideFolderOpen" /></hlm-empty-media>
          <p hlmEmptyTitle>Ainda não participa em nenhum projeto.</p>
        </hlm-empty-header>
        <hlm-empty-content>
          <a hlmBtn routerLink="/projetos">Crie o primeiro projeto.</a>
        </hlm-empty-content>
      </hlm-empty>
    } @else {
      <ul class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        @for (p of projects(); track p.id) {
          <li>
            <a
              [routerLink]="['/projetos', p.id]"
              class="group focus-visible:ring-ring/50 block h-full rounded-xl outline-none focus-visible:ring-3"
            >
              <section hlmCard class="h-full transition-shadow group-hover:shadow-md">
                <div hlmCardHeader>
                  <h3 hlmCardTitle class="truncate">{{ p.name }}</h3>
                  @if (p.description) {
                    <p hlmCardDescription class="line-clamp-2">{{ p.description }}</p>
                  }
                  <div hlmCardAction>
                    <span hlmBadge [variant]="p.myRole === 'MANAGER' ? 'default' : 'secondary'">
                      {{ roleLabel[p.myRole] }}
                    </span>
                  </div>
                </div>
                <div hlmCardContent class="space-y-4">
                  <dl class="grid grid-cols-3 gap-2 text-center">
                    <div class="bg-muted/50 rounded-lg p-2">
                      <dt class="text-muted-foreground text-xs">Por fazer</dt>
                      <dd class="text-xl font-semibold tabular-nums">{{ p.counts.todo }}</dd>
                    </div>
                    <div class="bg-muted/50 rounded-lg p-2">
                      <dt class="text-muted-foreground text-xs">Em curso</dt>
                      <dd class="text-xl font-semibold tabular-nums">{{ p.counts.inProgress }}</dd>
                    </div>
                    <div class="bg-muted/50 rounded-lg p-2">
                      <dt class="text-muted-foreground text-xs">Concluídas</dt>
                      <dd class="text-xl font-semibold tabular-nums">{{ p.counts.done }}</dd>
                    </div>
                  </dl>
                  @if (p.counts.total === 0) {
                    <p class="text-muted-foreground text-sm">Sem tarefas</p>
                  } @else {
                    <div class="space-y-1.5">
                      <p class="text-muted-foreground text-sm">{{ percent(p) }}% concluído</p>
                      <hlm-progress
                        [value]="percent(p)"
                        [attr.aria-label]="'Progresso de ' + p.name"
                      >
                        <hlm-progress-indicator />
                      </hlm-progress>
                    </div>
                  }
                </div>
              </section>
            </a>
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
