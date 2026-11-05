import { Component, effect, inject, input, output } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { HlmButtonImports } from '@spartan-ng/helm/button';
import { HlmFieldImports } from '@spartan-ng/helm/field';
import { HlmInputImports } from '@spartan-ng/helm/input';
import { HlmSpinnerImports } from '@spartan-ng/helm/spinner';
import { HlmTextareaImports } from '@spartan-ng/helm/textarea';
import { ProjectInput } from '../core/models';
import { trimmedLength } from '../core/ui-helpers';

/** Create/edit form for a project; keeps the typed values when the API rejects them. */
@Component({
  selector: 'app-project-form',
  imports: [
    ReactiveFormsModule,
    HlmButtonImports,
    HlmFieldImports,
    HlmInputImports,
    HlmSpinnerImports,
    HlmTextareaImports,
  ],
  template: `
    <form [formGroup]="form" (ngSubmit)="submit()" class="flex max-w-2xl flex-col gap-5" novalidate>
      <div hlmField>
        <label hlmFieldLabel for="project-name">Nome</label>
        <input
          hlmInput
          id="project-name"
          formControlName="name"
          maxlength="100"
          autocomplete="off"
          [attr.aria-invalid]="nameError() ? true : null"
        />
        @if (nameError(); as message) {
          <hlm-field-error forceShow>{{ message }}</hlm-field-error>
        }
      </div>
      <div hlmField>
        <label hlmFieldLabel for="project-description">Descrição (opcional)</label>
        <textarea
          hlmTextarea
          id="project-description"
          formControlName="description"
          rows="3"
          maxlength="1000"
          [attr.aria-invalid]="serverErrors()['description'] ? true : null"
        ></textarea>
        @if (serverErrors()['description']; as message) {
          <hlm-field-error forceShow>{{ message }}</hlm-field-error>
        }
      </div>
      <div class="flex flex-wrap gap-2">
        <button type="submit" hlmBtn [disabled]="saving()">
          @if (saving()) {
            <hlm-spinner aria-label="A guardar" />
          }
          {{ saving() ? 'A guardar…' : submitLabel() }}
        </button>
        <button
          type="button"
          hlmBtn
          variant="outline"
          (click)="cancelled.emit()"
          [disabled]="saving()"
        >
          Cancelar
        </button>
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
