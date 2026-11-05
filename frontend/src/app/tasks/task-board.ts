import { Component, OnInit, computed, inject, input, output, signal, viewChild } from '@angular/core';
import { NgIcon, provideIcons } from '@ng-icons/core';
import { lucideCalendar, lucideCircleCheck, lucidePlus } from '@ng-icons/lucide';
import { HlmAlertImports } from '@spartan-ng/helm/alert';
import { HlmAvatarImports } from '@spartan-ng/helm/avatar';
import { HlmBadgeImports } from '@spartan-ng/helm/badge';
import { HlmButtonImports } from '@spartan-ng/helm/button';
import { HlmCardImports } from '@spartan-ng/helm/card';
import { HlmSkeletonImports } from '@spartan-ng/helm/skeleton';
import { Task, TaskInput, TaskStatus } from '../core/models';
import { TasksService } from '../core/tasks.service';
import {
  errorMessage,
  fieldErrors,
  formatDate,
  initials,
  priorityBadge,
  priorityLabel,
  statusLabel,
  statuses,
} from '../core/ui-helpers';
import { TaskForm } from './task-form';

/** Board with one column per status (FR-30) and the task creation form (US-05). */
@Component({
  selector: 'app-task-board',
  imports: [
    NgIcon,
    TaskForm,
    HlmAlertImports,
    HlmAvatarImports,
    HlmBadgeImports,
    HlmButtonImports,
    HlmCardImports,
    HlmSkeletonImports,
  ],
  providers: [provideIcons({ lucideCalendar, lucideCircleCheck, lucidePlus })],
  template: `
    <div class="mb-4 flex flex-wrap items-center justify-between gap-3">
      <h2 class="text-xl font-semibold tracking-tight">Tarefas</h2>
      @if (!creating()) {
        <button type="button" hlmBtn (click)="startCreate()">
          <ng-icon name="lucidePlus" />
          Nova tarefa
        </button>
      }
    </div>

    @if (creating()) {
      <section hlmCard class="mb-6">
        <div hlmCardHeader>
          <h3 hlmCardTitle>Nova tarefa</h3>
        </div>
        <div hlmCardContent class="space-y-4">
          @if (createError()) {
            <div hlmAlert variant="destructive">
              <p hlmAlertDescription>{{ createError() }}</p>
            </div>
          }
          <app-task-form
            idPrefix="new-task"
            submitLabel="Criar tarefa"
            [saving]="saving()"
            [serverErrors]="createFieldErrors()"
            (submitted)="create($event)"
            (cancelled)="closeCreate()"
          />
        </div>
      </section>
    }

    @if (success()) {
      <div hlmAlert role="status" class="mb-4">
        <ng-icon name="lucideCircleCheck" />
        <p hlmAlertDescription>{{ success() }}</p>
      </div>
    }

    @if (loading()) {
      <div class="grid gap-4 md:grid-cols-3" aria-busy="true">
        <span class="sr-only">A carregar…</span>
        @for (i of [1, 2, 3]; track i) {
          <hlm-skeleton class="h-40 w-full" />
        }
      </div>
    } @else if (error()) {
      <div hlmAlert variant="destructive">
        <p hlmAlertDescription>{{ error() }}</p>
        <div hlmAlertAction>
          <button type="button" hlmBtn size="sm" variant="outline" (click)="reload()">Tentar novamente</button>
        </div>
      </div>
    } @else {
      <div class="grid items-start gap-4 md:grid-cols-3">
        @for (status of statuses; track status) {
          <section class="bg-muted/40 rounded-xl border p-3" [attr.aria-label]="statusLabel[status]">
            <h3 class="mb-3 flex items-center justify-between px-1 text-sm font-semibold">
              {{ statusLabel[status] }}
              <span hlmBadge variant="outline">{{ columns()[status].length }}</span>
            </h3>
            <ul class="space-y-2">
              @for (t of columns()[status]; track t.id) {
                <li hlmCard class="gap-2 py-3">
                  <div hlmCardContent class="space-y-2 px-3">
                    <p class="font-medium break-words">{{ t.title }}</p>
                    <div class="flex flex-wrap items-center gap-2 text-xs">
                      <span hlmBadge [variant]="priorityBadge[t.priority]">{{ priorityLabel[t.priority] }}</span>
                      @if (t.dueDate) {
                        <span class="text-muted-foreground flex items-center gap-1">
                          <ng-icon name="lucideCalendar" />
                          {{ formatDate(t.dueDate) }}
                        </span>
                      }
                    </div>
                    @if (t.assignees.length > 0) {
                      <div class="flex -space-x-1.5">
                        @for (a of t.assignees; track a.id) {
                          <hlm-avatar size="sm" [attr.title]="a.name">
                            <span hlmAvatarFallback>{{ initials(a.name) }}</span>
                          </hlm-avatar>
                        }
                      </div>
                    }
                  </div>
                </li>
              } @empty {
                <li class="text-muted-foreground px-1 py-4 text-center text-sm">Sem tarefas</li>
              }
            </ul>
          </section>
        }
      </div>
    }
  `,
})
export class TaskBoard implements OnInit {
  readonly projectId = input.required<string>();
  /** Emitted after any change confirmed by the API, so the parent can refresh counts. */
  readonly changed = output<void>();

  private readonly api = inject(TasksService);
  private readonly form = viewChild(TaskForm);

  protected readonly statuses = statuses;
  protected readonly statusLabel = statusLabel;
  protected readonly priorityLabel = priorityLabel;
  protected readonly priorityBadge = priorityBadge;
  protected readonly formatDate = formatDate;
  protected readonly initials = initials;

  protected readonly tasks = signal<Task[]>([]);
  protected readonly loading = signal(true);
  protected readonly error = signal<string | null>(null);
  protected readonly creating = signal(false);
  protected readonly saving = signal(false);
  protected readonly createError = signal<string | null>(null);
  protected readonly createFieldErrors = signal<Record<string, string>>({});
  protected readonly success = signal<string | null>(null);

  protected readonly columns = computed(() => {
    const byStatus: Record<TaskStatus, Task[]> = { TODO: [], IN_PROGRESS: [], DONE: [] };
    for (const t of this.tasks()) {
      byStatus[t.status].push(t);
    }
    return byStatus;
  });

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    this.loading.set(this.tasks().length === 0);
    this.error.set(null);
    this.api.list(this.projectId()).subscribe({
      next: (tasks) => {
        this.tasks.set(tasks);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set(errorMessage(err));
        this.loading.set(false);
      },
    });
  }

  protected startCreate(): void {
    this.success.set(null);
    this.creating.set(true);
  }

  protected create(input: TaskInput): void {
    this.saving.set(true);
    this.createError.set(null);
    this.createFieldErrors.set({});
    this.api.create(this.projectId(), input).subscribe({
      next: (task) => {
        this.saving.set(false);
        this.closeCreate();
        this.success.set(`Tarefa "${task.title}" criada.`);
        this.reload();
        this.changed.emit();
      },
      error: (err) => {
        this.saving.set(false);
        this.createError.set(errorMessage(err));
        this.createFieldErrors.set(fieldErrors(err));
      },
    });
  }

  protected closeCreate(): void {
    this.form()?.reset();
    this.creating.set(false);
    this.createError.set(null);
    this.createFieldErrors.set({});
  }
}
