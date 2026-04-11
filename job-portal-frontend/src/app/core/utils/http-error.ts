import { HttpErrorResponse } from '@angular/common/http';

export interface HttpErrorMessageOptions {
  defaultMessage: string;
  statusMessages?: Partial<Record<number, string>>;
}

export function getHttpErrorMessage(error: unknown, options: HttpErrorMessageOptions): string {
  if (error instanceof HttpErrorResponse) {
    const serverMessage = extractServerMessage(error.error);
    if (serverMessage) {
      return serverMessage;
    }

    const mappedMessage = options.statusMessages?.[error.status];
    if (mappedMessage) {
      return mappedMessage;
    }

    if (error.status === 0) {
      return 'Unable to reach the server. Please check that the backend is running.';
    }
  }

  return options.defaultMessage;
}

function extractServerMessage(payload: unknown): string | null {
  if (!payload) {
    return null;
  }

  if (typeof payload === 'string') {
    const trimmed = payload.trim();
    return trimmed ? trimmed : null;
  }

  if (typeof payload === 'object') {
    const record = payload as Record<string, unknown>;
    const candidates = [
      record['message'],
      record['error'],
      record['details'],
      record['title']
    ];

    for (const candidate of candidates) {
      if (typeof candidate === 'string' && candidate.trim()) {
        return candidate.trim();
      }
    }
  }

  return null;
}
