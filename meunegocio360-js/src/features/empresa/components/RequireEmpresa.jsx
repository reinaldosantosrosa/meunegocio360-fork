import { Navigate, useLocation } from 'react-router'
import { PERFIS } from '../../../shared/lib/perfis.js'
import { useAuth } from '../../auth/index.js'
import { useEmpresa } from '../hooks/useEmpresa.js'

function AguardandoEmpresaPage() {
  return (
    <main className="container app-page py-5">
      <section className="app-card card shadow-sm mx-auto">
        <div className="card-body p-4">
          <h1 className="h2">Aguardando o cadastro da empresa</h1>
          <p className="mb-0">
            As telas de negócio estarão disponíveis após o administrador
            cadastrar a empresa. Entre em contato com o administrador do
            sistema.
          </p>
        </div>
      </section>
    </main>
  )
}

export function RequireEmpresa({ children }) {
  const { status } = useEmpresa()
  const { user } = useAuth()
  const location = useLocation()

  if (status === 'ausente' && user?.perfil === PERFIS.ADMIN) {
    return (
      <Navigate to="/empresa/cadastro" replace state={{ from: location }} />
    )
  }

  if (status === 'ausente') {
    return <AguardandoEmpresaPage />
  }

  return children
}

export { AguardandoEmpresaPage }
