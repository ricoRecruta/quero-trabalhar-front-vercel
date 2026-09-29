import "./Estado.css";

/** Mensagem de estado neutra (carregando, lista vazia, etc.). */
export function Aviso({ children }) {
  return <p className="aviso">{children}</p>;
}

/** Bloco de erro com botão de "tentar novamente" opcional. */
export function AvisoErro({ mensagem, onTentarNovamente }) {
  return (
    <div className="aviso aviso-erro" role="alert">
      <p>{mensagem}</p>
      {onTentarNovamente && (
        <button type="button" onClick={onTentarNovamente}>
          Tentar novamente
        </button>
      )}
    </div>
  );
}

/** Paginação simples, no mesmo padrão usado pela tela de Vagas. */
export function Paginacao({ pagina, totalPaginas, carregando, onMudarPagina }) {
  if (!totalPaginas || totalPaginas <= 1) return null;

  return (
    <div className="paginacao">
      <button
        type="button"
        onClick={() => onMudarPagina(Math.max(0, pagina - 1))}
        disabled={pagina === 0 || carregando}
      >
        Anterior
      </button>
      <span>
        Página {pagina + 1} de {totalPaginas}
      </span>
      <button
        type="button"
        onClick={() => onMudarPagina(pagina + 1)}
        disabled={pagina + 1 >= totalPaginas || carregando}
      >
        Próxima
      </button>
    </div>
  );
}
