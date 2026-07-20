import { useCallback, useEffect, useState } from 'react';
import { apiRequest } from '../api/httpClient';
import type { PageResponse } from '../models/api';
import type { AuditEvent } from '../models/audit';

export function AuditPage(): JSX.Element {
  const [events, setEvents] = useState<AuditEvent[]>([]);
  const [entityType, setEntityType] = useState('');
  const [entityId, setEntityId] = useState('');
  const [action, setAction] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async (targetPage: number, filters = { entityType, entityId, action }) => {
    setLoading(true);
    setError(null);
    try {
      const query = new URLSearchParams({ page: String(targetPage), size: '20' });
      if (filters.entityType.trim()) query.set('entityType', filters.entityType.trim());
      if (filters.entityId.trim()) query.set('entityId', filters.entityId.trim());
      if (filters.action.trim()) query.set('action', filters.action.trim());
      const result = await apiRequest<PageResponse<AuditEvent>>(`/audit?${query.toString()}`);
      setEvents(result.content);
      setPage(result.number);
      setTotalPages(result.totalPages);
      setTotalElements(result.totalElements);
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'No se pudo consultar la auditoría');
    } finally {
      setLoading(false);
    }
  }, [action, entityId, entityType]);

  useEffect(() => { void load(0, { entityType: '', entityId: '', action: '' }); }, [load]);

  const clear = () => {
    setEntityType('');
    setEntityId('');
    setAction('');
    void load(0, { entityType: '', entityId: '', action: '' });
  };

  return (
    <section>
      <div className="page-heading">
        <div><h1>Auditoría</h1><p className="muted">Cambios sensibles, actor, motivo y valores</p></div>
      </div>
      <form className="card audit-filter" onSubmit={(event) => { event.preventDefault(); void load(0); }}>
        <label>Entidad<input value={entityType} onChange={(event) => setEntityType(event.target.value)} placeholder="ORDER, CLIENT…" /></label>
        <label>ID<input value={entityId} onChange={(event) => setEntityId(event.target.value)} placeholder="UUID o identificador" /></label>
        <label>Acción<input value={action} onChange={(event) => setAction(event.target.value)} placeholder="CREATE, UPDATE…" /></label>
        <button type="submit" disabled={loading}>Buscar</button>
        <button type="button" className="secondary-button" onClick={clear}>Limpiar</button>
      </form>
      {error && <div className="alert">{error}</div>}
      <div className="card table-wrap">
        <div className="table-summary"><strong>{totalElements}</strong> eventos</div>
        {loading && <p className="muted">Cargando auditoría…</p>}
        {!loading && events.length === 0 && <p className="muted">No hay eventos para el filtro indicado.</p>}
        {events.length > 0 && (
          <table>
            <thead><tr><th>Fecha</th><th>Entidad</th><th>Acción</th><th>Actor</th><th>Motivo</th><th>Cambios</th></tr></thead>
            <tbody>{events.map((event) => (
              <tr key={event.id}>
                <td>{formatDate(event.createdAt)}</td>
                <td><strong>{event.entityType}</strong><div className="muted small-text id-cell">{event.entityId}</div></td>
                <td><span className="badge">{event.action}</span></td>
                <td>{event.createdBy}</td>
                <td>{event.reason ?? '—'}</td>
                <td><details><summary>Ver JSON</summary><pre className="json-preview">{formatJson(event.oldValue, event.newValue)}</pre></details></td>
              </tr>
            ))}</tbody>
          </table>
        )}
      </div>
      {totalPages > 1 && (
        <div className="pagination">
          <button className="secondary-button" disabled={page === 0 || loading} onClick={() => void load(page - 1)}>Anterior</button>
          <span>Página {page + 1} de {totalPages}</span>
          <button className="secondary-button" disabled={page + 1 >= totalPages || loading} onClick={() => void load(page + 1)}>Siguiente</button>
        </div>
      )}
    </section>
  );
}

function formatDate(value: string): string {
  return new Intl.DateTimeFormat('es-AR', { dateStyle: 'short', timeStyle: 'short' }).format(new Date(value));
}

function formatJson(oldValue: string | null, newValue: string | null): string {
  return JSON.stringify({ oldValue: parseJson(oldValue), newValue: parseJson(newValue) }, null, 2);
}

function parseJson(value: string | null): unknown {
  if (!value) return null;
  try { return JSON.parse(value) as unknown; } catch { return value; }
}
