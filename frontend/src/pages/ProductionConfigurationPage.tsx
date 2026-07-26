import { useCallback, useEffect, useState } from 'react';
import { apiRequest } from '../api/httpClient';
import { useAuth } from '../auth/AuthContext';
import type {
  FragrancePolicy,
  MachineStatus,
  ProductionMachine,
  ProductionProgram,
} from '../models/production';
import {
  machineFormFrom,
  machineRequest,
  programFormFrom,
  programRequest,
  type MachineConfigurationForm,
  type ProgramConfigurationForm,
} from '../production/configurationForms';
import '../production-configuration.css';

export function ProductionConfigurationPage(): JSX.Element {
  const { session } = useAuth();
  const isAdmin = session?.roles.includes('ADMIN') ?? false;
  const [machines, setMachines] = useState<ProductionMachine[]>([]);
  const [programs, setPrograms] = useState<ProductionProgram[]>([]);
  const [machineForm, setMachineForm] = useState<MachineConfigurationForm | null>(null);
  const [programForm, setProgramForm] = useState<ProgramConfigurationForm | null>(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const [machineData, programData] = await Promise.all([
        apiRequest<ProductionMachine[]>('/production/machines'),
        apiRequest<ProductionProgram[]>('/production/programs'),
      ]);
      setMachines(machineData);
      setPrograms(programData);
      setMachineForm((current) => {
        const selected = machineData.find((value) => value.id === current?.id) ?? machineData[0];
        return selected ? machineFormFrom(selected) : null;
      });
      setProgramForm((current) => {
        const selected = programData.find((value) => value.id === current?.id) ?? programData[0];
        return selected ? programFormFrom(selected) : null;
      });
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'No se pudo cargar la configuración productiva');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { if (isAdmin) void load(); }, [isAdmin, load]);

  const execute = async (operation: () => Promise<void>, success: string) => {
    setSaving(true);
    setError(null);
    setMessage(null);
    try {
      await operation();
      setMessage(success);
      await load();
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'No se pudo guardar la configuración');
    } finally {
      setSaving(false);
    }
  };

  const saveMachine = () => execute(async () => {
    if (!machineForm || !machineForm.name.trim() || machineForm.capacityGrams <= 0) {
      throw new Error('La máquina requiere nombre y capacidad positiva');
    }
    await apiRequest(`/production/machines/${machineForm.id}`, {
      method: 'PUT',
      body: JSON.stringify(machineRequest(machineForm)),
    });
  }, 'Máquina actualizada.');

  const saveProgram = () => execute(async () => {
    if (!programForm || !programForm.name.trim() || programForm.durationMinutes <= 0) {
      throw new Error('El programa requiere nombre y duración positiva');
    }
    if (programForm.stage === 'WASH'
      && (!programForm.maxTemperatureC || !programForm.fragrancePolicy)) {
      throw new Error('Un programa de lavado requiere temperatura y política de fragancia');
    }
    await apiRequest(`/production/programs/${programForm.id}`, {
      method: 'PUT',
      body: JSON.stringify(programRequest(programForm)),
    });
  }, 'Programa actualizado.');

  if (!isAdmin) {
    return <section><div className="alert">Solo ADMIN puede modificar máquinas y programas.</div></section>;
  }

  return (
    <section>
      <div className="page-heading">
        <div>
          <h1>Configuración de producción</h1>
          <p className="muted">Máquinas y programas existentes</p>
        </div>
        <button className="secondary-button" disabled={loading || saving} onClick={() => void load()}>Actualizar</button>
      </div>

      {error && <div className="alert">{error}</div>}
      {message && <div className="success">{message}</div>}
      {loading && <p className="muted">Cargando configuración…</p>}

      {!loading && <div className="production-configuration-grid">
        <form className="card configuration-form" onSubmit={(event) => { event.preventDefault(); void saveMachine(); }}>
          <h2>Editar máquina</h2>
          <label>Máquina<select value={machineForm?.id ?? ''} onChange={(event) => {
            const selected = machines.find((value) => value.id === event.target.value);
            setMachineForm(selected ? machineFormFrom(selected) : null);
          }}>
            {machines.map((machine) => <option key={machine.id} value={machine.id}>{machine.code} · {machine.name}</option>)}
          </select></label>
          {machineForm && <>
            <label>Código<input value={machineForm.code} disabled /></label>
            <label>Tipo<input value={machineForm.machineType} disabled /></label>
            <label>Nombre<input value={machineForm.name} onChange={(event) => setMachineForm({ ...machineForm, name: event.target.value })} required /></label>
            <label>Capacidad g<input type="number" min="1" value={machineForm.capacityGrams} onChange={(event) => setMachineForm({ ...machineForm, capacityGrams: Number(event.target.value) })} /></label>
            <label>Estado<select value={machineForm.status} onChange={(event) => setMachineForm({ ...machineForm, status: event.target.value as MachineStatus })}>
              <option value="ACTIVE">Activa</option><option value="MAINTENANCE">Mantenimiento</option><option value="OUT_OF_SERVICE">Fuera de servicio</option>
            </select></label>
            <label className="checkbox-label"><input type="checkbox" checked={machineForm.active} onChange={(event) => setMachineForm({ ...machineForm, active: event.target.checked })} />Vigente</label>
            <label>Notas<textarea value={machineForm.notes} onChange={(event) => setMachineForm({ ...machineForm, notes: event.target.value })} /></label>
            <button disabled={saving}>Guardar máquina</button>
            <p className="muted small-text">Código y tipo son inmutables. El backend rechaza cambios mientras exista un ciclo activo.</p>
          </>}
        </form>

        <form className="card configuration-form" onSubmit={(event) => { event.preventDefault(); void saveProgram(); }}>
          <h2>Editar programa</h2>
          <label>Programa<select value={programForm?.id ?? ''} onChange={(event) => {
            const selected = programs.find((value) => value.id === event.target.value);
            setProgramForm(selected ? programFormFrom(selected) : null);
          }}>
            {programs.map((program) => <option key={program.id} value={program.id}>{program.code} · {program.name}</option>)}
          </select></label>
          {programForm && <>
            <label>Código<input value={programForm.code} disabled /></label>
            <label>Etapa<input value={programForm.stage} disabled /></label>
            <label>Nombre<input value={programForm.name} onChange={(event) => setProgramForm({ ...programForm, name: event.target.value })} required /></label>
            <label>Duración min<input type="number" min="1" value={programForm.durationMinutes} onChange={(event) => setProgramForm({ ...programForm, durationMinutes: Number(event.target.value) })} /></label>
            {programForm.stage === 'WASH' && <>
              <label>Temperatura °C<input type="number" min="20" max="95" value={programForm.maxTemperatureC ?? 30} onChange={(event) => setProgramForm({ ...programForm, maxTemperatureC: Number(event.target.value) })} /></label>
              <label>Fragancia<select value={programForm.fragrancePolicy ?? 'NONE'} onChange={(event) => setProgramForm({ ...programForm, fragrancePolicy: event.target.value as FragrancePolicy })}>
                <option value="NONE">Sin fragancia</option><option value="STANDARD">Estándar</option><option value="CUSTOM">Personalizada</option>
              </select></label>
              <label className="checkbox-label"><input type="checkbox" checked={programForm.usesSoftener} onChange={(event) => setProgramForm({ ...programForm, usesSoftener: event.target.checked })} />Usa suavizante</label>
            </>}
            <label className="checkbox-label"><input type="checkbox" checked={programForm.gentle} onChange={(event) => setProgramForm({ ...programForm, gentle: event.target.checked })} />Programa delicado</label>
            <label className="checkbox-label"><input type="checkbox" checked={programForm.active} onChange={(event) => setProgramForm({ ...programForm, active: event.target.checked })} />Vigente</label>
            <label>Notas<textarea value={programForm.notes} onChange={(event) => setProgramForm({ ...programForm, notes: event.target.value })} /></label>
            <button disabled={saving}>Guardar programa</button>
            <p className="muted small-text">Código y etapa son inmutables. Tras el primer uso, la base protege duración y parámetros técnicos.</p>
          </>}
        </form>
      </div>}
    </section>
  );
}
