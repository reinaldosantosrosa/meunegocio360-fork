<<<<<<< HEAD
const configuredBaseUrl = import.meta.env.VITE_API_BASE_URL || "/api/v1";

if (!configuredBaseUrl.startsWith("/") || configuredBaseUrl.startsWith("//")) {
  throw new Error(
    "VITE_API_BASE_URL deve ser um caminho relativo, como /api/v1.",
  );
}

const apiBaseUrl = configuredBaseUrl.replace(/\/+$/, "");
const requestTimeoutMs = 10_000;
let sessionHandlers = {};
let refreshInFlight = null;

export class ApiError extends Error {
  constructor({ status, code, detail, errors = [], retryAfter = null }) {
    super(detail || "A solicitação não pôde ser concluída.");
    this.name = "ApiError";
    this.status = status;
    this.code = code;
    this.detail = detail;
    this.errors = errors;
    this.retryAfter = retryAfter;
  }
}

export function configureHttpSession(handlers) {
  sessionHandlers = handlers || {};
}

function createRequestId() {
  if (typeof crypto.randomUUID === "function") {
    return crypto.randomUUID();
  }

  return `${Date.now()}-${Math.random().toString(16).slice(2)}`;
}

function toApiError(response, payload) {
  const retryAfterValue = response.headers.get("Retry-After");
  const retryAfter =
    retryAfterValue && /^\d+$/.test(retryAfterValue)
      ? Number(retryAfterValue)
      : null;

  return new ApiError({
    status: response.status,
    code: payload?.code || `HTTP_${response.status}`,
    detail: payload?.detail,
    errors: Array.isArray(payload?.errors) ? payload.errors : [],
    retryAfter,
  });
}

async function fetchResponse(path, options, accessToken, requestId) {
  const controller = new AbortController();
  const timeoutId = window.setTimeout(
    () => controller.abort(),
    requestTimeoutMs,
  );
  const externalSignal = options.signal;
  const abortFromCaller = () => controller.abort(externalSignal.reason);

  if (externalSignal) {
    if (externalSignal.aborted) {
      controller.abort(externalSignal.reason);
    } else {
      externalSignal.addEventListener("abort", abortFromCaller, { once: true });
    }
  }

  const headers = new Headers(options.headers);
  headers.set("Accept", "application/json, application/problem+json");
  headers.set("X-Request-Id", requestId);

  if (options.body !== undefined) {
    headers.set("Content-Type", "application/json");
  }

  if (accessToken) {
    headers.set("Authorization", `Bearer ${accessToken}`);
  }

  try {
    return await fetch(`${apiBaseUrl}${path}`, {
      method: options.method || "GET",
      headers,
      body:
        options.body === undefined ? undefined : JSON.stringify(options.body),
      credentials: "include",
      signal: controller.signal,
    });
  } catch (error) {
    if (externalSignal?.aborted) {
      throw error;
    }

    if (controller.signal.aborted) {
      throw new ApiError({
        status: 0,
        code: "REQUEST_TIMEOUT",
        detail: "A solicitação excedeu o tempo limite.",
      });
    }

    throw new ApiError({
      status: 0,
      code: "NETWORK_ERROR",
      detail: "Não foi possível conectar à API.",
    });
  } finally {
    window.clearTimeout(timeoutId);
    externalSignal?.removeEventListener("abort", abortFromCaller);
  }
}

async function readResponse(response) {
  if (response.status === 204) {
    return null;
  }

  const responseText = await response.text();
  if (!responseText) {
    return null;
  }

  try {
    return JSON.parse(responseText);
  } catch {
    throw new ApiError({
      status: response.status,
      code: "INVALID_RESPONSE",
      detail: "A API retornou uma resposta inválida.",
    });
  }
}

function refreshSession() {
  if (!refreshInFlight) {
    if (typeof sessionHandlers.refresh !== "function") {
      return Promise.reject(
        new ApiError({
          status: 401,
          code: "UNAUTHORIZED",
          detail: "A sessão não está disponível.",
        }),
      );
    }

    refreshInFlight = Promise.resolve()
      .then(() => sessionHandlers.refresh())
      .finally(() => {
        refreshInFlight = null;
      });
  }

  return refreshInFlight;
}

async function request(path, options, requestId, retried) {
  const tokenBeforeRequest = options.auth
    ? sessionHandlers.getAccessToken?.() || null
    : null;
  const response = await fetchResponse(
    path,
    options,
    tokenBeforeRequest,
    requestId,
  );
  const payload = await readResponse(response);

  if (response.ok) {
    return payload;
  }

  const apiError = toApiError(response, payload);
  if (!options.auth || options.retryOnUnauthorized === false || retried) {
    throw apiError;
  }

  if (response.status !== 401) {
    throw apiError;
  }

  const currentToken = sessionHandlers.getAccessToken?.() || null;
  try {
    if (!tokenBeforeRequest || currentToken === tokenBeforeRequest) {
      await refreshSession();
    }
  } catch (refreshError) {
    sessionHandlers.onSessionExpired?.(refreshError);
    throw refreshError;
  }

  return request(path, options, requestId, true);
}

