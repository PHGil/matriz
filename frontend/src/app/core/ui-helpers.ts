import { HttpErrorResponse } from '@angular/common/http';
import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';
import { Problem, ProjectRole } from './models';

/** User-facing message for a failed API call (pt-PT). */
export function errorMessage(error: unknown): string {
  if (!(error instanceof HttpErrorResponse)) {
    return 'Ocorreu um erro inesperado.';
  }
  const problem = error.error as Problem | null;
  switch (error.status) {
    case 0:
      return 'Não foi possível contactar o servidor. Tente novamente.';
    case 400:
      return 'Verifique os campos assinalados.';
    case 401:
      return 'A sua sessão expirou.';
    case 403:
      return 'Não tem permissão para esta operação.';
    case 404:
      return 'Não encontrado.';
    case 409:
      return problem?.detail ?? 'A operação entra em conflito com o estado atual.';
    default:
      return 'Ocorreu um erro no servidor. Tente novamente.';
  }
}

/** Field errors returned by the API (400), keyed by field name. */
export function fieldErrors(error: unknown): Record<string, string> {
  if (error instanceof HttpErrorResponse && error.status === 400) {
    const problem = error.error as Problem | null;
    return Object.fromEntries((problem?.errors ?? []).map((e) => [e.field, e.message]));
  }
  return {};
}

/** Mirrors the backend rule: trimmed length between min and max; whitespace-only counts as empty. */
export function trimmedLength(min: number, max: number): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const value = ((control.value as string | null) ?? '').trim();
    if (min > 0 && value.length === 0) {
      return { required: true };
    }
    if (value.length > 0 && (value.length < min || value.length > max)) {
      return { length: { min, max } };
    }
    return null;
  };
}

export const roleLabel: Record<ProjectRole, string> = {
  MANAGER: 'Gestor',
  MEMBER: 'Membro',
};
