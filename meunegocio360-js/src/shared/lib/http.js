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
}
