import { useCallback, useState } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { Alert, Button, Table } from 'react-bootstrap'
import { useLocation, useNavigate } from 'react-router'
import { ApiError } from '../../../shared/lib/http.js'
import { formatarDocumento } from '../../../shared/lib/documento.js'
import { formatarTelefone } from '../../../shared/lib/empresaFormat.js'
import { useUrlFilters } from '../../../shared/hooks/useUrlFilters.js'
import { ConfirmModal } from '../../../shared/ui/ConfirmModal.jsx'
import { ErrorAlert } from '../../../shared/ui/ErrorAlert.jsx'
import { ListToolbar } from '../../../shared/ui/ListToolbar.jsx'
import { Paginacao } from '../../../shared/ui/Paginacao.jsx'
import { StatusBadge } from '../../../shared/ui/StatusBadge.jsx'
import { alterarSituacaoCliente, excluirCliente } from '../api/clientesApi.js'
import { useBuscaClientes } from '../hooks/useBuscaClientes.js'
import { CLIENTES_QUERY_KEY } from '../queryKeys.js'

const PAGE_SIZE = 20

function mensagemErroSituacao(error) {
  if (error.code === 'VERSION_CONFLICT') {
    return 'Os dados do cliente foram alterados. Recarregue a lista antes de tentar novamente.'
  }
  if (error.status === 403 || error.code === 'ACCESS_DENIED') {
    return 'Você não tem permissão para alterar este cliente.'
  }
  if (error.code === 'CUSTOMER_NOT_FOUND') {
    return 'Este cliente não existe mais.'
  }
  return 'Não foi possível atualizar a situação do cliente. Verifique sua conexão e tente novamente.'
}

