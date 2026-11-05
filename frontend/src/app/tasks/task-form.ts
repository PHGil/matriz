import { Component, effect, inject, input, output } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { HlmButtonImports } from '@spartan-ng/helm/button';
import { HlmFieldImports } from '@spartan-ng/helm/field';
import { HlmInputImports } from '@spartan-ng/helm/input';
import { HlmSpinnerImports } from '@spartan-ng/helm/spinner';
import { HlmTextareaImports } from '@spartan-ng/helm/textarea';
import { TaskInput, TaskPriority } from '../core/models';
import { priorities, priorityLabel, selectClass, trimmedLength } from '../core/ui-helpers';

/** Create/edit form for a task; keeps the typed values when the API rejects them. */
@Component({
  selector: 'app-task-form',
  imports: [
    ReactiveFormsModule,
    HlmButtonImports,
    HlmFieldImports,
    HlmInputImports,
    HlmSpinnerImports,
    HlmTextareaImports,
  ],
  template: `
    <form [formGroup]="form" (ngSubmit)="submit()" class="flex flex-col gap-5" novalidate>
      <div hlmField>
        <label hlmFieldLabel [for]="idPrefix() + '-title'">Título</label>
        <input
          hlmInput
          [id]="idPrefix() + '-title'"
          formControlName="title"
          maxlength="150"
          autocomplete="off"
          [attr.aria-invalid]="titleError() ? true : null"
        />
        @if (titleError(); as message) {
          <hlm-field-error forceShow>{{ message }}</hlm-field-error>
        }
      </div>
      <div hlmField>
        <label hlmFieldLabel [for]="idPrefix() + '-description'">Descrição (opcional)</label>
        <textarea
          hlmTextarea
          [id]="idPrefix() + '-description'"
          formControlName="description"
          rows="3"
          maxlength="2000"
          [attr.aria-invalid]="descriptionError() ? true : null"
        ></textarea>
        @if (descriptionError(); as message) {
          <hlm-field-error forceShow>{{ message }}</hlm-field-error>
        }
      </div>
      <div class="grid gap-5 sm:grid-cols-2">
        <div hlmField>
          <label hlmFieldLabel [for]="idPrefix() + '-priority'">Prioridade</label>
          <select [id]="idPrefix() + '-priority'" formControlName="priority" [class]="selectClass">
            @for (p of priorities; track p) {
              <option [value]="p">{{ priorityLabel[p] }}</option>
            }
          </select>
        </div>
        <div hlmField>
          <label hlmFieldLabel [for]="idPrefix() + '-due'">Prazo (opcional)</label>
          <input hlmInput type="date" [id]="idPrefix() + '-due'" formControlName="dueDate" />
          @if (serverErrors()['dueDate']; as message) {
            <hlm-field-error forceShow>{{ message }}</hlm-field-error>
          }
        </div>
      </div>
      <div class="flex flex-wrap gap-2">
        <button type="submit" hlmBtn [disabled]="saving()">
          @if (saving()) {
            <hlm-spinner aria-label="A guardar" />
          }
          {{ saving() ? 'A guardar…' : submitLabel() }}
        </button>
        <button type="button" hlmBtn variant="outline" (click)="cancelled.emit()" [disabled]="saving()">
          Cancelar
        </button>
      </div>
    </form>
  `,
})
export class TaskForm {
  readonly initial = input<TaskInput | null>(null);
  readonly submitLabel = input('Guardar');
  readonly saving = input(false);
  readonly serverErrors = input<Record<string, string>>({});
  readonly idPrefix = input('task');
  readonly submitted = output<TaskInput>();
  readonly cancelled = output<void>();

  protected readonly priorities = priorities;
  protected readonly priorityLabel = priorityLabel;
  protected readonly selectClass = selectClass;

  private readonly fb = inject(FormBuilder).nonNullable;
  protected readonly form = this.fb.group({
    title: ['', trimmedLength(3, 150)],
    description: ['', trimmedLength(0, 2000)],
    priority: ['MEDIUM' as TaskPriority],
    dueDate: [''],
  });

  constructor() {
    effect(() => {
      const initial = this.initial();
      if (initial) {
        this.form.setValue({
          title: initial.title,
          description: initial.description ?? '',
          priority: initial.priority,
          dueDate: initial.dueDate ?? '',
        });
      }
    });
  }

  protected titleError(): string | null {
    const control = this.form.controls.title;
    if (control.touched && control.errors?.['required']) {
      return 'O título é obrigatório.';
    }
    if (control.touched && control.errors?.['length']) {
      return 'O título deve ter entre 3 e 150 caracteres.';
    }
    return this.serverErrors()['title'] ?? null;
  }

  protected descriptionError(): string | null {
    if (this.form.controls.description.errors?.['length']) {
      return 'A descrição não pode exceder 2000 caracteres.';
    }
    return this.serverErrors()['description'] ?? null;
  }

  protected submit(): void {
    this.form.markAllAsTouched();
    if (this.form.invalid || this.saving()) {
      return;
    }
    const { title, description, priority, dueDate } = this.form.getRawValue();
    this.submitted.emit({
      title: title.trim(),
      description: description.trim() || null,
      priority,
      dueDate: dueDate || null,
    });
  }

  reset(): void {
    this.form.reset();
  }
}
