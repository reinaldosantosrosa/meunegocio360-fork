import { useQuery } from '@tanstack/react-query'
import { ApiError } from '../../../shared/lib/http.js'
import { obterEmpresa } from '../api/empresaApi.js'
import { EmpresaContext } from '../context/EmpresaContext.js'
import { EMPRESA_QUERY_KEY } from '../queryKeys.js'

export function EmpresaProvider({ children }) {
  const query = useQuery({
    queryKey: EMPRESA_QUERY_KEY,
    queryFn: obterEmpresa,
    retry: false,
    refetchOnWindowFocus: false,
    staleTime: Infinity,
  })

  const ausente =
    query.error instanceof ApiError &&
    query.error.status === 404 &&
    query.error.code === 'COMPANY_NOT_REGISTERED'
  const empresaInativa = Boolean(query.data && !query.data.ativa)

  const value = {
    empresa: query.data ?? null,
    status: query.isPending
      ? 'carregando'
      : ausente || empresaInativa
        ? 'ausente'
        : query.isError
          ? 'erro'
          : 'ativa',
    atualizarConsulta: query.refetch,
  }

  return (
    <EmpresaContext.Provider value={value}>{children}</EmpresaContext.Provider>
  )
}
