import { useCallback } from 'react'
import { useSearchParams } from 'react-router'

const SITUACOES_VALIDAS = new Set(['ATIVO', 'INATIVO', 'TODOS'])

function lerPagina(value) {
  const page = Number(value)
  return Number.isSafeInteger(page) && page >= 0 ? page : 0
}

export function useUrlFilters() {
  const [searchParams, setSearchParams] = useSearchParams()
  const q = searchParams.get('q') ?? ''
  const situacaoParam = searchParams.get('situacao')
  const situacao = SITUACOES_VALIDAS.has(situacaoParam)
    ? situacaoParam
    : 'ATIVO'
  const page = lerPagina(searchParams.get('page'))

  const setFilters = useCallback(
    (updates) => {
      setSearchParams(
        (current) => {
          const next = new URLSearchParams(current)
          const currentSituation = current.get('situacao')
          next.set('q', updates.q ?? current.get('q') ?? '')
          next.set(
            'situacao',
            updates.situacao ??
              (SITUACOES_VALIDAS.has(currentSituation)
                ? currentSituation
                : 'ATIVO'),
          )
          next.set(
            'page',
            String(updates.page ?? lerPagina(current.get('page'))),
          )
          return next
        },
        { replace: true },
      )
    },
    [setSearchParams],
  )

  const clearFilters = useCallback(() => {
    setFilters({ q: '', situacao: 'ATIVO', page: 0 })
  }, [setFilters])

  return { q, situacao, page, setFilters, clearFilters }
}
