import { useCallback, useEffect, useMemo, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { apiRequest } from '../api/httpClient';
import { useAuth } from '../auth/AuthContext';
import type { Client } from '../models/client';
import type { OrderDetail, OrderStatus, PaymentResult } from '../models/order';

const paymentMethods = [
  ['CASH', 'Efectivo'],
  ['TRANSFER', 'Transferencia'],
  ['MERCADO_PAGO', 'Mercado Pago'],
  ['OTHER', 'Otro'],
] as const;

export function OrderDetailPage(): JSX.Element {
  const { id } = useParams<{ id: string }>();
  const { session } = useAuth();
  const [order, setOrder] = useState<OrderDetail | null>(null);
  const [client, setClient] = useState<Client | null>(null);
  const [loading, setLoading] = useState(true);
  const [working, setWorking] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [targetStatus, setTargetStatus] = useState<OrderStatus | ''>('');
  const [statusObservation, setStatusObservation] = useState('');
  const [paymentMethod, setPaymentMethod] = useState('TRANSFER');
  const [paymentAmount, setPaymentAmount] = useState('');
  const [paymentReference, setPaymentReference] = useState('');
  const [paymentResult, setPaymentResult] = useState<PaymentResult | null>(null);
  const canWrite = session?.roles.some((role) => role === 'ADMIN' || role === 'OPERATOR') ?? false;
  const canChangeStatus = session?.roles.some((role) => role === 'ADMIN' || role === 'OPERATOR' || role === 'DRIVER') ?? false;

  const load = useCallback(async () => {
    if (!id) return;
    setLoading(true);
    setError(null);
    try {
      const loadedOrder = await apiRequest<OrderDetail>(`/orders/${id}`);
      setOrder(loadedOrder);
      setTargetStatus(loadedOrder.allowedTransitions[0] ?? '');
      const loadedClient = await apiRequest<Client>(`/clients/${loadedOrder.clientId}`);
      setClient(loadedClient);
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'No se pudo cargar el pedido');
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => { void load(); }, [load]);

  const confirmedAmount = order?.confirmedPrice ?? null;
  const remainingAfterLastPayment = paymentResult?.remainingBalance ?? null;
  const priceBreakdown = useMemo(() => parseBreakdown(order?.priceBreakdown), [order?.priceBreakdown]);

  const perform = async (action: () => Promise<unknown>, message: string) => {
    setWorking(true);
    setError(null);
    setSuccess(null);
    try {
      await action();
      setSuccess(message);
      await load();
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'La operación no pudo completarse');
    } finally {
      setWorking(false);
    }
  };

  const confirmPrice = () => perform(
    () => apiRequest<OrderDetail>(`/orders/${id}/confirm-price`, { method: 'POST' }),
    'Precio confirmado y congelado correctamente.',
  );

  const changeStatus = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!targetStatus) return;
    await perform(
      () => apiRequest<OrderDetail>(`/orders/${id}/status`, {
        method: 'PATCH',
        body: JSON.stringify({
          newStatus: targetStatus,
          observation: statusObservation.trim() || null,
          location: null,
          notificationReference: null,
        }),
      }),
      `Estado actualizado a ${targetStatus}.`,
    );
    setStatusObservation('');
  };

  const registerPayment = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!paymentAmount || Number(paymentAmount) <= 0) {
      setError('Ingresá un importe positivo.');
      return;
    }
    setWorking(true);
    setError(null);
    setSuccess(null);
    try {
      const result = await apiRequest<PaymentResult>('/payments', {
        method: 'POST',
        body: JSON.stringify({
          orderId: id,
          methodCode: paymentMethod,
          amount: Number(paymentAmount),
          paidAt: null,
          reference: paymentReference.trim() || null,
          notes: null,
        }),
      });
      setPaymentResult(result);
      setPaymentAmount('');
      setPaymentReference('');
      setSuccess(`Pago registrado. Saldo restante: ${formatMoney(result.remainingBalance, result.currencyCode)}.`);
      await load();
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'No se pudo registrar el pago');
    } finally {
      setWorking(false);
    }
  };

  if (loading) return <section><div className="card muted">Cargando pedido…</div></section>;
  if (!order) return <section><div className="alert">{error ?? 'Pedido inexistente'}</div></section>;

  return (
    <section>
      <div className="page-heading">
        <div><h1>{order.orderNumber}</h1><p className="muted">Detalle, precio, estado y cobros</p></div>
        <Link className="text-link" to="/orders">Volver</Link>
      </div>
      {success && <div className="success">{success}</div>}
      {error && <div className="alert">{error}</div>}

      <div className="metric-grid">
        <article className="card metric"><span className="muted">Estado</span><strong className="metric-text">{order.status}</strong></article>
        <article className="card metric"><span className="muted">Pago</span><strong className="metric-text">{order.paymentStatus}</strong></article>
        <article className="card metric"><span className="muted">Precio</span><strong>{formatMoney(order.confirmedPrice ?? order.quotedPrice, order.currencyCode)}</strong></article>
        <article className="card metric"><span className="muted">Unidades</span><strong>{order.equivalentUnits}</strong></article>
      </div>

      <div className="detail-grid">
        <article className="card">
          <h2>Cliente y servicio</h2>
          <dl className="detail-list">
            <div><dt>Cliente</dt><dd>{client ? `${client.lastName}, ${client.firstName}` : order.clientId}</dd></div>
            <div><dt>WhatsApp</dt><dd>{client?.whatsapp ?? '—'}</dd></div>
            <div><dt>Servicio</dt><dd>{order.serviceCode}</dd></div>
            <div><dt>Piezas físicas</dt><dd>{order.physicalPieces}</dd></div>
            <div><dt>Peso declarado</dt><dd>{order.declaredWeightGrams ? `${order.declaredWeightGrams} g` : 'No informado'}</dd></div>
            <div><dt>Ciclo exclusivo</dt><dd>{order.exclusiveCycle ? 'Sí' : 'No'}</dd></div>
            <div><dt>Límite alcanzado</dt><dd>{order.limitReached}</dd></div>
            <div><dt>Retiro</dt><dd>{formatDate(order.pickupScheduledAt)}</dd></div>
            <div><dt>Promesa</dt><dd>{formatDate(order.promisedAt)}</dd></div>
          </dl>
        </article>

        <article className="card">
          <h2>Cotización</h2>
          <dl className="detail-list">
            <div><dt>Cotizado</dt><dd>{formatMoney(order.quotedPrice, order.currencyCode)}</dd></div>
            <div><dt>Confirmado</dt><dd>{confirmedAmount === null ? 'Pendiente' : formatMoney(confirmedAmount, order.currencyCode)}</dd></div>
            <div><dt>Requiere presupuesto manual</dt><dd>{order.requiresQuote ? 'Sí' : 'No'}</dd></div>
            <div><dt>Último saldo informado</dt><dd>{remainingAfterLastPayment === null ? 'Sin pago en esta sesión' : formatMoney(remainingAfterLastPayment, order.currencyCode)}</dd></div>
          </dl>
          {priceBreakdown && <pre className="json-preview">{JSON.stringify(priceBreakdown, null, 2)}</pre>}
          {canWrite && order.confirmedPrice === null && !order.requiresQuote && (
            <button disabled={working} onClick={() => void confirmPrice()}>Confirmar precio</button>
          )}
          {order.requiresQuote && order.confirmedPrice === null && (
            <div className="alert">El backend exige una cotización manual antes de confirmar. Ese ajuste todavía pertenece al corte de recepción.</div>
          )}
        </article>
      </div>

      <article className="card table-wrap">
        <h2>Prendas</h2>
        <table>
          <thead><tr><th>Prenda</th><th>Piezas</th><th>Grupos</th><th>Unidades</th><th>Peso estimado</th><th>Observaciones</th></tr></thead>
          <tbody>{order.items.map((item) => (
            <tr key={`${item.equivalenceCode}-${item.observations ?? ''}`}>
              <td>{item.name}<div className="muted small-text">{item.equivalenceCode}</div></td>
              <td>{item.physicalPieces}</td><td>{item.groups}</td><td>{item.equivalentUnits}</td>
              <td>{item.estimatedWeightGrams === null ? '—' : `${item.estimatedWeightGrams} g`}</td>
              <td>{item.observations ?? '—'}</td>
            </tr>
          ))}</tbody>
        </table>
      </article>

      <div className="detail-grid operations-grid">
        <article className="card">
          <h2>Cambiar estado</h2>
          {!canChangeStatus && <p className="muted">Tu rol solo permite consulta.</p>}
          {canChangeStatus && order.allowedTransitions.length === 0 && <p className="muted">No existen transiciones habilitadas desde el estado actual.</p>}
          {canChangeStatus && order.allowedTransitions.length > 0 && (
            <form className="form-stack" onSubmit={(event) => void changeStatus(event)}>
              <label>Próximo estado<select value={targetStatus} onChange={(event) => setTargetStatus(event.target.value as OrderStatus)}>{order.allowedTransitions.map((status) => <option key={status} value={status}>{status}</option>)}</select></label>
              <label>Observación<textarea rows={3} value={statusObservation} onChange={(event) => setStatusObservation(event.target.value)} /></label>
              <button type="submit" disabled={working || !targetStatus}>Actualizar estado</button>
            </form>
          )}
        </article>

        <article className="card">
          <h2>Registrar pago</h2>
          {!canWrite && <p className="muted">Solo administración y operación pueden registrar pagos.</p>}
          {canWrite && order.confirmedPrice === null && <p className="muted">Primero debe confirmarse el precio.</p>}
          {canWrite && order.confirmedPrice !== null && order.paymentStatus !== 'PAID' && (
            <form className="form-stack" onSubmit={(event) => void registerPayment(event)}>
              <label>Medio<select value={paymentMethod} onChange={(event) => setPaymentMethod(event.target.value)}>{paymentMethods.map(([code, label]) => <option key={code} value={code}>{label}</option>)}</select></label>
              <label>Importe<input type="number" min="0.01" step="0.01" value={paymentAmount} onChange={(event) => setPaymentAmount(event.target.value)} /></label>
              <label>Referencia<input value={paymentReference} onChange={(event) => setPaymentReference(event.target.value)} /></label>
              <button type="submit" disabled={working}>Registrar pago</button>
            </form>
          )}
          {order.paymentStatus === 'PAID' && <div className="success">Pedido completamente pagado.</div>}
        </article>
      </div>
    </section>
  );
}

function formatMoney(amount: number, currency: string): string {
  return new Intl.NumberFormat('es-AR', { style: 'currency', currency }).format(amount);
}

function formatDate(value: string | null): string {
  if (!value) return 'Sin programar';
  return new Intl.DateTimeFormat('es-AR', { dateStyle: 'short', timeStyle: 'short' }).format(new Date(value));
}

function parseBreakdown(value: string | undefined): unknown {
  if (!value) return null;
  try { return JSON.parse(value) as unknown; } catch { return value; }
}
