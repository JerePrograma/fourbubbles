import { useState, type FormEvent } from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { ApiError } from '../services/apiClient';

export function LoginPage() {
  const { session, login } = useAuth();
  const [email, setEmail] = useState('admin@local.invalid');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  if (session) return <Navigate to="/dashboard" replace />;

  async function submit(event: FormEvent) {
    event.preventDefault();
    setSubmitting(true);
    setError('');
    try {
      await login(email, password);
    } catch (reason) {
      setError(reason instanceof ApiError ? reason.message : 'No se pudo iniciar sesión');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <main className="login-page">
      <form className="card login-card" onSubmit={submit}>
        <span className="eyebrow">Gestión de lavandería</span>
        <h1>Ropa Lista</h1>
        <p className="muted">Ingresá con el administrador configurado para el ambiente.</p>
        <label>
          Correo
          <input type="email" value={email} onChange={(event) => setEmail(event.target.value)} required />
        </label>
        <label>
          Contraseña
          <input type="password" value={password} onChange={(event) => setPassword(event.target.value)} required />
        </label>
        {error && <div className="alert alert-error">{error}</div>}
        <button className="button" disabled={submitting}>
          {submitting ? 'Ingresando…' : 'Ingresar'}
        </button>
      </form>
    </main>
  );
}
