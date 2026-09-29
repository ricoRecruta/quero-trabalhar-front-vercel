import { useEffect, useState } from "react";
import { admin, mensagemDeErro } from "../../api";
import { Aviso, AvisoErro, Paginacao } from "../shared/Estado";
import { ItemCard, Secao } from "../shared/Painel";

/** Admin → Tipos de emprego: aprovar sugestões (individual/lote), criar direto no catálogo, remover. */
export default function AdminTiposDeEmprego() {
  const [pagina, setPagina] = useState(0);
  const [resultado, setResultado] = useState(null);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState("");
  const [tentativa, setTentativa] = useState(0);
  const [selecionados, setSelecionados] = useState([]);
  const [processandoId, setProcessandoId] = useState(null);
  const [processandoLote, setProcessandoLote] = useState(false);

  const [novoTitulo, setNovoTitulo] = useState("");
  const [novaDescricao, setNovaDescricao] = useState("");
  const [criando, setCriando] = useState(false);
  const [mensagemCriar, setMensagemCriar] = useState("");

  useEffect(() => {
    let ativo = true;
    admin
      .listarTiposNaoAprovados({ page: pagina, size: 10 })
      .then((dados) => {
        if (ativo) {
          setResultado(dados);
          setErro("");
          setSelecionados([]);
        }
      })
      .catch((err) => {
        if (ativo) setErro(mensagemDeErro(err, "Não foi possível carregar os tipos pendentes."));
      })
      .finally(() => {
        if (ativo) setCarregando(false);
      });
    return () => {
      ativo = false;
    };
  }, [pagina, tentativa]);

  function alternarSelecao(id) {
    setSelecionados((atual) =>
      atual.includes(id) ? atual.filter((x) => x !== id) : [...atual, id]
    );
  }

  function irParaPagina(p) {
    setCarregando(true);
    setPagina(p);
  }

  function recarregar() {
    setCarregando(true);
    setTentativa((n) => n + 1);
  }

  async function aprovarUm(tipo) {
    setProcessandoId(tipo.id);
    try {
      // A API permite ajustar título/descrição no momento da aprovação —
      // aqui aprovamos com o que já foi sugerido, sem edição.
      await admin.aprovarTipoDeEmprego(tipo.id, { titulo: tipo.titulo, descricao: tipo.descricao });
      recarregar();
    } catch (err) {
      setErro(mensagemDeErro(err, "Não foi possível aprovar."));
    } finally {
      setProcessandoId(null);
    }
  }

  async function removerUm(id) {
    if (!window.confirm("Remover esta sugestão de tipo de emprego?")) return;
    setProcessandoId(id);
    try {
      await admin.removerTipoDeEmprego(id);
      recarregar();
    } catch (err) {
      setErro(mensagemDeErro(err, "Não foi possível remover."));
    } finally {
      setProcessandoId(null);
    }
  }

  async function aprovarSelecionados() {
    setProcessandoLote(true);
    try {
      await admin.aprovarTiposEmLote(selecionados);
      recarregar();
    } catch (err) {
      setErro(mensagemDeErro(err, "Não foi possível aprovar em lote."));
    } finally {
      setProcessandoLote(false);
    }
  }

  async function handleCriar(e) {
    e.preventDefault();
    setCriando(true);
    setMensagemCriar("");
    try {
      await admin.criarTipoDeEmprego({ titulo: novoTitulo, descricao: novaDescricao });
      setMensagemCriar("Tipo criado e já aprovado.");
      setNovoTitulo("");
      setNovaDescricao("");
    } catch (err) {
      setMensagemCriar(mensagemDeErro(err, "Não foi possível criar o tipo."));
    } finally {
      setCriando(false);
    }
  }

  const tipos = resultado?.itens ?? [];

  return (
    <div>
      <Secao titulo="Criar tipo diretamente no catálogo" subtitulo="Já entra aprovado, sem passar pela fila de sugestões.">
        <form onSubmit={handleCriar} className="formulario-linha formulario-linha-2" style={{ marginBottom: 12 }}>
          <div className="formulario-campo">
            <label className="formulario-label" htmlFor="tipo-titulo">Título *</label>
            <input id="tipo-titulo" className="formulario-input" value={novoTitulo} onChange={(e) => setNovoTitulo(e.target.value)} required />
          </div>
          <div className="formulario-campo">
            <label className="formulario-label" htmlFor="tipo-descricao">Descrição</label>
            <input id="tipo-descricao" className="formulario-input" value={novaDescricao} onChange={(e) => setNovaDescricao(e.target.value)} />
          </div>
        </form>
        {mensagemCriar && <p style={{ fontSize: "0.88rem", marginBottom: 8 }}>{mensagemCriar}</p>}
        <button type="button" className="botao-pequeno botao-pequeno-primario" onClick={handleCriar} disabled={criando || !novoTitulo.trim()}>
          {criando ? "Criando..." : "Criar tipo"}
        </button>
      </Secao>

      <Secao
        titulo="Sugestões pendentes de aprovação"
        acoes={
          selecionados.length > 0 && (
            <button type="button" className="botao-pequeno botao-pequeno-primario" onClick={aprovarSelecionados} disabled={processandoLote}>
              {processandoLote ? "Aprovando..." : `Aprovar ${selecionados.length} selecionado(s)`}
            </button>
          )
        }
      >
        {erro && <AvisoErro mensagem={erro} onTentarNovamente={recarregar} />}
        {carregando && <Aviso>Carregando...</Aviso>}
        {!carregando && !erro && tipos.length === 0 && <Aviso>Nenhuma sugestão pendente.</Aviso>}

        <div className="lista-cards">
          {tipos.map((tipo) => (
            <ItemCard
              key={tipo.id}
              titulo={
                <label style={{ display: "flex", alignItems: "center", gap: 8, cursor: "pointer" }}>
                  <input
                    type="checkbox"
                    checked={selecionados.includes(tipo.id)}
                    onChange={() => alternarSelecao(tipo.id)}
                  />
                  {tipo.titulo}
                </label>
              }
              meta={tipo.descricao}
              acoes={
                <>
                  <button type="button" className="botao-pequeno botao-pequeno-primario" onClick={() => aprovarUm(tipo)} disabled={processandoId === tipo.id}>
                    Aprovar
                  </button>
                  <button type="button" className="botao-pequeno botao-pequeno-perigo" onClick={() => removerUm(tipo.id)} disabled={processandoId === tipo.id}>
                    Remover
                  </button>
                </>
              }
            />
          ))}
        </div>

        <Paginacao pagina={pagina} totalPaginas={resultado?.totalPaginas} carregando={carregando} onMudarPagina={irParaPagina} />
      </Secao>
    </div>
  );
}
