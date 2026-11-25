import { useEffect, useState } from 'react'
import { Button, Form } from 'react-bootstrap'

export function ListToolbar({
  query,
  onQueryChange,
  situation,
  onSituationChange,
  onClear,
  hasFilters,
}) {
  const [searchText, setSearchText] = useState(query)

  useEffect(() => {
    const normalized = searchText.trim()
    if (normalized === query) return undefined

    const timeout = window.setTimeout(() => onQueryChange(normalized), 400)
    return () => window.clearTimeout(timeout)
  }, [onQueryChange, query, searchText])

  return (
    <div className="row g-3 align-items-end mb-3">
      <Form.Group className="col-12 col-lg-6" controlId="clientes-busca">
        <Form.Label>Buscar por nome ou documento</Form.Label>
        <Form.Control
          type="search"
          value={searchText}
          onChange={(event) => setSearchText(event.target.value)}
          autoComplete="off"
        />
      </Form.Group>
      <Form.Group
        className="col-12 col-sm-6 col-lg-3"
        controlId="clientes-situacao"
      >
        <Form.Label>Situação</Form.Label>
        <Form.Select
          value={situation}
          onChange={(event) => onSituationChange(event.target.value)}
        >
          <option value="ATIVO">Ativos</option>
          <option value="INATIVO">Inativos</option>
          <option value="TODOS">Todos</option>
        </Form.Select>
      </Form.Group>
      {hasFilters && (
        <div className="col-12 col-sm-6 col-lg-3">
          <Button
            type="button"
            variant="outline-dark"
            className="min-touch-target"
            onClick={onClear}
          >
            Limpar filtros
          </Button>
        </div>
      )}
    </div>
  )
}
