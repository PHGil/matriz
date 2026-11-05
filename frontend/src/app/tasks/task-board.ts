import {
  Component,
  OnInit,
  computed,
  inject,
  input,
  output,
  signal,
  viewChild,
} from '@angular/core';
import { RouterLink } from '@angular/router';
import { NgIcon, provideIcons } from '@ng-icons/core';
import {
  lucideCalendar,
  lucideCircleCheck,
  lucideEllipsisVertical,
  lucidePlus,
} from '@ng-icons/lucide';
import { HlmAlertImports } from '@spartan-ng/helm/alert';
import { HlmAvatarImports } from '@spartan-ng/helm/avatar';
import { HlmBadgeImports } from '@spartan-ng/helm/badge';
import { HlmButtonImports } from '@spartan-ng/helm/button';
import { HlmCardImports } from '@spartan-ng/helm/card';
import { HlmDropdownMenuImports } from '@spartan-ng/helm/dropdown-menu';
import { HlmSkeletonImports } from '@spartan-ng/helm/skeleton';
import { Task, TaskInput, TaskPriority, TaskStatus } from '../core/models';
import { TasksService } from '../core/tasks.service';
import {
  errorMessage,
  fieldErrors,
  formatDate,
  initials,
  priorities,
  priorityBadge,
  priorityLabel,
  selectClass,
  statusLabel,
  statuses,
} from '../core/ui-helpers';
import { TaskForm } from './task-form';

