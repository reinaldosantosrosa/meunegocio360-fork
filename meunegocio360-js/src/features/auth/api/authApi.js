import { apiRequest } from '../../../shared/lib/http.js'

export function login(credentials) {
  return apiRequest('/auth/login', {
    method: 'POST',
    authenticated: false,
    retryUnauthorized: false,
    body: {
      email: credentials.email,
      senha: credentials.senha,
    },
  })
}

export function logout() {
  return apiRequest('/auth/logout', {
    method: 'POST',
    authenticated: false,
    retryUnauthorized: false,
  })
}
