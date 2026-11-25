import { zodResolver } from '@hookform/resolvers/zod'
import { useEffect, useState } from 'react'
import { Button, Form } from 'react-bootstrap'
import { Controller, useForm, useWatch } from 'react-hook-form'
import { Link } from 'react-router'
import { ApiError } from '../../../shared/lib/http.js'
import { formatarDocumento } from '../../../shared/lib/documento.js'
import { formatarTelefone } from '../../../shared/lib/empresaFormat.js'
import { DocumentoInput } from '../../../shared/ui/DocumentoInput.jsx'
import { ErrorAlert } from '../../../shared/ui/ErrorAlert.jsx'
import { clienteSchema } from '../schemas.js'

function valoresIniciais(cliente) {
  return {
    tipoPessoa: cliente?.tipoPessoa ?? 'FISICA',
    nome: cliente?.nome ?? '',
    documento: formatarDocumento(
      cliente?.tipoPessoa ?? 'FISICA',
      cliente?.documento ?? '',
    ),
    email: cliente?.email ?? '',
    telefone: formatarTelefone(cliente?.telefone ?? ''),
  }
}

function fieldProps(error, id) {
  return {
    isInvalid: Boolean(error),
    'aria-invalid': Boolean(error),
    'aria-describedby': error ? `${id}-error` : undefined,
  }
}

function applyServerErrors(error, setError, setFocus) {
  if (error.status === 409 && error.code === 'CUSTOMER_DOCUMENT_DUPLICATE') {
    setError('documento', {
      type: 'server',
      message:
        'Já existe um cliente com este documento. Ele pode estar inativo; busque por ele e reative.',
    })
    setFocus('documento')
    return null
  }

  if (
    error.status === 400 &&
    error.code === 'VALIDATION_ERROR' &&
    Array.isArray(error.errors)
  ) {
    const fields = new Set([
      'tipoPessoa',
      'nome',
      'documento',
      'email',
      'telefone',
    ])
    let firstField = null
    for (const fieldError of error.errors) {
      if (
        fields.has(fieldError.field) &&
        typeof fieldError.message === 'string'
      ) {
        setError(fieldError.field, {
          type: 'server',
          message: fieldError.message,
        })
        firstField ||= fieldError.field
      }
    }
    if (firstField) {
      setFocus(firstField)
      return null
    }
  }

  if (error.status === 409 && error.code === 'VERSION_CONFLICT') {
    return {
      message:
        'Os dados do cliente foram alterados. Recarregue os dados antes de salvar novamente.',
      reload: true,
    }
  }
  if (error.status === 422 && error.code === 'COMPANY_NOT_ACTIVE') {
    return {
      message: 'Cadastre ou ative a empresa para continuar.',
      companyInactive: true,
    }
  }
  if (error.status === 403 || error.code === 'ACCESS_DENIED') {
    return {
      message: 'Você não tem permissão para acessar esta funcionalidade.',
    }
  }
  return {
    message:
      'Não foi possível salvar o cliente. Verifique sua conexão e tente novamente.',
    retryable: error.status === 0 || error.status >= 500,
  }
}

