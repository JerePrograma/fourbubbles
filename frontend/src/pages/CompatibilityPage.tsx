import { useCallback, useEffect, useState } from 'react';
import type { FormEvent } from 'react';
import { Link, useParams } from 'react-router-dom';
import { apiRequest } from '../api/httpClient';
import { useAuth } from '../auth/AuthContext';
import type { PageResponse } from '../models/api';
import type { ColorGroup, CompatibilityEvaluation, FragrancePolicy, MaterialGroup, TreatmentProfile } from '../models/compatibility';
import type { OrderDetail, OrderSummary } from '../models/order';
import '../compatibility.css';

const colors: ColorGroup[] = ['WHITES', 'LIGHT', 'DARK', 'MIXED', 'UNKNOWN'];
const materials: MaterialGroup[] = ['COTTON', 'SYNTHETIC', 'DELICATE', 'WOOL', 'MIXED'];
const fragrances: FragrancePolicy[] = ['NONE', 'STANDARD', 'CUSTOM'];

export function CompatibilityPage(): JSX.Element {
  const { id } = useParams<{ id: string }>();
  const { session } = useAuth();
  const [order, setOrder] = useState<OrderDetail | null>(null);
  const [profile, setProfile] = useState<TreatmentProfile | null>(null);
  const [candidates, setCandidates] = useState<OrderSummary[]>([]);
  const [candidateId, setCandidateId] = useState('');
  const [evaluation, setEvaluation] = useState<CompatibilityEvaluation | null>(null);
  const [colorGroup, setColorGroup] = useState<ColorGroup>('LIGHT');
  const [materialGroup, setMaterialGroup] = useState<MaterialGroup>('COTTON');
  const [maxTemperatureC, setMaxTemperatureC] = useState('40');
  const [dryerAllowed, setDryerAllowed] = useState(true);
  const [fragrancePolicy, setFragrancePolicy] = useState<FragrancePolicy>('STANDARD');
  const [softenerAllowed, setSoftenerAllowed] = useState(true);
  const [hypoallergenic, setHypoallergenic] = useState(false);
  const [babyClothes, setBabyClothes] = useState(false);
  const [petContact, setPetContact] = useState(false);
  const [heavySoil, setHeavySoil] = useState(false);
  const [exclusiveCycle, setExclusiveCycle] = useState(false);
  const [notes, setNotes] = useState('');
  const [exceptionReason, setExceptionReason] = useState('');
  const [loading, setLoading] = useState(true);
  const [working, setWorking] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const canWrite = session?.roles.some((role) => role === 'ADMIN' || role === 'OPERATOR') ?? false;
  const isAdmin = session?.roles.includes('ADMIN') ?? false;

  const applyProfile = (value: TreatmentProfile) => {
    setColorGroup(value.colorGroup);
    setMaterialGroup(value.materialGroup);
    setMaxTemperatureC(String(value.maxTemperatureC));
    setDryerAllowed(value.dryerAllowed);
    setFragrancePolicy(value.fragrancePolicy);
    setSoftenerAllowed(value.softenerAllowed);
    setHypoallergenic(value.hypoallergenic);
    setBabyClothes(value.babyClothes);
    setPetContact(value.petContact);
    setHeavySoil(value.heavySoil);
    setExclusiveCycle(value.exclusiveCycle);
    setNotes(value.notes ?? '');
  };

  const load = useCallback(async () => {
    if (!id) return;
    setLoading(true);
    setError(null);
    try {
      const [loadedOrder, loadedProfile, classified] = await Promise.all([
        apiRequest<OrderDetail>(`/orders/${id}`),
        apiRequest<TreatmentProfile | null>(`/orders/${id}/compatibility-profile`),
        apiRequest<PageResponse<OrderSummary>>('/orders?status=CLASSIFIED&page=0&size=100'),
      ]);
      setOrder(loadedOrder);
      setProfile(loadedProfile);
      if (loadedProfile) applyProfile(loadedProfile);
      const available = classified.content.filter((candidate) => candidate.id !== id);
      setCandidates(available);
      setCandidateId((current) => current || available[0]?.id || '');
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'No se pudo cargar compatibilidad');
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => { void load(); }, [load]);

  const saveProfile = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!id || Number(maxTemperatureC) < 20 || Number(maxTemperatureC) > 95) {
      setError('La temperatura máxima debe estar entre 20 y 95 °C.');
      return;
    }
    setWorking(true); setError(null); setSuccess(null);
    try {
      const saved = await apiRequest<TreatmentProfile>(`/orders/${id}/compatibility-profile`, {
        method: 'PUT',
        body: JSON.stringify({ colorGroup, materialGroup, maxTemperatureC: Number(maxTemperatureC),
          dryerAllowed, fragrancePolicy, softenerAllowed, hypoallergenic, babyClothes,
          petContact, heavySoil, exclusiveCycle, notes: notes.trim() || null }),
      });
      setProfile(saved); applyProfile(saved); setEvaluation(null);
      setSuccess(`Perfil versión ${saved.version} guardado.`);
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'No se pudo guardar el perfil');
    } finally { setWorking(false); }
  };

  const evaluate = async () => {
    if (!id || !candidateId) return;
    setWorking(true); setError(null); setSuccess(null);
    try {
      const result = await apiRequest<CompatibilityEvaluation>('/compatibility/evaluate', {
        method: 'POST', body: JSON.stringify({ orderAId: id, orderBId: candidateId }),
      });
      setEvaluation(result);
      setSuccess(result.effectivelyCompatible ? 'Los pedidos pueden compartir tratamiento según la evaluación vigente.' : 'La evaluación bloquea el tratamiento compartido.');
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'No se pudo evaluar compatibilidad');
    } finally { setWorking(false); }
  };

  const authorizeException = async () => {
    if (!evaluation || !exceptionReason.trim()) {
      setError('La excepción requiere un motivo concreto.');
      return;
    }
    setWorking(true); setError(null); setSuccess(null);
    try {
      const result = await apiRequest<CompatibilityEvaluation>(`/compatibility/evaluations/${evaluation.id}/exception`, {
        method: 'POST', body: JSON.stringify({ reason: exceptionReason.trim() }),
      });
      setEvaluation(result); setSuccess('Excepción administrativa registrada.');
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'No se pudo autorizar la excepción');
    } finally { setWorking(false); }
  };

  if (loading) return <section><div className="card muted">Cargando compatibilidad…</div></section>;
  if (!order) return <section><div className="alert">{error ?? 'Pedido inexistente'}</div></section>;

  return <section>
    <div className="page-heading">
      <div><h1>Compatibilidad {order.orderNumber}</h1><p className="muted">Perfil real, reglas y comparación explicable</p></div>
      <Link className="text-link" to={`/orders/${order.id}`}>Volver al pedido</Link>
    </div>
    {success && <div className="success">{success}</div>}
    {error && <div className="alert">{error}</div>}
    {order.status !== 'CLASSIFIED' && <div className="alert">El perfil solo puede editarse y evaluarse en CLASSIFIED. Estado actual: {order.status}.</div>}

    <form className="card form-grid" onSubmit={(event) => void saveProfile(event)}>
      <h2 className="span-2">Perfil de tratamiento {profile ? `· versión ${profile.version}` : ''}</h2>
      <label>Color<select value={colorGroup} onChange={(event) => setColorGroup(event.target.value as ColorGroup)}>{colors.map((value) => <option key={value}>{value}</option>)}</select></label>
      <label>Material<select value={materialGroup} onChange={(event) => setMaterialGroup(event.target.value as MaterialGroup)}>{materials.map((value) => <option key={value}>{value}</option>)}</select></label>
      <label>Temperatura máxima °C<input type="number" min="20" max="95" value={maxTemperatureC} onChange={(event) => setMaxTemperatureC(event.target.value)} /></label>
      <label>Fragancia<select value={fragrancePolicy} onChange={(event) => setFragrancePolicy(event.target.value as FragrancePolicy)}>{fragrances.map((value) => <option key={value}>{value}</option>)}</select></label>
      <label className="checkbox-label"><input type="checkbox" checked={dryerAllowed} onChange={(event) => setDryerAllowed(event.target.checked)} /> Permite secadora</label>
      <label className="checkbox-label"><input type="checkbox" checked={softenerAllowed} onChange={(event) => setSoftenerAllowed(event.target.checked)} /> Permite suavizante</label>
      <label className="checkbox-label"><input type="checkbox" checked={hypoallergenic} onChange={(event) => setHypoallergenic(event.target.checked)} /> Hipoalergénico</label>
      <label className="checkbox-label"><input type="checkbox" checked={babyClothes} onChange={(event) => setBabyClothes(event.target.checked)} /> Ropa de bebé</label>
      <label className="checkbox-label"><input type="checkbox" checked={petContact} onChange={(event) => setPetContact(event.target.checked)} /> Contacto con mascotas</label>
      <label className="checkbox-label"><input type="checkbox" checked={heavySoil} onChange={(event) => setHeavySoil(event.target.checked)} /> Suciedad pesada</label>
      <label className="checkbox-label span-2"><input type="checkbox" checked={exclusiveCycle} onChange={(event) => setExclusiveCycle(event.target.checked)} /> Exige ciclo exclusivo</label>
      <label className="span-2">Notas<textarea rows={3} value={notes} onChange={(event) => setNotes(event.target.value)} maxLength={1000} /></label>
      {canWrite && order.status === 'CLASSIFIED' && <button className="span-2" disabled={working}>{working ? 'Guardando…' : 'Guardar perfil'}</button>}
    </form>

    <article className="card compatibility-evaluator">
      <h2>Comparar con otro pedido clasificado</h2>
      {!profile && <p className="muted">Guardá primero el perfil de este pedido.</p>}
      {profile && candidates.length === 0 && <p className="muted">No existen otros pedidos clasificados.</p>}
      {profile && candidates.length > 0 && <div className="button-row">
        <select value={candidateId} onChange={(event) => { setCandidateId(event.target.value); setEvaluation(null); }}>
          {candidates.map((candidate) => <option key={candidate.id} value={candidate.id}>{candidate.orderNumber} · {candidate.clientName}</option>)}
        </select>
        {canWrite && <button disabled={working || !candidateId} onClick={() => void evaluate()}>Evaluar compatibilidad</button>}
      </div>}
    </article>

    {evaluation && <EvaluationResult evaluation={evaluation} isAdmin={isAdmin} working={working}
      exceptionReason={exceptionReason} setExceptionReason={setExceptionReason} authorize={authorizeException} />}
  </section>;
}

