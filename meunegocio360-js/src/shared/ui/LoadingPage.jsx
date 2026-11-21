import { Spinner } from 'react-bootstrap'

export function LoadingPage({ message }) {
  return (
    <main className="container auth-shell d-flex align-items-center justify-content-center">
      <div className="text-center" role="status" aria-live="polite">
        <Spinner animation="border" variant="primary" aria-hidden="true" />
        <p className="mt-3 mb-0">{message}</p>
      </div>
    </main>
  )
}
