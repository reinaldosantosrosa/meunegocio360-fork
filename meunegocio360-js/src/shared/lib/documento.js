import { formatarCnpj, somenteDigitos, validarCnpj } from './empresaFormat.js'

export function formatarCpf(value) {
  const digits = somenteDigitos(value).slice(0, 11)
  if (digits.length <= 3) return digits
  if (digits.length <= 6) return `${digits.slice(0, 3)}.${digits.slice(3)}`
  if (digits.length <= 9) {
    return `${digits.slice(0, 3)}.${digits.slice(3, 6)}.${digits.slice(6)}`
  }
  return `${digits.slice(0, 3)}.${digits.slice(3, 6)}.${digits.slice(6, 9)}-${digits.slice(9)}`
}

export function validarCpf(value) {
  const digits = String(value ?? '').replace(/\D/g, '')
  if (digits.length !== 11 || /^(\d)\1{10}$/.test(digits)) return false

  const calcularDigito = (base, pesoInicial) => {
    const soma = [...base].reduce(
      (total, digito, index) => total + Number(digito) * (pesoInicial - index),
      0,
    )
    const resto = (soma * 10) % 11
    return resto === 10 ? 0 : resto
  }

  const primeiro = calcularDigito(digits.slice(0, 9), 10)
  const segundo = calcularDigito(digits.slice(0, 10), 11)
  return digits.endsWith(`${primeiro}${segundo}`)
}

export function formatarDocumento(tipoPessoa, value) {
  return tipoPessoa === 'FISICA' ? formatarCpf(value) : formatarCnpj(value)
}

export function validarDocumento(tipoPessoa, value) {
  return tipoPessoa === 'FISICA' ? validarCpf(value) : validarCnpj(value)
}
