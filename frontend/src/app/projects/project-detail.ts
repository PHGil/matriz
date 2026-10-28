import { Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { Project, ProjectInput } from '../core/models';
import { ProjectsService } from '../core/projects.service';
import { errorMessage, fieldErrors, roleLabel } from '../core/ui-helpers';
import { ProjectForm } from './project-form';

@Component({
  selector: 'app-project-detail',
  imports: [RouterLink, ProjectForm],
  template: `
    <p class="muted"><a routerLink="/projetos">Projetos</a> › {{ project()?.name }}</p>

    @if (loading()) {
      <p class="muted">A carregar…</p>
    } @else if (loadError()) {
      <p class="notice error" role="alert">{{ loadError() }}</p>
    } @else if (project(); as p) {
      @if (editing()) {
        <section class="panel">
          <h2>Editar projeto</h2>
          @if (actionError()) {
            <p class="notice error" role="alert">{{ actionError() }}</p>
          }
          <app-project-form
            [initial]="{ name: p.name, description: p.description }"
            [saving]="busy()"
            [serverErrors]="editFieldErrors()"
            (submitted)="save($event)"
            (cancelled)="editing.set(false)"
          />
        </section>
      } @else {
        <div class="toolbar">
          <div>
            <h1>{{ p.name }}</h1>
            @if (p.description) {
              <p>{{ p.description }}</p>
            }
            <span class="badge">{{ roleLabel[p.myRole] }}</span>
          </div>
          <div class="actions">
            <a [routerLink]="['/projetos', p.id, 'membros']" class="button">Membros</a>
            @if (isManager()) {
              <button type="button" (click)="startEdit()">Editar</button>
              <button type="button" class="danger" (click)="remove(p)" [disabled]="busy()">
                Eliminar
              </button>
            }
          </div>
        </div>
        @if (success()) {
          <p class="notice success" role="status">{{ success() }}</p>
        }
        @if (actionError()) {
          <p class="notice error" role="alert">{{ actionError() }}</p>
        }
        <p class="muted">O quadro de tarefas será disponibilizado no próximo incremento.</p>
      }
    }
  `,
})
export class ProjectDetail implements OnInit {
  readonly id = input.required<string>();

  private readonly api = inject(ProjectsService);
  private readonly router = inject(Router);
  protected readonly roleLabel = roleLabel;

  protected readonly project = signal<Project | null>(null);
  protected readonly loading = signal(true);
  protected readonly loadError = signal<string | null>(null);
  protected readonly editing = signal(false);
  protected readonly busy = signal(false);
  protected readonly actionError = signal<string | null>(null);
  protected readonly editFieldErrors = signal<Record<string, string>>({});
  protected readonly success = signal<string | null>(null);
  protected readonly isManager = computed(() => this.project()?.myRole === 'MANAGER');

  ngOnInit(): void {
    this.api.get(this.id()).subscribe({
      next: (p) => {
        this.project.set(p);
        this.loading.set(false);
      },
      error: (err) => {
        this.loadError.set(errorMessage(err));
        this.loading.set(false);
      },
    });
  }

  protected startEdit(): void {
    this.actionError.set(null);
    this.editFieldErrors.set({});
    this.success.set(null);
    this.editing.set(true);
  }

  protected save(input: ProjectInput): void {
    this.busy.set(true);
    this.actionError.set(null);
    this.editFieldErrors.set({});
    this.api.update(this.id(), input).subscribe({
      next: (p) => {
        this.project.set(p);
        this.busy.set(false);
        this.editing.set(false);
        this.success.set('Projeto atualizado.');
      },
      error: (err) => {
        this.busy.set(false);
        this.actionError.set(errorMessage(err));
        this.editFieldErrors.set(fieldErrors(err));
      },
    });
  }

  protected remove(p: Project): void {
    const confirmed = window.confirm(
      `Eliminar o projeto "${p.name}"? Todas as tarefas, responsáveis e comentários serão eliminados.`,
    );
    if (!confirmed) {
      return;
    }
    this.busy.set(true);
    this.actionError.set(null);
    this.api.delete(p.id).subscribe({
      next: () => this.router.navigate(['/projetos']),
      error: (err) => {
        this.busy.set(false);
        this.actionError.set(errorMessage(err));
      },
    });
  }
}
