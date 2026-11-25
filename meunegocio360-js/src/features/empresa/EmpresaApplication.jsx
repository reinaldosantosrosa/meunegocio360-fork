import { lazy, Suspense } from 'react'
import { Route, Routes, useLocation } from 'react-router'
import { PERFIS } from '../../shared/lib/perfis.js'
import { LoadingPage } from '../../shared/ui/LoadingPage.jsx'
import { ErrorAlert } from '../../shared/ui/ErrorAlert.jsx'
import { useAuth } from '../auth/index.js'
import { AppLayout } from './components/AppLayout.jsx'
import { EmpresaProvider } from './components/EmpresaProvider.jsx'
import { RequireEmpresa } from './components/RequireEmpresa.jsx'
import { RequireRole, AccessDeniedPage } from './components/RequireRole.jsx'
import { useEmpresa } from './hooks/useEmpresa.js'

const HomePage = lazy(() => import('../home/pages/HomePage.jsx'))
const EmpresaPage = lazy(() => import('./pages/EmpresaPage.jsx'))
const CadastroEmpresaPage = lazy(
  () => import('./pages/CadastroEmpresaPage.jsx'),
)
const ClientesPage = lazy(() =>
  import('../clientes/index.js').then((feature) =>
    feature.carregarPaginaClientes(),
  ),
)
const ClienteFormPage = lazy(() =>
  import('../clientes/index.js').then((feature) =>
    feature.carregarPaginaFormularioCliente(),
  ),
)
const ClienteSelectDemoPage = lazy(() =>
  import('../clientes/index.js').then((feature) =>
    feature.carregarPaginaDemonstracaoClienteSelect(),
  ),
)
const NotFoundPage = lazy(() => import('../../shared/ui/NotFoundPage.jsx'))

const CLIENTES_ROLES = [PERFIS.ADMIN, PERFIS.OPERADOR_COMERCIAL]

function CompanyResolvedRoutes() {
  const { status, atualizarConsulta } = useEmpresa()

  if (status === 'carregando') {
    return <LoadingPage message="Carregando os dados da empresa..." />
  }

  if (status === 'erro') {
    return (
      <main className="container app-page py-4">
        <ErrorAlert onRetry={() => atualizarConsulta()}>
          Não foi possível carregar os dados da empresa.
        </ErrorAlert>
      </main>
    )
  }

  return (
    <Suspense fallback={<LoadingPage message="Carregando..." />}>
      <Routes>
        <Route element={<AppLayout />}>
          <Route
            path="/"
            element={
              <RequireEmpresa>
                <HomePage />
              </RequireEmpresa>
            }
          />
          <Route
            path="/empresa"
            element={
              <RequireEmpresa>
                <EmpresaPage />
              </RequireEmpresa>
            }
          />
          <Route
            path="/empresa/cadastro"
            element={
              <RequireRole roles={[PERFIS.ADMIN]}>
                <CadastroEmpresaPage />
              </RequireRole>
            }
          />
          <Route
            path="/clientes"
            element={
              <RequireEmpresa>
                <RequireRole roles={CLIENTES_ROLES}>
                  <ClientesPage />
                </RequireRole>
              </RequireEmpresa>
            }
          />
          <Route
            path="/clientes/novo"
            element={
              <RequireEmpresa>
                <RequireRole roles={CLIENTES_ROLES}>
                  <ClienteFormPage />
                </RequireRole>
              </RequireEmpresa>
            }
          />
          <Route
            path="/clientes/:id"
            element={
              <RequireEmpresa>
                <RequireRole roles={CLIENTES_ROLES}>
                  <ClienteFormPage />
                </RequireRole>
              </RequireEmpresa>
            }
          />
          {import.meta.env.DEV && (
            <Route
              path="/dev/cliente-select"
              element={
                <RequireEmpresa>
                  <RequireRole roles={CLIENTES_ROLES}>
                    <ClienteSelectDemoPage />
                  </RequireRole>
                </RequireEmpresa>
              }
            />
          )}
          <Route
            path="*"
            element={
              <RequireEmpresa>
                <NotFoundPage />
              </RequireEmpresa>
            }
          />
        </Route>
      </Routes>
    </Suspense>
  )
}

export function EmpresaApplication() {
  const { user } = useAuth()
  const location = useLocation()
  const path = location.pathname.replace(/\/+$/, '') || '/'

  if (path === '/empresa/cadastro' && user?.perfil !== PERFIS.ADMIN) {
    return <AccessDeniedPage />
  }

  return (
    <EmpresaProvider>
      <CompanyResolvedRoutes />
    </EmpresaProvider>
  )
}
