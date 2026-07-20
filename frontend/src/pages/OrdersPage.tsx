import { useEffect, useState, type FormEvent } from 'react';
import type { CustomerSummary } from '../models/customer';
import type { LaundryOrder, ServicePlan } from '../models/order';
import { ApiError, apiRequest } from '../services/apiClient';

export function OrdersPage() {
  const [orders, setOrders] = useState<LaundryOrder[]>([]);
  const [customers, setCustomers] = useState<CustomerSummary[]>([]);
  const [plans, setPlans] = useState<ServicePlan[]>([]);
  const [showForm, setShowForm] = useState(false);
  const [customerId, setCustomerId] = useState('');
  const [planId, setPlanId] = useState('');
  const [error, setError] = useState('');

  async function reload() {
    const [orderData, customerData, planData] = await Promise.all([
      apiRequest<LaundryOrder[]>('/v1/orders'),
      apiRequest<CustomerSummary[]>('/v1/customers'),
      apiRequest<ServicePlan[]>('/v1/service-plans')
    ]);
    setOrders(orderData);
    setCustomers(customerData);
    setPlans(planData);
    setCustomerId((value) => value || customerData[0]?.id || '');
    setPlanId((value) => value || planData[0]?.id || '');
  }

  useEffect(() => {
    void reload();
  }, []);

  async function submit(event: FormEvent) {
    event.preventDefault();
    const customer = customers.find((item) => item.id === customerId);
    if (!customer?.primaryAddress) {
      setError('El cliente necesita un domicilio principal');
      return;
    }
    setError('');
    try {
      await apiRequest('/v1/orders', {
        method: 'POST',
        body: JSON.stringify({
          customerId,
          addressId: customer.primaryAddress.id,
          servicePlanId: planId,
          modality: 'ROUTE',
          exclusiveCycle: false
        })
      });
      setShowForm(false);
      await reload();
    } catch (reason) {
      setError(reason instanceof ApiError ? reason.message : 'No se pudo crear el pedido');
    }
  }

  return (
    <>
      <div className="heading-row">
        <div><h1>Pedidos</h1><p className="muted">La operación conserva precio, equivalencias y estados históricos.</p></div>
        <button className="button" onClick={() => setShowForm((value) => !value)}>Nuevo pedido</button>
      </div>

      {showForm && (
        <form className="card form-grid" onSubmit={submit}>
          <h2>Crear consulta</h2>
          <label className="wide">Cliente
            <select value={customerId} onChange={(e) => setCustomerId(e.target.value)} required>
              {customers.map((customer) => <option key={customer.id} value={customer.id}>{customer.lastName}, {customer.firstName}</option>)}
            </select>
          </label>
          <label className="wide">Servicio
            <select value={planId} onChange={(e) => setPlanId(e.target.value)} required>
              {plans.map((plan) => <option key={plan.id} value={plan.id}>{plan.name}</option>)}
            </select>
          </label>
          {error && <div className="alert alert-error wide">{error}</div>}
          <div className="form-actions wide"><button className="button">Crear pedido</button></div>
        </form>
      )}

      <section className="card">
        {orders.length === 0 ? <p className="muted">Todavía no hay pedidos.</p> : orders.map((order) => (
          <div className="row" key={order.id}>
            <div>
              <strong>{order.orderNumber}</strong>
              <div>{order.customerName}</div>
              <div className="muted">{order.serviceName} · {order.actualWeightGrams ? `${order.actualWeightGrams} g` : 'Sin pesar'}</div>
            </div>
            <div className="right">
              <span className="badge">{order.status}</span>
              {order.confirmedAmount !== undefined && <strong>${order.confirmedAmount.toLocaleString('es-AR')}</strong>}
            </div>
          </div>
        ))}
      </section>
    </>
  );
}
