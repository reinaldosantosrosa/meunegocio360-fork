import { useContext } from 'react'
import { EmpresaContext } from '../context/EmpresaContext.js'

export function useEmpresa() {
  const context = useContext(EmpresaContext)
  if (!context) {
    throw new Error('useEmpresa deve ser usado dentro de EmpresaProvider.')
  }
  return context
}
