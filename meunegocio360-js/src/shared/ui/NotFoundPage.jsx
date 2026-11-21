import { useEffect } from 'react'
import { Link } from 'react-router'

function NotFoundPage() {
  useEffect(() => {
    document.title = 'Página não encontrada | MeuNegocio360'
  }, [])

  return (
    <main className="container auth-shell d-flex align-items-center justify-content-center py-4">
      <section className="card auth-card shadow-sm text-center">
        <div className="card-body p-4 p-md-5">
          <h1 className="h2 mb-3">Página não encontrada</h1>
          <p className="mb-4">O endereço informado não existe.</p>
          <Link className="btn btn-primary w-100" to="/">
            Ir para a página inicial
          </Link>
        </div>
      </section>
    </main>
  )
}

export default NotFoundPage
