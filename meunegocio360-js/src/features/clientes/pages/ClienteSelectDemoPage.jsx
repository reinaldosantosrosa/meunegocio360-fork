import { useState } from 'react'
import { ClienteSelect } from '../components/ClienteSelect.jsx'

export default function ClienteSelectDemoPage() {
  const [cliente, setCliente] = useState(null)

  return (
    <main className="container app-page py-4 py-md-5">
      <section className="app-card card shadow-sm">
        <div className="card-body p-4 p-md-5">
          <h1 className="h2 mb-4">Demonstração do seletor de clientes</h1>
          <ClienteSelect
            label="Cliente"
            value={cliente}
            onChange={setCliente}
          />
          {cliente && (
            <p className="mt-3" role="status">
              Cliente selecionado: {cliente.nome}
            </p>
          )}
        </div>
      </section>
    </main>
  )
}
