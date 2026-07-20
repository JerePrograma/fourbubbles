import { useEffect, useState } from 'react';
import type { CustomerSummary } from '../models/customer';
import type { LaundryOrder } from '../models/order';
import { apiRequest } from '../services/apiClient';

export function DashboardPage() {
  const [customers, setCustomers] = useState<CustomerSummary[]>([]);
  const [orders, setOrders] = useState<LaundryOrder[]>([]);

  useEffect(() => {
    void Promise.all([
      apiRequest<CustomerSummary[]>('/v1/customers'),
      apiRequest<LaundryOrder[]>('/v1/orders')
    ]).then(([customerData, orderData]) => {
      setCustomers(customerData);
      setOrders(orderData);
    });
  }, []);

  const active = orders.filter((order) => !['CLOSED', 'CANCELLED'].includes(order.status));
  const pendingPayment = orders.filter((order) =>
    order.confirmedAmount && (!order.deliveredAt || order.status === 'PAYMENT_PENDING'));

  return (
    <>
      <h1>Tablero operativo</h1>
      <section className="metric-grid">
        <article className="metric"><span>Clientes</span><strong>{customers.length}</strong></article>
        <article className="metric"><span>Pedidos activos</span><strong>{active.length}</strong></article>
        <article className="metric"><span>Pagos a revisar</span><strong>{pendingPayment.length}</strong></article>
      </section>
      <section className="card">
        <h2>Prioridad inmediata</h2>
        {active.length === 0 ? (
          <p className="muted">No hay pedidos activos.</p>
        ) : (
          <div className="stack">
            {active.slice(0, 6).map((order) => (
              <div className="row" key={order.id}>
                <div><strong>{order.orderNumber}</strong><div className="muted">{order.customerName}</div></div>
                <span className="badge">{order.status}</span>
              </div>
            ))}
          </div>
        )}
      </section>
    </>
  );
}
