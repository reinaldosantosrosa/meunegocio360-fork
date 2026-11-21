<<<<<<< HEAD
import { apiRequest } from "../../../shared/lib/http.js";
import { parseLoginResponse } from "./authSchema.js";

let refreshInFlight = null;

export async function login(credentials) {
  const response = await apiRequest("/auth/login", {
    method: "POST",
    body: credentials,
    auth: false,
    retryOnUnauthorized: false,
  });

  return parseLoginResponse(response);
}

export function refreshSession() {
  if (!refreshInFlight) {
    refreshInFlight = apiRequest("/auth/refresh", {
      method: "POST",
      auth: false,
      retryOnUnauthorized: false,
    })
      .then(parseLoginResponse)
      .finally(() => {
        refreshInFlight = null;
      });
  }

  return refreshInFlight;
}

export function logout() {
  return apiRequest("/auth/logout", {
    method: "POST",
    auth: false,
    retryOnUnauthorized: false,
  });
=======
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
>>>>>>> e2c207fd89fbc28644cb875e085f1f170624a68f
}
