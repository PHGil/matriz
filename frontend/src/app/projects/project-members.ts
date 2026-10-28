import { Component, OnInit, computed, inject, input, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Observable, forkJoin } from 'rxjs';
import { Member, Project, UserDirectoryEntry } from '../core/models';
import { ProjectsService } from '../core/projects.service';
import { errorMessage, roleLabel } from '../core/ui-helpers';

@Component({
  selector: 'app-project-members',
  imports: [RouterLink],
  template: `
    <p class="muted">
      <a routerLink="/projetos">Projetos</a> ›
      <a [routerLink]="['/projetos', id()]">{{ project()?.name }}</a> › Membros
    </p>
    <h1>Membros</h1>

    @if (loading()) {
      <p class="muted">A carregar…</p>
    } @else if (loadError()) {
      <p class="notice error" role="alert">{{ loadError() }}</p>
    } @else {
      @if (error()) {
        <p class="notice error" role="alert">{{ error() }}</p>
      }
      @if (success()) {
        <p class="notice success" role="status">{{ success() }}</p>
      }
      <table class="table">
        <thead>
          <tr>
            <th>Nome</th>
            <th>Papel</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          @for (m of members(); track m.user.id) {
            <tr>
              <td>
                {{ m.user.name }}
                @if (!m.user.active) {
                  (desativado)
                }
              </td>
              <td>{{ roleLabel[m.role] }}</td>
              <td>
                @if (isManager() && m.role === 'MEMBER') {
                  <button type="button" class="danger" (click)="remove(m)" [disabled]="busy()">
                    Remover
                  </button>
                }
              </td>
            </tr>
          }
        </tbody>
      </table>

      @if (isManager()) {
        <section class="panel">
          <h2>Adicionar membro</h2>
          <label class="search">
            Pesquisar utilizador (nome ou email)
            <input type="search" [value]="query()" (input)="search($any($event.target).value)" />
          </label>
          @if (searching()) {
            <p class="muted">A pesquisar…</p>
          } @else if (candidates().length > 0) {
            <ul class="list">
              @for (u of candidates(); track u.id) {
                <li>
                  {{ u.name }} <span class="muted">{{ u.email }}</span>
                  <button type="button" (click)="add(u)" [disabled]="busy()">Adicionar</button>
                </li>
              }
            </ul>
          } @else if (query().trim()) {
            <p class="muted">Nenhum utilizador disponível.</p>
          }
        </section>
      }
    }
  `,
})
export class ProjectMembers implements OnInit {
  readonly id = input.required<string>();

  private readonly api = inject(ProjectsService);
  protected readonly roleLabel = roleLabel;

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
    if (
      !window.confirm(
        `Remover ${member.user.name} do projeto? Deixa de ser responsável pelas tarefas do projeto.`,
      )
    ) {
      return;
    }
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
