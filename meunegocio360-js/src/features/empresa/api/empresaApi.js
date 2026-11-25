import { apiRequest } from '../../../shared/lib/http.js'

export function obterEmpresa() {
  return apiRequest('/empresa')
}

export function listarEmpresasInativas(page = 0, size = 20) {
  const params = new URLSearchParams({ page: String(page), size: String(size) })
  return apiRequest(`/empresa/inativas?${params}`)
}

export function cadastrarEmpresa(dados) {
  return apiRequest('/empresa', {
    method: 'POST',
    body: dados,
  })
}

export function atualizarEmpresa(id, dados) {
  return apiRequest(`/empresa/${id}`, {
    method: 'PUT',
    body: dados,
  })
}

export function alterarSituacaoEmpresa(id, ativa, versao) {
  return apiRequest(`/empresa/${id}/situacao`, {
    method: 'PATCH',
    body: { ativa, versao },
  })
}
