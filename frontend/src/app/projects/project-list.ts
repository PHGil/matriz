import { Component, OnInit, inject, signal, viewChild } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Router, RouterLink } from '@angular/router';
import { Subject, debounceTime, distinctUntilChanged, switchMap } from 'rxjs';
import { ProjectInput, ProjectSummary } from '../core/models';
import { ProjectsService } from '../core/projects.service';
import { errorMessage, fieldErrors, roleLabel } from '../core/ui-helpers';
import { ProjectForm } from './project-form';

@Component({
  selector: 'app-project-list',
  imports: [RouterLink, ProjectForm],
  template: `
    <div class="toolbar">
      <h1>Projetos</h1>
      @if (!creating()) {
        <button type="button" class="primary" (click)="creating.set(true)">+ Novo projeto</button>
      }
    </div>

    @if (creating()) {
      <section class="panel">
        <h2>Novo projeto</h2>
        @if (createError()) {
          <p class="notice error" role="alert">{{ createError() }}</p>
        }
        <app-project-form
          submitLabel="Criar projeto"
          [saving]="saving()"
          [serverErrors]="createFieldErrors()"
          (submitted)="create($event)"
          (cancelled)="closeForm()"
        />
      </section>
    }

    <label class="search">
      Pesquisar por nome
      <input type="search" [value]="query()" (input)="onSearch($any($event.target).value)" />
    </label>

    @if (loading()) {
      <p class="muted">A carregar…</p>
    } @else if (error()) {
      <p class="notice error" role="alert">
        {{ error() }} <button type="button" (click)="reload()">Tentar novamente</button>
      </p>
    } @else if (projects().length === 0) {
      <p class="muted">
        {{ query() ? 'Nenhum projeto corresponde à pesquisa.' : 'Ainda não tem projetos.' }}
      </p>
    } @else {
      <table class="table">
        <thead>
          <tr>
            <th>Nome</th>
            <th>Papel</th>
            <th>Por fazer</th>
            <th>Em curso</th>
            <th>Concluídas</th>
          </tr>
        </thead>
        <tbody>
          @for (p of projects(); track p.id) {
            <tr>
              <td>
                <a [routerLink]="['/projetos', p.id]">{{ p.name }}</a>
              </td>
              <td>{{ roleLabel[p.myRole] }}</td>
              <td>{{ p.counts.todo }}</td>
              <td>{{ p.counts.inProgress }}</td>
              <td>{{ p.counts.done }}</td>
            </tr>
          }
        </tbody>
      </table>
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
