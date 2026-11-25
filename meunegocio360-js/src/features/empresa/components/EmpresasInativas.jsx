import { Button } from 'react-bootstrap'
import { formatarCnpj } from '../../../shared/lib/empresaFormat.js'
import { ErrorAlert } from '../../../shared/ui/ErrorAlert.jsx'

export function EmpresasInativas({
  query,
  page,
  onPageChange,
  onReactivate,
  isReactivating,
  reactivationError,
  onRetryReactivation,
  onReloadReactivation,
}) {
  const pagina = query.data
  const empresas = pagina?.content ?? []

  return (
    <section className="mb-4" aria-labelledby="empresas-inativas-title">
      <h2 id="empresas-inativas-title" className="h3">
        Empresas inativas
      </h2>
      <p>Selecione uma empresa para reativá-la.</p>

      {reactivationError && (
        <ErrorAlert
          onRetry={onRetryReactivation}
          onReload={onReloadReactivation}
        >
          {reactivationError}
        </ErrorAlert>
      )}

      {query.isPending && (
        <p role="status">Carregando empresas inativas...</p>
      )}
      {query.isError && (
        <ErrorAlert onRetry={() => query.refetch()}>
          Não foi possível carregar as empresas inativas.
        </ErrorAlert>
      )}
      {query.isSuccess && empresas.length === 0 && (
        <p role="status">Nenhuma empresa inativa cadastrada.</p>
      )}
      {empresas.length > 0 && (
        <>
          <ul className="list-group mb-3">
            {empresas.map((empresa) => {
              const nome = empresa.nomeFantasia || empresa.razaoSocial
              return (
                <li
                  key={empresa.id}
                  className="list-group-item d-flex flex-column flex-sm-row align-items-sm-center justify-content-between gap-3"
                >
                  <div>
                    <h3 className="h5 mb-1">{nome}</h3>
                    <p className="mb-0">CNPJ: {formatarCnpj(empresa.cnpj)}</p>
                  </div>
                  <Button
                    type="button"
                    variant="primary"
                    className="min-touch-target flex-shrink-0"
                    onClick={() => onReactivate(empresa)}
                    disabled={isReactivating}
                    aria-label={`Reativar ${nome}`}
                  >
                    {isReactivating ? 'Reativando...' : 'Reativar'}
                  </Button>
                </li>
              )
            })}
          </ul>

          {pagina.totalPages > 1 && (
            <nav
              className="d-flex flex-wrap align-items-center gap-2"
              aria-label="Paginação de empresas inativas"
            >
              <Button
                type="button"
                variant="outline-dark"
                className="min-touch-target"
                disabled={page === 0 || query.isFetching || isReactivating}
                onClick={() => onPageChange(page - 1)}
              >
                Página anterior
              </Button>
              <span aria-live="polite">
                Página {pagina.page + 1} de {pagina.totalPages}
              </span>
              <Button
                type="button"
                variant="outline-dark"
                className="min-touch-target"
                disabled={
                  pagina.page + 1 >= pagina.totalPages ||
                  query.isFetching ||
                  isReactivating
                }
                onClick={() => onPageChange(page + 1)}
              >
                Próxima página
              </Button>
            </nav>
          )}
        </>
      )}
    </section>
  )
}
