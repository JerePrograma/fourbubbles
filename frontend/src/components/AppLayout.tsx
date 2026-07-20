import { NavLink, Outlet } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

const links = [
  ['/dashboard', 'Inicio'],
  ['/customers', 'Clientes'],
  ['/orders', 'Pedidos']
] as const;

export function AppLayout() {
  const { session, logout } = useAuth();

  return (
    <div className="app-shell">
      <header className="topbar">
        <div>
          <strong>Ropa Lista</strong>
          <span className="muted"> Operación</span>
        </div>
        <button className="button button-ghost" onClick={logout}>Salir</button>
      </header>
      <nav className="bottom-nav" aria-label="Navegación principal">
        {links.map(([path, label]) => (
          <NavLink key={path} to={path} className={({ isActive }) => isActive ? 'active' : ''}>
            {label}
          </NavLink>
        ))}
      </nav>
      <main className="content">
        <div className="page-heading">
          <span className="eyebrow">{session?.displayName}</span>
        </div>
        <Outlet />
      </main>
    </div>
  );
}
