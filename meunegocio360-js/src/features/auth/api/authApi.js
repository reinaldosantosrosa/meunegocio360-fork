<<<<<<< HEAD
import { apiRequest } from '../../../shared/lib/http.js'
=======
<<<<<<< HEAD
import { apiRequest } from "../../../shared/lib/http.js";
import { parseLoginResponse } from "./authSchema.js";
>>>>>>> feat/rf-be-004-cadastro-de-fornecedor

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
<<<<<<< HEAD
=======
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
>>>>>>> feat/rf-be-004-cadastro-de-fornecedor
  return apiRequest('/auth/logout', {
    method: 'POST',
    authenticated: false,
    retryUnauthorized: false,
  })
<<<<<<< HEAD
=======
>>>>>>> e2c207fd89fbc28644cb875e085f1f170624a68f
>>>>>>> feat/rf-be-004-cadastro-de-fornecedor
}
