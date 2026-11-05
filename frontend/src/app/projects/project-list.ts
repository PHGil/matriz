import { Component, OnInit, inject, signal, viewChild } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Router, RouterLink } from '@angular/router';
import { NgIcon, provideIcons } from '@ng-icons/core';
import { lucideFolderOpen, lucidePlus, lucideRefreshCw, lucideSearch } from '@ng-icons/lucide';
import { HlmAlertImports } from '@spartan-ng/helm/alert';
import { HlmBadgeImports } from '@spartan-ng/helm/badge';
import { HlmButtonImports } from '@spartan-ng/helm/button';
import { HlmCardImports } from '@spartan-ng/helm/card';
import { HlmEmptyImports } from '@spartan-ng/helm/empty';
import { HlmInputImports } from '@spartan-ng/helm/input';
import { HlmLabelImports } from '@spartan-ng/helm/label';
import { HlmSkeletonImports } from '@spartan-ng/helm/skeleton';
import { HlmTableImports } from '@spartan-ng/helm/table';
import { Subject, debounceTime, distinctUntilChanged, switchMap } from 'rxjs';
import { ProjectInput, ProjectSummary } from '../core/models';
import { ProjectsService } from '../core/projects.service';
import { errorMessage, fieldErrors, roleLabel } from '../core/ui-helpers';
import { ProjectForm } from './project-form';

@Component({
  selector: 'app-project-list',
  imports: [
    RouterLink,
    ProjectForm,
    NgIcon,
    HlmAlertImports,
    HlmBadgeImports,
    HlmButtonImports,
    HlmCardImports,
    HlmEmptyImports,
    HlmInputImports,
    HlmLabelImports,
    HlmSkeletonImports,
    HlmTableImports,
  ],
  providers: [provideIcons({ lucideFolderOpen, lucidePlus, lucideRefreshCw, lucideSearch })],
  template: `
    <div class="mb-6 flex flex-wrap items-center justify-between gap-4">
      <h1 class="text-3xl font-semibold tracking-tight">Projetos</h1>
      @if (!creating()) {
        <button type="button" hlmBtn (click)="creating.set(true)">
          <ng-icon name="lucidePlus" />
          Novo projeto
        </button>
      }
    </div>

    @if (creating()) {
      <section hlmCard class="mb-6">
        <div hlmCardHeader>
          <h2 hlmCardTitle>Novo projeto</h2>
        </div>
        <div hlmCardContent class="space-y-4">
          @if (createError()) {
            <div hlmAlert variant="destructive">
              <p hlmAlertDescription>{{ createError() }}</p>
            </div>
          }
          <app-project-form
            submitLabel="Criar projeto"
            [saving]="saving()"
            [serverErrors]="createFieldErrors()"
            (submitted)="create($event)"
            (cancelled)="closeForm()"
          />
        </div>
      </section>
    }

    <div class="mb-4 max-w-md">
      <label hlmLabel for="project-search" class="sr-only">Pesquisar por nome</label>
      <div class="relative">
        <ng-icon
          name="lucideSearch"
          class="text-muted-foreground pointer-events-none absolute top-1/2 left-2.5 -translate-y-1/2"
        />
        <input
          hlmInput
          id="project-search"
          type="search"
          class="pl-8"
          placeholder="Pesquisar por nome"
          [value]="query()"
          (input)="onSearch($any($event.target).value)"
        />
      </div>
    </div>

    @if (loading()) {
      <div class="space-y-2" aria-busy="true">
        <span class="sr-only">A carregar…</span>
        @for (i of [1, 2, 3, 4]; track i) {
          <hlm-skeleton class="h-10 w-full" />
        }
      </div>
    } @else if (error()) {
      <div hlmAlert variant="destructive">
        <p hlmAlertDescription>{{ error() }}</p>
        <div hlmAlertAction>
          <button type="button" hlmBtn variant="outline" size="sm" (click)="reload()">
            <ng-icon name="lucideRefreshCw" />
            Tentar novamente
          </button>
        </div>
      </div>
    } @else if (projects().length === 0) {
      <hlm-empty class="border border-dashed">
        <hlm-empty-header>
          <hlm-empty-media variant="icon"><ng-icon name="lucideFolderOpen" /></hlm-empty-media>
          <p hlmEmptyTitle>
            {{ query() ? 'Nenhum projeto corresponde à pesquisa.' : 'Ainda não tem projetos.' }}
          </p>
        </hlm-empty-header>
      </hlm-empty>
    } @else {
      <div hlmTableContainer class="bg-card rounded-xl border">
        <table hlmTable>
          <thead hlmTHead>
            <tr hlmTr>
              <th hlmTh class="ps-4">Nome</th>
              <th hlmTh>Papel</th>
              <th hlmTh class="text-end">Por fazer</th>
              <th hlmTh class="text-end">Em curso</th>
              <th hlmTh class="pe-4 text-end">Concluídas</th>
            </tr>
          </thead>
          <tbody hlmTBody>
            @for (p of projects(); track p.id) {
              <tr hlmTr>
                <td hlmTd class="ps-4">
                  <a
                    [routerLink]="['/projetos', p.id]"
                    class="font-medium underline-offset-4 hover:underline"
                    >{{ p.name }}</a
                  >
                </td>
                <td hlmTd>
                  <span hlmBadge [variant]="p.myRole === 'MANAGER' ? 'default' : 'secondary'">
                    {{ roleLabel[p.myRole] }}
                  </span>
                </td>
                <td hlmTd class="text-end tabular-nums">{{ p.counts.todo }}</td>
                <td hlmTd class="text-end tabular-nums">{{ p.counts.inProgress }}</td>
                <td hlmTd class="pe-4 text-end tabular-nums">{{ p.counts.done }}</td>
              </tr>
            }
          </tbody>
        </table>
      </div>
    }
  `,
})
export class ProjectList implements OnInit {
  private readonly api = inject(ProjectsService);
  private readonly router = inject(Router);
  private readonly form = viewChild(ProjectForm);
  protected readonly roleLabel = roleLabel;

  protected readonly projects = signal<ProjectSummary[]>([]);
  protected readonly query = signal('');
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly creating = signal(false);
  protected readonly saving = signal(false);
  protected readonly createError = signal<string | null>(null);
  protected readonly createFieldErrors = signal<Record<string, string>>({});

  private readonly search$ = new Subject<string>();

  constructor() {
    this.search$
      .pipe(
        debounceTime(300),
        distinctUntilChanged(),
        switchMap((q) => {
          this.loading.set(true);
          this.error.set(null);
          return this.api.list(q);
        }),
        takeUntilDestroyed(),
      )
      .subscribe({
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

  ngOnInit(): void {
    this.reload();
  }

  protected onSearch(value: string): void {
    this.query.set(value);
    this.search$.next(value);
  }

  protected reload(): void {
    this.loading.set(true);
    this.error.set(null);
    this.api.list(this.query()).subscribe({
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

  protected create(input: ProjectInput): void {
    this.saving.set(true);
    this.createError.set(null);
    this.createFieldErrors.set({});
    this.api.create(input).subscribe({
      next: (project) => {
        this.saving.set(false);
        this.router.navigate(['/projetos', project.id]);
      },
      error: (err) => {
        this.saving.set(false);
        this.createError.set(errorMessage(err));
        this.createFieldErrors.set(fieldErrors(err));
      },
    });
  }

  protected closeForm(): void {
    this.form()?.reset();
    this.creating.set(false);
    this.createError.set(null);
    this.createFieldErrors.set({});
  }
}
