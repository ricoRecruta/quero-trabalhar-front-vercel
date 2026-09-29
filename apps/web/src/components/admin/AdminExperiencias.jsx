import { useEffect, useState } from "react";
import { admin, mensagemDeErro, tiposDeEmprego } from "../../api";
import { Aviso, AvisoErro, Paginacao } from "../shared/Estado";
import { ItemCard } from "../shared/Painel";
import SeletorTipoDeEmprego from "../shared/SeletorTipoDeEmprego";

function FormularioEdicao({ experiencia, onSalvo, onCancelar }) {
  const [tipoDeEmpregoId, setTipoDeEmpregoId] = useState(experiencia.tipoDeEmprego);
  const [descricao, setDescricao] = useState(experiencia.descricao);
  const [dataInicio, setDataInicio] = useState(experiencia.dataInicio);
  const [dataFim, setDataFim] = useState(experiencia.dataFim ?? "");
  const [salvando, setSalvando] = useState(false);
  const [erro, setErro] = useState("");

  async function handleSubmit(e) {
    e.preventDefault();
    setSalvando(true);
    setErro("");
    try {
      await admin.atualizarExperiencia(experiencia.id, {
        tipoDeEmpregoId,
        descricao,
        dataInicio,
        dataFim: dataFim || null,
      });
      onSalvo();
    } catch (err) {
      setErro(mensagemDeErro(err, "Não foi possível salvar."));
    } finally {
      setSalvando(false);
    }
  }

  return (
    <form onSubmit={handleSubmit} style={{ background: "#f9fafb", borderRadius: 12, padding: 16, marginTop: 10 }}>
      <div className="formulario-linha formulario-linha-3" style={{ marginBottom: 12 }}>
        <SeletorTipoDeEmprego value={tipoDeEmpregoId} onChange={setTipoDeEmpregoId} obrigatorio />
        <div className="formulario-campo">
          <label className="formulario-label" htmlFor={`ae-inicio-${experiencia.id}`}>Início</label>
          <input id={`ae-inicio-${experiencia.id}`} type="date" className="formulario-input" value={dataInicio} onChange={(e) => setDataInicio(e.target.value)} />
        </div>
        <div className="formulario-campo">
          <label className="formulario-label" htmlFor={`ae-fim-${experiencia.id}`}>Fim</label>
          <input id={`ae-fim-${experiencia.id}`} type="date" className="formulario-input" value={dataFim} onChange={(e) => setDataFim(e.target.value)} />
        </div>
      </div>
      <textarea className="formulario-textarea" style={{ height: 70, width: "100%", marginBottom: 12 }} value={descricao} onChange={(e) => setDescricao(e.target.value)} />
      {erro && <p style={{ color: "#b42318", fontSize: "0.85rem", marginBottom: 10 }}>{erro}</p>}
      <div style={{ display: "flex", gap: 8 }}>
        <button type="submit" className="botao-pequeno botao-pequeno-primario" disabled={salvando}>
          {salvando ? "Salvando..." : "Salvar"}
        </button>
        <button type="button" className="botao-pequeno" onClick={onCancelar}>Cancelar</button>
      </div>
    </form>
  );
}

/** Admin → Experiências profissionais: listar todas, editar, remover. */
export default function AdminExperiencias() {
  const [pagina, setPagina] = useState(0);
  const [resultado, setResultado] = useState(null);
  const [tituloPorTipo, setTituloPorTipo] = useState({});
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState("");
  const [tentativa, setTentativa] = useState(0);
  const [editandoId, setEditandoId] = useState(null);
  const [removendoId, setRemovendoId] = useState(null);

  useEffect(() => {
    tiposDeEmprego.listarAprovados().then((lista) => {
      setTituloPorTipo(Object.fromEntries(lista.map((t) => [t.id, t.titulo])));
    }).catch(() => {});
  }, []);

  useEffect(() => {
    let ativo = true;
    admin
      .listarExperiencias({ page: pagina, size: 10 })
      .then((dados) => {
        if (ativo) {
          setResultado(dados);
          setErro("");
        }
      })
      .catch((err) => {
        if (ativo) setErro(mensagemDeErro(err, "Não foi possível carregar as experiências."));
      })
      .finally(() => {
        if (ativo) setCarregando(false);
      });
    return () => {
      ativo = false;
    };
  }, [pagina, tentativa]);

  async function handleRemover(id) {
    if (!window.confirm("Remover esta experiência?")) return;
    setRemovendoId(id);
    try {
      await admin.removerExperiencia(id);
      recarregar();
    } catch (err) {
      setErro(mensagemDeErro(err, "Não foi possível remover."));
    } finally {
      setRemovendoId(null);
    }
  }

  function irParaPagina(p) {
    setCarregando(true);
    setPagina(p);
  }

  function recarregar() {
    setCarregando(true);
    setTentativa((n) => n + 1);
  }

  const experienciasLista = resultado?.itens ?? [];

  return (
    <div>
      {erro && <AvisoErro mensagem={erro} onTentarNovamente={recarregar} />}
      {carregando && <Aviso>Carregando...</Aviso>}
      {!carregando && !erro && experienciasLista.length === 0 && <Aviso>Nenhuma experiência cadastrada.</Aviso>}

      <div className="lista-cards">
        {experienciasLista.map((exp) => (
          <ItemCard
            key={exp.id}
            titulo={tituloPorTipo[exp.tipoDeEmprego] ?? `Tipo #${exp.tipoDeEmprego}`}
            meta={`${exp.descricao} · ${exp.dataInicio}${exp.dataFim ? ` até ${exp.dataFim}` : " (atual)"}`}
            acoes={
              <>
                <button type="button" className="botao-pequeno" onClick={() => setEditandoId(editandoId === exp.id ? null : exp.id)}>
                  {editandoId === exp.id ? "Fechar" : "Editar"}
                </button>
                <button type="button" className="botao-pequeno botao-pequeno-perigo" onClick={() => handleRemover(exp.id)} disabled={removendoId === exp.id}>
                  {removendoId === exp.id ? "Removendo..." : "Remover"}
                </button>
              </>
            }
          >
            {editandoId === exp.id && (
              <FormularioEdicao
                experiencia={exp}
                onSalvo={() => {
                  setEditandoId(null);
                  recarregar();
                }}
                onCancelar={() => setEditandoId(null)}
              />
            )}
          </ItemCard>
        ))}
      </div>

      <Paginacao pagina={pagina} totalPaginas={resultado?.totalPaginas} carregando={carregando} onMudarPagina={irParaPagina} />
    </div>
  );
}
