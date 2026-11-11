import { LowerCasePipe } from '@angular/common';
import { Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { Router } from '@angular/router';
import { NgIcon, provideIcons } from '@ng-icons/core';
import { lucideCalendar, lucideCircleCheck, lucidePencil, lucideTrash2 } from '@ng-icons/lucide';
import { HlmAlertImports } from '@spartan-ng/helm/alert';
import { HlmAlertDialogImports } from '@spartan-ng/helm/alert-dialog';
import { HlmBadgeImports } from '@spartan-ng/helm/badge';
import { HlmBreadcrumbImports } from '@spartan-ng/helm/breadcrumb';
import { HlmButtonImports } from '@spartan-ng/helm/button';
import { HlmCardImports } from '@spartan-ng/helm/card';
import { HlmSkeletonImports } from '@spartan-ng/helm/skeleton';
import { forkJoin } from 'rxjs';
import { Project, Task, TaskChanges, TaskInput, TaskStatus, UserSummary } from '../core/models';
import { ProjectsService } from '../core/projects.service';
import { TasksService } from '../core/tasks.service';
import {
  errorMessage,
  fieldErrors,
  formatDate,
  priorityBadge,
  priorityLabel,
  selectClass,
  statusLabel,
  statuses,
} from '../core/ui-helpers';
import { TaskAssignees } from './task-assignees';
import { TaskComments } from './task-comments';
import { TaskForm } from './task-form';

@Component({
  selector: 'app-task-detail',
  imports: [
    LowerCasePipe,
    NgIcon,
    TaskAssignees,
    TaskComments,
    TaskForm,
    HlmAlertImports,
    HlmAlertDialogImports,
    HlmBadgeImports,
    HlmBreadcrumbImports,
    HlmButtonImports,
    HlmCardImports,
    HlmSkeletonImports,
  ],
  providers: [provideIcons({ lucideCalendar, lucideCircleCheck, lucidePencil, lucideTrash2 })],
  template: `
    <nav hlmBreadcrumb class="mb-4">
      <ol hlmBreadcrumbList>
        <li hlmBreadcrumbItem><a hlmBreadcrumbLink link="/projetos">Projetos</a></li>
        <li hlmBreadcrumbSeparator></li>
        <li hlmBreadcrumbItem>
          <a hlmBreadcrumbLink [link]="['/projetos', id()]">{{ project()?.name }}</a>
        </li>
        @if (task(); as t) {
          <li hlmBreadcrumbSeparator></li>
          <li hlmBreadcrumbItem>
            <span hlmBreadcrumbPage>{{ t.title }}</span>
          </li>
        }
      </ol>
    </nav>

    @if (loading()) {
      <div class="space-y-3" aria-busy="true">
        <span class="sr-only">A carregar…</span>
        <hlm-skeleton class="h-9 w-80 max-w-full" />
        <hlm-skeleton class="h-32 w-full" />
      </div>
    } @else if (loadError()) {
      <div hlmAlert variant="destructive">
        <p hlmAlertDescription>{{ loadError() }}</p>
      </div>
    } @else if (task(); as t) {
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

        @if (editing()) {
          <section hlmCard>
            <div hlmCardHeader>
              <h2 hlmCardTitle>Editar tarefa</h2>
            </div>
            <div hlmCardContent>
              <app-task-form
                idPrefix="edit-task"
                [initial]="formValue()"
                [saving]="busy()"
                [serverErrors]="editFieldErrors()"
                (submitted)="save($event)"
                (cancelled)="editing.set(false)"
              />
            </div>
          </section>
        } @else {
          <section hlmCard>
            <div hlmCardHeader class="flex flex-wrap items-start justify-between gap-3">
              <div class="min-w-0 space-y-2">
                <h1 class="text-2xl font-semibold tracking-tight break-words">{{ t.title }}</h1>
                <div class="flex flex-wrap items-center gap-2 text-sm">
                  <span hlmBadge [variant]="priorityBadge[t.priority]">
                    Prioridade {{ priorityLabel[t.priority] | lowercase }}
                  </span>
                  @if (t.dueDate) {
                    <span class="text-muted-foreground flex items-center gap-1">
                      <ng-icon name="lucideCalendar" />
                      Prazo {{ formatDate(t.dueDate) }}
                    </span>
                  }
                </div>
              </div>
              <div class="flex flex-wrap items-center gap-2">
                <label for="task-status" class="text-sm font-medium">Estado</label>
                <select
                  id="task-status"
                  [class]="selectClass + ' w-40'"
                  [disabled]="busy()"
                  (change)="changeStatus($any($event.target).value)"
                >
                  @for (s of statuses; track s) {
                    <option [value]="s" [selected]="s === t.status">{{ statusLabel[s] }}</option>
                  }
                </select>
                <button
                  type="button"
                  hlmBtn
                  variant="outline"
                  (click)="startEdit()"
                  [disabled]="busy()"
                >
                  <ng-icon name="lucidePencil" />
                  Editar
                </button>
                @if (isManager()) {
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
                        <h2 hlmAlertDialogTitle>Eliminar a tarefa "{{ t.title }}"?</h2>
                        <p hlmAlertDialogDescription>
                          Os responsáveis e comentários da tarefa também serão eliminados.
                        </p>
                      </hlm-alert-dialog-header>
                      <hlm-alert-dialog-footer>
                        <button hlmAlertDialogCancel>Cancelar</button>
                        <button
                          hlmAlertDialogAction
                          variant="destructive"
                          (click)="ctx.close(); remove()"
                        >
                          Eliminar
                        </button>
                      </hlm-alert-dialog-footer>
                    </hlm-alert-dialog-content>
                  </hlm-alert-dialog>
                }
              </div>
            </div>
            <div hlmCardContent class="space-y-4">
              @if (t.description) {
                <p class="whitespace-pre-line">{{ t.description }}</p>
              } @else {
                <p class="text-muted-foreground">Sem descrição.</p>
              }
              <app-task-assignees
                [projectId]="id()"
                [taskId]="taskId()"
                [assignees]="t.assignees"
                (assigneesChange)="setAssignees($event)"
              />
              <p class="text-muted-foreground text-xs">
                Criada em {{ formatDate(t.createdAt) }} · Atualizada em
                {{ formatDate(t.updatedAt) }}
              </p>
            </div>
          </section>
        }

        <section hlmCard>
          <div hlmCardContent>
            <app-task-comments [projectId]="id()" [taskId]="taskId()" />
          </div>
        </section>
      </div>
    }
  `,
})
export class TaskDetail implements OnInit {
  readonly id = input.required<string>();
  readonly taskId = input.required<string>();

  private readonly projectsApi = inject(ProjectsService);
  private readonly api = inject(TasksService);
  private readonly router = inject(Router);

  protected readonly statuses = statuses;
  protected readonly statusLabel = statusLabel;
  protected readonly priorityLabel = priorityLabel;
  protected readonly priorityBadge = priorityBadge;
  protected readonly selectClass = selectClass;
  protected readonly formatDate = formatDate;

  protected readonly project = signal<Project | null>(null);
  protected readonly task = signal<Task | null>(null);
  protected readonly loading = signal(true);
  protected readonly loadError = signal<string | null>(null);
  protected readonly editing = signal(false);
  protected readonly busy = signal(false);
  protected readonly actionError = signal<string | null>(null);
  protected readonly editFieldErrors = signal<Record<string, string>>({});
  protected readonly success = signal<string | null>(null);
  protected readonly isManager = computed(() => this.project()?.myRole === 'MANAGER');
  protected readonly formValue = computed<TaskInput | null>(() => {
    const t = this.task();
    return t
      ? { title: t.title, description: t.description, priority: t.priority, dueDate: t.dueDate }
      : null;
  });

  ngOnInit(): void {
    forkJoin({
      project: this.projectsApi.get(this.id()),
      task: this.api.get(this.id(), this.taskId()),
    }).subscribe({
      next: ({ project, task }) => {
        this.project.set(project);
        this.task.set(task);
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

  protected changeStatus(status: TaskStatus): void {
    this.apply({ status }, `Estado alterado para ${statusLabel[status]}.`);
  }

  protected save(input: TaskInput): void {
    this.apply(input, 'Tarefa atualizada.', () => this.editing.set(false));
  }

  protected setAssignees(assignees: UserSummary[]): void {
    this.task.update((t) => (t ? { ...t, assignees } : t));
  }

  /** Only shown to the Manager; the API enforces it too (FR-25). */
  protected remove(): void {
    this.busy.set(true);
    this.actionError.set(null);
    this.api.delete(this.id(), this.taskId()).subscribe({
      next: () => this.router.navigate(['/projetos', this.id()]),
      error: (err) => {
        this.busy.set(false);
        this.actionError.set(errorMessage(err));
      },
    });
  }

  private apply(changes: TaskChanges, message: string, onSuccess?: () => void): void {
    this.busy.set(true);
    this.actionError.set(null);
    this.editFieldErrors.set({});
    this.success.set(null);
    this.api.update(this.id(), this.taskId(), changes).subscribe({
      next: (task) => {
        this.task.set(task);
        this.busy.set(false);
        this.success.set(message);
        onSuccess?.();
      },
      error: (err) => {
        this.busy.set(false);
        this.actionError.set(errorMessage(err));
        this.editFieldErrors.set(fieldErrors(err));
        // Revert the status <select> to the confirmed value
        this.task.update((t) => (t ? { ...t } : t));
      },
    });
  }
}
