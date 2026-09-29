import "./Painel.css";

/** Bloco de seção com título, subtítulo opcional e ações no canto. */
export function Secao({ titulo, subtitulo, acoes, children }) {
  return (
    <section className="secao">
      <div className="secao-cabecalho">
        <div>
          <h2 className="secao-titulo">{titulo}</h2>
          {subtitulo && <p className="secao-subtitulo">{subtitulo}</p>}
        </div>
        {acoes && <div className="secao-acoes">{acoes}</div>}
      </div>
      {children}
    </section>
  );
}

/** Selo pequeno colorido — usado para status (aprovado, pendente, etc.). */
export function Badge({ tom = "neutro", children }) {
  return <span className={`badge badge-${tom}`}>{children}</span>;
}

/** Linha de item de lista: título, metadados e ações à direita. */
export function ItemCard({ titulo, meta, badge, acoes, children }) {
  return (
    <div className="item-card">
      <div className="item-card-info">
        <div className="item-card-titulo-linha">
          <p className="item-card-titulo">{titulo}</p>
          {badge}
        </div>
        {meta && <p className="item-card-meta">{meta}</p>}
        {children}
      </div>
      {acoes && <div className="item-card-acoes">{acoes}</div>}
    </div>
  );
}
