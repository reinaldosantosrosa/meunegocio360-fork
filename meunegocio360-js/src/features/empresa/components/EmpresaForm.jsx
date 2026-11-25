import { zodResolver } from '@hookform/resolvers/zod'
import { useEffect, useState } from 'react'
import { Button, Form } from 'react-bootstrap'
import { Controller, useForm } from 'react-hook-form'
import { ApiError } from '../../../shared/lib/http.js'
import {
  FUSOS_HORARIOS,
  formatarCnpj,
  formatarTelefone,
} from '../../../shared/lib/empresaFormat.js'
import { ErrorAlert } from '../../../shared/ui/ErrorAlert.jsx'
import { empresaSchema } from '../schemas.js'

function valoresIniciais(empresa) {
  return {
    razaoSocial: empresa?.razaoSocial ?? '',
    nomeFantasia: empresa?.nomeFantasia ?? '',
    cnpj: formatarCnpj(empresa?.cnpj ?? ''),
    email: empresa?.email ?? '',
    telefone: formatarTelefone(empresa?.telefone ?? ''),
    fusoHorario: empresa?.fusoHorario ?? 'America/Sao_Paulo',
    moeda: empresa?.moeda ?? 'BRL',
  }
}

function fieldError(error, id) {
  return error
    ? {
        isInvalid: true,
        'aria-invalid': true,
        'aria-describedby': `${id}-error`,
      }
    : { 'aria-invalid': false }
}

function applyServerErrors(error, setError, setFocus) {
  if (error.code === 'COMPANY_CNPJ_DUPLICATE') {
    setError('cnpj', {
      type: 'server',
      message: 'Já existe uma empresa cadastrada com este CNPJ.',
    })
    setFocus('cnpj')
    return null
  }

  if (
    error.status === 400 &&
    error.code === 'VALIDATION_ERROR' &&
    Array.isArray(error.errors)
  ) {
    const fields = new Set([
      'razaoSocial',
      'nomeFantasia',
      'cnpj',
      'email',
      'telefone',
      'fusoHorario',
      'moeda',
    ])
    let firstField = null
    for (const fieldErrorResponse of error.errors) {
      if (
        fields.has(fieldErrorResponse.field) &&
        typeof fieldErrorResponse.message === 'string'
      ) {
        setError(fieldErrorResponse.field, {
          type: 'server',
          message: fieldErrorResponse.message,
        })
        firstField ||= fieldErrorResponse.field
      }
    }
    if (firstField) {
      setFocus(firstField)
      return null
    }
  }

  if (error.status === 409 && error.code === 'COMPANY_ALREADY_ACTIVE') {
    return 'Já existe uma empresa ativa. Inative-a antes de cadastrar ou reativar outra.'
  }
  if (error.status === 409 && error.code === 'VERSION_CONFLICT') {
    return 'Os dados da empresa foram alterados. Recarregue os dados antes de salvar novamente.'
  }
  if (error.status === 403 && error.code === 'ACCESS_DENIED') {
    return 'Você não tem permissão para alterar os dados da empresa.'
  }
  return 'Não foi possível salvar os dados da empresa. Verifique sua conexão e tente novamente.'
}