export function ClienteForm({
  cliente = null,
  onSave,
  isPending = false,
  onCancel,
  onReload,
}) {
  const {
    register,
    handleSubmit,
    reset,
    setError,
    setFocus,
    setValue,
    clearErrors,
    trigger,
    control,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(clienteSchema),
    defaultValues: valoresIniciais(cliente),
  })
  const tipoPessoa = useWatch({ control, name: 'tipoPessoa' })
  const documentoValue = useWatch({ control, name: 'documento' })
  const tipoError = errors.tipoPessoa
  const nomeError = errors.nome
  const documentoError = errors.documento
  const emailError = errors.email
  const telefoneError = errors.telefone
  const [documentWarning, setDocumentWarning] = useState('')
  const [serverError, setServerError] = useState(null)

  useEffect(() => {
    reset(valoresIniciais(cliente))
  }, [cliente, reset])

  async function changeTipoPessoa(event, onChange) {
    const nextTipo = event.target.value
    const documentDigits = String(documentoValue ?? '').replace(/\D/g, '')
    const requiredLength = nextTipo === 'FISICA' ? 11 : 14
    onChange(event)
    clearErrors('documento')

    if (documentDigits && documentDigits.length !== requiredLength) {
      setValue('documento', '', {
        shouldDirty: true,
        shouldTouch: true,
        shouldValidate: true,
      })
      setDocumentWarning(
        `Documento removido. Informe um ${nextTipo === 'FISICA' ? 'CPF' : 'CNPJ'} para o novo tipo de pessoa.`,
      )
      return
    }
    setDocumentWarning('')
    if (documentDigits) await trigger('documento')
  }

  async function submit(values) {
    setServerError(null)
    setDocumentWarning('')
    try {
      await onSave({
        ...values,
        ...(cliente ? { versao: cliente.versao } : {}),
      })
    } catch (error) {
      if (!(error instanceof ApiError)) throw error
      setServerError(applyServerErrors(error, setError, setFocus))
    }
  }

  const retrySubmit = handleSubmit(submit)

  return (
    <Form
      noValidate
      onSubmit={(event) => {
        if (isPending) {
          event.preventDefault()
          return
        }
        retrySubmit(event)
      }}
    >
      {serverError?.message && (
        <ErrorAlert
          onRetry={serverError.retryable ? retrySubmit : undefined}
          onReload={
            serverError.reload
              ? async () => {
                  setServerError(null)
                  await onReload?.()
                }
              : undefined
          }
        >
          {serverError.message}
          {serverError.companyInactive && (
            <>
              {' '}
              <Link to="/empresa">Acessar cadastro da empresa</Link>
            </>
          )}
        </ErrorAlert>
      )}

      <Controller
        name="tipoPessoa"
        control={control}
        render={({ field }) => (
          <fieldset
            className="mb-3"
            aria-describedby={tipoError ? 'cliente-tipo-error' : undefined}
          >
            <legend className="fs-6">Tipo de pessoa</legend>
            <div className="d-flex flex-wrap gap-3">
              <Form.Check
                type="radio"
                id="cliente-tipo-fisica"
                label="Pessoa física"
                value="FISICA"
                name={field.name}
                checked={field.value === 'FISICA'}
                onBlur={field.onBlur}
                onChange={(event) => changeTipoPessoa(event, field.onChange)}
                inputRef={field.ref}
                isInvalid={Boolean(tipoError)}
                aria-invalid={Boolean(tipoError)}
              />
              <Form.Check
                type="radio"
                id="cliente-tipo-juridica"
                label="Pessoa jurídica"
                value="JURIDICA"
                name={field.name}
                checked={field.value === 'JURIDICA'}
                onBlur={field.onBlur}
                onChange={(event) => changeTipoPessoa(event, field.onChange)}
                isInvalid={Boolean(tipoError)}
                aria-invalid={Boolean(tipoError)}
              />
            </div>
            {tipoError && (
              <div
                id="cliente-tipo-error"
                className="invalid-feedback d-block"
                role="alert"
              >
                {tipoError.message}
              </div>
            )}
          </fieldset>
        )}
      />

      <Form.Group className="mb-3" controlId="cliente-nome">
        <Form.Label>Nome ou razão social</Form.Label>
        <Form.Control
          type="text"
          autoComplete="name"
          maxLength={150}
          {...register('nome')}
          {...fieldProps(nomeError, 'cliente-nome')}
        />
        {nomeError && (
          <Form.Control.Feedback
            id="cliente-nome-error"
            type="invalid"
            role="alert"
          >
            {nomeError.message}
          </Form.Control.Feedback>
        )}
      </Form.Group>

      <Form.Group className="mb-3" controlId="cliente-documento">
        <Form.Label>{tipoPessoa === 'FISICA' ? 'CPF' : 'CNPJ'}</Form.Label>
        <Controller
          name="documento"
          control={control}
          render={({ field }) => (
            <DocumentoInput
              id="cliente-documento"
              tipoPessoa={tipoPessoa}
              value={field.value}
              name={field.name}
              onBlur={field.onBlur}
              inputRef={field.ref}
              onChange={(value) => {
                field.onChange(value)
                setDocumentWarning('')
              }}
              invalid={Boolean(documentoError)}
              errorId="cliente-documento-error"
            />
          )}
        />
        {documentWarning && (
          <div className="form-text" role="status" aria-live="polite">
            {documentWarning}
          </div>
        )}
        {documentoError && (
          <Form.Control.Feedback
            id="cliente-documento-error"
            type="invalid"
            role="alert"
          >
            {documentoError.message}
          </Form.Control.Feedback>
        )}
      </Form.Group>

      <Form.Group className="mb-3" controlId="cliente-email">
        <Form.Label>E-mail (opcional)</Form.Label>
        <Form.Control
          type="email"
          autoComplete="email"
          maxLength={160}
          {...register('email')}
          {...fieldProps(emailError, 'cliente-email')}
        />
        {emailError && (
          <Form.Control.Feedback
            id="cliente-email-error"
            type="invalid"
            role="alert"
          >
            {emailError.message}
          </Form.Control.Feedback>
        )}
      </Form.Group>

      <Form.Group className="mb-3" controlId="cliente-telefone">
        <Form.Label>Telefone (opcional)</Form.Label>
        <Controller
          name="telefone"
          control={control}
          render={({ field }) => (
            <Form.Control
              {...field}
              type="tel"
              inputMode="tel"
              autoComplete="tel"
              maxLength={15}
              placeholder="(00) 00000-0000"
              value={formatarTelefone(field.value)}
              onChange={(event) =>
                field.onChange(formatarTelefone(event.target.value))
              }
              {...fieldProps(telefoneError, 'cliente-telefone')}
            />
          )}
        />
        {telefoneError && (
          <Form.Control.Feedback
            id="cliente-telefone-error"
            type="invalid"
            role="alert"
          >
            {telefoneError.message}
          </Form.Control.Feedback>
        )}
      </Form.Group>

      <div className="d-flex flex-wrap gap-2 mt-4">
        <Button
          type="submit"
          variant="primary"
          className="min-touch-target"
          disabled={isPending}
        >
          {isPending
            ? 'Salvando...'
            : cliente
              ? 'Salvar alterações'
              : 'Cadastrar cliente'}
        </Button>
        {onCancel && (
          <Button
            type="button"
            variant="outline-dark"
            className="min-touch-target"
            onClick={onCancel}
            disabled={isPending}
          >
            Cancelar
          </Button>
        )}
      </div>
    </Form>
  )
}
