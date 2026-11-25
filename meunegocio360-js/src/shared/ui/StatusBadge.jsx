import { Badge } from 'react-bootstrap'

export function StatusBadge({ active }) {
  return (
    <Badge bg={active ? 'success' : 'secondary'}>
      {active ? 'Ativo' : 'Inativo'}
    </Badge>
  )
}
