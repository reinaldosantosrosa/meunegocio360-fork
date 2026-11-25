import { useEffect } from 'react'
import { Card } from 'react-bootstrap'
import { useLocation } from 'react-router'
import { useAuth } from '../../auth/index.js'

function HomePage() {
  const { user } = useAuth()
  const location = useLocation()

  useEffect(() => {
    document.title = 'Início | MeuNegocio360'
  }, [])

  return (
    <main className="container app-page py-4 py-md-5">
      <h1 className="h2 mb-4">Início</h1>
      {location.state?.successMessage && (
        <div className="alert alert-success" role="status">
          {location.state.successMessage}
        </div>
      )}
      <Card className="shadow-sm">
        <Card.Body className="p-4">
          <h2 className="h3">Olá, {user?.nome || user?.email}</h2>
          <p className="mb-1">
            A empresa e seus parâmetros estão prontos para uso.
          </p>
        </Card.Body>
      </Card>
    </main>
  )
}

export default HomePage
