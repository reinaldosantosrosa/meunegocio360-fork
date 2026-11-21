import { useEffect, useState } from 'react'
import { Button, Card } from 'react-bootstrap'
import { useNavigate } from 'react-router'
import { useAuth } from '../../auth/index.js'

function HomePage() {
  const { user, signOut } = useAuth()
  const navigate = useNavigate()
  const [isSigningOut, setIsSigningOut] = useState(false)

  useEffect(() => {
    document.title = 'Início | MeuNegocio360'
  }, [])

  const handleSignOut = async () => {
    setIsSigningOut(true)
    try {
      await signOut()
      navigate('/login', { replace: true })
    } catch {
      navigate('/login', { replace: true, state: { logoutError: true } })
    }
  }

  return (
    <main className="container py-4 py-md-5">
      <header className="d-flex flex-column flex-sm-row align-items-sm-center justify-content-between gap-3 mb-4">
        <h1 className="h2 mb-0">MeuNegocio360</h1>
        <Button
          type="button"
          variant="outline-dark"
          onClick={handleSignOut}
          disabled={isSigningOut}
        >
          {isSigningOut ? 'Saindo...' : 'Sair'}
        </Button>
      </header>

      <Card className="shadow-sm">
        <Card.Body className="p-4">
          <h2 className="h3">Olá, {user.nome}</h2>
          <p className="mb-1">
            <strong>Perfil:</strong> {user.perfil}
          </p>
          <p className="mb-0">
            <strong>E-mail:</strong> {user.email}
          </p>
        </Card.Body>
      </Card>
    </main>
  )
}

export default HomePage
