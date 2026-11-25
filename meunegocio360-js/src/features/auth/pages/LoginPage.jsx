import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation } from '@tanstack/react-query'
import { useEffect, useState } from 'react'
import { Alert, Button, Card, Form, Spinner } from 'react-bootstrap'
import { useForm } from 'react-hook-form'
import { Navigate, useLocation, useNavigate } from 'react-router'
import brandLogo from '../../../assets/logo_meunegocio360_v2_sem_fundo.png'
import { ApiError } from '../../../shared/lib/http.js'
import { login } from '../api/authApi.js'
import { useAuth } from '../hooks/useAuth.js'
import { loginSchema } from '../schemas.js'

function currentTime() {
  return Date.now()
}

function getSafeReturnPath(from) {
  const pathname = typeof from === 'string' ? from : from?.pathname
  if (
    !pathname?.startsWith('/') ||
    pathname.startsWith('//') ||
    pathname.includes('\\')
  ) {
    return '/'
  }

  if (typeof from === 'object' && from !== null) {
    return {
      pathname,
      search: from.search || '',
      hash: from.hash || '',
    }
  }

  return pathname
}

function LoginPage() {
  const { status, acceptSession } = useAuth()
  const location = useLocation()
  const navigate = useNavigate()
  const [alert, setAlert] = useState(() => {
    if (location.state?.logoutError) {
      return { type: 'logout' }
    }
    if (location.state?.sessionExpired) {
      return { type: 'expired' }
    }
    return null
  })
  const [lockedUntil, setLockedUntil] = useState(null)
  const [now, setNow] = useState(0)
  const {
    register,
    handleSubmit,
    reset,
    resetField,
    setError,
    setFocus,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(loginSchema),
    mode: 'onSubmit',
    reValidateMode: 'onChange',
  })
  const mutation = useMutation({ mutationFn: login, gcTime: 0 })
  const lockSeconds = lockedUntil
    ? Math.max(0, Math.ceil((lockedUntil - now) / 1000))
    : 0
  const returnPath = getSafeReturnPath(location.state?.from)

  useEffect(() => {
    document.title = 'Entrar | MeuNegocio360'
  }, [])

  useEffect(() => {
    if (!lockedUntil) {
      return undefined
    }

    const timerId = window.setInterval(() => {
      const currentTime = Date.now()
      setNow(currentTime)
      if (currentTime >= lockedUntil) {
        setLockedUntil(null)
        setAlert(null)
      }
    }, 1000)

    return () => window.clearInterval(timerId)
  }, [lockedUntil])

  if (status === 'authenticated') {
    return <Navigate to="/" replace />
  }

  const onSubmit = async (values) => {
    setAlert(null)
    try {
      const session = await mutation.mutateAsync(values)
      mutation.reset()
      acceptSession(session)
      reset()
      navigate(returnPath, { replace: true })
    } catch (error) {
      if (!(error instanceof ApiError)) {
        setAlert({ type: 'network' })
        return
      }

      if (error.status === 401 && error.code === 'INVALID_CREDENTIALS') {
        setAlert({ type: 'invalid' })
        resetField('senha')
        setFocus('senha')
        return
      }

      if (error.status === 429 && error.code === 'ACCOUNT_TEMPORARILY_LOCKED') {
        const seconds = error.retryAfterSeconds ?? 60
        setLockedUntil(currentTime() + seconds * 1000)
        setNow(currentTime())
        setAlert({ type: 'blocked' })
        return
      }

      if (error.status === 400 && error.code === 'VALIDATION_ERROR') {
        const fieldMap = { email: 'email', senha: 'senha' }
        let firstField = null

        for (const fieldError of error.errors) {
          const field = fieldMap[fieldError.field]
          if (field && typeof fieldError.message === 'string') {
            setError(field, { type: 'server', message: fieldError.message })
            firstField ||= field
          }
        }

        if (firstField) {
          setFocus(firstField)
        } else {
          setAlert({ type: 'validation' })
        }
        return
      }

      setAlert({ type: 'network' })
    }
  }

  const retryLogin = handleSubmit(onSubmit)
  const emailRegistration = register('email')
  const passwordRegistration = register('senha')

  return (
    <main className="container auth-shell d-flex align-items-center justify-content-center py-4">
      <Card className="auth-card shadow-sm">
        <Card.Body className="p-4 p-md-5">
          <div className="text-center mb-4">
            <img
              className="auth-brand-logo mb-3"
              src={brandLogo}
              alt="MeuNegocio360"
            />
            <h1 className="h2 mb-1">Entrar</h1>
            <p className="text-body-secondary mb-0">
              Acesse sua conta MeuNegocio360
            </p>
          </div>

          {alert && (
            <Alert
              variant={
                alert.type === 'network' || alert.type === 'logout'
                  ? 'warning'
                  : 'danger'
              }
              role="alert"
            >
              {alert.type === 'invalid' && 'E-mail ou senha inválidos.'}
              {alert.type === 'blocked' &&
                `Muitas tentativas. Tente novamente em ${Math.ceil(lockSeconds / 60)} ${
                  Math.ceil(lockSeconds / 60) === 1 ? 'minuto' : 'minutos'
                }.`}
              {alert.type === 'network' && (
                <>
                  <p className="mb-2">
                    Não foi possível entrar agora. Verifique sua conexão e tente
                    novamente.
                  </p>
                  <Button
                    type="button"
                    variant="outline-dark"
                    onClick={retryLogin}
                    disabled={mutation.isPending || lockSeconds > 0}
                  >
                    Tentar novamente
                  </Button>
                </>
              )}
              {alert.type === 'validation' &&
                'Revise os dados informados e tente novamente.'}
              {alert.type === 'expired' &&
                'Sua sessão expirou. Entre novamente.'}
              {alert.type === 'logout' &&
                'Não foi possível confirmar o encerramento da sessão no servidor. Entre novamente.'}
            </Alert>
          )}

          <Form noValidate onSubmit={retryLogin}>
            <Form.Group className="mb-3" controlId="login-email">
              <Form.Label>E-mail</Form.Label>
              <Form.Control
                {...emailRegistration}
                type="email"
                autoComplete="username"
                inputMode="email"
                isInvalid={Boolean(errors.email)}
                aria-invalid={Boolean(errors.email)}
                aria-describedby={
                  errors.email ? 'login-email-error' : undefined
                }
                autoFocus
              />
              {errors.email && (
                <Form.Control.Feedback
                  id="login-email-error"
                  type="invalid"
                  role="alert"
                >
                  {errors.email.message}
                </Form.Control.Feedback>
              )}
            </Form.Group>

            <Form.Group className="mb-4" controlId="login-password">
              <Form.Label>Senha</Form.Label>
              <Form.Control
                {...passwordRegistration}
                type="password"
                autoComplete="current-password"
                isInvalid={Boolean(errors.senha)}
                aria-invalid={Boolean(errors.senha)}
                aria-describedby={
                  errors.senha ? 'login-password-error' : undefined
                }
              />
              {errors.senha && (
                <Form.Control.Feedback
                  id="login-password-error"
                  type="invalid"
                  role="alert"
                >
                  {errors.senha.message}
                </Form.Control.Feedback>
              )}
            </Form.Group>

            <Button
              type="submit"
              variant="primary"
              className="w-100"
              disabled={mutation.isPending || lockSeconds > 0}
            >
              {mutation.isPending ? (
                <>
                  <Spinner size="sm" animation="border" aria-hidden="true" />{' '}
                  Entrando...
                </>
              ) : (
                'Entrar'
              )}
            </Button>
          </Form>
        </Card.Body>
      </Card>
    </main>
  )
}

export default LoginPage
