import { Alert, Button } from 'react-bootstrap'

export function ErrorAlert({
  children,
  onRetry,
  retryLabel = 'Tentar novamente',
  onReload,
}) {
  return (
    <Alert variant="danger" role="alert">
      <div>{children}</div>
      {(onRetry || onReload) && (
        <div className="d-flex flex-wrap gap-2 mt-3">
          {onRetry && (
            <Button
              type="button"
              variant="outline-dark"
              onClick={onRetry}
              className="min-touch-target"
            >
              {retryLabel}
            </Button>
          )}
          {onReload && (
            <Button
              type="button"
              variant="outline-dark"
              onClick={onReload}
              className="min-touch-target"
            >
              Recarregar dados
            </Button>
          )}
        </div>
      )}
    </Alert>
  )
}
