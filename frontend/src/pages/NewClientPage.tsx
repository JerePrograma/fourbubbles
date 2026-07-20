import { zodResolver } from '@hookform/resolvers/zod';
import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { Link, useNavigate } from 'react-router-dom';
import { z } from 'zod';
import { apiRequest } from '../api/httpClient';

const schema = z.object({
  firstName: z.string().min(1).max(100),
  lastName: z.string().min(1).max(100),
  phone: z.string().min(6).max(30),
  whatsapp: z.string().min(6).max(30),
  email: z.string().email().or(z.literal('')),
  acquisitionSource: z.string().max(100),
  zoneCode: z.enum(['MARCOS_PAZ', 'MARIANO_ACOSTA']),
  street: z.string().min(1).max(160),
  number: z.string().min(1).max(20),
  locality: z.string().min(1).max(120),
  neighborhood: z.string().max(120),
  references: z.string().max(500),
  fragrance: z.string().max(60),
  softenerAllowed: z.boolean(),
  dryerAllowed: z.boolean(),
  hypoallergenic: z.boolean(),
  separateColors: z.boolean(),
  specialInstructions: z.string().max(500),
  notes: z.string().max(2000),
});

type FormData = z.infer<typeof schema>;

export function NewClientPage(): JSX.Element {
  const navigate = useNavigate();
  const [error, setError] = useState<string | null>(null);
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: {
      zoneCode: 'MARCOS_PAZ', acquisitionSource: '', neighborhood: '', references: '',
      fragrance: '', softenerAllowed: false, dryerAllowed: true, hypoallergenic: false,
      separateColors: true, specialInstructions: '', notes: '', email: '',
    },
  });

  const submit = handleSubmit(async (data) => {
    setError(null);
    try {
      const created = await apiRequest<{ id: string }>('/clients', {
        method: 'POST',
        body: JSON.stringify({
          firstName: data.firstName,
          lastName: data.lastName,
          phone: data.phone,
          whatsapp: data.whatsapp,
          email: data.email || null,
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
          addresses: [{
            zoneCode: data.zoneCode,
            street: data.street,
            number: data.number,
            locality: data.locality,
            neighborhood: data.neighborhood || null,
            references: data.references || null,
            primaryAddress: true,
          }],
        }),
      });
      navigate(`/clients?created=${created.id}`);
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'No se pudo crear el cliente');
    }
  });

  return (
    <section>
      <div className="page-heading">
        <div><h1>Nuevo cliente</h1><p className="muted">Alta con domicilio y preferencias operativas</p></div>
        <Link className="text-link" to="/clients">Volver</Link>
      </div>
      <form className="card form-grid" onSubmit={(event) => void submit(event)}>
        <label>Nombre<input {...register('firstName')} /></label>
        <label>Apellido<input {...register('lastName')} /></label>
        <label>Teléfono<input inputMode="tel" {...register('phone')} /></label>
        <label>WhatsApp<input inputMode="tel" {...register('whatsapp')} /></label>
        <label>Correo<input type="email" {...register('email')} /></label>
        <label>Origen<input {...register('acquisitionSource')} placeholder="WhatsApp, referido, redes…" /></label>

        <div className="span-2 section-divider"><h2>Domicilio principal</h2></div>
        <label>Zona<select {...register('zoneCode')}><option value="MARCOS_PAZ">Marcos Paz</option><option value="MARIANO_ACOSTA">Mariano Acosta</option></select></label>
        <label>Localidad<input {...register('locality')} /></label>
        <label>Calle<input {...register('street')} /></label>
        <label>Número<input {...register('number')} /></label>
        <label>Barrio<input {...register('neighborhood')} /></label>
        <label>Referencias<input {...register('references')} /></label>

        <div className="span-2 section-divider"><h2>Preferencias</h2></div>
        <label>Fragancia<input {...register('fragrance')} placeholder="Ej. suave o sin perfume" /></label>
        <label className="checkbox-label"><input type="checkbox" {...register('softenerAllowed')} /> Permite suavizante</label>
        <label className="checkbox-label"><input type="checkbox" {...register('dryerAllowed')} /> Permite secadora</label>
        <label className="checkbox-label"><input type="checkbox" {...register('hypoallergenic')} /> Requiere tratamiento hipoalergénico</label>
        <label className="checkbox-label"><input type="checkbox" {...register('separateColors')} /> Requiere separación de colores</label>
        <label className="span-2">Instrucciones especiales<textarea rows={3} {...register('specialInstructions')} /></label>
        <label className="span-2">Notas internas<textarea rows={3} {...register('notes')} /></label>

        {Object.keys(errors).length > 0 && <div className="alert span-2">Revisá los campos obligatorios y sus longitudes.</div>}
        {error && <div className="alert span-2">{error}</div>}
        <button className="span-2" type="submit" disabled={isSubmitting}>{isSubmitting ? 'Guardando…' : 'Crear cliente'}</button>
      </form>
    </section>
  );
}