export default function ClientesPage() {
  const { q, situacao, page, setFilters, clearFilters } = useUrlFilters()
  const queryClient = useQueryClient()
  const location = useLocation()
  const navigate = useNavigate()
  const [notice, setNotice] = useState(null)
  const [actionError, setActionError] = useState(null)
  const [deleteTarget, setDeleteTarget] = useState(null)
  const [deleteError, setDeleteError] = useState(null)
  const [deactivateTarget, setDeactivateTarget] = useState(null)
  const [deactivateError, setDeactivateError] = useState(null)

  const clientesQuery = useBuscaClientes({
    q,
    situacao,
    page,
    size: PAGE_SIZE,
    sort: 'nome',
  })
  const statusMutation = useMutation({
    mutationFn: ({ id, ativo, versao }) =>
      alterarSituacaoCliente(id, ativo, versao),
  })
  const deleteMutation = useMutation({
    mutationFn: excluirCliente,
  })

  const handleSearchChange = useCallback(
    (value) => setFilters({ q: value, page: 0 }),
    [setFilters],
  )
  const handleSituationChange = useCallback(
    (value) => setFilters({ situacao: value, page: 0 }),
    [setFilters],
  )
  const handlePageChange = useCallback(
    (value) => setFilters({ page: value }),
    [setFilters],
  )

  async function atualizarLista() {
    await queryClient.invalidateQueries({ queryKey: CLIENTES_QUERY_KEY })
  }

  async function recarregarAlvo(id, setTarget, clearError) {
    const result = await clientesQuery.refetch()
    const current = result.data?.content.find((cliente) => cliente.id === id)
    setTarget(current ?? null)
    clearError(null)
  }

  async function alterarSituacao(cliente, ativo, modal = null) {
    setNotice(null)
    if (modal === 'delete') setDeleteError(null)
    else if (modal === 'deactivate') setDeactivateError(null)
    else setActionError(null)

    try {
      await statusMutation.mutateAsync({
        id: cliente.id,
        ativo,
        versao: cliente.versao,
      })
      setNotice({
        message: ativo ? 'Cliente reativado.' : 'Cliente inativado.',
        variant: 'success',
      })
      if (modal === 'delete') setDeleteTarget(null)
      else if (modal === 'deactivate') setDeactivateTarget(null)
      await atualizarLista()
    } catch (error) {
      if (!(error instanceof ApiError)) throw error
      const errorState = {
        message: mensagemErroSituacao(error),
        retryable: error.status === 0 || error.status >= 500,
        reloadRequired: error.code === 'VERSION_CONFLICT',
        retry: { cliente, ativo },
      }
      if (modal === 'delete') setDeleteError({ ...errorState, kind: 'action' })
      else if (modal === 'deactivate') setDeactivateError(errorState)
      else setActionError(errorState)
    }
  }

  async function excluirSelecionado() {
    if (!deleteTarget) return
    setNotice(null)
    setDeleteError(null)
    try {
      await deleteMutation.mutateAsync(deleteTarget.id)
      setNotice({ message: 'Cliente excluído.', variant: 'success' })
      setDeleteTarget(null)
      await atualizarLista()
    } catch (error) {
      if (!(error instanceof ApiError)) throw error
      if (error.status === 422 && error.code === 'CUSTOMER_HAS_HISTORY') {
        setDeleteError({
          kind: 'history',
          message:
            'Este cliente possui vendas ou ordens de serviço vinculadas e não pode ser excluído. Você pode inativá-lo.',
        })
      } else if (error.status === 404 && error.code === 'CUSTOMER_NOT_FOUND') {
        setNotice({ message: 'Este cliente não existe mais.', variant: 'info' })
        setDeleteTarget(null)
        await atualizarLista()
      } else {
        setDeleteError({
          kind: 'action',
          message:
            error.status === 403 || error.code === 'ACCESS_DENIED'
              ? 'Você não tem permissão para excluir este cliente.'
              : 'Não foi possível excluir o cliente. Verifique sua conexão e tente novamente.',
          retryable: error.status === 0 || error.status >= 500,
        })
      }
    }
  }

  function iniciarEdicao(cliente) {
    navigate(`/clientes/${cliente.id}`, {
      state: { returnTo: `${location.pathname}${location.search}` },
    })
  }

  function iniciarCadastro() {
    navigate('/clientes/novo', {
      state: { returnTo: `${location.pathname}${location.search}` },
    })
  }

  const clientes = clientesQuery.data?.content ?? []
  const hasFilters = Boolean(q.trim()) || situacao !== 'ATIVO'
  const returnToList = location.state?.successMessage

  return (
    <main className="container app-page py-4 py-md-5">
      <section className="app-card card shadow-sm">
        <div className="card-body p-4 p-md-5">
          <header className="d-flex flex-column flex-sm-row justify-content-between align-items-sm-center gap-3 mb-4">
            <h1 className="h2 mb-0">Clientes</h1>
            <Button
              type="button"
              variant="primary"
              className="min-touch-target"
              onClick={iniciarCadastro}
            >
              Novo cliente
            </Button>
          </header>

          {returnToList && (
            <Alert variant="success" role="status">
              {returnToList}
            </Alert>
          )}
          {notice && (
            <Alert variant={notice.variant} role="status">
              {notice.message}
            </Alert>
          )}
          {actionError && (
            <ErrorAlert
              onRetry={
                actionError.retryable
                  ? () =>
                      alterarSituacao(
                        actionError.retry.cliente,
                        actionError.retry.ativo,
                      )
                  : undefined
              }
              onReload={
                actionError.reloadRequired
                  ? async () => {
                      setActionError(null)
                      await atualizarLista()
                    }
                  : undefined
              }
            >
              {actionError.message}
            </ErrorAlert>
          )}

          <ListToolbar
            key={q}
            query={q}
            onQueryChange={handleSearchChange}
            situation={situacao}
            onSituationChange={handleSituationChange}
            onClear={clearFilters}
            hasFilters={hasFilters}
          />

          {clientesQuery.isPending && (
            <p role="status" aria-live="polite">
              Carregando clientes...
            </p>
          )}
          {clientesQuery.isFetching && !clientesQuery.isPending && (
            <p role="status" aria-live="polite">
              Atualizando lista...
            </p>
          )}
          {clientesQuery.isError && (
            <ErrorAlert
              onRetry={
                clientesQuery.error.status !== 403 &&
                clientesQuery.error.code !== 'ACCESS_DENIED'
                  ? () => clientesQuery.refetch()
                  : undefined
              }
            >
              {clientesQuery.error.status === 403 ||
              clientesQuery.error.code === 'ACCESS_DENIED'
                ? 'Você não tem permissão para acessar clientes.'
                : 'Não foi possível carregar os clientes.'}
            </ErrorAlert>
          )}
          {clientesQuery.isSuccess && clientes.length === 0 && !hasFilters && (
            <p role="status">
              Nenhum cliente cadastrado. Cadastre o primeiro cliente.
            </p>
          )}
          {clientesQuery.isSuccess && clientes.length === 0 && hasFilters && (
            <div>
              <p role="status">Nenhum cliente encontrado para a busca.</p>
            </div>
          )}
          {clientes.length > 0 && (
            <>
              <Table responsive className="align-middle">
                <caption className="visually-hidden">Lista de clientes</caption>
                <thead>
                  <tr>
                    <th scope="col">Nome</th>
                    <th scope="col">Documento</th>
                    <th scope="col">Telefone</th>
                    <th scope="col">E-mail</th>
                    <th scope="col">Situação</th>
                    <th scope="col">Ações</th>
                  </tr>
                </thead>
                <tbody>
                  {clientes.map((cliente) => (
                    <tr key={cliente.id}>
                      <th scope="row">{cliente.nome}</th>
                      <td>
                        {formatarDocumento(
                          cliente.tipoPessoa,
                          cliente.documento,
                        )}
                      </td>
                      <td>
                        {cliente.telefone
                          ? formatarTelefone(cliente.telefone)
                          : '—'}
                      </td>
                      <td>{cliente.email || '—'}</td>
                      <td>
                        <StatusBadge active={cliente.ativo} />
                      </td>
                      <td>
                        <div className="d-flex flex-wrap gap-2">
                          <Button
                            type="button"
                            variant="outline-dark"
                            className="min-touch-target"
                            onClick={() => iniciarEdicao(cliente)}
                          >
                            Editar
                          </Button>
                          {cliente.ativo ? (
                            <Button
                              type="button"
                              variant="outline-dark"
                              className="min-touch-target"
                              onClick={() => {
                                setDeactivateError(null)
                                setDeactivateTarget(cliente)
                              }}
                              disabled={statusMutation.isPending}
                            >
                              Inativar
                            </Button>
                          ) : (
                            <Button
                              type="button"
                              variant="outline-dark"
                              className="min-touch-target"
                              onClick={() => alterarSituacao(cliente, true)}
                              disabled={statusMutation.isPending}
                            >
                              {statusMutation.isPending
                                ? 'Reativando...'
                                : 'Reativar'}
                            </Button>
                          )}
                          <Button
                            type="button"
                            variant="outline-dark"
                            className="min-touch-target"
                            onClick={() => {
                              setDeleteError(null)
                              setDeleteTarget(cliente)
                            }}
                            disabled={
                              deleteMutation.isPending ||
                              statusMutation.isPending
                            }
                          >
                            Excluir
                          </Button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </Table>

              <Paginacao
                page={clientesQuery.data.page}
                totalPages={clientesQuery.data.totalPages}
                isFetching={clientesQuery.isFetching}
                onPageChange={handlePageChange}
                label="Paginação de clientes"
              />
            </>
          )}
        </div>
      </section>

      <ConfirmModal
        show={Boolean(deactivateTarget)}
        title={`Inativar ${deactivateTarget?.nome ?? 'cliente'}?`}
        onCancel={() => {
          setDeactivateTarget(null)
          setDeactivateError(null)
        }}
        onConfirm={() =>
          deactivateTarget &&
          alterarSituacao(deactivateTarget, false, 'deactivate')
        }
        isPending={statusMutation.isPending}
        confirmLabel="Inativar cliente"
        pendingLabel="Inativando..."
      >
        {deactivateError && (
          <ErrorAlert
            onRetry={
              deactivateError.retryable
                ? () =>
                    alterarSituacao(
                      deactivateError.retry.cliente,
                      deactivateError.retry.ativo,
                      'deactivate',
                    )
                : undefined
            }
            onReload={
              deactivateError.reloadRequired
                ? () =>
                    recarregarAlvo(
                      deactivateTarget?.id,
                      setDeactivateTarget,
                      setDeactivateError,
                    )
                : undefined
            }
          >
            {deactivateError.message}
          </ErrorAlert>
        )}
        Ao inativar, o cliente deixa de aparecer na lista de ativos, mas seus
        dados são preservados para consulta e histórico.
      </ConfirmModal>

      <ConfirmModal
        show={Boolean(deleteTarget)}
        title={`Excluir o cliente ${deleteTarget?.nome ?? ''}?`}
        onCancel={() => {
          setDeleteTarget(null)
          setDeleteError(null)
        }}
        onConfirm={excluirSelecionado}
        isPending={deleteMutation.isPending || statusMutation.isPending}
        confirmLabel="Excluir cliente"
        pendingLabel={
          statusMutation.isPending ? 'Inativando...' : 'Excluindo...'
        }
      >
        <p>Esta ação não pode ser desfeita.</p>
        {deleteError && (
          <>
            <ErrorAlert
              onRetry={
                deleteError.retryable
                  ? deleteError.retry
                    ? () =>
                        alterarSituacao(
                          deleteError.retry.cliente,
                          deleteError.retry.ativo,
                          'delete',
                        )
                    : excluirSelecionado
                  : undefined
              }
              onReload={
                deleteError.reloadRequired
                  ? () =>
                      recarregarAlvo(
                        deleteTarget?.id,
                        setDeleteTarget,
                        setDeleteError,
                      )
                  : undefined
              }
            >
              {deleteError.message}
            </ErrorAlert>
            {deleteError.kind === 'history' && deleteTarget && (
              <Button
                type="button"
                variant="outline-dark"
                className="min-touch-target"
                disabled={statusMutation.isPending}
                onClick={() => alterarSituacao(deleteTarget, false, 'delete')}
              >
                {statusMutation.isPending
                  ? 'Inativando...'
                  : 'Inativar cliente'}
              </Button>
            )}
          </>
        )}
      </ConfirmModal>
    </main>
  )
}
