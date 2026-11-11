import { Component, OnInit, computed, inject, input, output, signal } from '@angular/core';
import { NgIcon, provideIcons } from '@ng-icons/core';
import { lucideUserPlus, lucideX } from '@ng-icons/lucide';
import { HlmAlertImports } from '@spartan-ng/helm/alert';
import { HlmAvatarImports } from '@spartan-ng/helm/avatar';
import { HlmBadgeImports } from '@spartan-ng/helm/badge';
import { HlmButtonImports } from '@spartan-ng/helm/button';
import { Member, UserSummary } from '../core/models';
import { ProjectsService } from '../core/projects.service';
import { TasksService } from '../core/tasks.service';
import { errorMessage, fieldErrors, initials, selectClass } from '../core/ui-helpers';

/** Task assignees (US-12): any member can assign active project members and remove assignments. */
@Component({
  selector: 'app-task-assignees',
  imports: [NgIcon, HlmAlertImports, HlmAvatarImports, HlmBadgeImports, HlmButtonImports],
  providers: [provideIcons({ lucideUserPlus, lucideX })],
  template: `
    <h2 class="mb-2 text-sm font-semibold">Responsáveis</h2>
    @if (error()) {
      <div hlmAlert variant="destructive" class="mb-2">
        <p hlmAlertDescription>{{ error() }}</p>
      </div>
    }
    @if (assignees().length === 0) {
      <p class="text-muted-foreground text-sm">Sem responsáveis.</p>
    } @else {
      <ul class="flex flex-wrap gap-3">
        @for (a of assignees(); track a.id) {
          <li class="flex items-center gap-2 text-sm">
            <hlm-avatar size="sm"
              ><span hlmAvatarFallback>{{ initials(a.name) }}</span></hlm-avatar
            >
            {{ a.name }}
            @if (!a.active) {
              <span hlmBadge variant="outline">desativado</span>
            }
            <button
              type="button"
              hlmBtn
              variant="ghost"
              size="icon-sm"
              [attr.aria-label]="'Remover ' + a.name"
              [disabled]="busy()"
              (click)="remove(a)"
            >
              <ng-icon name="lucideX" />
            </button>
          </li>
        }
      </ul>
    }
    <div class="mt-3 flex flex-wrap items-center gap-2">
      <label for="assignee-select" class="sr-only">Membro a atribuir</label>
      <select
        #picker
        id="assignee-select"
        [class]="selectClass + ' w-56'"
        [disabled]="busy() || eligible().length === 0"
      >
        @if (loadingMembers()) {
          <option value="">A carregar membros…</option>
        } @else if (eligible().length === 0) {
          <option value="">Sem membros disponíveis</option>
        } @else {
          @for (m of eligible(); track m.user.id) {
            <option [value]="m.user.id">{{ m.user.name }}</option>
          }
        }
      </select>
      <button
        type="button"
        hlmBtn
        variant="outline"
        size="sm"
        [disabled]="busy() || eligible().length === 0"
        (click)="picker.value && assign(picker.value)"
      >
        <ng-icon name="lucideUserPlus" />
        {{ busy() ? 'A atribuir…' : 'Atribuir' }}
      </button>
    </div>
  `,
})
export class TaskAssignees implements OnInit {
  readonly projectId = input.required<string>();
  readonly taskId = input.required<string>();
  readonly assignees = input.required<UserSummary[]>();
  readonly assigneesChange = output<UserSummary[]>();

  private readonly projectsApi = inject(ProjectsService);
  private readonly api = inject(TasksService);

  protected readonly selectClass = selectClass;
  protected readonly initials = initials;

  protected readonly members = signal<Member[]>([]);
  protected readonly loadingMembers = signal(true);
  protected readonly busy = signal(false);
  protected readonly error = signal<string | null>(null);
  /** Only active members not yet assigned; the API checks this too. */
  protected readonly eligible = computed(() => {
    const assigned = new Set(this.assignees().map((a) => a.id));
    return this.members().filter((m) => m.user.active && !assigned.has(m.user.id));
  });

  ngOnInit(): void {
    this.projectsApi.members(this.projectId()).subscribe({
      next: (members) => {
        this.members.set(members);
        this.loadingMembers.set(false);
      },
      error: (err) => {
        this.loadingMembers.set(false);
        this.error.set(errorMessage(err));
      },
    });
  }

  protected assign(userId: string): void {
    this.busy.set(true);
    this.error.set(null);
    this.api.addAssignee(this.projectId(), this.taskId(), userId).subscribe({
      next: (user) => {
        this.busy.set(false);
        this.assigneesChange.emit([...this.assignees(), user]);
      },
      error: (err) => {
        this.busy.set(false);
        this.error.set(fieldErrors(err)['userId'] ?? errorMessage(err));
      },
    });
  }

  protected remove(user: UserSummary): void {
    this.busy.set(true);
    this.error.set(null);
    this.api.removeAssignee(this.projectId(), this.taskId(), user.id).subscribe({
      next: () => {
        this.busy.set(false);
        this.assigneesChange.emit(this.assignees().filter((a) => a.id !== user.id));
      },
      error: (err) => {
        this.busy.set(false);
        this.error.set(errorMessage(err));
      },
    });
  }
}