/** Board with one column per status (FR-30) and the task creation form (US-05). */
@Component({
  selector: 'app-task-board',
  imports: [
    NgIcon,
    RouterLink,
    TaskForm,
    HlmAlertImports,
    HlmAvatarImports,
    HlmBadgeImports,
    HlmButtonImports,
    HlmCardImports,
    HlmDropdownMenuImports,
    HlmSkeletonImports,
  ],
  providers: [
    provideIcons({ lucideCalendar, lucideCircleCheck, lucideEllipsisVertical, lucidePlus }),
  ],
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

    <div class="mb-4 flex flex-wrap items-end gap-3" role="search" aria-label="Filtrar tarefas">
      <div class="flex flex-col gap-1">
        <label for="filter-status" class="text-sm font-medium">Estado</label>
        <select
          id="filter-status"
          [class]="selectClass + ' w-40'"
          (change)="setStatusFilter($any($event.target).value)"
        >
          <option value="" [selected]="statusFilter() === ''">Todos</option>
          @for (s of statuses; track s) {
            <option [value]="s" [selected]="statusFilter() === s">{{ statusLabel[s] }}</option>
          }
        </select>
      </div>
      <div class="flex flex-col gap-1">
        <label for="filter-priority" class="text-sm font-medium">Prioridade</label>
        <select
          id="filter-priority"
          [class]="selectClass + ' w-40'"
          (change)="setPriorityFilter($any($event.target).value)"
        >
          <option value="" [selected]="priorityFilter() === ''">Todas</option>
          @for (p of priorities; track p) {
            <option [value]="p" [selected]="priorityFilter() === p">{{ priorityLabel[p] }}</option>
          }
        </select>
      </div>
      @if (filtered()) {
        <button type="button" hlmBtn variant="ghost" size="sm" (click)="clearFilters()">
          Limpar filtros
        </button>
        <p class="text-muted-foreground w-full text-xs">
          Filtros ativos: o progresso do projeto continua a contar todas as tarefas.
        </p>
      }
    </div>

    @if (success()) {
      <div hlmAlert role="status" class="mb-4">
        <ng-icon name="lucideCircleCheck" />
        <p hlmAlertDescription>{{ success() }}</p>
      </div>
    }
    @if (actionError()) {
      <div hlmAlert variant="destructive" class="mb-4">
        <p hlmAlertDescription>{{ actionError() }}</p>
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
          <button type="button" hlmBtn size="sm" variant="outline" (click)="reload()">
            Tentar novamente
          </button>
        </div>
      </div>
    } @else {
      <div class="grid items-start gap-4 md:grid-cols-3">
        @for (status of statuses; track status) {
          <section
            class="bg-muted/40 rounded-xl border p-3"
            [attr.aria-label]="statusLabel[status]"
          >
            <h3 class="mb-3 flex items-center justify-between px-1 text-sm font-semibold">
              {{ statusLabel[status] }}
              <span hlmBadge variant="outline">{{ columns()[status].length }}</span>
            </h3>
            <ul class="space-y-2">
              @for (t of columns()[status]; track t.id) {
                <li hlmCard class="gap-2 py-3" [attr.aria-busy]="movingId() === t.id">
                  <div hlmCardContent class="space-y-2 px-3">
                    <div class="flex items-start justify-between gap-2">
                      <a
                        class="font-medium break-words hover:underline"
                        [routerLink]="['/projetos', projectId(), 'tarefas', t.id]"
                        >{{ t.title }}</a
                      >
                      <button
                        type="button"
                        hlmBtn
                        variant="ghost"
                        size="icon-sm"
                        class="-me-1 -mt-1 shrink-0"
                        [attr.aria-label]="'Mover ' + t.title"
                        [disabled]="movingId() !== null"
                        [hlmDropdownMenuTrigger]="moveMenu"
                        align="end"
                      >
                        <ng-icon name="lucideEllipsisVertical" />
                      </button>
                      <ng-template #moveMenu>
                        <hlm-dropdown-menu class="w-44">
                          <hlm-dropdown-menu-label>Mover para</hlm-dropdown-menu-label>
                          @for (target of statuses; track target) {
                            @if (target !== t.status) {
                              <button
                                type="button"
                                hlmDropdownMenuItem
                                (triggered)="move(t, target)"
                              >
                                {{ statusLabel[target] }}
                              </button>
                            }
                          }
                        </hlm-dropdown-menu>
                      </ng-template>
                    </div>
                    <div class="flex flex-wrap items-center gap-2 text-xs">
                      <span hlmBadge [variant]="priorityBadge[t.priority]">{{
                        priorityLabel[t.priority]
                      }}</span>
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
                <li class="text-muted-foreground px-1 py-4 text-center text-sm">
                  {{ filtered() ? 'Nenhuma tarefa corresponde aos filtros' : 'Sem tarefas' }}
                </li>
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
  protected readonly priorities = priorities;
  protected readonly selectClass = selectClass;
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
  protected readonly actionError = signal<string | null>(null);
  protected readonly movingId = signal<string | null>(null);
  protected readonly statusFilter = signal<TaskStatus | ''>('');
  protected readonly priorityFilter = signal<TaskPriority | ''>('');
  protected readonly filtered = computed(
    () => this.statusFilter() !== '' || this.priorityFilter() !== '',
  );

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
    this.api
      .list(this.projectId(), { status: this.statusFilter(), priority: this.priorityFilter() })
      .subscribe({
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

  protected setStatusFilter(value: TaskStatus | ''): void {
    this.statusFilter.set(value);
    this.reload();
  }

  protected setPriorityFilter(value: TaskPriority | ''): void {
    this.priorityFilter.set(value);
    this.reload();
  }

  protected clearFilters(): void {
    this.statusFilter.set('');
    this.priorityFilter.set('');
    this.reload();
  }

  protected startCreate(): void {
    this.success.set(null);
    this.creating.set(true);
  }

  /** Status change from the board (US-06); the list is updated with the task returned by the API. */
  protected move(task: Task, status: TaskStatus): void {
    this.movingId.set(task.id);
    this.actionError.set(null);
    this.success.set(null);
    this.api.update(this.projectId(), task.id, { status }).subscribe({
      next: (updated) => {
        if (this.filtered()) {
          this.reload();
        } else {
          this.tasks.update((list) => list.map((t) => (t.id === updated.id ? updated : t)));
        }
        this.movingId.set(null);
        this.success.set(`"${updated.title}" movida para ${statusLabel[updated.status]}.`);
        this.changed.emit();
      },
      error: (err) => {
        this.movingId.set(null);
        this.actionError.set(errorMessage(err));
      },
    });
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
