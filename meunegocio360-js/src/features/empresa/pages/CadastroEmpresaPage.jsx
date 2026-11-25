import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { useEffect, useState } from 'react'
import { Navigate, useNavigate } from 'react-router'
import { ApiError } from '../../../shared/lib/http.js'
import {
  alterarSituacaoEmpresa,
  cadastrarEmpresa,
  listarEmpresasInativas,
} from '../api/empresaApi.js'
import { EmpresasInativas } from '../components/EmpresasInativas.jsx'
import { EmpresaForm } from '../components/EmpresaForm.jsx'
import { useEmpresa } from '../hooks/useEmpresa.js'
import {
  EMPRESA_QUERY_KEY,
  EMPRESAS_INATIVAS_QUERY_KEY,
} from '../queryKeys.js'

const PAGE_SIZE = 20

function CadastroEmpresaPage() {
  const { empresa } = useEmpresa()
  const queryClient = useQueryClient()
  const navigate = useNavigate()
  const [page, setPage] = useState(0)
  const createMutation = useMutation({ mutationFn: cadastrarEmpresa })
  const activateMutation = useMutation({
    mutationFn: (inativa) =>
      alterarSituacaoEmpresa(inativa.id, true, inativa.versao),
  })
  const [reactivationError, setReactivationError] = useState(null)
  const empresasInativasQuery = useQuery({
    queryKey: [...EMPRESAS_INATIVAS_QUERY_KEY, page, PAGE_SIZE],
    queryFn: () => listarEmpresasInativas(page, PAGE_SIZE),
    enabled: !empresa?.ativa,
    retry: false,
    refetchOnWindowFocus: false,
  })

  useEffect(() => {
    document.title = 'Cadastre sua empresa | MeuNegocio360'
  }, [])

  if (empresa?.ativa) {
    return <Navigate to="/empresa" replace />
  }

  async function handleCreate(values) {
    const company = await createMutation.mutateAsync(values)
    queryClient.setQueryData(EMPRESA_QUERY_KEY, company)
    navigate('/', {
      replace: true,
      state: { successMessage: 'Empresa cadastrada com sucesso.' },
    })
  }

  async function handleReactivate(inativa) {
    setReactivationError(null)
    try {
      const company = await activateMutation.mutateAsync(inativa)
      queryClient.setQueryData(EMPRESA_QUERY_KEY, company)
      navigate('/', {
        replace: true,
        state: { successMessage: 'Empresa reativada com sucesso.' },
      })
      queryClient.invalidateQueries({ queryKey: EMPRESAS_INATIVAS_QUERY_KEY })
    } catch (error) {
      if (!(error instanceof ApiError)) throw error
      if (error.code === 'COMPANY_ALREADY_ACTIVE') {
        await queryClient.invalidateQueries({ queryKey: EMPRESA_QUERY_KEY })
      }
      setReactivationError({
        company: inativa,
        message:
          error.code === 'COMPANY_ALREADY_ACTIVE'
            ? 'Já existe uma empresa ativa. Os dados serão atualizados.'
            : error.code === 'VERSION_CONFLICT'
              ? 'Os dados desta empresa foram alterados. Recarregue a lista antes de tentar novamente.'
              : error.code === 'ACCESS_DENIED' || error.status === 403
                  ? 'Você não tem permissão para alterar dados da empresa.'
                : 'Não foi possível reativar a empresa. Verifique sua conexão e tente novamente.',
        retryable:
          error.status === 0 ||
          error.code === 'NETWORK_ERROR' ||
          error.code === 'TIMEOUT' ||
          error.status >= 500,
        reloadRequired: error.code === 'VERSION_CONFLICT',
      })
    }
  }

  async function reloadInactiveCompanies() {
    setReactivationError(null)
    await Promise.all([
      queryClient.invalidateQueries({ queryKey: EMPRESA_QUERY_KEY }),
      empresasInativasQuery.refetch(),
    ])
  }

  return (
    <main className="container app-page py-4 py-md-5">
      <div className="app-content-narrow">
        <h1 className="h2 mb-4">Cadastre sua empresa</h1>
        <section className="app-card card shadow-sm mb-4">
          <div className="card-body p-4">
            <EmpresasInativas
              query={empresasInativasQuery}
              page={page}
              onPageChange={setPage}
              onReactivate={handleReactivate}
              isReactivating={activateMutation.isPending}
              reactivationError={reactivationError?.message}
              onRetryReactivation={
                reactivationError?.retryable
                  ? () => handleReactivate(reactivationError.company)
                  : undefined
              }
              onReloadReactivation={
                reactivationError?.reloadRequired
                  ? reloadInactiveCompanies
                  : undefined
              }
            />
          </div>
        </section>
        <section className="app-card card shadow-sm">
          <div className="card-body p-4 p-md-5">
            <h2 className="h3 mb-4">Cadastrar nova empresa</h2>
            <EmpresaForm
              onSave={handleCreate}
              isPending={createMutation.isPending}
            />
          </div>
        </section>
      </div>
    </main>
  )
}

export default CadastroEmpresaPage
