import { Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { NgIcon, provideIcons } from '@ng-icons/core';
import { lucideCircleCheck, lucidePencil, lucideTrash2, lucideUsers } from '@ng-icons/lucide';
import { HlmAlertImports } from '@spartan-ng/helm/alert';
import { HlmAlertDialogImports } from '@spartan-ng/helm/alert-dialog';
import { HlmBadgeImports } from '@spartan-ng/helm/badge';
import { HlmBreadcrumbImports } from '@spartan-ng/helm/breadcrumb';
import { HlmButtonImports } from '@spartan-ng/helm/button';
import { HlmCardImports } from '@spartan-ng/helm/card';
import { HlmProgressImports } from '@spartan-ng/helm/progress';
import { HlmSkeletonImports } from '@spartan-ng/helm/skeleton';
import { Project, ProjectInput, ProjectProgress } from '../core/models';
import { ProjectsService } from '../core/projects.service';
import { errorMessage, fieldErrors, roleLabel } from '../core/ui-helpers';
import { TaskBoard } from '../tasks/task-board';
import { ProjectForm } from './project-form';

@Component({
  selector: 'app-project-detail',
  imports: [
    RouterLink,
    ProjectForm,
    TaskBoard,
    NgIcon,
    HlmAlertImports,
    HlmAlertDialogImports,
    HlmBadgeImports,
    HlmBreadcrumbImports,
    HlmButtonImports,
    HlmCardImports,
    HlmProgressImports,
    HlmSkeletonImports,
  ],
  providers: [
    provideIcons({
      lucideCircleCheck,
      lucidePencil,
      lucideTrash2,
      lucideUsers,
    }),
  ],
  template: `
    <nav hlmBreadcrumb class="mb-4">
      <ol hlmBreadcrumbList>
        <li hlmBreadcrumbItem><a hlmBreadcrumbLink link="/projetos">Projetos</a></li>
        @if (project(); as p) {
          <li hlmBreadcrumbSeparator></li>
          <li hlmBreadcrumbItem>
            <span hlmBreadcrumbPage>{{ p.name }}</span>
          </li>
        }
      </ol>
    </nav>

    @if (loading()) {
      <div class="space-y-3" aria-busy="true">
        <span class="sr-only">A carregar…</span>
        <hlm-skeleton class="h-9 w-64" />
        <hlm-skeleton class="h-5 w-96 max-w-full" />
      </div>
    } @else if (loadError()) {
      <div hlmAlert variant="destructive">
        <p hlmAlertDescription>{{ loadError() }}</p>
      </div>
    } @else if (project(); as p) {
      @if (editing()) {
        <section hlmCard>
          <div hlmCardHeader>
            <h2 hlmCardTitle>Editar projeto</h2>
          </div>
          <div hlmCardContent class="space-y-4">
            @if (actionError()) {
              <div hlmAlert variant="destructive">
                <p hlmAlertDescription>{{ actionError() }}</p>
              </div>
            }
            <app-project-form
              [initial]="{ name: p.name, description: p.description }"
              [saving]="busy()"
              [serverErrors]="editFieldErrors()"
              (submitted)="save($event)"
              (cancelled)="editing.set(false)"
            />
          </div>
        </section>
      } @else {
        <div class="mb-6 flex flex-wrap items-start justify-between gap-4">
          <div class="min-w-0 space-y-2">
            <div class="flex flex-wrap items-center gap-3">
              <h1 class="text-3xl font-semibold tracking-tight break-words">{{ p.name }}</h1>
              <span hlmBadge [variant]="p.myRole === 'MANAGER' ? 'default' : 'secondary'">
                {{ roleLabel[p.myRole] }}
              </span>
            </div>
            @if (p.description) {
              <p class="text-muted-foreground max-w-prose">{{ p.description }}</p>
            }
          </div>
          <div class="flex flex-wrap gap-2">
            <a hlmBtn variant="outline" [routerLink]="['/projetos', p.id, 'membros']">
              <ng-icon name="lucideUsers" />
              Membros
            </a>
            @if (isManager()) {
              <button type="button" hlmBtn variant="outline" (click)="startEdit()">
                <ng-icon name="lucidePencil" />
                Editar
              </button>
              <hlm-alert-dialog>
                <button
                  type="button"
                  hlmBtn
                  variant="destructive"
                  hlmAlertDialogTrigger
                  [disabled]="busy()"
                >
                  <ng-icon name="lucideTrash2" />
                  Eliminar
                </button>
                <hlm-alert-dialog-content *hlmAlertDialogPortal="let ctx">
                  <hlm-alert-dialog-header>
                    <h2 hlmAlertDialogTitle>Eliminar o projeto "{{ p.name }}"?</h2>
                    <p hlmAlertDialogDescription>
                      Todas as tarefas, responsáveis e comentários serão eliminados.
                    </p>
                  </hlm-alert-dialog-header>
                  <hlm-alert-dialog-footer>
                    <button hlmAlertDialogCancel>Cancelar</button>
                    <button
                      hlmAlertDialogAction
                      variant="destructive"
                      (click)="ctx.close(); remove(p)"
                    >
                      Eliminar
                    </button>
                  </hlm-alert-dialog-footer>
                </hlm-alert-dialog-content>
              </hlm-alert-dialog>
            }
          </div>
        </div>
        <div class="space-y-4">
          @if (success()) {
            <div hlmAlert role="status">
              <ng-icon name="lucideCircleCheck" />
              <p hlmAlertDescription>{{ success() }}</p>
            </div>
          }
          @if (actionError()) {
            <div hlmAlert variant="destructive">
              <p hlmAlertDescription>{{ actionError() }}</p>
            </div>
          }
          <section hlmCard [attr.aria-busy]="progress() === null">
            <div hlmCardHeader>
              <h2 hlmCardTitle>Progresso</h2>
            </div>
            <div hlmCardContent class="space-y-3">
              @if (progress(); as pr) {
                <dl class="grid grid-cols-3 gap-2 text-center">
                  <div class="bg-muted/50 rounded-lg p-2">
                    <dt class="text-muted-foreground text-xs">Por fazer</dt>
                    <dd class="text-xl font-semibold tabular-nums">{{ pr.todo }}</dd>
                  </div>
                  <div class="bg-muted/50 rounded-lg p-2">
                    <dt class="text-muted-foreground text-xs">Em curso</dt>
                    <dd class="text-xl font-semibold tabular-nums">{{ pr.inProgress }}</dd>
                  </div>
                  <div class="bg-muted/50 rounded-lg p-2">
                    <dt class="text-muted-foreground text-xs">Concluídas</dt>
                    <dd class="text-xl font-semibold tabular-nums">{{ pr.done }}</dd>
                  </div>
                </dl>
                @if (pr.percentDone === null) {
                  <p class="text-muted-foreground text-sm">Sem tarefas</p>
                } @else {
                  <div class="space-y-1.5">
                    <p class="text-muted-foreground text-sm">{{ pr.percentDone }}% concluído</p>
                    <hlm-progress [value]="pr.percentDone" aria-label="Progresso do projeto">
                      <hlm-progress-indicator />
                    </hlm-progress>
                  </div>
                }
              } @else {
                <hlm-skeleton class="h-16 w-full" />
              }
            </div>
          </section>
          <app-task-board [projectId]="p.id" (changed)="loadProgress()" />
        </div>
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
  protected readonly progress = signal<ProjectProgress | null>(null);

  ngOnInit(): void {
    this.loadProgress();
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

  /** Counts always cover all tasks of the project (US-07, US-09 AC2). */
  protected loadProgress(): void {
    this.api.progress(this.id()).subscribe({
      next: (progress) => this.progress.set(progress),
      error: () => this.progress.set(null),
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
