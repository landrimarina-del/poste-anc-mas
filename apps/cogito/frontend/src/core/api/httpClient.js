class ApiError extends Error {
  constructor(message, resultCode, status) {
    super(message);
    this.name = 'ApiError';
    this.resultCode = resultCode;
    this.status = status;
  }
}

async function request(path, options = {}) {
  const response = await fetch(path, {
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
      ...(options.headers ?? {})
    },
    ...options
  });

  const contentType = response.headers.get('content-type') ?? '';
  const isJson = contentType.includes('application/json');
  const payload = isJson ? await response.json() : null;

  if (!response.ok) {
    const message = payload?.resultMessage ?? `Errore HTTP ${response.status}`;
    throw new ApiError(message, payload?.resultCode ?? response.status, response.status);
  }

  const resultCode = payload?.resultCode;
  if (typeof resultCode === 'number' && resultCode !== 0) {
    throw new ApiError(payload?.resultMessage ?? 'Errore applicativo', resultCode, response.status);
  }

  return payload?.details ?? null;
}

export const httpClient = {
  request,
  ApiError
};