export function apiRequest(path, options = {}) {
  if (!path.startsWith("/") || path.startsWith("//")) {
    throw new Error("O caminho da API deve ser relativo e começar com /.");
  }

  return request(
    path,
    {
      auth: options.auth !== false,
      retryOnUnauthorized: options.retryOnUnauthorized !== false,
      method: options.method,
      body: options.body,
      headers: options.headers,
      signal: options.signal,
    },
    createRequestId(),
    false,
  );
=======
const configuredApiBaseUrl = import.meta.env.VITE_API_BASE_URL || '/api/v1'

if (
  !configuredApiBaseUrl.startsWith('/') ||
  configuredApiBaseUrl.startsWith('//')
) {
  throw new Error(
    'VITE_API_BASE_URL deve ser um caminho relativo, como /api/v1.',
  )
}

const apiBaseUrl = configuredApiBaseUrl.replace(/\/+$/, '')
if (!apiBaseUrl) {
  throw new Error('VITE_API_BASE_URL não pode ser vazio.')
}
const requestTimeoutMs = 10000
let refreshInFlight = null
let authLifecycle = {}

export class ApiError extends Error {
  constructor({ status, code, detail, errors = [], retryAfterSeconds = null }) {
    super(code || 'API_ERROR')
    this.name = 'ApiError'
    this.status = status
    this.code = code
    this.detail = detail
    this.errors = errors
    this.retryAfterSeconds = retryAfterSeconds
  }
}

export function configureAuthLifecycle(lifecycle) {
  authLifecycle = lifecycle
}

function getRetryAfterSeconds(value) {
  if (!value) {
    return null
  }

  const seconds = Number(value)
  if (Number.isFinite(seconds)) {
    return Math.max(0, Math.ceil(seconds))
  }

  const date = Date.parse(value)
  return Number.isNaN(date)
    ? null
    : Math.max(0, Math.ceil((date - Date.now()) / 1000))
}

async function fetchWithTimeout(url, options) {
  const controller = new AbortController()
  let timedOut = false
  const timeoutId = window.setTimeout(() => {
    timedOut = true
    controller.abort()
  }, requestTimeoutMs)

  try {
    return await fetch(url, { ...options, signal: controller.signal })
  } catch (error) {
    if (timedOut) {
      throw new ApiError({ status: 0, code: 'TIMEOUT' })
    }

    if (error instanceof TypeError) {
      throw new ApiError({ status: 0, code: 'NETWORK_ERROR' })
    }

    throw error
  } finally {
    window.clearTimeout(timeoutId)
  }
}

async function parseResponse(response) {
  let payload = null

  if (response.status !== 204) {
    const text = await response.text()
    if (text) {
      try {
        payload = JSON.parse(text)
      } catch {
        throw new ApiError({
          status: response.status,
          code: 'INVALID_RESPONSE',
        })
      }
    }
  }

  if (!response.ok) {
    const problem =
      payload && typeof payload === 'object' && !Array.isArray(payload)
        ? payload
        : {}

    throw new ApiError({
      status: response.status,
      code: typeof problem.code === 'string' ? problem.code : 'HTTP_ERROR',
      detail: typeof problem.detail === 'string' ? problem.detail : undefined,
      errors: Array.isArray(problem.errors) ? problem.errors : [],
      retryAfterSeconds: getRetryAfterSeconds(
        response.headers.get('Retry-After'),
      ),
    })
  }

  return payload
}

function createRequestUrl(path) {
  return `${apiBaseUrl}/${path.replace(/^\/+/, '')}`
}

export async function apiRequest(path, options = {}) {
  const {
    authenticated = true,
    retryUnauthorized = true,
    headers: inputHeaders,
    body: inputBody,
    ...fetchOptions
  } = options
  const headers = new Headers(inputHeaders)
  headers.set('Accept', 'application/json, application/problem+json')
  headers.set('X-Request-Id', crypto.randomUUID())

  if (inputBody !== undefined && typeof inputBody !== 'string') {
    headers.set('Content-Type', 'application/json')
  }

  const tokenForRequest = authenticated
    ? authLifecycle.getAccessToken?.() || null
    : null
  if (tokenForRequest) {
    headers.set('Authorization', `Bearer ${tokenForRequest}`)
  }

  const response = await fetchWithTimeout(createRequestUrl(path), {
    ...fetchOptions,
    headers,
    body:
      inputBody !== undefined && typeof inputBody !== 'string'
        ? JSON.stringify(inputBody)
        : inputBody,
    credentials: 'same-origin',
  })

  if (
    response.status === 401 &&
    authenticated &&
    tokenForRequest &&
    path !== '/auth/refresh'
  ) {
    if (!retryUnauthorized) {
      if (authLifecycle.getAccessToken?.()) {
        authLifecycle.onSessionExpired?.()
      }
      return parseResponse(response)
    }

    try {
      await refreshAuthSession()
    } catch (error) {
      if (authLifecycle.getAccessToken?.()) {
        authLifecycle.onSessionExpired?.()
      }
      throw error
    }

    return apiRequest(path, { ...options, retryUnauthorized: false })
  }

  return parseResponse(response)
}

export function refreshAuthSession() {
  if (!refreshInFlight) {
    refreshInFlight = apiRequest('/auth/refresh', {
      method: 'POST',
      authenticated: false,
      retryUnauthorized: false,
    })
      .then((session) => {
        if (!session?.accessToken || !session?.usuario) {
          throw new ApiError({ status: 0, code: 'INVALID_RESPONSE' })
        }

        authLifecycle.onSessionUpdated?.(session)
        return session
      })
      .finally(() => {
        refreshInFlight = null
      })
  }

  return refreshInFlight
>>>>>>> e2c207fd89fbc28644cb875e085f1f170624a68f
}
