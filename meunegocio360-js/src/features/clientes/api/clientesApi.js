import { apiRequest } from '../../../shared/lib/http.js'

export function listarClientes({
  q = '',
  situacao = 'ATIVO',
  page = 0,
  size = 20,
  sort = 'nome',
}) {
  const params = new URLSearchParams({
    q,
    situacao,
    page: String(page),
    size: String(size),
    sort,
  })
  return apiRequest(`/clientes?${params}`)
}

export function obterCliente(id) {
  return apiRequest(`/clientes/${encodeURIComponent(id)}`)
}

export function cadastrarCliente(dados) {
  return apiRequest('/clientes', {
    method: 'POST',
    body: dados,
  })
}

export function atualizarCliente(id, dados) {
  return apiRequest(`/clientes/${encodeURIComponent(id)}`, {
    method: 'PUT',
    body: dados,
  })
}

export function alterarSituacaoCliente(id, ativo, versao) {
  return apiRequest(`/clientes/${encodeURIComponent(id)}/situacao`, {
    method: 'PATCH',
    body: { ativo, versao },
  })
}

export function excluirCliente(id) {
  return apiRequest(`/clientes/${encodeURIComponent(id)}`, {
    method: 'DELETE',
  })
}
