import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { apiRequest } from '../api/httpClient';

interface ClientSummary {
  id: string;
  firstName: string;
  lastName: string;
  whatsapp: string;
  status: string;
}
interface Page<T> { content: T[]; totalElements: number; }

export function ClientsPage(): JSX.Element {
  const [clients, setClients] = useState<ClientSummary[]>([]);
  const [error, setError] = useState<string | null>(null);
  useEffect(() => {
    apiRequest<Page<ClientSummary>>('/clients?size=50')
      .then((page) => setClients(page.content))
      .catch((cause) => setError(cause instanceof Error ? cause.message : 'Error al cargar clientes'));
  }, []);

  return (
    <section>
      <div className="page-heading"><div><h1>Clientes</h1><p className="muted">Clientes activos y domicilios</p></div><Link className="button" to="/clients/new">Nuevo</Link></div>
      {error && <div className="alert">{error}</div>}
      <div className="list">
        {clients.length === 0 && !error && <div className="card muted">No hay clientes cargados.</div>}
        {clients.map((client) => (
          <article className="card row" key={client.id}>
            <div><strong>{client.lastName}, {client.firstName}</strong><div className="muted">WhatsApp: {client.whatsapp}</div></div>
            <span className="badge">{client.status}</span>
          </article>
        ))}
      </div>
    </section>
  );
}
