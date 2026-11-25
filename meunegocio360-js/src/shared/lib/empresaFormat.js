export function somenteDigitos(value) {
  return String(value ?? '')
    .replace(/\D/g, '')
    .slice(0, 14)
}

export function formatarCnpj(value) {
  const digits = somenteDigitos(value)
  if (digits.length <= 2) return digits
  if (digits.length <= 5) return `${digits.slice(0, 2)}.${digits.slice(2)}`
  if (digits.length <= 8) {
    return `${digits.slice(0, 2)}.${digits.slice(2, 5)}.${digits.slice(5)}`
  }
  if (digits.length <= 12) {
    return `${digits.slice(0, 2)}.${digits.slice(2, 5)}.${digits.slice(5, 8)}/${digits.slice(8)}`
  }
  return `${digits.slice(0, 2)}.${digits.slice(2, 5)}.${digits.slice(5, 8)}/${digits.slice(8, 12)}-${digits.slice(12)}`
}

export function formatarTelefone(value) {
  const digits = String(value ?? '')
    .replace(/\D/g, '')
    .slice(0, 11)
  if (!digits) return ''

  const ddd = digits.slice(0, 2)
  const numero = digits.slice(2)
  if (digits.length <= 2) return `(${ddd}`
  if (digits.length <= 6) return `(${ddd}) ${numero}`
  if (digits.length <= 10) {
    return `(${ddd}) ${numero.slice(0, 4)}-${numero.slice(4)}`
  }
  return `(${ddd}) ${numero.slice(0, 5)}-${numero.slice(5)}`
}

export function validarCnpj(value) {
  const digits = String(value ?? '').replace(/\D/g, '')
  if (digits.length !== 14 || /^(\d)\1{13}$/.test(digits)) return false

  const calcularDigito = (base, pesos) => {
    const soma = [...base].reduce(
      (total, digito, index) => total + Number(digito) * pesos[index],
      0,
    )
    const resto = soma % 11
    return resto < 2 ? 0 : 11 - resto
  }

  const primeiro = calcularDigito(
    digits.slice(0, 12),
    [5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2],
  )
  const segundo = calcularDigito(
    digits.slice(0, 12) + primeiro,
    [6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2],
  )
  return digits.endsWith(`${primeiro}${segundo}`)
}

export const FUSOS_HORARIOS = Object.freeze([
  { value: 'America/Sao_Paulo', label: 'Brasília (America/Sao_Paulo)' },
  { value: 'America/Manaus', label: 'Manaus (America/Manaus)' },
  { value: 'America/Rio_Branco', label: 'Rio Branco (America/Rio_Branco)' },
  { value: 'America/Porto_Velho', label: 'Porto Velho (America/Porto_Velho)' },
  { value: 'America/Boa_Vista', label: 'Boa Vista (America/Boa_Vista)' },
  { value: 'America/Cuiaba', label: 'Cuiabá (America/Cuiaba)' },
  {
    value: 'America/Campo_Grande',
    label: 'Campo Grande (America/Campo_Grande)',
  },
  { value: 'America/Belem', label: 'Belém (America/Belem)' },
  { value: 'America/Fortaleza', label: 'Fortaleza (America/Fortaleza)' },
  { value: 'America/Recife', label: 'Recife (America/Recife)' },
  { value: 'America/Maceio', label: 'Maceió (America/Maceio)' },
  { value: 'America/Bahia', label: 'Salvador (America/Bahia)' },
  { value: 'America/Araguaina', label: 'Araguaína (America/Araguaina)' },
  { value: 'America/Noronha', label: 'Fernando de Noronha (America/Noronha)' },
  { value: 'UTC', label: 'UTC' },
])
