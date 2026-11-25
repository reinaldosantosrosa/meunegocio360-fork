import { Form } from 'react-bootstrap'
import { formatarDocumento } from '../lib/documento.js'

export function DocumentoInput({
  tipoPessoa,
  value,
  onChange,
  name,
  onBlur,
  inputRef,
  invalid = false,
  id,
  errorId,
}) {
  const fisica = tipoPessoa === 'FISICA'

  return (
    <Form.Control
      id={id}
      name={name}
      type="text"
      inputMode="numeric"
      autoComplete="off"
      maxLength={fisica ? 14 : 18}
      placeholder={fisica ? '000.000.000-00' : '00.000.000/0000-00'}
      value={formatarDocumento(tipoPessoa, value)}
      onChange={(event) =>
        onChange(formatarDocumento(tipoPessoa, event.target.value))
      }
      onBlur={onBlur}
      ref={inputRef}
      isInvalid={invalid}
      aria-invalid={invalid}
      aria-describedby={invalid ? errorId : undefined}
    />
  )
}
