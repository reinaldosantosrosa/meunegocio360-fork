import { Link } from 'react-router'
import { PERFIS } from '../../../shared/lib/perfis.js'
import { useAuth } from '../../auth/index.js'

function AccessDeniedPage() {
  return (
    <main className="container app-page py-5">
      <section className="app-card card shadow-sm mx-auto text-center">
        <div className="card-body p-4">
          <h1 className="h2">Acesso negado</h1>
          <p>Você não tem permissão para acessar esta página.</p>
          <Link className="btn btn-primary min-touch-target" to="/">
            Ir para o início
          </Link>
        </div>
      </section>
    </main>
  )
}

export function RequireRole({ roles, children }) {
  const { user } = useAuth()
  const allowedRoles = Array.isArray(roles) ? roles : [roles]

  if (!allowedRoles.includes(user?.perfil)) {
    return <AccessDeniedPage />
  }

  return children
}

export { AccessDeniedPage, PERFIS }