export function EmpresaForm({
  empresa = null,
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
    control,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(empresaSchema),
    defaultValues: valoresIniciais(empresa),
  })
  const razaoError = errors.razaoSocial
  const fantasiaError = errors.nomeFantasia
  const cnpjError = errors.cnpj
  const emailError = errors.email
  const telefoneError = errors.telefone
  const fusoError = errors.fusoHorario
  const [alertMessage, setAlertMessage] = useState(null)
  const [canRetry, setCanRetry] = useState(false)
  const [needsReload, setNeedsReload] = useState(false)

  useEffect(() => {
    reset(valoresIniciais(empresa))
  }, [empresa, reset])

  async function submit(values) {
    setAlertMessage(null)
    setCanRetry(false)
    setNeedsReload(false)
    try {
      await onSave({
        ...values,
        ...(empresa ? { versao: empresa.versao } : {}),
      })
    } catch (error) {
      if (!(error instanceof ApiError)) {
        throw error
      }
      setAlertMessage(applyServerErrors(error, setError, setFocus))
      setCanRetry(error.status === 0 || error.status >= 500)
      setNeedsReload(error.code === 'VERSION_CONFLICT')
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
      {alertMessage && (
        <ErrorAlert
          onRetry={canRetry ? retrySubmit : undefined}
          onReload={
            needsReload
              ? async () => {
                  setAlertMessage(null)
                  await onReload?.()
                }
              : undefined
          }
        >
          {alertMessage}
        </ErrorAlert>
      )}

      <Form.Group className="mb-3" controlId="empresa-razao-social">
        <Form.Label>Razão social</Form.Label>
        <Form.Control
          type="text"
          autoComplete="organization"
          maxLength={150}
          {...register('razaoSocial')}
          {...fieldError(razaoError, 'empresa-razao-social')}
        />
        {razaoError && (
          <Form.Control.Feedback
            id="empresa-razao-social-error"
            type="invalid"
            role="alert"
          >
            {razaoError.message}
          </Form.Control.Feedback>
        )}
      </Form.Group>

      <Form.Group className="mb-3" controlId="empresa-nome-fantasia">
        <Form.Label>Nome fantasia (opcional)</Form.Label>
        <Form.Control
          type="text"
          autoComplete="organization"
          maxLength={150}
          {...register('nomeFantasia')}
          {...fieldError(fantasiaError, 'empresa-nome-fantasia')}
        />
        {fantasiaError && (
          <Form.Control.Feedback
            id="empresa-nome-fantasia-error"
            type="invalid"
            role="alert"
          >
            {fantasiaError.message}
          </Form.Control.Feedback>
        )}
      </Form.Group>

      <Form.Group className="mb-3" controlId="empresa-cnpj">
        <Form.Label>CNPJ</Form.Label>
        <Controller
          name="cnpj"
          control={control}
          render={({ field }) => (
            <Form.Control
              {...field}
              type="text"
              inputMode="numeric"
              autoComplete="off"
              maxLength={18}
              placeholder="00.000.000/0000-00"
              value={formatarCnpj(field.value)}
              onChange={(event) =>
                field.onChange(formatarCnpj(event.target.value))
              }
              {...fieldError(cnpjError, 'empresa-cnpj')}
            />
          )}
        />
        {cnpjError && (
          <Form.Control.Feedback
            id="empresa-cnpj-error"
            type="invalid"
            role="alert"
          >
            {cnpjError.message}
          </Form.Control.Feedback>
        )}
      </Form.Group>

      <Form.Group className="mb-3" controlId="empresa-email">
        <Form.Label>E-mail (opcional)</Form.Label>
        <Form.Control
          type="email"
          autoComplete="email"
          maxLength={160}
          {...register('email')}
          {...fieldError(emailError, 'empresa-email')}
        />
        {emailError && (
          <Form.Control.Feedback
            id="empresa-email-error"
            type="invalid"
            role="alert"
          >
            {emailError.message}
          </Form.Control.Feedback>
        )}
      </Form.Group>

      <Form.Group className="mb-3" controlId="empresa-telefone">
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
              {...fieldError(telefoneError, 'empresa-telefone')}
            />
          )}
        />
        {telefoneError && (
          <Form.Control.Feedback
            id="empresa-telefone-error"
            type="invalid"
            role="alert"
          >
            {telefoneError.message}
          </Form.Control.Feedback>
        )}
      </Form.Group>

      <div className="row">
        <Form.Group className="col-md-8 mb-3" controlId="empresa-fuso">
          <Form.Label>Fuso horário</Form.Label>
          <Form.Select
            {...register('fusoHorario')}
            {...fieldError(fusoError, 'empresa-fuso')}
          >
            {FUSOS_HORARIOS.map((fuso) => (
              <option key={fuso.value} value={fuso.value}>
                {fuso.label}
              </option>
            ))}
          </Form.Select>
          {fusoError && (
            <Form.Control.Feedback
              id="empresa-fuso-error"
              type="invalid"
              role="alert"
            >
              {fusoError.message}
            </Form.Control.Feedback>
          )}
        </Form.Group>

        <Form.Group className="col-md-4 mb-3" controlId="empresa-moeda">
          <Form.Label>Moeda</Form.Label>
          <input type="hidden" {...register('moeda')} />
          <Form.Select value="BRL" disabled>
            <option value="BRL">BRL — Real brasileiro</option>
          </Form.Select>
        </Form.Group>
      </div>

      <div className="d-flex flex-wrap gap-2 mt-4">
        <Button
          type="submit"
          variant="primary"
          className="min-touch-target"
          disabled={isPending}
        >
          {isPending
            ? 'Salvando...'
            : empresa
              ? 'Salvar alterações'
              : 'Cadastrar empresa'}
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
