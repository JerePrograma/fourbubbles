import { NavLink, Outlet } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

const navItems = [
  ['/dashboard', 'Inicio'],
  ['/clients', 'Clientes'],
  ['/orders', 'Pedidos'],
  ['/agenda', 'Agenda'],
] as const;

export function AppShell(): JSX.Element {
  const { session, logout } = useAuth();
  return (
    <div className="app-shell">
      <header className="topbar">
        <div>
          <strong>Ropa Lista</strong>
          <span className="muted"> Operación</span>
        </div>
        <button className="link-button" onClick={() => void logout()}>
          Salir ({session?.username})
        </button>
      </header>
      <main className="content"><Outlet /></main>
      <nav className="bottom-nav" aria-label="Navegación principal">
        {navItems.map(([to, label]) => (
          <NavLink key={to} to={to} className={({ isActive }) => isActive ? 'active' : undefined}>
            {label}
          </NavLink>
        ))}
      </nav>
    </div>
  );
}
