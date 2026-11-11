import { Component, OnInit, inject, input, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { HlmAlertImports } from '@spartan-ng/helm/alert';
import { HlmAvatarImports } from '@spartan-ng/helm/avatar';
import { HlmButtonImports } from '@spartan-ng/helm/button';
import { HlmFieldImports } from '@spartan-ng/helm/field';
import { HlmSkeletonImports } from '@spartan-ng/helm/skeleton';
import { HlmSpinnerImports } from '@spartan-ng/helm/spinner';
import { HlmTextareaImports } from '@spartan-ng/helm/textarea';
import { Comment } from '../core/models';
import { TasksService } from '../core/tasks.service';
import {
  errorMessage,
  fieldErrors,
  formatDateTime,
  initials,
  trimmedLength,
} from '../core/ui-helpers';

/** Task comments (US-13); author and date come from the API. */
@Component({
  selector: 'app-task-comments',
  imports: [
    ReactiveFormsModule,
    HlmAlertImports,
    HlmAvatarImports,
    HlmButtonImports,
    HlmFieldImports,
    HlmSkeletonImports,
    HlmSpinnerImports,
    HlmTextareaImports,
  ],
  template: `
    <h2 class="mb-3 text-sm font-semibold">Comentários</h2>
    @if (loading()) {
      <div class="space-y-2" aria-busy="true">
        <span class="sr-only">A carregar…</span>
        <hlm-skeleton class="h-12 w-full" />
      </div>
    } @else if (loadError()) {
      <div hlmAlert variant="destructive">
        <p hlmAlertDescription>{{ loadError() }}</p>
      </div>
    } @else {
      @if (comments().length === 0) {
        <p class="text-muted-foreground text-sm">Sem comentários.</p>
      } @else {
        <ul class="space-y-4">
          @for (c of comments(); track c.id) {
            <li class="flex gap-3">
              <hlm-avatar size="sm"
                ><span hlmAvatarFallback>{{ initials(c.author.name) }}</span></hlm-avatar
              >
              <div class="min-w-0">
                <p class="text-sm">
                  <span class="font-medium">{{ c.author.name }}</span>
                  <span class="text-muted-foreground"> · {{ formatDateTime(c.createdAt) }}</span>
                </p>
                <p class="text-sm break-words whitespace-pre-line">{{ c.content }}</p>
              </div>
            </li>
          }
        </ul>
      }

      <form [formGroup]="form" (ngSubmit)="submit()" class="mt-4 flex flex-col gap-3" novalidate>
        <div hlmField>
          <label hlmFieldLabel for="comment-content">Novo comentário</label>
          <textarea
            hlmTextarea
            id="comment-content"
            formControlName="content"
            rows="3"
            maxlength="2000"
            [attr.aria-invalid]="contentError() ? true : null"
          ></textarea>
          @if (contentError(); as message) {
            <hlm-field-error forceShow>{{ message }}</hlm-field-error>
          }
        </div>
        @if (submitError()) {
          <div hlmAlert variant="destructive">
            <p hlmAlertDescription>{{ submitError() }}</p>
          </div>
        }
        <div>
          <button type="submit" hlmBtn [disabled]="saving()">
            @if (saving()) {
              <hlm-spinner aria-label="A enviar" />
            }
            {{ saving() ? 'A enviar…' : 'Comentar' }}
          </button>
        </div>
      </form>
    }
  `,
})
export class TaskComments implements OnInit {
  readonly projectId = input.required<string>();
  readonly taskId = input.required<string>();

  private readonly api = inject(TasksService);

  protected readonly formatDateTime = formatDateTime;
  protected readonly initials = initials;

  protected readonly comments = signal<Comment[]>([]);
  protected readonly loading = signal(true);
  protected readonly loadError = signal<string | null>(null);
  protected readonly saving = signal(false);
  protected readonly submitError = signal<string | null>(null);
  protected readonly serverErrors = signal<Record<string, string>>({});

  protected readonly form = inject(FormBuilder).nonNullable.group({
    content: ['', trimmedLength(1, 2000)],
  });

  ngOnInit(): void {
    this.api.comments(this.projectId(), this.taskId()).subscribe({
      next: (comments) => {
        this.comments.set(comments);
        this.loading.set(false);
      },
      error: (err) => {
        this.loadError.set(errorMessage(err));
        this.loading.set(false);
      },
    });
  }

  protected contentError(): string | null {
    const control = this.form.controls.content;
    if (control.touched && control.errors?.['required']) {
      return 'O comentário é obrigatório.';
    }
    if (control.errors?.['length']) {
      return 'O comentário não pode exceder 2000 caracteres.';
    }
    return this.serverErrors()['content'] ?? null;
  }

  protected submit(): void {
    this.form.markAllAsTouched();
    if (this.form.invalid || this.saving()) {
      return;
    }
    this.saving.set(true);
    this.submitError.set(null);
    this.serverErrors.set({});
    this.api
      .addComment(this.projectId(), this.taskId(), this.form.getRawValue().content.trim())
      .subscribe({
        next: (comment) => {
          this.saving.set(false);
          this.comments.update((list) => [...list, comment]);
          this.form.reset();
        },
        error: (err) => {
          // The typed content is kept so the user can retry
          this.saving.set(false);
          this.submitError.set(errorMessage(err));
          this.serverErrors.set(fieldErrors(err));
        },
      });
  }
}
