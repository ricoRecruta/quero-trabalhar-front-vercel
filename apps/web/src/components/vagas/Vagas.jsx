import { useEffect, useState } from "react";
import "./Vagas.css";
import { ApiError, candidatos, oportunidades } from "../../api";
import { vagaParaCartao } from "../../api/mapeadores";

function PinIcon() {
  return (
    <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" strokeWidth="2">
      <path d="M12 21s-7-6.2-7-11a7 7 0 1 1 14 0c0 4.8-7 11-7 11z" />
      <circle cx="12" cy="10" r="2.5" />
    </svg>
  );
}

function BuildingIcon() {
  return (
    <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" strokeWidth="2">
      <rect x="4" y="3" width="16" height="18" rx="2" />
      <path d="M9 7h2M13 7h2M9 11h2M13 11h2M9 15h2M13 15h2" />
    </svg>
  );
}

function BriefcaseIcon() {
  return (
    <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" strokeWidth="2">
      <rect x="3" y="7" width="18" height="13" rx="2" />
      <path d="M8 7V5a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
    </svg>
  );
}

function FilterIcon() {
  return (
    <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" strokeWidth="2">
      <path d="M4 6h16M7 12h10M10 18h4" />
    </svg>
  );
}

const TAMANHO_PAGINA = 12;

function VagaCard({ vaga, onCandidatar, estadoCandidatura }) {
  const enviando = estadoCandidatura === "enviando";
  const concluida = estadoCandidatura === "ok";

  return (
    <div className="vaga-card">
      <h3 className="vaga-cargo">{vaga.cargo}</h3>

      <div className="vaga-info-linha">
        <span className="vaga-info-item">
          <BuildingIcon /> {vaga.publicador}
        </span>
        <span className="vaga-info-item">
          <BriefcaseIcon /> {vaga.modalidade}
        </span>
      </div>

      {vaga.descricao ? (
        <p className="vaga-descricao">{vaga.descricao}</p>
      ) : (
        <div className="vaga-imagem-placeholder" />
      )}

      <div className="vaga-rodape">
        <span className="vaga-info-item">
          <PinIcon /> {vaga.localidade}
        </span>
      </div>

      <button
        type="button"
        className="vaga-candidatar-botao"
        onClick={() => onCandidatar(vaga)}
        disabled={enviando || concluida}
      >
        {enviando ? "Enviando..." : concluida ? "Interesse registrado" : "Candidatar"}
      </button>

      {estadoCandidatura && estadoCandidatura !== "enviando" && estadoCandidatura !== "ok" && (
        <p className="vaga-erro" role="alert">
          {estadoCandidatura}
        </p>
      )}
    </div>
  );
}

export default function Vagas() {
  const [busca, setBusca] = useState("");
  const [termoAplicado, setTermoAplicado] = useState("");
  const [pagina, setPagina] = useState(0);
  const [tentativa, setTentativa] = useState(0);

  const [resultado, setResultado] = useState(null);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState("");
  const [candidaturas, setCandidaturas] = useState({});

  // Espera o usuário parar de digitar antes de consultar a API.
  //
  // O `if` comparando com termoAplicado é essencial: sem ele, este efeito
  // dispara 400ms depois de QUALQUER montagem (mesmo sem o usuário ter
  // digitado nada), religando `carregando` sem que o efeito de busca abaixo
  // rode de novo pra desligá-lo (já que termoAplicado não muda de valor) —
  // o indicador de carregamento ficava preso em "true" pra sempre.
  useEffect(() => {
    const timer = setTimeout(() => {
      const termoLimpo = busca.trim();
      if (termoLimpo === termoAplicado) return;
      setCarregando(true);
      setTermoAplicado(termoLimpo);
      setPagina(0);
    }, 400);
    return () => clearTimeout(timer);
  }, [busca, termoAplicado]);

  // Busca as vagas. O estado só é alterado nos callbacks da promise, nunca no
  // corpo do efeito, para não disparar renderizações em cascata.
  useEffect(() => {
    const controlador = new AbortController();

    oportunidades
      .listar({
        page: pagina,
        size: TAMANHO_PAGINA,
        termo: termoAplicado,
        signal: controlador.signal,
      })
      .then((dados) => {
        setResultado(dados);
        setErro("");
      })
      .catch((err) => {
        if (err.name === "AbortError") return;
        setResultado(null);
        setErro(
          err instanceof ApiError
            ? err.message
            : "Não foi possível carregar as vagas. Verifique se a API está no ar."
        );
      })
      .finally(() => {
        if (!controlador.signal.aborted) setCarregando(false);
      });

    return () => controlador.abort();
  }, [pagina, termoAplicado, tentativa]);

  function irParaPagina(proxima) {
    setCarregando(true);
    setPagina(proxima);
  }

  function tentarNovamente() {
    setCarregando(true);
    setTentativa((n) => n + 1);
  }

  async function handleCandidatar(vaga) {
    setCandidaturas((atual) => ({ ...atual, [vaga.id]: "enviando" }));
    try {
      await candidatos.demonstrarInteresse(vaga.id);
      setCandidaturas((atual) => ({ ...atual, [vaga.id]: "ok" }));
    } catch (err) {
      const mensagem =
        err instanceof ApiError && err.naoAutorizado
          ? "Entre na sua conta de candidato para se candidatar."
          : err instanceof ApiError
            ? err.message
            : "Não foi possível registrar seu interesse.";
      setCandidaturas((atual) => ({ ...atual, [vaga.id]: mensagem }));
    }
  }

  const vagas = (resultado?.itens ?? []).map(vagaParaCartao);
  const totalPaginas = resultado?.totalPaginas ?? 0;

  return (
    <>
      <h1 className="vagas-titulo">Vagas Disponíveis</h1>
      <p className="vagas-subtitulo">
        Encontre oportunidades de emprego na sua região
      </p>

      <div className="vagas-busca-linha">
        <input
          type="text"
          placeholder="Buscar vagas..."
          value={busca}
          onChange={(e) => setBusca(e.target.value)}
          className="vagas-busca-input"
        />
        <button type="button" className="vagas-filtrar-botao">
          <FilterIcon /> Filtrar
        </button>
      </div>

      {erro && (
        <div className="vagas-aviso vagas-aviso-erro" role="alert">
          <p>{erro}</p>
          <button type="button" onClick={tentarNovamente}>
            Tentar novamente
          </button>
        </div>
      )}

      {carregando && <p className="vagas-aviso">Carregando vagas...</p>}

      {!carregando && !erro && vagas.length === 0 && (
        <p className="vagas-aviso">
          {termoAplicado
            ? `Nenhuma vaga encontrada para "${termoAplicado}".`
            : "Nenhuma vaga publicada até o momento."}
        </p>
      )}

      <div className="vagas-grid">
        {vagas.map((vaga) => (
          <VagaCard
            key={vaga.id}
            vaga={vaga}
            onCandidatar={handleCandidatar}
            estadoCandidatura={candidaturas[vaga.id]}
          />
        ))}
      </div>

      {totalPaginas > 1 && (
        <div className="vagas-paginacao">
          <button
            type="button"
            onClick={() => irParaPagina(Math.max(0, pagina - 1))}
            disabled={pagina === 0 || carregando}
          >
            Anterior
          </button>
          <span>
            Página {pagina + 1} de {totalPaginas}
          </span>
          <button
            type="button"
            onClick={() => irParaPagina(pagina + 1)}
            disabled={pagina + 1 >= totalPaginas || carregando}
          >
            Próxima
          </button>
        </div>
      )}
    </>
  );
}
