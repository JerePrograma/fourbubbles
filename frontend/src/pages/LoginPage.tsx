import { zodResolver } from '@hookform/resolvers/zod';
import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { Navigate, useLocation, useNavigate } from 'react-router-dom';
import { z } from 'zod';
import { useAuth } from '../auth/AuthContext';

const schema = z.object({
  username: z.string().min(1, 'Ingresá el usuario'),
  password: z.string().min(8, 'La contraseña debe tener al menos 8 caracteres'),
});
type FormData = z.infer<typeof schema>;

export function LoginPage(): JSX.Element {
  const { session, login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [error, setError] = useState<string | null>(null);
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<FormData>({
    resolver: zodResolver(schema),
  });

  if (session) return <Navigate to="/dashboard" replace />;

  const submit = handleSubmit(async (data) => {
    setError(null);
    try {
      await login(data.username, data.password);
      const from = (location.state as { from?: string } | null)?.from ?? '/dashboard';
      navigate(from, { replace: true });
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'No fue posible iniciar sesión');
    }
  });

  return (
    <main className="login-layout">
      <form className="card login-card" onSubmit={(event) => void submit(event)}>
        <h1>Ropa Lista</h1>
        <p className="muted">Gestión operativa de lavandería</p>
        <label>Usuario<input autoComplete="username" {...register('username')} /></label>
        {errors.username && <small className="error">{errors.username.message}</small>}
        <label>Contraseña<input type="password" autoComplete="current-password" {...register('password')} /></label>
        {errors.password && <small className="error">{errors.password.message}</small>}
        {error && <div className="alert">{error}</div>}
        <button type="submit" disabled={isSubmitting}>{isSubmitting ? 'Ingresando…' : 'Ingresar'}</button>
      </form>
    </main>
  );
}
