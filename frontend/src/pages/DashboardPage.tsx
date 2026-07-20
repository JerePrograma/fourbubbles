const indicators = [
  ['Pedidos de hoy', '—'],
  ['Retiros pendientes', '—'],
  ['En lavado', '—'],
  ['Listos para entregar', '—'],
];

export function DashboardPage(): JSX.Element {
  return (
    <section>
      <div className="page-heading">
        <div><h1>Agenda operativa</h1><p className="muted">Resumen diario inicial</p></div>
      </div>
      <div className="metric-grid">
        {indicators.map(([label, value]) => <article className="card metric" key={label}><span>{label}</span><strong>{value}</strong></article>)}
      </div>
      <article className="card">
        <h2>Estado de esta entrega</h2>
        <p>El tablero visual está preparado. Las métricas agregadas se implementan en la fase financiera; no se muestran números inventados.</p>
      </article>
    </section>
  );
}
