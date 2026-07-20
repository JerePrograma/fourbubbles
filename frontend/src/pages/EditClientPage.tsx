import { zodResolver } from '@hookform/resolvers/zod';
import { useEffect, useState } from 'react';
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
  const [error, setError] = useState<string | null>(null);
  const { register, handleSubmit, reset, formState: { errors, isSubmitting } } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: {
      status: 'ACTIVE', acquisitionSource: '', fragrance: '', softenerAllowed: false,
      dryerAllowed: false, hypoallergenic: false, separateColors: false,
      specialInstructions: '', notes: '', email: '',
    },
  });

  useEffect(() => {
    if (!id) return;
    apiRequest<Client>(`/clients/${id}`)
      .then((loaded) => {
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
      })
      .catch((cause) => setError(cause instanceof Error ? cause.message : 'No se pudo cargar el cliente'))
      .finally(() => setLoading(false));
  }, [id, reset]);

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

  if (loading) return <section><div className="card muted">Cargando cliente…</div></section>;
  if (!client) return <section><div className="alert">{error ?? 'Cliente inexistente'}</div></section>;

  return (
    <section>
      <div className="page-heading">
        <div><h1>Editar cliente</h1><p className="muted">Perfil, estado y preferencias operativas</p></div>
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

        <div className="span-2 card compact-card">
          <strong>Domicilios</strong>
          {client.addresses.map((address) => (
            <div className="muted" key={address.id}>{address.street} {address.number}, {address.locality} ({address.zoneName}){address.primaryAddress ? ' — principal' : ''}</div>
          ))}
          <div className="muted small-text">La edición histórica de domicilios se mantiene fuera de este corte para no sobrescribir trazabilidad.</div>
        </div>

        {Object.keys(errors).length > 0 && <div className="alert span-2">Revisá los campos marcados.</div>}
        {error && <div className="alert span-2">{error}</div>}
        <button className="span-2" type="submit" disabled={isSubmitting}>{isSubmitting ? 'Guardando…' : 'Guardar cambios'}</button>
      </form>
    </section>
  );
}
