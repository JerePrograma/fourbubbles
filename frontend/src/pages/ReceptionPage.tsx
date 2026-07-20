import { useCallback, useEffect, useState } from 'react';
import type { FormEvent } from 'react';
import { Link, useParams } from 'react-router-dom';
import { apiRequest } from '../api/httpClient';
import { useAuth } from '../auth/AuthContext';
import type { OrderDetail } from '../models/order';
import type { ReceptionApprovalStatus, ReceptionDetail } from '../models/reception';
import '../reception.css';

type EditableItem = {
  equivalenceCode: string;
  name: string;
  declared: number;
  actual: string;
  damageDetected: boolean;
  stainDetected: boolean;
  observations: string;
};

export function ReceptionPage(): JSX.Element {
  const { id } = useParams<{ id: string }>();
  const { session } = useAuth();
  const [order, setOrder] = useState<OrderDetail | null>(null);
  const [reception, setReception] = useState<ReceptionDetail | null>(null);
  const [items, setItems] = useState<EditableItem[]>([]);
  const [actualWeight, setActualWeight] = useState('');
  const [receivedAt, setReceivedAt] = useState('');
  const [conditionNotes, setConditionNotes] = useState('');
  const [bagCode, setBagCode] = useState('');
  const [objectKey, setObjectKey] = useState('');
  const [fileName, setFileName] = useState('');
  const [contentType, setContentType] = useState('image/jpeg');
  const [sizeBytes, setSizeBytes] = useState('');
  const [sha256, setSha256] = useState('');
  const [caption, setCaption] = useState('');
  const [decisionNotes, setDecisionNotes] = useState('');
  const [idempotencyKey] = useState(() => `web-reception-${crypto.randomUUID()}`);
  const [loading, setLoading] = useState(true);
  const [working, setWorking] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const canWrite = session?.roles.some((role) => role === 'ADMIN' || role === 'OPERATOR') ?? false;

  const load = useCallback(async () => {
    if (!id) return;
    setLoading(true);
    setError(null);
    try {
      const [loadedOrder, loadedReception] = await Promise.all([
        apiRequest<OrderDetail>(`/orders/${id}`),
        apiRequest<ReceptionDetail | null>(`/orders/${id}/reception`),
      ]);
      setOrder(loadedOrder);
      setReception(loadedReception);
      if (!loadedReception) {
        setItems(loadedOrder.items.map((item) => ({
          equivalenceCode: item.equivalenceCode,
          name: item.name,
          declared: item.physicalPieces,
          actual: String(item.physicalPieces),
          damageDetected: false,
          stainDetected: false,
          observations: '',
        })));
        setActualWeight(loadedOrder.declaredWeightGrams ? String(loadedOrder.declaredWeightGrams) : '');
      }
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'No se pudo cargar la recepción');
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => { void load(); }, [load]);

  const updateItem = (index: number, patch: Partial<EditableItem>) => {
    setItems((current) => current.map((item, itemIndex) => itemIndex === index ? { ...item, ...patch } : item));
  };

  const submitReception = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!id || Number(actualWeight) <= 0) {
      setError('Ingresá un peso real positivo.');
      return;
    }
    if (items.some((item) => Number(item.actual) < 0 || !Number.isInteger(Number(item.actual)))) {
      setError('Las cantidades reales deben ser enteros no negativos.');
      return;
    }
    if (objectKey.trim() && (!fileName.trim() || !sizeBytes || !/^[0-9a-fA-F]{64}$/.test(sha256))) {
      setError('La evidencia requiere nombre, tamaño y SHA-256 de 64 caracteres hexadecimales.');
      return;
    }
    setWorking(true);
    setError(null);
    setSuccess(null);
    try {
      const evidences = objectKey.trim() ? [{
        objectKey: objectKey.trim(),
        fileName: fileName.trim(),
        contentType: contentType.trim(),
        sizeBytes: Number(sizeBytes),
        sha256: sha256.trim(),
        caption: caption.trim() || null,
      }] : [];
      const result = await apiRequest<ReceptionDetail>(`/orders/${id}/reception`, {
        method: 'POST',
        headers: { 'Idempotency-Key': idempotencyKey },
        body: JSON.stringify({
          receivedAt: receivedAt ? new Date(receivedAt).toISOString() : null,
          actualWeightGrams: Number(actualWeight),
          conditionNotes: conditionNotes.trim() || null,
          bagCode: bagCode.trim() || null,
          items: items.map((item) => ({
            equivalenceCode: item.equivalenceCode,
            actualPhysicalPieces: Number(item.actual),
            damageDetected: item.damageDetected,
            stainDetected: item.stainDetected,
            observations: item.observations.trim() || null,
          })),
          evidences,
        }),
      });
      setReception(result);
      setSuccess(result.requiresCustomerApproval
        ? 'Recepción registrada. Las diferencias requieren aprobación.'
        : 'Recepción registrada y pedido clasificado.');
      await load();
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'No se pudo registrar la recepción');
    } finally {
      setWorking(false);
    }
  };

  const decide = async (decision: Extract<ReceptionApprovalStatus, 'APPROVED' | 'REJECTED'>) => {
    if (!id) return;
    setWorking(true);
    setError(null);
    setSuccess(null);
    try {
      const result = await apiRequest<ReceptionDetail>(`/orders/${id}/reception/decision`, {
        method: 'POST',
        body: JSON.stringify({ decision, notes: decisionNotes.trim() || null }),
      });
      setReception(result);
      setSuccess(decision === 'APPROVED' ? 'Diferencias aprobadas; pedido clasificado.' : 'Diferencias rechazadas; pedido cancelado.');
      await load();
    } catch (cause) {
      setError(cause instanceof Error ? cause.message : 'No se pudo registrar la decisión');
    } finally {
      setWorking(false);
    }
  };

  if (loading) return <section><div className="card muted">Cargando recepción…</div></section>;
  if (!order) return <section><div className="alert">{error ?? 'Pedido inexistente'}</div></section>;

  return (
    <section>
      <div className="page-heading">
        <div><h1>Recepción {order.orderNumber}</h1><p className="muted">Conteo, peso, inspección y diferencias reales</p></div>
        <Link className="text-link" to={`/orders/${order.id}`}>Volver al pedido</Link>
      </div>
      {success && <div className="success">{success}</div>}
      {error && <div className="alert">{error}</div>}

      {reception ? (
        <ReceptionSummary reception={reception} canWrite={canWrite} working={working}
          decisionNotes={decisionNotes} setDecisionNotes={setDecisionNotes} decide={decide} />
      ) : (
        <>
          {order.status !== 'PICKED_UP' && <div className="alert">La recepción solo puede registrarse cuando el pedido está en PICKED_UP. Estado actual: {order.status}.</div>}
          {!canWrite && <div className="card muted">Tu rol permite consultar la recepción, pero no registrarla.</div>}
          {canWrite && order.status === 'PICKED_UP' && (
            <form className="reception-layout" onSubmit={(event) => void submitReception(event)}>
              <article className="card form-grid">
                <h2 className="span-2">Datos generales</h2>
                <label>Peso declarado<input value={order.declaredWeightGrams ?? ''} disabled /></label>
                <label>Peso real (g)<input type="number" min="1" value={actualWeight} onChange={(event) => setActualWeight(event.target.value)} required /></label>
                <label>Fecha y hora<input type="datetime-local" value={receivedAt} onChange={(event) => setReceivedAt(event.target.value)} /></label>
                <label>Código de bolsa<input value={bagCode} onChange={(event) => setBagCode(event.target.value)} maxLength={100} /></label>
                <label className="span-2">Condición general<textarea rows={3} value={conditionNotes} onChange={(event) => setConditionNotes(event.target.value)} maxLength={2000} /></label>
              </article>

              <article className="card table-wrap span-full">
                <h2>Conteo e inspección por prenda</h2>
                <table>
                  <thead><tr><th>Prenda</th><th>Declarado</th><th>Real</th><th>Daño</th><th>Mancha</th><th>Observaciones</th></tr></thead>
                  <tbody>{items.map((item, index) => (
                    <tr key={item.equivalenceCode}>
                      <td>{item.name}<div className="muted small-text">{item.equivalenceCode}</div></td>
                      <td>{item.declared}</td>
                      <td><input className="compact-input" type="number" min="0" step="1" value={item.actual} onChange={(event) => updateItem(index, { actual: event.target.value })} /></td>
                      <td><input type="checkbox" checked={item.damageDetected} onChange={(event) => updateItem(index, { damageDetected: event.target.checked })} /></td>
                      <td><input type="checkbox" checked={item.stainDetected} onChange={(event) => updateItem(index, { stainDetected: event.target.checked })} /></td>
                      <td><input value={item.observations} onChange={(event) => updateItem(index, { observations: event.target.value })} maxLength={1000} /></td>
                    </tr>
                  ))}</tbody>
                </table>
              </article>

              <article className="card form-grid span-full">
                <h2 className="span-2">Evidencia externa opcional</h2>
                <p className="muted span-2">La aplicación registra metadatos. El archivo debe existir previamente en el almacenamiento definido por la operación.</p>
                <label className="span-2">Clave del objeto<input value={objectKey} onChange={(event) => setObjectKey(event.target.value)} placeholder="receptions/pedido/frente.jpg" /></label>
                <label>Nombre<input value={fileName} onChange={(event) => setFileName(event.target.value)} /></label>
                <label>Tipo MIME<input value={contentType} onChange={(event) => setContentType(event.target.value)} /></label>
                <label>Tamaño en bytes<input type="number" min="1" value={sizeBytes} onChange={(event) => setSizeBytes(event.target.value)} /></label>
                <label>SHA-256<input value={sha256} onChange={(event) => setSha256(event.target.value)} maxLength={64} /></label>
                <label className="span-2">Descripción<input value={caption} onChange={(event) => setCaption(event.target.value)} maxLength={500} /></label>
              </article>

              <button className="span-full" disabled={working}>{working ? 'Registrando…' : 'Registrar recepción'}</button>
            </form>
          )}
        </>
      )}
    </section>
  );
}

