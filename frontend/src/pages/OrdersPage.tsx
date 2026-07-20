import { useCallback, useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { apiRequest } from '../api/httpClient';
import { useAuth } from '../auth/AuthContext';
import type { PageResponse } from '../models/api';
import type { OrderStatus, OrderSummary } from '../models/order';

const statusOptions: Array<{ value: '' | OrderStatus; label: string }> = [
  { value: '', label: 'Todos' },
  { value: 'INQUIRY', label: 'Consulta' },
  { value: 'QUOTED', label: 'Cotizado' },
  { value: 'WAITING_CONFIRMATION', label: 'Esperando confirmación' },
  { value: 'RESERVED', label: 'Reservado' },
  { value: 'PICKUP_SCHEDULED', label: 'Retiro programado' },
  { value: 'PICKED_UP', label: 'Retirado' },
  { value: 'RECEIVED', label: 'Recibido' },
  { value: 'WAITING_WASH', label: 'Esperando lavado' },
  { value: 'WASHING', label: 'Lavando' },
  { value: 'DRYING', label: 'Secando' },
  { value: 'QUALITY_CONTROL', label: 'Control de calidad' },
  { value: 'READY_FOR_DELIVERY', label: 'Listo para entregar' },
  { value: 'DELIVERY_SCHEDULED', label: 'Entrega programada' },
  { value: 'DELIVERED', label: 'Entregado' },
  { value: 'CLOSED', label: 'Cerrado' },
  { value: 'CANCELLED', label: 'Cancelado' },
];

export function OrdersPage(): JSX.Element {
  const { session } = useAuth();
  const [searchParams] = useSearchParams();
  const [orders, setOrders] = useState<OrderSummary[]>([]);
  const [orderNumber, setOrderNumber] = useState('');
  const [statusFilter, setStatusFilter] = useState<'' | OrderStatus>('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const canWrite = session?.roles.some((role) => role === 'ADMIN' || role === 'OPERATOR') ?? false;

  const load = useCallback(async (targetPage: number, numberFilter: string, status: '' | OrderStatus) => {
    setLoading(true);
    setError(null);
    try {
      const query = new URLSearchParams({ page: String(targetPage), size: '20' });
      if (numberFilter.trim()) query.set('orderNumber', numberFilter.trim());
      if (status) query.set('status', status);
      const response = await apiRequest<PageResponse<OrderSummary>>(`/orders?${query.toString()}`);
      setOrders(response.content);
      setPage(response.number);
      setTotalPages(response.totalPages);
      setTotalElements(response.totalElements);
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'No se pudieron cargar los pedidos');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { void load(0, '', ''); }, [load]);

  const applyFilters = () => void load(0, orderNumber, statusFilter);
  const clearFilters = () => {
    setOrderNumber('');
    setStatusFilter('');
    void load(0, '', '');
  };

  return (
    <section>
      <div className="page-heading">
        <div><h1>Pedidos</h1><p className="muted">Cotización, seguimiento y estado de pago</p></div>
        {canWrite && <Link className="button" to="/orders/new">Nuevo pedido</Link>}
      </div>
      {searchParams.has('created') && <div className="success">Pedido {searchParams.get('created')} creado correctamente.</div>}
      <form className="card filter-bar" onSubmit={(event) => { event.preventDefault(); applyFilters(); }}>
        <label>Número
          <input value={orderNumber} onChange={(event) => setOrderNumber(event.target.value)} placeholder="RL-000001" />
        </label>
        <label>Estado
          <select value={statusFilter} onChange={(event) => setStatusFilter(event.target.value as '' | OrderStatus)}>
            {statusOptions.map((option) => <option key={option.value} value={option.value}>{option.label}</option>)}
          </select>
        </label>
        <button type="submit" disabled={loading}>Buscar</button>
        <button type="button" className="secondary-button" onClick={clearFilters}>Limpiar</button>
      </form>

      {error && <div className="alert">{error}</div>}
      <div className="card table-wrap">
        <div className="table-summary"><strong>{totalElements}</strong> pedidos encontrados</div>
        {loading && <p className="muted">Cargando pedidos…</p>}
        {!loading && orders.length === 0 && <p className="muted">No hay pedidos para el filtro indicado.</p>}
        {orders.length > 0 && (
          <table>
            <thead><tr><th>Pedido</th><th>Cliente</th><th>Servicio</th><th>Estado</th><th>Pago</th><th>Importe</th><th>Retiro</th></tr></thead>
            <tbody>
              {orders.map((order) => (
                <tr key={order.id}>
                  <td><strong>{order.orderNumber}</strong><div className="muted small-text">{formatDate(order.createdAt)}</div></td>
                  <td>{order.clientName}</td>
                  <td>{order.serviceName}<div className="muted small-text">{order.physicalPieces} piezas · {order.equivalentUnits} unidades</div></td>
                  <td><span className="badge">{order.status}</span></td>
                  <td><span className="badge neutral-badge">{order.paymentStatus}</span></td>
                  <td>{formatMoney(order.confirmedPrice ?? order.quotedPrice, order.currencyCode)}</td>
                  <td>{order.pickupScheduledAt ? formatDate(order.pickupScheduledAt) : 'Sin programar'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {totalPages > 1 && (
        <div className="pagination">
          <button className="secondary-button" disabled={page === 0 || loading} onClick={() => void load(page - 1, orderNumber, statusFilter)}>Anterior</button>
          <span>Página {page + 1} de {totalPages}</span>
          <button className="secondary-button" disabled={page + 1 >= totalPages || loading} onClick={() => void load(page + 1, orderNumber, statusFilter)}>Siguiente</button>
        </div>
      )}
    </section>
  );
}

function formatMoney(amount: number, currency: string): string {
  return new Intl.NumberFormat('es-AR', { style: 'currency', currency }).format(amount);
}

function formatDate(value: string): string {
  return new Intl.DateTimeFormat('es-AR', { dateStyle: 'short', timeStyle: 'short' }).format(new Date(value));
}
