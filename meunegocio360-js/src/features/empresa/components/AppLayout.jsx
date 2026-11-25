import { useState } from 'react'
import { Button, Container, Nav, Navbar } from 'react-bootstrap'
import { Link, NavLink, Outlet, useNavigate } from 'react-router'
import brandLogo from '../../../assets/logo_meunegocio360_v1_branca.png'
import { PERFIS } from '../../../shared/lib/perfis.js'
import { useAuth } from '../../auth/index.js'
import { useEmpresa } from '../hooks/useEmpresa.js'

const menuItems = [
  { label: 'Início', path: '/', roles: Object.values(PERFIS), end: true },
  { label: 'Empresa', path: '/empresa', roles: Object.values(PERFIS) },
  {
    label: 'Clientes',
    path: '/clientes',
    roles: [PERFIS.ADMIN, PERFIS.OPERADOR_COMERCIAL],
  },
]

function MenuItem({ item, profile }) {
  if (!item.roles.includes(profile)) return null

  return (
    <Nav.Link as={NavLink} to={item.path} end={item.end}>
      {item.label}
    </Nav.Link>
  )
}

function MenuItems({ profile }) {
  return menuItems.map((item) => (
    <MenuItem key={item.path} item={item} profile={profile} />
  ))
}

function MenuIcon() {
  return (
    <svg
      aria-hidden="true"
      viewBox="0 0 24 24"
      width="24"
      height="24"
      focusable="false"
    >
      <path
        d="M4 6h16M4 12h16M4 18h16"
        fill="none"
        stroke="currentColor"
        strokeLinecap="round"
        strokeWidth="2"
      />
    </svg>
  )
}

function SearchIcon() {
  return (
    <svg
      aria-hidden="true"
      viewBox="0 0 24 24"
      width="24"
      height="24"
      focusable="false"
    >
      <circle
        cx="10.8"
        cy="10.8"
        r="6.8"
        fill="none"
        stroke="currentColor"
        strokeWidth="2"
      />
      <path
        d="m16 16 4 4"
        fill="none"
        stroke="currentColor"
        strokeLinecap="round"
        strokeWidth="2"
      />
    </svg>
  )
}

export function AppLayout() {
  const { empresa } = useEmpresa()
  const { user, signOut } = useAuth()
  const navigate = useNavigate()
  const [isSigningOut, setIsSigningOut] = useState(false)
  const companyName =
    empresa?.nomeFantasia || empresa?.razaoSocial || 'MeuNegocio360'

  async function handleSignOut() {
    setIsSigningOut(true)
    try {
      await signOut()
      navigate('/login', { replace: true })
    } catch {
      navigate('/login', { replace: true, state: { logoutError: true } })
    } finally {
      setIsSigningOut(false)
    }
  }

  return (
    <div className="app-shell">
      <Navbar expand="lg" className="app-navbar">
        <Container fluid className="px-3 px-lg-4">
          <div className="app-navbar-head">
            <Navbar.Toggle
              aria-controls="app-navigation"
              aria-label="Abrir menu de navegação"
            >
              <MenuIcon />
            </Navbar.Toggle>
            <Navbar.Brand
              as={Link}
              to="/"
              className="app-navbar-brand"
              aria-label="MeuNegocio360 — início"
            >
              <img src={brandLogo} alt="MeuNegocio360" />
            </Navbar.Brand>
            <span className="app-navbar-search" aria-hidden="true">
              <SearchIcon />
            </span>
          </div>

          <Navbar.Collapse id="app-navigation">
            <Nav className="app-navbar-links">
              <MenuItems profile={user?.perfil} />
            </Nav>
            <div className="app-navbar-meta">
              <span className="app-navbar-company">{companyName}</span>
              <span className="app-navbar-user">
                {user?.nome || user?.email}
              </span>
              <Button
                type="button"
                variant="outline-dark"
                className="min-touch-target"
                onClick={handleSignOut}
                disabled={isSigningOut}
              >
                {isSigningOut ? 'Saindo...' : 'Sair'}
              </Button>
            </div>
          </Navbar.Collapse>
        </Container>
      </Navbar>

      <div className="app-main">
        <Outlet />
      </div>
    </div>
  )
}
