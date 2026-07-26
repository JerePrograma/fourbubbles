import { useCallback, useEffect, useMemo, useState } from 'react';
import { apiRequest } from '../api/httpClient';
import { useAuth } from '../auth/AuthContext';
import type { PageResponse } from '../models/api';
import type { ProductionCycle } from '../models/production';
import type { ProductionSeparation } from '../models/productionSeparation';
import { normalizeContainerCode, pendingSeparationCount } from '../production/separationState';
import '../production-separation.css';

export function ProductionSeparationPage(): JSX.Element {
  const { session } = useAuth();
  const canOperate = session?.roles.some((role) => role === 'ADMIN' || role === 'OPERATOR') ?? false;
  const [cycles, setCycles] = useState<ProductionCycle[]>([]);
  const [separations, setSeparations] = useState<Record<string, ProductionSeparation[]>>({});
  const [codes, setCodes] = useState<Record<string, string>>({});
  const [savingKey, setSavingKey] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const page = await apiRequest<PageResponse<ProductionCycle>>(
        '/production/cycles?status=PLANNED&page=0&size=100',
      );
      const requiringSeparation = page.content.filter((cycle) =>
        cycle.orders.some((order) => order.separationRequired));
      const records = await Promise.all(requiringSeparation.map(async (cycle) => [
        cycle.id,
        await apiRequest<ProductionSeparation[]>(`/production/cycles/${cycle.id}/separations`),
      ] as const));
      setCycles(requiringSeparation);
      setSeparations(Object.fromEntries(records));
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'No se pudo cargar la separación física');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { void load(); }, [load]);

  const totalPending = useMemo(() => Object.values(separations)
    .reduce((total, values) => total + pendingSeparationCount(values), 0), [separations]);

  const confirm = async (cycleId: string, orderId: string) => {
    const key = `${cycleId}:${orderId}`;
    const containerCode = normalizeContainerCode(codes[key] ?? '');
    if (!/^[A-Z0-9._:-]{3,80}$/.test(containerCode)) {
      setError('El código debe contener entre 3 y 80 caracteres: letras, números, punto, guion, guion bajo o dos puntos.');
      return;
    }
    setSavingKey(key);
    setError(null);
    setMessage(null);
    try {
      await apiRequest<ProductionSeparation>(
        `/production/cycles/${cycleId}/separations/${orderId}`,
        { method: 'PUT', body: JSON.stringify({ containerCode }) },
      );
      setCodes((values) => ({ ...values, [key]: '' }));
      setMessage('Separación física confirmada.');
      await load();
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'No se pudo confirmar la separación');
    } finally {
      setSavingKey(null);
    }
  };

  return (
    <section>
      <div className="page-heading">
        <div>
          <h1>Separación física</h1>
          <p className="muted">Contenedores identificados para ciclos compartidos autorizados por excepción</p>
        </div>
        <span className="badge neutral-badge">Pendientes: {totalPending}</span>
      </div>

      {error && <div className="alert">{error}</div>}
      {message && <div className="success">{message}</div>}
      {loading && <p className="muted">Cargando ciclos pendientes…</p>}
      {!loading && cycles.length === 0 && (
        <div className="card"><p className="muted">No hay ciclos planificados que requieran separación.</p></div>
      )}

      <div className="separation-list">
        {cycles.map((cycle) => {
          const records = separations[cycle.id] ?? [];
          return (
            <article className="card separation-cycle" key={cycle.id}>
              <header>
                <div>
                  <h2>{cycle.cycleNumber}</h2>
                  <p className="muted">{cycle.machineCode} · {cycle.programCode} · {cycle.plannedWeightGrams} g</p>
                </div>
                <span className="badge">{pendingSeparationCount(records) === 0 ? 'LISTO' : 'PENDIENTE'}</span>
              </header>

              {records.map((record) => {
                const key = `${cycle.id}:${record.orderId}`;
                return (
                  <div className="separation-order" key={record.orderId}>
                    <div>
                      <strong>{record.orderNumber}</strong>
                      {record.confirmedAt ? (
                        <p className="muted small-text">
                          Contenedor {record.containerCode} · confirmado por {record.confirmedBy}
                        </p>
                      ) : (
                        <p className="muted small-text">Debe identificarse antes de iniciar el ciclo.</p>
                      )}
                    </div>
                    {!record.confirmedAt && canOperate && (
                      <div className="separation-confirmation">
                        <input
                          aria-label={`Contenedor para ${record.orderNumber}`}
                          placeholder="Ej. BAG-001"
                          value={codes[key] ?? ''}
                          onChange={(event) => setCodes((values) => ({ ...values, [key]: event.target.value }))}
                        />
                        <button
                          disabled={savingKey === key}
                          onClick={() => void confirm(cycle.id, record.orderId)}
                        >
                          Confirmar
                        </button>
                      </div>
                    )}
                  </div>
                );
              })}
            </article>
          );
        })}
      </div>
    </section>
  );
}
