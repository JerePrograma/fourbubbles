import { useEffect, useState, type FormEvent } from 'react';
import type { CreateCustomerRequest, CustomerSummary, Zone } from '../models/customer';
import { ApiError, apiRequest } from '../services/apiClient';

const initialForm: CreateCustomerRequest = {
  firstName: '',
  lastName: '',
  phone: '',
  whatsapp: '',
  email: '',
  acquisitionSource: 'WHATSAPP',
  primaryAddress: {
    zoneId: '',
    street: '',
    streetNumber: '',
    neighborhood: '',
    locality: '',
    references: ''
  },
  preferences: {
    fragrance: '',
    softenerAllowed: true,
    babyClothes: false,
    dryerAllowed: true,
    colorMixAllowed: false,
    exclusiveCycle: false,
    stainTreatment: false
  }
};

export function CustomersPage() {
  const [customers, setCustomers] = useState<CustomerSummary[]>([]);
  const [zones, setZones] = useState<Zone[]>([]);
  const [form, setForm] = useState<CreateCustomerRequest>(initialForm);
  const [showForm, setShowForm] = useState(false);
  const [error, setError] = useState('');

  async function reload() {
    const [customerData, zoneData] = await Promise.all([
      apiRequest<CustomerSummary[]>('/v1/customers'),
      apiRequest<Zone[]>('/v1/zones')
    ]);
    setCustomers(customerData);
    setZones(zoneData);
    if (!form.primaryAddress.zoneId && zoneData[0]) {
      setForm((current) => ({
        ...current,
        primaryAddress: {
          ...current.primaryAddress,
          zoneId: zoneData[0]!.id,
          locality: zoneData[0]!.locality
        }
      }));
    }
  }

  useEffect(() => {
    void reload();
  }, []);

  async function submit(event: FormEvent) {
    event.preventDefault();
    setError('');
    try {
      await apiRequest('/v1/customers', { method: 'POST', body: JSON.stringify(form) });
      setForm(initialForm);
      setShowForm(false);
      await reload();
    } catch (reason) {
      setError(reason instanceof ApiError ? reason.message : 'No se pudo crear el cliente');
    }
  }

  return (
    <>
      <div className="heading-row">
        <div><h1>Clientes</h1><p className="muted">Datos mínimos y restricciones operativas.</p></div>
        <button className="button" onClick={() => setShowForm((value) => !value)}>Nuevo cliente</button>
      </div>

      {showForm && (
        <form className="card form-grid" onSubmit={submit}>
          <h2>Alta rápida</h2>
          <label>Nombre<input value={form.firstName} onChange={(e) => setForm({...form, firstName: e.target.value})} required /></label>
          <label>Apellido<input value={form.lastName} onChange={(e) => setForm({...form, lastName: e.target.value})} required /></label>
          <label>Teléfono<input value={form.phone} onChange={(e) => setForm({...form, phone: e.target.value})} required /></label>
          <label>WhatsApp<input value={form.whatsapp} onChange={(e) => setForm({...form, whatsapp: e.target.value})} /></label>
          <label className="wide">Zona
            <select value={form.primaryAddress.zoneId} onChange={(e) => {
              const zone = zones.find((item) => item.id === e.target.value);
              setForm({...form, primaryAddress: {...form.primaryAddress, zoneId: e.target.value, locality: zone?.locality ?? ''}});
            }} required>
              {zones.map((zone) => <option key={zone.id} value={zone.id}>{zone.name}</option>)}
            </select>
          </label>
          <label>Calle<input value={form.primaryAddress.street} onChange={(e) => setForm({...form, primaryAddress: {...form.primaryAddress, street: e.target.value}})} required /></label>
          <label>Número<input value={form.primaryAddress.streetNumber} onChange={(e) => setForm({...form, primaryAddress: {...form.primaryAddress, streetNumber: e.target.value}})} required /></label>
          <label className="check"><input type="checkbox" checked={form.preferences.babyClothes} onChange={(e) => setForm({...form, preferences: {...form.preferences, babyClothes: e.target.checked}})} /> Ropa de bebé</label>
          <label className="check"><input type="checkbox" checked={form.preferences.exclusiveCycle} onChange={(e) => setForm({...form, preferences: {...form.preferences, exclusiveCycle: e.target.checked}})} /> Ciclo exclusivo</label>
          {error && <div className="alert alert-error wide">{error}</div>}
          <div className="form-actions wide"><button className="button">Guardar</button></div>
        </form>
      )}

      <section className="card">
        {customers.length === 0 ? <p className="muted">Todavía no hay clientes.</p> : customers.map((customer) => (
          <div className="row" key={customer.id}>
            <div>
              <strong>{customer.lastName}, {customer.firstName}</strong>
              <div className="muted">{customer.phone} · {customer.primaryAddress?.zoneName ?? 'Sin zona'}</div>
            </div>
            <span className="badge">{customer.status}</span>
          </div>
        ))}
      </section>
    </>
  );
}
