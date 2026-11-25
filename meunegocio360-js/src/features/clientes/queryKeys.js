export const CLIENTES_QUERY_KEY = ['clientes']

export function chaveListaClientes(params) {
  return [...CLIENTES_QUERY_KEY, 'lista', params]
}

export function chaveCliente(id) {
  return [...CLIENTES_QUERY_KEY, 'detalhe', id]
}
