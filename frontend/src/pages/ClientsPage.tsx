import { useCallback, useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { apiRequest } from '../api/httpClient';
import { useAuth } from '../auth/AuthContext';
import type { PageResponse } from '../models/api';
import type { Client } from '../models/client';

export function ClientsPage(): JSX.Element {
  const { session } = useAuth();
  const [searchParams] = useSearchParams();
  const [clients, setClients] = useState<Client[]>([]);
  const [lastName, setLastName] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const canWrite = session?.roles.some((role) => role === 'ADMIN' || role === 'OPERATOR') ?? false;

  const load = useCallback(async (query: string) => {
    setLoading(true);
    setError(null);
    try {
      const page = await apiRequest<PageResponse<Client>>(`/clients?size=100&lastName=${encodeURIComponent(query)}`);
      setClients(page.content);
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'Error al cargar clientes');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { void load(''); }, [load]);

  return (
    <section>
      <div className="page-heading">
        <div><h1>Clientes</h1><p className="muted">Clientes, domicilios y preferencias</p></div>
        {canWrite && <Link className="button" to="/clients/new">Nuevo</Link>}
      </div>
      {(searchParams.has('created') || searchParams.has('updated')) && (
        <div className="success">Cliente guardado correctamente.</div>
      )}
      <form className="card filter-bar" onSubmit={(event) => { event.preventDefault(); void load(lastName); }}>
        <label>Apellido
          <input value={lastName} onChange={(event) => setLastName(event.target.value)} placeholder="Buscar por apellido" />
        </label>
        <button type="submit" disabled={loading}>Buscar</button>
        <button type="button" className="secondary-button" onClick={() => { setLastName(''); void load(''); }}>Limpiar</button>
      </form>
      {error && <div className="alert">{error}</div>}
      <div className="list">
        {loading && <div className="card muted">Cargando clientes…</div>}
        {!loading && clients.length === 0 && !error && <div className="card muted">No hay clientes para el filtro indicado.</div>}
        {clients.map((client) => {
          const primary = client.addresses.find((address) => address.primaryAddress);
          return (
            <article className="card row responsive-row" key={client.id}>
              <div>
                <strong>{client.lastName}, {client.firstName}</strong>
                <div className="muted">WhatsApp: {client.whatsapp}</div>
                {primary && <div className="muted">{primary.street} {primary.number}, {primary.locality}</div>}
              </div>
              <div className="row-actions">
                <span className="badge">{client.status}</span>
                {canWrite && <Link className="text-link" to={`/clients/${client.id}/edit`}>Editar</Link>}
              </div>
            </article>
          );
        })}
      </div>
    </section>
  );
}
