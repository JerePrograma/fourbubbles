import { useCallback, useEffect, useState } from 'react';
import { apiRequest } from '../api/httpClient';
import type { ProductionMetrics } from '../models/productionMetrics';
import { completionLabel, separationLabel } from '../production/metricsState';
import '../production-metrics.css';

export function ProductionMetricsPage(): JSX.Element {
  const [metrics, setMetrics] = useState<ProductionMetrics | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      setMetrics(await apiRequest<ProductionMetrics>('/production/metrics'));
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'No se pudieron cargar las métricas');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { void load(); }, [load]);

  return (
    <section>
      <div className="page-heading">
        <div>
          <h1>Métricas de producción</h1>
          <p className="muted">Ventana móvil de los últimos 30 días</p>
        </div>
        <button className="secondary-button" disabled={loading} onClick={() => void load()}>Actualizar</button>
      </div>

      {error && <div className="alert">{error}</div>}
      {loading && <p className="muted">Calculando métricas…</p>}
      {metrics && (
        <>
          <div className="metrics-grid">
            <MetricCard label="Ciclos" value={String(metrics.totalCycles)} detail={completionLabel(metrics)} />
            <MetricCard label="Finalización" value={`${metrics.completionRatePercent}%`} detail={`${metrics.cancelledCycles} cancelados`} />
            <MetricCard label="Duración media" value={`${metrics.averageDurationMinutes} min`} detail="Ciclos completados" />
            <MetricCard label="Pedidos asignados" value={String(metrics.assignedOrders)} detail={`${metrics.sharedCycles} ciclos compartidos`} />
            <MetricCard label="Peso planificado" value={formatWeight(metrics.plannedWeightGrams)} detail={`Real: ${formatWeight(metrics.actualWeightGrams)}`} />
            <MetricCard label="Separación lista" value={`${metrics.separationReadyPercent}%`} detail={separationLabel(metrics)} />
          </div>

          <div className="card metrics-breakdown">
            <h2>Desglose</h2>
            <dl>
              <div><dt>Planificados</dt><dd>{metrics.plannedCycles}</dd></div>
              <div><dt>En ejecución</dt><dd>{metrics.runningCycles}</dd></div>
              <div><dt>Completados</dt><dd>{metrics.completedCycles}</dd></div>
              <div><dt>Lavados completados</dt><dd>{metrics.completedWashCycles}</dd></div>
              <div><dt>Secados completados</dt><dd>{metrics.completedDryCycles}</dd></div>
              <div><dt>Ciclos con separación</dt><dd>{metrics.separationRequiredCycles}</dd></div>
            </dl>
          </div>

          <p className="muted small-text">
            Período: {formatDate(metrics.from)} — {formatDate(metrics.to)}. Los datos se calculan desde ciclos persistidos; no estiman costos ni capacidad histórica.
          </p>
        </>
      )}
    </section>
  );
}

function MetricCard({ label, value, detail }: { label: string; value: string; detail: string }): JSX.Element {
  return <article className="card metric-card"><span className="muted">{label}</span><strong>{value}</strong><small>{detail}</small></article>;
}

function formatWeight(grams: number): string {
  return `${new Intl.NumberFormat('es-AR').format(grams)} g`;
}

function formatDate(value: string): string {
  return new Intl.DateTimeFormat('es-AR', { dateStyle: 'short', timeStyle: 'short' }).format(new Date(value));
}