function ReceptionSummary({ reception, canWrite, working, decisionNotes, setDecisionNotes, decide }: {
  reception: ReceptionDetail;
  canWrite: boolean;
  working: boolean;
  decisionNotes: string;
  setDecisionNotes: (value: string) => void;
  decide: (decision: 'APPROVED' | 'REJECTED') => Promise<void>;
}): JSX.Element {
  return (
    <>
      <div className="metric-grid">
        <article className="card metric"><span className="muted">Etiqueta</span><strong>{reception.labelCode}</strong></article>
        <article className="card metric"><span className="muted">Estado</span><strong className="metric-text">{reception.orderStatus}</strong></article>
        <article className="card metric"><span className="muted">Aprobación</span><strong>{reception.approvalStatus}</strong></article>
        <article className="card metric"><span className="muted">Peso real</span><strong>{reception.actualWeightGrams} g</strong></article>
      </div>
      <div className="detail-grid">
        <article className="card">
          <h2>Resumen</h2>
          <dl className="detail-list">
            <div><dt>Recibido</dt><dd>{formatDate(reception.receivedAt)}</dd></div>
            <div><dt>Piezas declaradas</dt><dd>{reception.declaredPhysicalPieces}</dd></div>
            <div><dt>Piezas reales</dt><dd>{reception.actualPhysicalPieces}</dd></div>
            <div><dt>Diferencia</dt><dd>{signed(reception.pieceDifference)}</dd></div>
            <div><dt>Peso declarado</dt><dd>{reception.declaredWeightGrams === null ? '—' : `${reception.declaredWeightGrams} g`}</dd></div>
            <div><dt>Diferencia de peso</dt><dd>{reception.weightDifferenceGrams === null ? '—' : `${signed(reception.weightDifferenceGrams)} g`}</dd></div>
            <div><dt>Daño</dt><dd>{reception.damageDetected ? 'Sí' : 'No'}</dd></div>
            <div><dt>Mancha</dt><dd>{reception.stainDetected ? 'Sí' : 'No'}</dd></div>
            <div><dt>Bolsa</dt><dd>{reception.bagCode ?? '—'}</dd></div>
          </dl>
          {reception.conditionNotes && <p>{reception.conditionNotes}</p>}
        </article>
        <article className="card">
          <h2>Decisión</h2>
          {!reception.requiresCustomerApproval && <div className="success">No se detectaron diferencias que requieran aprobación.</div>}
          {reception.approvalStatus === 'PENDING' && canWrite && (
            <div className="form-stack">
              <label>Notas<textarea rows={4} value={decisionNotes} onChange={(event) => setDecisionNotes(event.target.value)} /></label>
              <div className="button-row">
                <button disabled={working} onClick={() => void decide('APPROVED')}>Aprobar diferencias</button>
                <button className="danger-button" disabled={working} onClick={() => void decide('REJECTED')}>Rechazar</button>
              </div>
            </div>
          )}
          {reception.approvalStatus !== 'PENDING' && reception.approvalStatus !== 'NOT_REQUIRED' && (
            <dl className="detail-list">
              <div><dt>Resultado</dt><dd>{reception.approvalStatus}</dd></div>
              <div><dt>Actor</dt><dd>{reception.approvalBy ?? '—'}</dd></div>
              <div><dt>Fecha</dt><dd>{reception.approvalAt ? formatDate(reception.approvalAt) : '—'}</dd></div>
              <div><dt>Notas</dt><dd>{reception.approvalNotes ?? '—'}</dd></div>
            </dl>
          )}
        </article>
      </div>
      <article className="card table-wrap">
        <h2>Composición real</h2>
        <table><thead><tr><th>Prenda</th><th>Declarado</th><th>Real</th><th>Diferencia</th><th>Daño</th><th>Mancha</th><th>Observaciones</th></tr></thead>
          <tbody>{reception.items.map((item) => <tr key={item.equivalenceCode}>
            <td>{item.equivalenceName}<div className="muted small-text">{item.equivalenceCode}</div></td>
            <td>{item.declaredPhysicalPieces}</td><td>{item.actualPhysicalPieces}</td><td>{signed(item.pieceDifference)}</td>
            <td>{item.damageDetected ? 'Sí' : 'No'}</td><td>{item.stainDetected ? 'Sí' : 'No'}</td><td>{item.observations ?? '—'}</td>
          </tr>)}</tbody>
        </table>
      </article>
      {reception.evidences.length > 0 && <article className="card"><h2>Evidencias registradas</h2>{reception.evidences.map((evidence) => <div key={evidence.id} className="evidence-row"><strong>{evidence.fileName}</strong><span>{evidence.contentType} · {evidence.sizeBytes} bytes</span><code>{evidence.objectKey}</code><span>{evidence.caption ?? 'Sin descripción'}</span></div>)}</article>}
    </>
  );
}

function signed(value: number): string { return value > 0 ? `+${value}` : String(value); }
function formatDate(value: string): string { return new Intl.DateTimeFormat('es-AR', { dateStyle: 'short', timeStyle: 'short' }).format(new Date(value)); }
