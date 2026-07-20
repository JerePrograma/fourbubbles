import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';

export function ProtectedRoute(): JSX.Element {
  const { session, initializing } = useAuth();
  if (initializing) return <div className="centered">Cargando sesión…</div>;
  return session ? <Outlet /> : <Navigate to="/login" replace />;
}
