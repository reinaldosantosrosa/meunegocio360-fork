export { ClienteSelect } from './components/ClienteSelect.jsx'

export function carregarPaginaClientes() {
  return import('./pages/ClientesPage.jsx')
}

export function carregarPaginaFormularioCliente() {
  return import('./pages/ClienteFormPage.jsx')
}

export function carregarPaginaDemonstracaoClienteSelect() {
  return import('./pages/ClienteSelectDemoPage.jsx')
}
