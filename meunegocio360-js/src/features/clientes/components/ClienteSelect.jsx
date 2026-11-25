import { useEffect, useId, useRef, useState } from 'react'
import { Alert, Button, Form } from 'react-bootstrap'
import { formatarDocumento } from '../../../shared/lib/documento.js'
import { useBuscaClientes } from '../hooks/useBuscaClientes.js'

export function ClienteSelect({
  value,
  onChange,
  invalid = false,
  disabled = false,
  label,
}) {
  const id = useId()
  const skipValueSync = useRef(false)
  const [searchText, setSearchText] = useState(value?.nome ?? '')
  const [debouncedSearch, setDebouncedSearch] = useState('')
  const [isOpen, setIsOpen] = useState(false)
  const [activeIndex, setActiveIndex] = useState(-1)

  useEffect(() => {
    if (skipValueSync.current) {
      skipValueSync.current = false
      return
    }
    setSearchText(value?.nome ?? '')
  }, [value?.id, value?.nome])

  useEffect(() => {
    const timeout = window.setTimeout(
      () => setDebouncedSearch(searchText.trim()),
      400,
    )
    return () => window.clearTimeout(timeout)
  }, [searchText])

  const query = useBuscaClientes({
    q: debouncedSearch,
    situacao: 'ATIVO',
    page: 0,
    size: 10,
    enabled: isOpen && debouncedSearch.length >= 2,
  })
  const suggestions = query.data?.content ?? []
  const normalizedSearch = searchText.trim()
  const searchSettled = normalizedSearch === debouncedSearch
  const visibleSuggestions =
    isOpen && searchSettled && debouncedSearch.length >= 2 ? suggestions : []
  const activeOptionIndex =
    activeIndex >= 0 && activeIndex < visibleSuggestions.length
      ? activeIndex
      : -1
  const listboxId = `${id}-clientes-listbox`
  const activeOptionId =
    activeOptionIndex >= 0
      ? `${id}-cliente-option-${activeOptionIndex}`
      : undefined

  function choose(cliente) {
    onChange({
      id: cliente.id,
      nome: cliente.nome,
      documento: cliente.documento,
    })
    setSearchText(cliente.nome)
    setActiveIndex(-1)
    setIsOpen(false)
  }

  function handleKeyDown(event) {
    if (event.key === 'ArrowDown') {
      event.preventDefault()
      setIsOpen(true)
      setActiveIndex((current) =>
        visibleSuggestions.length
          ? Math.min(current + 1, visibleSuggestions.length - 1)
          : -1,
      )
    } else if (event.key === 'ArrowUp') {
      event.preventDefault()
      setIsOpen(true)
      setActiveIndex(
        visibleSuggestions.length
          ? activeOptionIndex <= 0
            ? visibleSuggestions.length - 1
            : activeOptionIndex - 1
          : -1,
      )
    } else if (event.key === 'Enter' && isOpen && visibleSuggestions.length) {
      event.preventDefault()
      choose(visibleSuggestions[activeOptionIndex < 0 ? 0 : activeOptionIndex])
    } else if (event.key === 'Escape') {
      setIsOpen(false)
      setActiveIndex(-1)
    }
  }

  return (
    <div className="position-relative">
      <Form.Label htmlFor={`${id}-input`}>{label}</Form.Label>
      <Form.Control
        id={`${id}-input`}
        type="text"
        role="combobox"
        autoComplete="off"
        value={searchText}
        disabled={disabled}
        isInvalid={invalid}
        aria-invalid={invalid}
        aria-autocomplete="list"
        aria-haspopup="listbox"
        aria-expanded={isOpen}
        aria-controls={isOpen ? listboxId : undefined}
        aria-activedescendant={isOpen ? activeOptionId : undefined}
        onFocus={() => {
          if (searchText.trim().length >= 2) setIsOpen(true)
        }}
        onChange={(event) => {
          const nextText = event.target.value
          setSearchText(nextText)
          setIsOpen(true)
          setActiveIndex(-1)
          if (value?.id) {
            skipValueSync.current = true
            onChange(null)
          }
        }}
        onKeyDown={handleKeyDown}
        onBlur={(event) => {
          if (
            !event.currentTarget.parentElement?.contains(event.relatedTarget)
          ) {
            setIsOpen(false)
            setActiveIndex(-1)
          }
        }}
      />

      {isOpen && (
        <div className="position-absolute start-0 end-0 top-100 z-3 mt-1 rounded border bg-body shadow">
          {normalizedSearch.length < 2 && (
            <p className="mb-0 p-2" role="status" aria-live="polite">
              Digite ao menos 2 caracteres.
            </p>
          )}
          {normalizedSearch.length >= 2 &&
            (!searchSettled || query.isPending) && (
              <p className="mb-0 p-2" role="status" aria-live="polite">
                Buscando…
              </p>
            )}
          {normalizedSearch.length >= 2 && searchSettled && query.isError && (
            <Alert variant="danger" className="m-2" role="alert">
              <span>Erro ao buscar.</span>{' '}
              <Button
                type="button"
                variant="link"
                className="p-0 min-touch-target"
                onMouseDown={(event) => event.preventDefault()}
                onClick={() => query.refetch()}
              >
                Tentar novamente
              </Button>
            </Alert>
          )}
          {normalizedSearch.length >= 2 &&
            searchSettled &&
            query.isSuccess &&
            suggestions.length === 0 && (
              <p className="mb-0 p-2" role="status" aria-live="polite">
                Nenhum cliente encontrado.
              </p>
            )}
          <ul
            id={listboxId}
            role="listbox"
            aria-label="Sugestões de clientes ativos"
            className="list-group list-group-flush"
          >
            {visibleSuggestions.map((cliente, index) => (
              <li
                key={cliente.id}
                id={`${id}-cliente-option-${index}`}
                role="option"
                aria-selected={value?.id === cliente.id}
                className={`list-group-item list-group-item-action ${
                  index === activeOptionIndex ? 'active' : ''
                }`}
                onMouseDown={(event) => event.preventDefault()}
                onClick={() => choose(cliente)}
              >
                <span className="d-block">{cliente.nome}</span>
                <small>
                  {formatarDocumento(cliente.tipoPessoa, cliente.documento)}
                </small>
              </li>
            ))}
          </ul>
        </div>
      )}
    </div>
  )
}
