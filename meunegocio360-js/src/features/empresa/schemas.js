import { z } from 'zod'
import { FUSOS_HORARIOS, validarCnpj } from '../../shared/lib/empresaFormat.js'

const optionalText = z
  .string()
  .trim()
  .optional()
  .transform((value) => value || null)

export const empresaSchema = z.object({
  razaoSocial: z
    .string()
    .trim()
    .min(3, 'Informe a razão social.')
    .max(150, 'A razão social deve ter no máximo 150 caracteres.'),
  nomeFantasia: optionalText.pipe(
    z.union([
      z.string().max(150, 'O nome fantasia deve ter no máximo 150 caracteres.'),
      z.null(),
    ]),
  ),
  cnpj: z
    .string()
    .refine((value) => value.replace(/\D/g, '').length > 0, 'Informe o CNPJ.')
    .refine(
      (value) => !value.replace(/\D/g, '') || validarCnpj(value),
      'CNPJ inválido.',
    )
    .transform((value) => value.replace(/\D/g, '')),
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
  fusoHorario: z
    .string()
    .refine(
      (value) => FUSOS_HORARIOS.some((fuso) => fuso.value === value),
      'Selecione um fuso horário.',
    ),
  moeda: z.literal('BRL'),
})
