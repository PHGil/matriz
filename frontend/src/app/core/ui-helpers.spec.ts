import { HttpErrorResponse } from '@angular/common/http';
import { FormControl } from '@angular/forms';
import { errorMessage, fieldErrors, trimmedLength } from './ui-helpers';

describe('ui-helpers', () => {
  it('trimmedLength rejects whitespace-only and out-of-range values', () => {
    const validator = trimmedLength(3, 100);
    expect(validator(new FormControl('   '))).toEqual({ required: true });
    expect(validator(new FormControl(' ab '))).toEqual({ length: { min: 3, max: 100 } });
    expect(validator(new FormControl('  Website  '))).toBeNull();
  });

  it('maps API problems to messages and field errors', () => {
    const badRequest = new HttpErrorResponse({
      status: 400,
      error: {
        title: 'Validation failed',
        status: 400,
        errors: [{ field: 'name', message: 'O nome é obrigatório.' }],
      },
    });
    expect(errorMessage(badRequest)).toBe('Verifique os campos assinalados.');
    expect(fieldErrors(badRequest)).toEqual({ name: 'O nome é obrigatório.' });

    const conflict = new HttpErrorResponse({
      status: 409,
      error: { title: 'Conflict', status: 409, detail: 'Já é membro.' },
    });
    expect(errorMessage(conflict)).toBe('Já é membro.');
    expect(fieldErrors(conflict)).toEqual({});
  });
});