function EvaluationResult({ evaluation, isAdmin, working, exceptionReason, setExceptionReason, authorize }: {
  evaluation: CompatibilityEvaluation; isAdmin: boolean; working: boolean;
  exceptionReason: string; setExceptionReason: (value: string) => void; authorize: () => Promise<void>;
}): JSX.Element {
  return <div className="compatibility-results">
    <article className={`card ${evaluation.effectivelyCompatible ? 'compatible-card' : 'blocked-card'}`}>
      <h2>{evaluation.effectivelyCompatible ? 'Compatible' : 'Bloqueado'}</h2>
      <p>Regla {evaluation.ruleVersion} · perfiles v{evaluation.profileAVersion}/v{evaluation.profileBVersion}</p>
      {evaluation.overridden && <div className="alert">El resultado original fue exceptuado por administración.</div>}
      <dl className="detail-list">
        <div><dt>Temperatura</dt><dd>{evaluation.recommendation.maxTemperatureC} °C</dd></div>
        <div><dt>Programa</dt><dd>{evaluation.recommendation.programMode}</dd></div>
        <div><dt>Secadora</dt><dd>{evaluation.recommendation.dryerAllowed ? 'Sí' : 'No'}</dd></div>
        <div><dt>Suavizante</dt><dd>{evaluation.recommendation.softenerAllowed ? 'Sí' : 'No'}</dd></div>
        <div><dt>Fragancia</dt><dd>{evaluation.recommendation.fragrancePolicy}</dd></div>
        <div><dt>Modo</dt><dd>{evaluation.recommendation.cycleMode}</dd></div>
      </dl>
    </article>
    <article className="card"><h2>Razones</h2>{evaluation.reasons.length === 0 ? <p className="muted">Sin conflictos ni advertencias.</p> : <ul className="reason-list">{evaluation.reasons.map((reason) => <li key={reason.code}><span className={`badge ${reason.severity === 'HARD' ? 'hard-reason' : 'warning-reason'}`}>{reason.severity}</span><strong>{reason.code}</strong><span>{reason.message}</span></li>)}</ul>}</article>
    {!evaluation.compatible && !evaluation.overridden && isAdmin && <article className="card span-full"><h2>Excepción administrativa</h2><label>Motivo<textarea rows={3} value={exceptionReason} onChange={(event) => setExceptionReason(event.target.value)} /></label><button disabled={working || !exceptionReason.trim()} onClick={() => void authorize()}>Autorizar excepción</button></article>}
    {evaluation.exception && <article className="card span-full"><h2>Excepción registrada</h2><p>{evaluation.exception.reason}</p><p className="muted">{evaluation.exception.authorizedBy} · {new Date(evaluation.exception.authorizedAt).toLocaleString('es-AR')}</p></article>}
  </div>;
}
