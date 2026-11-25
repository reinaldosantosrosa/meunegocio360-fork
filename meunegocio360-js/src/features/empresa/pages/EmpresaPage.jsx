import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useEffect, useState } from 'react'
import { Button, Table } from 'react-bootstrap'
import { ApiError } from '../../../shared/lib/http.js'
import { ConfirmModal } from '../../../shared/ui/ConfirmModal.jsx'
import { ErrorAlert } from '../../../shared/ui/ErrorAlert.jsx'
import {
  formatarCnpj,
  formatarTelefone,
} from '../../../shared/lib/empresaFormat.js'
import { PERFIS } from '../../../shared/lib/perfis.js'
import { useAuth } from '../../auth/index.js'
import { alterarSituacaoEmpresa, atualizarEmpresa } from '../api/empresaApi.js'
import { EmpresaForm } from '../components/EmpresaForm.jsx'
import { RequireRole } from '../components/RequireRole.jsx'
import { useEmpresa } from '../hooks/useEmpresa.js'
import { EMPRESA_QUERY_KEY } from '../queryKeys.js'

function CompanyDetails({ empresa }) {
  const fields = [
    ['Razão social', empresa.razaoSocial],
    ['Nome fantasia', empresa.nomeFantasia || '—'],
    ['CNPJ', formatarCnpj(empresa.cnpj)],
    ['E-mail', empresa.email || '—'],
    ['Telefone', empresa.telefone ? formatarTelefone(empresa.telefone) : '—'],
    ['Fuso horário', empresa.fusoHorario],
    ['Moeda', empresa.moeda],
  ]

  return (
    <Table responsive className="align-middle app-company-table">
      <tbody>
        {fields.map(([label, value]) => (
          <tr key={label}>
            <th scope="row">{label}</th>
            <td>{value}</td>
          </tr>
        ))}
      </tbody>
    </Table>
  )
}

function EmpresaPage() {
  const { empresa, atualizarConsulta } = useEmpresa()
  const { user } = useAuth()
  const queryClient = useQueryClient()
  const [editing, setEditing] = useState(false)
  const [confirmingDeactivation, setConfirmingDeactivation] = useState(false)
  const [successMessage, setSuccessMessage] = useState('')
  const [actionError, setActionError] = useState('')
  const [canRetryDeactivation, setCanRetryDeactivation] = useState(false)
  const [needsDeactivationReload, setNeedsDeactivationReload] = useState(false)
  const updateMutation = useMutation({
    mutationFn: (values) => atualizarEmpresa(empresa.id, values),
  })
  const deactivateMutation = useMutation({
    mutationFn: ({ id, versao }) => alterarSituacaoEmpresa(id, false, versao),
  })
  const isAdmin = user?.perfil === PERFIS.ADMIN

  useEffect(() => {
    document.title = 'Empresa | MeuNegocio360'
  }, [])

  async function handleSave(values) {
    const updated = await updateMutation.mutateAsync(values)
    queryClient.setQueryData(EMPRESA_QUERY_KEY, updated)
    setEditing(false)
    setSuccessMessage('Dados da empresa atualizados.')
    setActionError('')
  }

  async function handleDeactivate() {
    setCanRetryDeactivation(false)
    setNeedsDeactivationReload(false)
    try {
      const inactive = await deactivateMutation.mutateAsync({
        id: empresa.id,
        versao: empresa.versao,
      })
      queryClient.setQueryData(EMPRESA_QUERY_KEY, inactive)
      setConfirmingDeactivation(false)
      setSuccessMessage('')
      setActionError('')
    } catch (error) {
      if (!(error instanceof ApiError)) throw error
      setConfirmingDeactivation(false)
      setActionError(
        error?.code === 'VERSION_CONFLICT'
          ? 'Os dados da empresa foram alterados. Recarregue os dados antes de tentar novamente.'
          : error?.code === 'ACCESS_DENIED'
            ? 'Você não tem permissão para alterar os dados da empresa.'
            : 'Não foi possível inativar a empresa. Verifique sua conexão e tente novamente.',
      )
      setCanRetryDeactivation(
        error.code === 'NETWORK_ERROR' ||
          error.code === 'TIMEOUT' ||
          error.status >= 500,
      )
      setNeedsDeactivationReload(error.code === 'VERSION_CONFLICT')
    }
  }

  return (
    <main className="container app-page py-4 py-md-5">
      <section className="app-card card shadow-sm">
        <div className="card-body p-4 p-md-5">
          <header className="d-flex flex-column flex-sm-row justify-content-between align-items-sm-center gap-3 mb-4">
            <h1 className="h2 mb-0">Empresa</h1>
            {!editing && isAdmin && (
              <div className="d-flex flex-wrap gap-2">
                <Button
                  type="button"
                  variant="outline-dark"
                  className="min-touch-target"
                  onClick={() => {
                    setSuccessMessage('')
                    setActionError('')
                    setEditing(true)
                  }}
                >
                  Editar
                </Button>
                <Button
                  type="button"
                  variant="outline-dark"
                  className="min-touch-target"
                  onClick={() => setConfirmingDeactivation(true)}
                >
                  Inativar
                </Button>
              </div>
            )}
          </header>

          {successMessage && (
            <div className="alert alert-success" role="status">
              {successMessage}
            </div>
          )}
          {actionError && (
            <ErrorAlert
              onRetry={
                canRetryDeactivation
                  ? () => setConfirmingDeactivation(true)
                  : undefined
              }
              onReload={
                needsDeactivationReload
                  ? async () => {
                      setActionError('')
                      await atualizarConsulta()
                    }
                  : undefined
              }
            >
              {actionError}
            </ErrorAlert>
          )}

          {editing ? (
            <EmpresaForm
              empresa={empresa}
              onSave={handleSave}
              isPending={updateMutation.isPending}
              onCancel={() => setEditing(false)}
              onReload={atualizarConsulta}
            />
          ) : (
            <CompanyDetails empresa={empresa} />
          )}
        </div>
      </section>

      <ConfirmModal
        show={confirmingDeactivation}
        title="Inativar empresa?"
        onCancel={() => setConfirmingDeactivation(false)}
        onConfirm={handleDeactivate}
        isPending={deactivateMutation.isPending}
        confirmLabel="Inativar empresa"
      >
        Ao inativar a empresa, não será possível cadastrar clientes,
        fornecedores, produtos nem vendas até que uma empresa seja ativada.
      </ConfirmModal>
    </main>
  )
}

export default function GuardedEmpresaPage() {
  return (
    <RequireRole
      roles={[
        PERFIS.ADMIN,
        PERFIS.OPERADOR_COMERCIAL,
        PERFIS.OPERADOR_FINANCEIRO,
      ]}
    >
      <EmpresaPage />
    </RequireRole>
  )
}
