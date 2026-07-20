import { zodResolver } from '@hookform/resolvers/zod';
import { useCallback, useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { z } from 'zod';
import { apiRequest } from '../api/httpClient';
import type { Client } from '../models/client';

const schema = z.object({
  firstName: z.string().min(1).max(100),
  lastName: z.string().min(1).max(100),
  phone: z.string().min(6).max(30),
  whatsapp: z.string().min(6).max(30),
  email: z.string().email().or(z.literal('')),
  status: z.enum(['ACTIVE', 'SUSPENDED', 'INACTIVE']),
  acquisitionSource: z.string().max(100),
  fragrance: z.string().max(60),
  softenerAllowed: z.boolean(),
  dryerAllowed: z.boolean(),
  hypoallergenic: z.boolean(),
  separateColors: z.boolean(),
  specialInstructions: z.string().max(500),
  notes: z.string().max(2000),
});

type FormData = z.infer<typeof schema>;

export function EditClientPage(): JSX.Element {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [client, setClient] = useState<Client | null>(null);
  const [loading, setLoading] = useState(true);
  const [workingAddress, setWorkingAddress] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [addressForm, setAddressForm] = useState({
    zoneCode: 'MARCOS_PAZ', street: '', number: '', extra: '', locality: 'Marcos Paz',
    neighborhood: '', references: '', primaryAddress: false,
  });
  const { register, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: {
      status: 'ACTIVE', acquisitionSource: '', fragrance: '', softenerAllowed: false,
      dryerAllowed: false, hypoallergenic: false, separateColors: false,
      specialInstructions: '', notes: '', email: '',
    },
  });

  const load = useCallback(async () => {
    if (!id) return;
    setLoading(true);
    try {
      const loaded = await apiRequest<Client>(`/clients/${id}`);
      setClient(loaded);
      reset({
        firstName: loaded.firstName,
        lastName: loaded.lastName,
        phone: loaded.phone,
        whatsapp: loaded.whatsapp,
        email: loaded.email ?? '',
        status: loaded.status,
        acquisitionSource: loaded.acquisitionSource ?? '',
        fragrance: loaded.preferences?.fragrance ?? '',
        softenerAllowed: loaded.preferences?.softenerAllowed ?? false,
        dryerAllowed: loaded.preferences?.dryerAllowed ?? false,
        hypoallergenic: loaded.preferences?.hypoallergenic ?? false,
        separateColors: loaded.preferences?.separateColors ?? false,
        specialInstructions: loaded.preferences?.specialInstructions ?? '',
        notes: loaded.notes ?? '',
      });
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'No se pudo cargar el cliente');
    } finally {
      setLoading(false);
    }
  }, [id, reset]);

  useEffect(() => { void load(); }, [load]);

  const submit = handleSubmit(async (data) => {
    if (!id) return;
    setError(null);
    try {
      await apiRequest<Client>(`/clients/${id}`, {
        method: 'PUT',
        body: JSON.stringify({
          firstName: data.firstName,
          lastName: data.lastName,
          phone: data.phone,
          whatsapp: data.whatsapp,
          email: data.email || null,
          status: data.status,
          acquisitionSource: data.acquisitionSource || null,
          preferences: {
            fragrance: data.fragrance || null,
            softenerAllowed: data.softenerAllowed,
            dryerAllowed: data.dryerAllowed,
            hypoallergenic: data.hypoallergenic,
            separateColors: data.separateColors,
            specialInstructions: data.specialInstructions || null,
          },
          notes: data.notes || null,
        }),
      });
      navigate(`/clients?updated=${id}`);
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'No se pudo actualizar el cliente');
    }
  });

  const performAddressAction = async (request: () => Promise<Client>) => {
    setWorkingAddress(true);
    setError(null);
    try {
      const updated = await request();
      setClient(updated);
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'No se pudo modificar el domicilio');
    } finally {
      setWorkingAddress(false);
    }
  };

  const addAddress = async (event: React.FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!id || !addressForm.street.trim() || !addressForm.number.trim() || !addressForm.locality.trim()) {
      setError('Calle, número y localidad son obligatorios.');
      return;
    }
    await performAddressAction(() => apiRequest<Client>(`/clients/${id}/addresses`, {
      method: 'POST',
      body: JSON.stringify({
        zoneCode: addressForm.zoneCode,
        street: addressForm.street.trim(),
        number: addressForm.number.trim(),
        extra: addressForm.extra.trim() || null,
        locality: addressForm.locality.trim(),
        neighborhood: addressForm.neighborhood.trim() || null,
        references: addressForm.references.trim() || null,
        primaryAddress: addressForm.primaryAddress,
      }),
    }));
    setAddressForm({ zoneCode: 'MARCOS_PAZ', street: '', number: '', extra: '', locality: 'Marcos Paz', neighborhood: '', references: '', primaryAddress: false });
  };

  if (loading) return <section><div className="card muted">Cargando cliente…</div></section>;
  if (!client) return <section><div className="alert">{error ?? 'Cliente inexistente'}</div></section>;

  return (
    <section>
      <div className="page-heading">
        <div><h1>Editar cliente</h1><p className="muted">Perfil, preferencias y domicilios versionados</p></div>
        <Link className="text-link" to="/clients">Volver</Link>
      </div>
      <form className="card form-grid" onSubmit={(event) => void submit(event)}>
        <label>Nombre<input {...register('firstName')} /></label>
        <label>Apellido<input {...register('lastName')} /></label>
        <label>Teléfono<input inputMode="tel" {...register('phone')} /></label>
        <label>WhatsApp<input inputMode="tel" {...register('whatsapp')} /></label>
        <label>Correo<input type="email" {...register('email')} /></label>
        <label>Estado<select {...register('status')}><option value="ACTIVE">Activo</option><option value="SUSPENDED">Suspendido</option><option value="INACTIVE">Inactivo</option></select></label>
        <label className="span-2">Origen de adquisición<input {...register('acquisitionSource')} /></label>
        <div className="span-2 section-divider"><h2>Preferencias</h2></div>
        <label>Fragancia<input {...register('fragrance')} placeholder="Ej. suave o sin perfume" /></label>
        <label className="checkbox-label"><input type="checkbox" {...register('softenerAllowed')} /> Permite suavizante</label>
        <label className="checkbox-label"><input type="checkbox" {...register('dryerAllowed')} /> Permite secadora</label>
        <label className="checkbox-label"><input type="checkbox" {...register('hypoallergenic')} /> Requiere tratamiento hipoalergénico</label>
        <label className="checkbox-label"><input type="checkbox" {...register('separateColors')} /> Requiere separación de colores</label>
        <label className="span-2">Instrucciones especiales<textarea rows={3} {...register('specialInstructions')} /></label>
        <label className="span-2">Notas internas<textarea rows={3} {...register('notes')} /></label>
        {Object.keys(errors).length > 0 && <div className="alert span-2">Revisá los campos marcados.</div>}
        {error && <div className="alert span-2">{error}</div>}
        <button className="span-2" type="submit" disabled={isSubmitting}>{isSubmitting ? 'Guardando…' : 'Guardar perfil'}</button>
      </form>

      <article className="card address-section">
        <div className="section-heading"><div><h2>Domicilios activos</h2><p className="muted">El historial no se sobrescribe.</p></div></div>
        <div className="list">
          {client.addresses.map((address) => (
            <div className="address-card" key={address.id}>
              <div><strong>{address.street} {address.number}, {address.locality}</strong><div className="muted">{address.zoneName}{address.neighborhood ? ` · ${address.neighborhood}` : ''}</div><div className="muted small-text">Vigente desde {formatDate(address.validFrom)}</div></div>
              <div className="row-actions">
                {address.primaryAddress ? <span className="badge">Principal</span> : (
                  <>
                    <button className="secondary-button compact-button" disabled={workingAddress} onClick={() => void performAddressAction(() => apiRequest<Client>(`/clients/${id}/addresses/${address.id}/make-primary`, { method: 'POST' }))}>Hacer principal</button>
                    <button className="danger-button compact-button" disabled={workingAddress} onClick={() => void performAddressAction(() => apiRequest<Client>(`/clients/${id}/addresses/${address.id}`, { method: 'DELETE' }))}>Desactivar</button>
                  </>
                )}
              </div>
            </div>
          ))}
        </div>

        <div className="section-divider address-add-heading"><h2>Agregar domicilio</h2></div>
        <form className="form-grid" onSubmit={(event) => void addAddress(event)}>
          <label>Zona<select value={addressForm.zoneCode} onChange={(event) => setAddressForm((current) => ({ ...current, zoneCode: event.target.value, locality: event.target.value === 'MARCOS_PAZ' ? 'Marcos Paz' : 'Mariano Acosta' }))}><option value="MARCOS_PAZ">Marcos Paz</option><option value="MARIANO_ACOSTA">Mariano Acosta</option></select></label>
          <label>Localidad<input value={addressForm.locality} onChange={(event) => setAddressForm((current) => ({ ...current, locality: event.target.value }))} /></label>
          <label>Calle<input value={addressForm.street} onChange={(event) => setAddressForm((current) => ({ ...current, street: event.target.value }))} /></label>
          <label>Número<input value={addressForm.number} onChange={(event) => setAddressForm((current) => ({ ...current, number: event.target.value }))} /></label>
          <label>Complemento<input value={addressForm.extra} onChange={(event) => setAddressForm((current) => ({ ...current, extra: event.target.value }))} /></label>
          <label>Barrio<input value={addressForm.neighborhood} onChange={(event) => setAddressForm((current) => ({ ...current, neighborhood: event.target.value }))} /></label>
          <label className="span-2">Referencias<input value={addressForm.references} onChange={(event) => setAddressForm((current) => ({ ...current, references: event.target.value }))} /></label>
          <label className="checkbox-label span-2"><input type="checkbox" checked={addressForm.primaryAddress} onChange={(event) => setAddressForm((current) => ({ ...current, primaryAddress: event.target.checked }))} /> Convertir en domicilio principal</label>
          <button className="span-2" disabled={workingAddress}>Agregar domicilio</button>
        </form>
      </article>

      {client.addressHistory.length > 0 && (
        <article className="card address-section">
          <h2>Historial de domicilios</h2>
          <div className="list">{client.addressHistory.map((address) => (
            <div className="address-card historical-address" key={address.id}>
              <div><strong>{address.street} {address.number}, {address.locality}</strong><div className="muted">{address.zoneName}</div></div>
              <div className="muted small-text">{formatDate(address.validFrom)} → {formatDate(address.validTo)}</div>
            </div>
          ))}</div>
        </article>
      )}
    </section>
  );
}

function formatDate(value: string | null): string {
  if (!value) return 'actualidad';
  return new Intl.DateTimeFormat('es-AR', { dateStyle: 'short', timeStyle: 'short' }).format(new Date(value));
}
