import { Component, effect, inject, input, output } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ProjectInput } from '../core/models';
import { trimmedLength } from '../core/ui-helpers';

/** Create/edit form for a project; keeps the typed values when the API rejects them. */
@Component({
  selector: 'app-project-form',
  imports: [ReactiveFormsModule],
  template: `
    <form [formGroup]="form" (ngSubmit)="submit()" class="form" novalidate>
      <label>
        Nome
        <input formControlName="name" maxlength="100" autocomplete="off" />
      </label>
      @if (nameError(); as message) {
        <span class="field-error">{{ message }}</span>
      }
      <label>
        Descrição (opcional)
        <textarea formControlName="description" rows="3" maxlength="1000"></textarea>
      </label>
      @if (serverErrors()['description']; as message) {
        <span class="field-error">{{ message }}</span>
      }
      <div class="actions">
        <button type="submit" class="primary" [disabled]="saving()">
          {{ saving() ? 'A guardar…' : submitLabel() }}
        </button>
        <button type="button" (click)="cancelled.emit()" [disabled]="saving()">Cancelar</button>
      </div>
    </form>
  `,
})
export class ProjectForm {
  readonly initial = input<ProjectInput | null>(null);
  readonly submitLabel = input('Guardar');
  readonly saving = input(false);
  readonly serverErrors = input<Record<string, string>>({});
  readonly submitted = output<ProjectInput>();
  readonly cancelled = output<void>();

  private readonly fb = inject(FormBuilder).nonNullable;
  protected readonly form = this.fb.group({
    name: ['', trimmedLength(3, 100)],
    description: ['', trimmedLength(0, 1000)],
  });

  constructor() {
    effect(() => {
      const initial = this.initial();
      if (initial) {
        this.form.setValue({ name: initial.name, description: initial.description ?? '' });
      }
    });
  }

  protected nameError(): string | null {
    const control = this.form.controls.name;
    if (control.touched && control.errors?.['required']) {
      return 'O nome é obrigatório.';
    }
    if (control.touched && control.errors?.['length']) {
      return 'O nome deve ter entre 3 e 100 caracteres.';
    }
    return this.serverErrors()['name'] ?? null;
  }

  protected submit(): void {
    this.form.markAllAsTouched();
    if (this.form.invalid || this.saving()) {
      return;
    }
    const { name, description } = this.form.getRawValue();
    this.submitted.emit({ name: name.trim(), description: description.trim() || null });
  }

  reset(): void {
    this.form.reset();
  }
}
