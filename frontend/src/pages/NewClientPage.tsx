import { zodResolver } from '@hookform/resolvers/zod';
import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { useNavigate } from 'react-router-dom';
import { z } from 'zod';
import { apiRequest } from '../api/httpClient';

const schema = z.object({
  firstName: z.string().min(1), lastName: z.string().min(1), phone: z.string().min(6), whatsapp: z.string().min(6),
  email: z.string().email().or(z.literal('')), zoneCode: z.enum(['MARCOS_PAZ', 'MARIANO_ACOSTA']),
  street: z.string().min(1), number: z.string().min(1), locality: z.string().min(1), neighborhood: z.string().optional(), references: z.string().optional(),
});
type FormData = z.infer<typeof schema>;

export function NewClientPage(): JSX.Element {
  const navigate = useNavigate();
  const [error, setError] = useState<string | null>(null);
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<FormData>({ resolver: zodResolver(schema), defaultValues: { zoneCode: 'MARCOS_PAZ' } });
  const submit = handleSubmit(async (data) => {
    setError(null);
    try {
      const created = await apiRequest<{ id: string }>('/clients', { method: 'POST', body: JSON.stringify({
        firstName: data.firstName, lastName: data.lastName, phone: data.phone, whatsapp: data.whatsapp,
        email: data.email || null, preferencesJson: '{}', addresses: [{ zoneCode: data.zoneCode, street: data.street,
          number: data.number, locality: data.locality, neighborhood: data.neighborhood, references: data.references, primaryAddress: true }],
      }) });
      navigate(`/clients?created=${created.id}`);
    } catch (cause) { setError(cause instanceof Error ? cause.message : 'No se pudo crear el cliente'); }
  });
  return (
    <section><div className="page-heading"><div><h1>Nuevo cliente</h1><p className="muted">Alta con domicilio principal</p></div></div>
      <form className="card form-grid" onSubmit={(e) => void submit(e)}>
        <label>Nombre<input {...register('firstName')} /></label><label>Apellido<input {...register('lastName')} /></label>
        <label>Teléfono<input inputMode="tel" {...register('phone')} /></label><label>WhatsApp<input inputMode="tel" {...register('whatsapp')} /></label>
        <label className="span-2">Correo<input type="email" {...register('email')} /></label>
        <label>Zona<select {...register('zoneCode')}><option value="MARCOS_PAZ">Marcos Paz</option><option value="MARIANO_ACOSTA">Mariano Acosta</option></select></label>
        <label>Localidad<input {...register('locality')} /></label><label>Calle<input {...register('street')} /></label><label>Número<input {...register('number')} /></label>
        <label>Barrio<input {...register('neighborhood')} /></label><label>Referencias<input {...register('references')} /></label>
        {Object.keys(errors).length > 0 && <div className="alert span-2">Revisá los campos obligatorios.</div>}
        {error && <div className="alert span-2">{error}</div>}
        <button className="span-2" type="submit" disabled={isSubmitting}>{isSubmitting ? 'Guardando…' : 'Crear cliente'}</button>
      </form>
    </section>
  );
}
