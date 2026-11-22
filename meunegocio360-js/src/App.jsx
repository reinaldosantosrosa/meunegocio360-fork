import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { lazy, Suspense } from 'react'
import { BrowserRouter, Route, Routes } from 'react-router'
import { AuthProvider, RequireAuth, useAuth } from './features/auth'
import { LoadingPage } from './shared/ui/LoadingPage.jsx'

const LoginPage = lazy(() => import('./features/auth/pages/LoginPage.jsx'))
const HomePage = lazy(() => import('./features/home/pages/HomePage.jsx'))
const NotFoundPage = lazy(() => import('./shared/ui/NotFoundPage.jsx'))

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: false,
      refetchOnWindowFocus: false,
    },
  },
})

function AuthenticatedRoutes() {
  const { status, retryRestore } = useAuth()

  if (status === 'loading') {
    return <LoadingPage message="Verificando sua sessão..." />
  }

  if (status === 'unavailable') {
    return (
      <main className="container auth-shell d-flex align-items-center justify-content-center py-4">
        <section
          className="card auth-card shadow-sm"
          aria-labelledby="restore-error-title"
        >
          <div className="card-body p-4 p-md-5 text-center">
            <h1 id="restore-error-title" className="h2 mb-3">
              Não foi possível conectar
            </h1>
            <p className="mb-4">
              Não foi possível verificar sua sessão. Tente novamente.
            </p>
            <button
              type="button"
              className="btn btn-primary w-100"
              onClick={retryRestore}
            >
              Tentar novamente
            </button>
          </div>
        </section>
      </main>
    )
  }

  return (
    <Suspense fallback={<LoadingPage message="Carregando..." />}>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route
          path="/"
          element={
            <RequireAuth>
              <HomePage />
            </RequireAuth>
          }
        />
        <Route path="*" element={<NotFoundPage />} />
      </Routes>
    </Suspense>
  )
}

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <AuthProvider>
          <AuthenticatedRoutes />
        </AuthProvider>
      </BrowserRouter>
    </QueryClientProvider>
  )
}

export default App
