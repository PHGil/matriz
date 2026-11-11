import { HttpErrorResponse } from '@angular/common/http';
import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';
import { Problem, ProjectRole, TaskPriority, TaskStatus } from './models';

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

/** UI labels for the API's technical values (NFR-05). */
export const statusLabel: Record<TaskStatus, string> = {
  TODO: 'Por fazer',
  IN_PROGRESS: 'Em curso',
  DONE: 'Concluído',
};

export const priorityLabel: Record<TaskPriority, string> = {
  LOW: 'Baixa',
  MEDIUM: 'Média',
  HIGH: 'Alta',
};

export const statuses: TaskStatus[] = ['TODO', 'IN_PROGRESS', 'DONE'];
export const priorities: TaskPriority[] = ['LOW', 'MEDIUM', 'HIGH'];

export const priorityBadge: Record<TaskPriority, 'destructive' | 'secondary' | 'outline'> = {
  HIGH: 'destructive',
  MEDIUM: 'secondary',
  LOW: 'outline',
};

/** Native <select> styled like hlmInput (spartan/ui has no select in this project). */
export const selectClass =
  'border-input dark:bg-input/30 focus-visible:border-ring focus-visible:ring-ring/50 h-8 w-full min-w-0 rounded-lg border bg-transparent px-2 py-1 text-base outline-none focus-visible:ring-3 disabled:cursor-not-allowed disabled:opacity-50 md:text-sm';

/** "2026-11-15" -> "15/11/2026" (pt-PT). */
export function formatDate(isoDate: string | null): string {
  if (!isoDate) {
    return '';
  }
  const [year, month, day] = isoDate.slice(0, 10).split('-');
  return `${day}/${month}/${year}`;
}

/** ISO date-time -> "15/11/2026, 14:30" in the browser's time zone (pt-PT). */
export function formatDateTime(isoDateTime: string): string {
  return new Date(isoDateTime).toLocaleString('pt-PT', { dateStyle: 'short', timeStyle: 'short' });
}

/** Up to two initials for avatar fallbacks, e.g. "Ana Silva" -> "AS". */
export function initials(name: string): string {
  const parts = name.trim().split(/\s+/).filter(Boolean);
  const first = parts[0]?.[0] ?? '';
  const last = parts.length > 1 ? parts[parts.length - 1][0] : '';
  return (first + last).toUpperCase();
}
