import { useCallback, useEffect, useMemo, useState } from 'react';
import { apiRequest } from '../api/httpClient';
import { useAuth } from '../auth/AuthContext';
import type { PageResponse } from '../models/api';
import type { OrderSummary } from '../models/order';
import type {
  MachineStatus,
  MachineType,
  ProductionCycle,
  ProductionCycleStatus,
  ProductionMachine,
  ProductionProgram,
  ProductionStage,
  QualityDecision,
} from '../models/production';
import '../production.css';

const emptyMachine = {
  code: '', name: '', machineType: 'WASHER' as MachineType,
  capacityGrams: 10000, status: 'ACTIVE' as MachineStatus, active: true, notes: '',
};

const emptyProgram = {
  code: '', name: '', stage: 'WASH' as ProductionStage, durationMinutes: 45,
  maxTemperatureC: 30, gentle: false, usesSoftener: false,
  fragrancePolicy: 'NONE', active: true, notes: '',
};

export function ProductionPage(): JSX.Element {
  const { session } = useAuth();
  const canOperate = session?.roles.some((role) => role === 'ADMIN' || role === 'OPERATOR') ?? false;
  const isAdmin = session?.roles.includes('ADMIN') ?? false;
  const [machines, setMachines] = useState<ProductionMachine[]>([]);
  const [programs, setPrograms] = useState<ProductionProgram[]>([]);
  const [cycles, setCycles] = useState<ProductionCycle[]>([]);
  const [candidates, setCandidates] = useState<OrderSummary[]>([]);
  const [programId, setProgramId] = useState('');
  const [machineId, setMachineId] = useState('');
  const [firstOrderId, setFirstOrderId] = useState('');
  const [secondOrderId, setSecondOrderId] = useState('');
  const [cycleNotes, setCycleNotes] = useState('');
  const [actualWeights, setActualWeights] = useState<Record<string, string>>({});
  const [observations, setObservations] = useState<Record<string, string>>({});
  const [machineForm, setMachineForm] = useState(emptyMachine);
  const [programForm, setProgramForm] = useState(emptyProgram);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);

  const selectedProgram = programs.find((program) => program.id === programId) ?? null;
  const selectedStage = selectedProgram?.stage ?? 'WASH';
  const availableMachines = useMemo(() => machines.filter((machine) =>
    machine.active && machine.status === 'ACTIVE'
      && (!selectedProgram || machine.machineType === selectedProgram.requiredMachineType)),
  [machines, selectedProgram]);

  const loadBase = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const [machineData, programData, cyclePage] = await Promise.all([
        apiRequest<ProductionMachine[]>('/production/machines'),
        apiRequest<ProductionProgram[]>('/production/programs'),
        apiRequest<PageResponse<ProductionCycle>>('/production/cycles?page=0&size=50'),
      ]);
      setMachines(machineData);
      setPrograms(programData);
      setCycles(cyclePage.content);
      if (!programId && programData.length > 0) setProgramId(programData[0].id);
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'No se pudo cargar producción');
    } finally {
      setLoading(false);
    }
  }, [programId]);

  const loadCandidates = useCallback(async (stage: ProductionStage) => {
    const statuses = stage === 'WASH'
      ? ['CLASSIFIED', 'WAITING_WASH', 'REWASH_REQUIRED']
      : ['WAITING_DRY'];
    try {
      const pages = await Promise.all(statuses.map((status) =>
        apiRequest<PageResponse<OrderSummary>>(`/orders?status=${status}&page=0&size=100`)));
      const unique = new Map<string, OrderSummary>();
      pages.flatMap((page) => page.content).forEach((order) => unique.set(order.id, order));
      setCandidates([...unique.values()].sort((a, b) => a.orderNumber.localeCompare(b.orderNumber)));
      setFirstOrderId('');
      setSecondOrderId('');
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'No se pudieron cargar los pedidos candidatos');
    }
  }, []);

  useEffect(() => { void loadBase(); }, [loadBase]);
  useEffect(() => { void loadCandidates(selectedStage); }, [loadCandidates, selectedStage]);
  useEffect(() => {
    if (availableMachines.every((machine) => machine.id !== machineId)) {
      setMachineId(availableMachines[0]?.id ?? '');
    }
  }, [availableMachines, machineId]);

  const run = async (operation: () => Promise<void>, successMessage: string) => {
    setSaving(true);
    setError(null);
    setMessage(null);
    try {
      await operation();
      setMessage(successMessage);
      await loadBase();
      await loadCandidates(selectedStage);
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'La operación no pudo completarse');
    } finally {
      setSaving(false);
    }
  };

  const createCycle = () => run(async () => {
    if (!machineId || !programId || !firstOrderId) throw new Error('Seleccioná máquina, programa y pedido principal');
    const orderIds = secondOrderId && secondOrderId !== firstOrderId
      ? [firstOrderId, secondOrderId]
      : [firstOrderId];
    await apiRequest<ProductionCycle>('/production/cycles', {
      method: 'POST',
      headers: { 'Idempotency-Key': `web-cycle-${crypto.randomUUID()}` },
      body: JSON.stringify({ machineId, programId, orderIds, notes: cycleNotes || null }),
    });
    setCycleNotes('');
  }, 'Ciclo planificado correctamente');

  const cycleAction = (cycle: ProductionCycle, action: 'start' | 'complete' | 'cancel') => run(async () => {
    const observation = observations[cycle.id] || null;
    const body = action === 'complete'
      ? { actualWeightGrams: Number(actualWeights[cycle.id]), observation }
      : { observation };
    if (action === 'complete' && (!Number.isFinite(body.actualWeightGrams) || Number(body.actualWeightGrams) <= 0)) {
      throw new Error('Informá el peso real del ciclo');
    }
    await apiRequest<ProductionCycle>(`/production/cycles/${cycle.id}/${action}`, {
      method: 'POST', body: JSON.stringify(body),
    });
  }, action === 'start' ? 'Ciclo iniciado' : action === 'complete' ? 'Ciclo completado' : 'Ciclo cancelado');

  const quality = (orderId: string, decision: QualityDecision) => run(async () => {
    const observation = observations[orderId]?.trim();
    if (!observation) throw new Error('El control de calidad requiere observación');
    await apiRequest(`/production/orders/${orderId}/quality-control`, {
      method: 'PATCH', body: JSON.stringify({ decision, observation }),
    });
  }, decision === 'PASS' ? 'Control de calidad aprobado' : 'Pedido enviado a relavado');

  const createMachine = () => run(async () => {
    await apiRequest('/production/machines', {
      method: 'POST', body: JSON.stringify({ ...machineForm, notes: machineForm.notes || null }),
    });
    setMachineForm(emptyMachine);
  }, 'Máquina creada');

  const createProgram = () => run(async () => {
    const isWash = programForm.stage === 'WASH';
    await apiRequest('/production/programs', {
      method: 'POST',
      body: JSON.stringify({
        ...programForm,
        maxTemperatureC: isWash ? Number(programForm.maxTemperatureC) : null,
        fragrancePolicy: isWash ? programForm.fragrancePolicy : null,
        usesSoftener: isWash ? programForm.usesSoftener : false,
        notes: programForm.notes || null,
      }),
    });
    setProgramForm(emptyProgram);
  }, 'Programa creado');

  return (
    <section>
      <div className="page-heading">
        <div><h1>Producción</h1><p className="muted">Máquinas, programas, ciclos y control de calidad</p></div>
      </div>
      {error && <div className="alert">{error}</div>}
      {message && <div className="success">{message}</div>}
      {loading && <p className="muted">Cargando producción…</p>}

      {canOperate && (
        <div className="card production-planner">
          <h2>Planificar ciclo</h2>
          <div className="production-grid">
            <label>Programa<select value={programId} onChange={(event) => setProgramId(event.target.value)}>
              <option value="">Seleccionar</option>
              {programs.filter((program) => program.active).map((program) =>
                <option key={program.id} value={program.id}>{program.name} · {program.stage}</option>)}
            </select></label>
            <label>Máquina<select value={machineId} onChange={(event) => setMachineId(event.target.value)}>
              <option value="">Seleccionar</option>
              {availableMachines.map((machine) =>
                <option key={machine.id} value={machine.id}>{machine.name} · {formatWeight(machine.capacityGrams)}</option>)}
            </select></label>
            <label>Pedido principal<select value={firstOrderId} onChange={(event) => setFirstOrderId(event.target.value)}>
              <option value="">Seleccionar</option>
              {candidates.map((order) => <option key={order.id} value={order.id}>{order.orderNumber} · {order.clientName}</option>)}
            </select></label>
            <label>Segundo pedido<select value={secondOrderId} onChange={(event) => setSecondOrderId(event.target.value)}>
              <option value="">Ciclo individual</option>
              {candidates.filter((order) => order.id !== firstOrderId).map((order) =>
                <option key={order.id} value={order.id}>{order.orderNumber} · {order.clientName}</option>)}
            </select></label>
          </div>
          <label>Notas<textarea value={cycleNotes} onChange={(event) => setCycleNotes(event.target.value)} /></label>
          <button disabled={saving} onClick={() => void createCycle()}>Planificar ciclo</button>
          <p className="muted small-text">El backend valida perfiles, programa, compatibilidad vigente, exclusividad, peso y disponibilidad.</p>
        </div>
      )}

      <div className="card table-wrap">
        <h2>Ciclos</h2>
        {cycles.length === 0 && !loading && <p className="muted">Todavía no hay ciclos.</p>}
        {cycles.length > 0 && <table>
          <thead><tr><th>Ciclo</th><th>Etapa</th><th>Máquina / programa</th><th>Peso</th><th>Pedidos</th><th>Estado</th><th>Operación</th></tr></thead>
          <tbody>{cycles.map((cycle) => <tr key={cycle.id}>
            <td><strong>{cycle.cycleNumber}</strong><div className="muted small-text">{formatDate(cycle.createdAt)}</div></td>
            <td>{cycle.stage}</td>
            <td>{cycle.machineCode}<div className="muted small-text">{cycle.programCode}</div></td>
            <td>{formatWeight(cycle.actualWeightGrams ?? cycle.plannedWeightGrams)}<div className="muted small-text">Plan: {formatWeight(cycle.plannedWeightGrams)}</div></td>
            <td>{cycle.orders.map((order) => <div key={order.orderId}>{order.orderNumber} · {order.orderStatus}{order.separationRequired ? ' · separación' : ''}</div>)}</td>
            <td><span className="badge">{cycle.status}</span></td>
            <td>
              {canOperate && cycle.status === 'PLANNED' && <>
                <button onClick={() => void cycleAction(cycle, 'start')} disabled={saving}>Iniciar</button>
                <button className="secondary-button" onClick={() => void cycleAction(cycle, 'cancel')} disabled={saving}>Cancelar</button>
              </>}
              {canOperate && cycle.status === 'RUNNING' && <>
                <input className="compact-input" type="number" min="1" placeholder="Peso real g" value={actualWeights[cycle.id] ?? ''} onChange={(event) => setActualWeights((values) => ({ ...values, [cycle.id]: event.target.value }))} />
                <button onClick={() => void cycleAction(cycle, 'complete')} disabled={saving}>Completar</button>
              </>}
              {canOperate && (cycle.status === 'PLANNED' || cycle.status === 'RUNNING') &&
                <textarea className="compact-textarea" placeholder="Observación" value={observations[cycle.id] ?? ''} onChange={(event) => setObservations((values) => ({ ...values, [cycle.id]: event.target.value }))} />}
              {canOperate && cycle.orders.filter((order) => order.orderStatus === 'QUALITY_CONTROL').map((order) => <div className="quality-actions" key={order.orderId}>
                <textarea placeholder={`Observación ${order.orderNumber}`} value={observations[order.orderId] ?? ''} onChange={(event) => setObservations((values) => ({ ...values, [order.orderId]: event.target.value }))} />
                <button onClick={() => void quality(order.orderId, 'PASS')} disabled={saving}>Aprobar</button>
                <button className="secondary-button" onClick={() => void quality(order.orderId, 'REWASH')} disabled={saving}>Relavar</button>
              </div>)}
            </td>
          </tr>)}</tbody>
        </table>}
      </div>

      {isAdmin && <div className="production-admin">
        <form className="card" onSubmit={(event) => { event.preventDefault(); void createMachine(); }}>
          <h2>Nueva máquina</h2>
          <label>Código<input value={machineForm.code} onChange={(event) => setMachineForm({ ...machineForm, code: event.target.value })} required /></label>
          <label>Nombre<input value={machineForm.name} onChange={(event) => setMachineForm({ ...machineForm, name: event.target.value })} required /></label>
          <label>Tipo<select value={machineForm.machineType} onChange={(event) => setMachineForm({ ...machineForm, machineType: event.target.value as MachineType })}><option value="WASHER">Lavadora</option><option value="DRYER">Secadora</option></select></label>
          <label>Capacidad g<input type="number" min="1" value={machineForm.capacityGrams} onChange={(event) => setMachineForm({ ...machineForm, capacityGrams: Number(event.target.value) })} /></label>
          <button disabled={saving}>Crear máquina</button>
        </form>
        <form className="card" onSubmit={(event) => { event.preventDefault(); void createProgram(); }}>
          <h2>Nuevo programa</h2>
          <label>Código<input value={programForm.code} onChange={(event) => setProgramForm({ ...programForm, code: event.target.value })} required /></label>
          <label>Nombre<input value={programForm.name} onChange={(event) => setProgramForm({ ...programForm, name: event.target.value })} required /></label>
          <label>Etapa<select value={programForm.stage} onChange={(event) => setProgramForm({ ...programForm, stage: event.target.value as ProductionStage })}><option value="WASH">Lavado</option><option value="DRY">Secado</option></select></label>
          <label>Duración min<input type="number" min="1" value={programForm.durationMinutes} onChange={(event) => setProgramForm({ ...programForm, durationMinutes: Number(event.target.value) })} /></label>
          {programForm.stage === 'WASH' && <>
            <label>Temperatura<input type="number" min="20" max="95" value={programForm.maxTemperatureC} onChange={(event) => setProgramForm({ ...programForm, maxTemperatureC: Number(event.target.value) })} /></label>
            <label>Fragancia<select value={programForm.fragrancePolicy} onChange={(event) => setProgramForm({ ...programForm, fragrancePolicy: event.target.value })}><option value="NONE">Sin fragancia</option><option value="STANDARD">Estándar</option><option value="CUSTOM">Personalizada</option></select></label>
            <label className="checkbox-label"><input type="checkbox" checked={programForm.usesSoftener} onChange={(event) => setProgramForm({ ...programForm, usesSoftener: event.target.checked })} />Usa suavizante</label>
          </>}
          <label className="checkbox-label"><input type="checkbox" checked={programForm.gentle} onChange={(event) => setProgramForm({ ...programForm, gentle: event.target.checked })} />Programa delicado</label>
          <button disabled={saving}>Crear programa</button>
        </form>
      </div>}
    </section>
  );
}

function formatWeight(grams: number): string {
  return `${new Intl.NumberFormat('es-AR').format(grams)} g`;
}

function formatDate(value: string): string {
  return new Intl.DateTimeFormat('es-AR', { dateStyle: 'short', timeStyle: 'short' }).format(new Date(value));
}
