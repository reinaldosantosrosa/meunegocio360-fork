import { Button } from 'react-bootstrap'

export function Paginacao({
  page,
  totalPages,
  isFetching = false,
  onPageChange,
  label = 'Paginação',
}) {
  if (totalPages <= 1) return null

  return (
    <nav
      className="d-flex flex-wrap align-items-center justify-content-between gap-2"
      aria-label={label}
    >
      <Button
        type="button"
        variant="outline-dark"
        className="min-touch-target"
        disabled={page <= 0 || isFetching}
        onClick={() => onPageChange(page - 1)}
      >
        Página anterior
      </Button>
      <span aria-live="polite">
        Página {page + 1} de {totalPages}
      </span>
      <Button
        type="button"
        variant="outline-dark"
        className="min-touch-target"
        disabled={page + 1 >= totalPages || isFetching}
        onClick={() => onPageChange(page + 1)}
      >
        Próxima página
      </Button>
    </nav>
  )
}
