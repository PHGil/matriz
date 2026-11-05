import { Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { NgIcon, provideIcons } from '@ng-icons/core';
import { lucideCircleCheck, lucideSearch, lucideUserMinus, lucideUserPlus } from '@ng-icons/lucide';
import { HlmAlertImports } from '@spartan-ng/helm/alert';
import { HlmAlertDialogImports } from '@spartan-ng/helm/alert-dialog';
import { HlmAvatarImports } from '@spartan-ng/helm/avatar';
import { HlmBadgeImports } from '@spartan-ng/helm/badge';
import { HlmBreadcrumbImports } from '@spartan-ng/helm/breadcrumb';
import { HlmButtonImports } from '@spartan-ng/helm/button';
import { HlmCardImports } from '@spartan-ng/helm/card';
import { HlmInputImports } from '@spartan-ng/helm/input';
import { HlmLabelImports } from '@spartan-ng/helm/label';
import { HlmSkeletonImports } from '@spartan-ng/helm/skeleton';
import { HlmSpinnerImports } from '@spartan-ng/helm/spinner';
import { HlmTableImports } from '@spartan-ng/helm/table';
import { Observable, forkJoin } from 'rxjs';
import { Member, Project, UserDirectoryEntry } from '../core/models';
import { ProjectsService } from '../core/projects.service';
import { errorMessage, initials, roleLabel } from '../core/ui-helpers';

@Component({
  selector: 'app-project-members',
  imports: [
    NgIcon,
    HlmAlertImports,
    HlmAlertDialogImports,
    HlmAvatarImports,
    HlmBadgeImports,
    HlmBreadcrumbImports,
    HlmButtonImports,
    HlmCardImports,
    HlmInputImports,
    HlmLabelImports,
    HlmSkeletonImports,
    HlmSpinnerImports,
    HlmTableImports,
  ],
  providers: [provideIcons({ lucideCircleCheck, lucideSearch, lucideUserMinus, lucideUserPlus })],
  template: `
    <nav hlmBreadcrumb class="mb-4">
      <ol hlmBreadcrumbList>
        <li hlmBreadcrumbItem><a hlmBreadcrumbLink link="/projetos">Projetos</a></li>
        <li hlmBreadcrumbSeparator></li>
        <li hlmBreadcrumbItem>
          <a hlmBreadcrumbLink [link]="['/projetos', id()]">{{ project()?.name }}</a>
        </li>
        <li hlmBreadcrumbSeparator></li>
        <li hlmBreadcrumbItem><span hlmBreadcrumbPage>Membros</span></li>
      </ol>
    </nav>
    <h1 class="mb-6 text-3xl font-semibold tracking-tight">Membros</h1>

    @if (loading()) {
      <div class="space-y-2" aria-busy="true">
        <span class="sr-only">A carregar…</span>
        @for (i of [1, 2, 3]; track i) {
          <hlm-skeleton class="h-12 w-full" />
        }
      </div>
    } @else if (loadError()) {
      <div hlmAlert variant="destructive">
        <p hlmAlertDescription>{{ loadError() }}</p>
      </div>
    } @else {
      <div class="grid items-start gap-6 lg:grid-cols-[minmax(0,1fr)_22rem]">
        <div class="space-y-4">
          @if (error()) {
            <div hlmAlert variant="destructive">
              <p hlmAlertDescription>{{ error() }}</p>
            </div>
          }
          @if (success()) {
            <div hlmAlert role="status">
              <ng-icon name="lucideCircleCheck" />
              <p hlmAlertDescription>{{ success() }}</p>
            </div>
          }
          <div hlmTableContainer class="bg-card rounded-xl border">
            <table hlmTable>
              <thead hlmTHead>
                <tr hlmTr>
                  <th hlmTh class="ps-4">Nome</th>
                  <th hlmTh>Papel</th>
                  <th hlmTh class="pe-4"><span class="sr-only">Ações</span></th>
                </tr>
              </thead>
              <tbody hlmTBody>
                @for (m of members(); track m.user.id) {
                  <tr hlmTr>
                    <td hlmTd class="ps-4">
                      <div class="flex items-center gap-3" [class.opacity-60]="!m.user.active">
                        <hlm-avatar size="sm">
                          <span hlmAvatarFallback>{{ initials(m.user.name) }}</span>
                        </hlm-avatar>
                        <span class="font-medium">{{ m.user.name }}</span>
                        @if (!m.user.active) {
                          <span hlmBadge variant="outline">desativado</span>
                        }
                      </div>
                    </td>
                    <td hlmTd>
                      <span hlmBadge [variant]="m.role === 'MANAGER' ? 'default' : 'secondary'">
                        {{ roleLabel[m.role] }}
                      </span>
                    </td>
                    <td hlmTd class="pe-4 text-end">
                      @if (isManager() && m.role === 'MEMBER') {
                        <hlm-alert-dialog>
                          <button
                            type="button"
                            hlmBtn
                            variant="ghost"
                            size="sm"
                            class="text-destructive hover:text-destructive"
                            hlmAlertDialogTrigger
                            [disabled]="busy()"
                          >
                            <ng-icon name="lucideUserMinus" />
                            Remover
                          </button>
                          <hlm-alert-dialog-content *hlmAlertDialogPortal="let ctx">
                            <hlm-alert-dialog-header>
                              <h2 hlmAlertDialogTitle>Remover {{ m.user.name }} do projeto?</h2>
                              <p hlmAlertDialogDescription>
                                Deixa de ser responsável pelas tarefas do projeto.
                              </p>
                            </hlm-alert-dialog-header>
                            <hlm-alert-dialog-footer>
                              <button hlmAlertDialogCancel>Cancelar</button>
                              <button
                                hlmAlertDialogAction
                                variant="destructive"
                                (click)="ctx.close(); remove(m)"
                              >
                                Remover
                              </button>
                            </hlm-alert-dialog-footer>
                          </hlm-alert-dialog-content>
                        </hlm-alert-dialog>
                      }
                    </td>
                  </tr>
                }
              </tbody>
            </table>
          </div>
        </div>

        @if (isManager()) {
          <section hlmCard>
            <div hlmCardHeader>
              <h2 hlmCardTitle>Adicionar membro</h2>
            </div>
            <div hlmCardContent class="space-y-3">
              <label hlmLabel for="member-search" class="sr-only">
                Pesquisar utilizador (nome ou email)
              </label>
              <div class="relative">
                <ng-icon
                  name="lucideSearch"
                  class="text-muted-foreground pointer-events-none absolute top-1/2 left-2.5 -translate-y-1/2"
                />
                <input
                  hlmInput
                  id="member-search"
                  type="search"
                  class="pl-8"
                  placeholder="Pesquisar utilizador (nome ou email)"
                  [value]="query()"
                  (input)="search($any($event.target).value)"
                />
              </div>
              @if (searching()) {
                <p class="text-muted-foreground flex items-center gap-2 text-sm">
                  <hlm-spinner aria-label="A pesquisar" />
                  A pesquisar…
                </p>
              } @else if (candidates().length > 0) {
                <ul class="divide-y">
                  @for (u of candidates(); track u.id) {
                    <li class="flex items-center gap-3 py-2">
                      <hlm-avatar size="sm">
                        <span hlmAvatarFallback>{{ initials(u.name) }}</span>
                      </hlm-avatar>
                      <div class="min-w-0 flex-1">
                        <p class="truncate text-sm font-medium">{{ u.name }}</p>
                        <p class="text-muted-foreground truncate text-xs">{{ u.email }}</p>
                      </div>
                      <button
                        type="button"
                        hlmBtn
                        variant="outline"
                        size="sm"
                        (click)="add(u)"
                        [disabled]="busy()"
                      >
                        <ng-icon name="lucideUserPlus" />
                        Adicionar
                      </button>
                    </li>
                  }
                </ul>
              } @else if (query().trim()) {
                <p class="text-muted-foreground text-sm">Nenhum utilizador disponível.</p>
              }
            </div>
          </section>
        }
      </div>
    }
  `,
})
export class ProjectMembers implements OnInit {
  readonly id = input.required<string>();

  private readonly api = inject(ProjectsService);
  protected readonly roleLabel = roleLabel;
  protected readonly initials = initials;

  protected readonly project = signal<Project | null>(null);
  protected readonly members = signal<Member[]>([]);
  protected readonly results = signal<UserDirectoryEntry[]>([]);
  protected readonly query = signal('');
  protected readonly searching = signal(false);
  protected readonly loading = signal(true);
  protected readonly loadError = signal<string | null>(null);
  protected readonly busy = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly success = signal<string | null>(null);
  protected readonly isManager = computed(() => this.project()?.myRole === 'MANAGER');
  protected readonly candidates = computed(() => {
    const memberIds = new Set(this.members().map((m) => m.user.id));
    return this.results().filter((u) => !memberIds.has(u.id));
  });

  ngOnInit(): void {
    forkJoin({ project: this.api.get(this.id()), members: this.api.members(this.id()) }).subscribe({
      next: ({ project, members }) => {
        this.project.set(project);
        this.members.set(members);
        this.loading.set(false);
      },
      error: (err) => {
        this.loadError.set(errorMessage(err));
        this.loading.set(false);
      },
    });
  }

  protected search(value: string): void {
    this.query.set(value);
    if (!value.trim()) {
      this.results.set([]);
      this.searching.set(false);
      return;
    }
    this.searching.set(true);
    this.api.searchUsers(value).subscribe({
      next: (users) => {
        if (this.query() === value) {
          this.results.set(users);
          this.searching.set(false);
        }
      },
      error: (err) => {
        this.searching.set(false);
        this.error.set(errorMessage(err));
      },
    });
  }

  protected add(user: UserDirectoryEntry): void {
    this.run(`${user.name} adicionado ao projeto.`, () => this.api.addMember(this.id(), user.id));
  }

  protected remove(member: Member): void {
    this.run(`${member.user.name} removido do projeto.`, () =>
      this.api.removeMember(this.id(), member.user.id),
    );
  }

  /** Runs a change, then reloads the member list from the API so the view reflects the confirmed state. */
  private run(successMessage: string, action: () => Observable<unknown>): void {
    this.busy.set(true);
    this.error.set(null);
    this.success.set(null);
    action().subscribe({
      next: () => {
        this.api.members(this.id()).subscribe({
          next: (members) => {
            this.members.set(members);
            this.busy.set(false);
            this.success.set(successMessage);
          },
          error: (err) => {
            this.busy.set(false);
            this.error.set(errorMessage(err));
          },
        });
      },
      error: (err) => {
        this.busy.set(false);
        this.error.set(errorMessage(err));
      },
    });
  }
}
