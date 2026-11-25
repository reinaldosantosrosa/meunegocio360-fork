import { useEffect } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Button } from 'react-bootstrap'
import { Link, useLocation, useNavigate, useParams } from 'react-router'
import { ApiError } from '../../../shared/lib/http.js'
import { safeInternalPath } from '../../../shared/lib/safeInternalPath.js'
import { ErrorAlert } from '../../../shared/ui/ErrorAlert.jsx'
import { LoadingPage } from '../../../shared/ui/LoadingPage.jsx'
import {
  atualizarCliente,
  cadastrarCliente,
  obterCliente,
} from '../api/clientesApi.js'
import { ClienteForm } from '../components/ClienteForm.jsx'
import { chaveCliente, CLIENTES_QUERY_KEY } from '../queryKeys.js'

function urlParaLista(nome, situacao) {
  const params = new URLSearchParams({
    q: nome,
    situacao,
    page: '0',
  })
  return `/clientes?${params}`
}

export default function ClienteFormPage() {
  const { id } = useParams()
  const isEditing = Boolean(id)
  const location = useLocation()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const clienteQuery = useQuery({
    queryKey: chaveCliente(id),
    queryFn: () => obterCliente(id),
    enabled: isEditing,
    retry: false,
  })
  const saveMutation = useMutation({
    mutationFn: (values) =>
      isEditing ? atualizarCliente(id, values) : cadastrarCliente(values),
  })

  useEffect(() => {
    document.title = isEditing
      ? 'Editar cliente | MeuNegocio360'
      : 'Novo cliente | MeuNegocio360'
  }, [isEditing])

  const cliente = clienteQuery.data
  const returnTo = safeInternalPath(location.state?.returnTo || '/clientes')

  async function handleSave(values) {
    const saved = await saveMutation.mutateAsync(values)
    if (isEditing) {
      queryClient.setQueryData(chaveCliente(id), saved)
    }
    await queryClient.invalidateQueries({ queryKey: CLIENTES_QUERY_KEY })
    navigate(urlParaLista(saved.nome, isEditing ? 'TODOS' : 'ATIVO'), {
      replace: true,
      state: {
        successMessage: isEditing
          ? 'Cliente atualizado com sucesso.'
          : 'Cliente cadastrado com sucesso.',
      },
    })
  }

  if (isEditing && clienteQuery.isPending) {
    return <LoadingPage message="Carregando os dados do cliente..." />
  }

  if (isEditing && clienteQuery.isError) {
    if (
      clienteQuery.error instanceof ApiError &&
      clienteQuery.error.status === 404 &&
      clienteQuery.error.code === 'CUSTOMER_NOT_FOUND'
    ) {
      return (
        <main className="container app-page py-4 py-md-5">
          <section className="app-card card shadow-sm">
            <div className="card-body p-4 p-md-5">
              <h1 className="h2">Cliente não encontrado</h1>
              <p>Este cliente não existe ou não está mais disponível.</p>
              <Link to="/clientes" className="btn btn-primary min-touch-target">
                Voltar para clientes
              </Link>
            </div>
          </section>
        </main>
      )
    }

    return (
      <main className="container app-page py-4 py-md-5">
        <ErrorAlert
          onRetry={
            clienteQuery.error.status === 0 || clienteQuery.error.status >= 500
              ? () => clienteQuery.refetch()
              : undefined
          }
        >
          {clienteQuery.error.status === 403 ||
          clienteQuery.error.code === 'ACCESS_DENIED'
            ? 'Você não tem permissão para acessar clientes.'
            : 'Não foi possível carregar os dados do cliente.'}
        </ErrorAlert>
        <Button
          type="button"
          variant="outline-dark"
          className="min-touch-target"
          onClick={() => navigate(returnTo)}
        >
          Voltar para clientes
        </Button>
      </main>
    )
  }

  return (
    <main className="container app-page py-4 py-md-5">
      <section className="app-card card shadow-sm">
        <div className="card-body p-4 p-md-5">
          <h1 className="h2 mb-4">
            {isEditing ? 'Editar cliente' : 'Novo cliente'}
          </h1>
          <ClienteForm
            cliente={cliente}
            onSave={handleSave}
            isPending={saveMutation.isPending}
            onCancel={() => navigate(returnTo)}
            onReload={() => clienteQuery.refetch()}
          />
        </div>
      </section>
    </main>
  )
}
