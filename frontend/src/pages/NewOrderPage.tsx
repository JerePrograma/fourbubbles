import { useEffect, useMemo, useState } from 'react';
import type { FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { apiRequest } from '../api/httpClient';
import type { PageResponse } from '../models/api';
import type { GarmentEquivalence, ServiceOffering } from '../models/catalog';
import type { Client } from '../models/client';
import type { CreatedOrder } from '../models/order';
import { calculateOrderDraftTotals, toOffsetDateTime } from '../order/orderDraft';
import type { DraftOrderItem } from '../order/orderDraft';

const emptyItem = (): DraftOrderItem => ({ equivalenceCode: '', physicalPieces: 1, observations: '' });

export function NewOrderPage(): JSX.Element {
  const navigate = useNavigate();
  const [clients, setClients] = useState<Client[]>([]);
  const [services, setServices] = useState<ServiceOffering[]>([]);
  const [equivalences, setEquivalences] = useState<GarmentEquivalence[]>([]);
  const [clientId, setClientId] = useState('');
  const [addressId, setAddressId] = useState('');
  const [serviceCode, setServiceCode] = useState('');
  const [promotionCode, setPromotionCode] = useState('');
  const [declaredWeightGrams, setDeclaredWeightGrams] = useState('');
  const [exclusiveCycle, setExclusiveCycle] = useState(false);
  const [pickupScheduledAt, setPickupScheduledAt] = useState('');
  const [promisedAt, setPromisedAt] = useState('');
  const [notes, setNotes] = useState('');
  const [items, setItems] = useState<DraftOrderItem[]>([emptyItem()]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    Promise.all([
      apiRequest<PageResponse<Client>>('/clients?size=100'),
      apiRequest<ServiceOffering[]>('/catalog/services'),
      apiRequest<GarmentEquivalence[]>('/catalog/equivalences'),
    ])
      .then(([clientPage, loadedServices, loadedEquivalences]) => {
        setClients(clientPage.content);
        setServices(loadedServices);
        setEquivalences(loadedEquivalences);
        const firstClient = clientPage.content[0];
        if (firstClient) {
          setClientId(firstClient.id);
          setAddressId(firstClient.addresses.find((address) => address.primaryAddress)?.id ?? firstClient.addresses[0]?.id ?? '');
        }
        if (loadedServices[0]) setServiceCode(loadedServices[0].code);
        if (loadedEquivalences[0]) setItems([{ ...emptyItem(), equivalenceCode: loadedEquivalences[0].code }]);
      })
      .catch((cause) => setError(cause instanceof Error ? cause.message : 'No se pudo cargar la configuración del pedido'))
      .finally(() => setLoading(false));
  }, []);

  const selectedClient = clients.find((client) => client.id === clientId);
  const selectedService = services.find((service) => service.code === serviceCode);
  const totals = useMemo(() => calculateOrderDraftTotals(items, equivalences), [items, equivalences]);

  const changeClient = (nextClientId: string) => {
    setClientId(nextClientId);
    const client = clients.find((candidate) => candidate.id === nextClientId);
    setAddressId(client?.addresses.find((address) => address.primaryAddress)?.id ?? client?.addresses[0]?.id ?? '');
  };

  const updateItem = (index: number, patch: Partial<DraftOrderItem>) => {
    setItems((current) => current.map((item, itemIndex) => itemIndex === index ? { ...item, ...patch } : item));
  };

  const submit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError(null);
    const validItems = items.filter((item) => item.equivalenceCode && item.physicalPieces > 0);
    if (!clientId || !addressId || !serviceCode || validItems.length === 0) {
      setError('Seleccioná cliente, domicilio, servicio y al menos una prenda.');
      return;
    }
    setSubmitting(true);
    try {
      const created = await apiRequest<CreatedOrder>('/orders', {
        method: 'POST',
        body: JSON.stringify({
          clientId,
          addressId,
          serviceCode,
          promotionCode: promotionCode.trim() || null,
          declaredWeightGrams: declaredWeightGrams ? Number(declaredWeightGrams) : null,
          exclusiveCycle,
          pickupScheduledAt: toOffsetDateTime(pickupScheduledAt),
          promisedAt: toOffsetDateTime(promisedAt),
          notes: notes.trim() || null,
          items: validItems.map((item) => ({
            equivalenceCode: item.equivalenceCode,
            physicalPieces: item.physicalPieces,
            observations: item.observations.trim() || null,
          })),
        }),
      });
      navigate(`/orders?created=${encodeURIComponent(created.orderNumber)}`);
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'No se pudo crear el pedido');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) return <section><div className="card muted">Cargando catálogo y clientes…</div></section>;

  return (
    <section>
      <div className="page-heading">
        <div><h1>Nuevo pedido</h1><p className="muted">Carga de piezas, equivalencias y cotización inicial</p></div>
        <Link className="text-link" to="/orders">Volver</Link>
      </div>
      {clients.length === 0 && <div className="alert">Primero debés crear un cliente con domicilio habilitado.</div>}
      <form className="form-grid" onSubmit={(event) => void submit(event)}>
        <article className="card form-grid span-2">
          <div className="span-2 section-divider"><h2>Cliente y servicio</h2></div>
          <label>Cliente
            <select value={clientId} onChange={(event) => changeClient(event.target.value)}>
              {clients.map((client) => <option key={client.id} value={client.id}>{client.lastName}, {client.firstName} — {client.whatsapp}</option>)}
            </select>
          </label>
          <label>Domicilio
            <select value={addressId} onChange={(event) => setAddressId(event.target.value)}>
              {selectedClient?.addresses.map((address) => (
                <option key={address.id} value={address.id}>{address.street} {address.number}, {address.locality}{address.primaryAddress ? ' — principal' : ''}</option>
              ))}
            </select>
          </label>
          <label>Servicio
            <select value={serviceCode} onChange={(event) => setServiceCode(event.target.value)}>
              {services.map((service) => <option key={service.code} value={service.code}>{service.name}</option>)}
            </select>
          </label>
          <label>Promoción opcional<input value={promotionCode} onChange={(event) => setPromotionCode(event.target.value)} placeholder="Ej. FIRST_TRIAL" /></label>
          {selectedService && (
            <div className="span-2 info-box">
              <strong>{selectedService.name}</strong>
              <span>{selectedService.description}</span>
              <span>Límite: {selectedService.maxEquivalentUnits ?? 'sin tope'} unidades / {selectedService.maxWeightGrams ? `${selectedService.maxWeightGrams} g` : 'sin tope de peso'}</span>
              {selectedService.requiresQuote && <span>Este servicio requiere presupuesto manual.</span>}
            </div>
          )}
        </article>

        <article className="card span-2">
          <div className="section-heading"><div><h2>Prendas</h2><p className="muted">Las piezas físicas se conservan; las unidades equivalentes se calculan por grupo.</p></div><button type="button" className="secondary-button" onClick={() => setItems((current) => [...current, { ...emptyItem(), equivalenceCode: equivalences[0]?.code ?? '' }])}>Agregar ítem</button></div>
          <div className="item-list">
            {items.map((item, index) => {
              const rule = equivalences.find((candidate) => candidate.code === item.equivalenceCode);
              const groups = rule ? Math.ceil(item.physicalPieces / rule.physicalUnitsPerGroup) : 0;
              const equivalentUnits = rule ? groups * rule.equivalentUnits : 0;
              return (
                <div className="item-row" key={`${index}-${item.equivalenceCode}`}>
                  <label>Tipo de prenda
                    <select value={item.equivalenceCode} onChange={(event) => updateItem(index, { equivalenceCode: event.target.value })}>
                      {equivalences.map((equivalence) => <option key={equivalence.code} value={equivalence.code}>{equivalence.name} ({equivalence.code})</option>)}
                    </select>
                  </label>
                  <label>Piezas físicas<input type="number" min={1} value={item.physicalPieces} onChange={(event) => updateItem(index, { physicalPieces: Number(event.target.value) })} /></label>
                  <label>Observaciones<input value={item.observations} onChange={(event) => updateItem(index, { observations: event.target.value })} /></label>
                  <div className="item-result"><strong>{equivalentUnits}</strong><span>unidades · {groups} grupos</span></div>
                  <button type="button" className="danger-button" disabled={items.length === 1} onClick={() => setItems((current) => current.filter((_, itemIndex) => itemIndex !== index))}>Quitar</button>
                </div>
              );
            })}
          </div>
          <div className="totals-grid">
            <div><span>Piezas físicas</span><strong>{totals.physicalPieces}</strong></div>
            <div><span>Unidades equivalentes</span><strong>{totals.equivalentUnits}</strong></div>
            <div><span>Peso estimado</span><strong>{totals.estimatedWeightGrams === null ? 'No calculable' : `${totals.estimatedWeightGrams} g`}</strong></div>
            <div><span>Requiere revisión</span><strong>{totals.requiresQuote ? 'Sí' : 'No'}</strong></div>
          </div>
          {totals.exclusiveCycleRequired && <div className="alert">La composición exige ciclo exclusivo aunque la casilla manual no esté seleccionada.</div>}
        </article>

        <article className="card form-grid span-2">
          <div className="span-2 section-divider"><h2>Programación y notas</h2></div>
          <label>Peso declarado en gramos<input type="number" min={1} value={declaredWeightGrams} onChange={(event) => setDeclaredWeightGrams(event.target.value)} /></label>
          <label className="checkbox-label"><input type="checkbox" checked={exclusiveCycle} onChange={(event) => setExclusiveCycle(event.target.checked)} /> Solicitar ciclo exclusivo</label>
          <label>Retiro programado<input type="datetime-local" value={pickupScheduledAt} onChange={(event) => setPickupScheduledAt(event.target.value)} /></label>
          <label>Promesa de entrega<input type="datetime-local" value={promisedAt} onChange={(event) => setPromisedAt(event.target.value)} /></label>
          <label className="span-2">Notas<textarea rows={3} value={notes} onChange={(event) => setNotes(event.target.value)} /></label>
        </article>

        {error && <div className="alert span-2">{error}</div>}
        <button className="span-2" type="submit" disabled={submitting || clients.length === 0}>{submitting ? 'Cotizando…' : 'Crear y cotizar pedido'}</button>
      </form>
    </section>
  );
}
