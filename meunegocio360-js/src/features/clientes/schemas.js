import { z } from 'zod'
import { validarDocumento } from '../../shared/lib/documento.js'

const optionalText = z
  .string()
  .trim()
  .optional()
  .transform((value) => value || null)

export const clienteSchema = z
  .object({
    tipoPessoa: z
      .string()
      .refine(
        (value) => value === 'FISICA' || value === 'JURIDICA',
        'Selecione o tipo de pessoa.',
      ),
    nome: z
      .string()
      .trim()
      .min(2, 'Informe o nome.')
      .max(150, 'O nome deve ter no máximo 150 caracteres.'),
    documento: z.string().trim(),
    email: optionalText.pipe(
      z.union([
        z
          .string()
          .email('Informe um e-mail válido.')
          .max(160, 'O e-mail deve ter no máximo 160 caracteres.'),
        z.null(),
      ]),
    ),
    telefone: optionalText.pipe(
      z.union([
        z
          .string()
          .refine(
            (value) => [10, 11].includes(value.replace(/\D/g, '').length),
            'Informe um telefone válido com DDD.',
          )
          .transform((value) => value.replace(/\D/g, '')),
        z.null(),
      ]),
    ),
  })
  .superRefine((values, context) => {
    const digits = values.documento.replace(/\D/g, '')
    if (!digits) {
      context.addIssue({
        code: 'custom',
        path: ['documento'],
        message: 'Informe o documento.',
      })
      return
    }

    if (!validarDocumento(values.tipoPessoa, digits)) {
      context.addIssue({
        code: 'custom',
        path: ['documento'],
        message:
          values.tipoPessoa === 'FISICA' ? 'CPF inválido.' : 'CNPJ inválido.',
      })
    }
  })
  .transform((values) => ({
    ...values,
    documento: values.documento.replace(/\D/g, ''),
  }))
