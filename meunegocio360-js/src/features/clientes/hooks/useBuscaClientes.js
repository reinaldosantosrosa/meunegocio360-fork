import { useQuery } from '@tanstack/react-query'
import { listarClientes } from '../api/clientesApi.js'
import { chaveListaClientes } from '../queryKeys.js'

export function useBuscaClientes({
  q = '',
  situacao = 'ATIVO',
  page = 0,
  size = 20,
  sort = 'nome',
  enabled = true,
}) {
  const params = { q, situacao, page, size, sort }
  return useQuery({
    queryKey: chaveListaClientes(params),
    queryFn: () => listarClientes(params),
    enabled,
  })
}
