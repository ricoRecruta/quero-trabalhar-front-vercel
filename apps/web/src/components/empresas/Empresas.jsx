import { useEffect, useState } from "react";
import "../criarVaga/Formulario.css";
import { empresas as empresasApi, mensagemDeErro } from "../../api";
import { Aviso, AvisoErro, Paginacao } from "../shared/Estado";
import { ItemCard, Secao } from "../shared/Painel";
import SeletorLocalidade from "../shared/SeletorLocalidade";

function localidadeTexto(emp) {
  if (emp.cidade) return `${emp.cidade} - ${emp.estadoSigla ?? emp.estado ?? ""}`;
  if (emp.estado) return emp.estado;
  if (emp.pais) return emp.pais;
  return "Localidade não informada";
}

function FormularioNovaEmpresa({ onCriada, onCancelar }) {
  const [nome, setNome] = useState("");
  const [descricao, setDescricao] = useState("");
  const [site, setSite] = useState("");
  const [emailPublico, setEmailPublico] = useState("");
  const [telefonePublico, setTelefonePublico] = useState("");
  const [localidade, setLocalidade] = useState({
    paisId: null,
    paisNome: "",
    estadoId: null,
    estadoNome: "",
    cidadeId: null,
    cidadeNome: "",
  });
  const [salvando, setSalvando] = useState(false);
  const [erro, setErro] = useState("");

  async function handleSubmit(e) {
    e.preventDefault();
    setSalvando(true);
    setErro("");
    try {
      const criada = await empresasApi.criar({
        nome,
        descricao: descricao || null,
        site: site || null,
        emailPublico: emailPublico || null,
        telefonePublico: telefonePublico || null,
        paisId: localidade.paisId,
        estadoId: localidade.estadoId,
        cidadeId: localidade.cidadeId,
        localidadeTexto: null,
      });
      onCriada(criada);
    } catch (err) {
      setErro(mensagemDeErro(err, "Não foi possível cadastrar a empresa."));
    } finally {
      setSalvando(false);
    }
  }

  return (
    <form onSubmit={handleSubmit}>
      <div className="formulario-linha formulario-linha-2">
        <div className="formulario-campo">
          <label htmlFor="emp-nome" className="formulario-label">
            Nome *
          </label>
          <input
            id="emp-nome"
            className="formulario-input"
            value={nome}
            onChange={(e) => setNome(e.target.value)}
            required
          />
        </div>
        <div className="formulario-campo">
          <label htmlFor="emp-site" className="formulario-label">
            Site
          </label>
          <input
            id="emp-site"
            className="formulario-input"
            value={site}
            onChange={(e) => setSite(e.target.value)}
            placeholder="https://..."
          />
        </div>
      </div>

      <div className="formulario-linha formulario-linha-2">
        <div className="formulario-campo">
          <label htmlFor="emp-email" className="formulario-label">
            E-mail público
          </label>
          <input
            id="emp-email"
            type="email"
            className="formulario-input"
            value={emailPublico}
            onChange={(e) => setEmailPublico(e.target.value)}
          />
        </div>
        <div className="formulario-campo">
          <label htmlFor="emp-telefone" className="formulario-label">
            Telefone público
          </label>
          <input
            id="emp-telefone"
            className="formulario-input"
            value={telefonePublico}
            onChange={(e) => setTelefonePublico(e.target.value)}
          />
        </div>
      </div>

      <div className="formulario-campo" style={{ marginBottom: 24 }}>
        <label htmlFor="emp-descricao" className="formulario-label">
          Descrição
        </label>
        <textarea
          id="emp-descricao"
          className="formulario-textarea"
          value={descricao}
          onChange={(e) => setDescricao(e.target.value)}
        />
      </div>

      <SeletorLocalidade value={localidade} onChange={setLocalidade} />

      {erro && (
        <p style={{ color: "#b42318", fontSize: "0.9rem", margin: "16px 0" }} role="alert">
          {erro}
        </p>
      )}

      <div className="formulario-botoes">
        <button type="submit" className="formulario-botao-primario" disabled={salvando}>
          {salvando ? "Salvando..." : "Cadastrar empresa"}
        </button>
        <button type="button" className="formulario-botao-secundario" onClick={onCancelar}>
          Cancelar
        </button>
      </div>
    </form>
  );
}

function DetalheEmpresa({ empresa, onFechar }) {
  const [recrutadoresLista, setRecrutadoresLista] = useState(null);
  const [oportunidadesResultado, setOportunidadesResultado] = useState(null);
  const [erro, setErro] = useState("");

  useEffect(() => {
    let ativo = true;
    Promise.allSettled([
      empresasApi.listarRecrutadores(empresa.id),
      empresasApi.listarOportunidades(empresa.id, { size: 20 }),
    ]).then(([r1, r2]) => {
      if (!ativo) return;
      setRecrutadoresLista(r1.status === "fulfilled" ? r1.value : []);
      setOportunidadesResultado(r2.status === "fulfilled" ? r2.value : null);
      if (r1.status === "rejected" && r2.status === "rejected") {
        setErro(mensagemDeErro(r1.reason, "Não foi possível carregar os detalhes da empresa."));
      }
    });
    return () => {
      ativo = false;
    };
  }, [empresa.id]);

  return (
    <div>
      <button type="button" className="botao-pequeno" onClick={onFechar} style={{ marginBottom: 20 }}>
        ← Voltar para a lista
      </button>

      <h1 className="formulario-titulo">{empresa.nome}</h1>
      <p className="formulario-subtitulo">{localidadeTexto(empresa)}</p>
      {empresa.descricao && <p style={{ color: "#4b5563", marginBottom: 24 }}>{empresa.descricao}</p>}

      {erro && <AvisoErro mensagem={erro} />}

      <Secao titulo="Recrutadores">
        {recrutadoresLista === null && <Aviso>Carregando...</Aviso>}
        {recrutadoresLista?.length === 0 && <Aviso>Nenhum recrutador vinculado.</Aviso>}
        <div className="lista-cards">
          {recrutadoresLista?.map((r) => (
            <ItemCard key={r.recrutadorId} titulo={r.nome} />
          ))}
        </div>
      </Secao>

      <Secao titulo="Vagas abertas por esta empresa">
        {oportunidadesResultado === null && <Aviso>Carregando...</Aviso>}
        {oportunidadesResultado?.itens.length === 0 && <Aviso>Nenhuma vaga pública no momento.</Aviso>}
        <div className="lista-cards">
          {oportunidadesResultado?.itens.map((v) => (
            <ItemCard key={v.id} titulo={v.tipoDeEmprego ?? v.descricao} meta={v.descricao} />
          ))}
        </div>
      </Secao>
    </div>
  );
}

/** Entidade Empresa: listar, buscar, criar e ver detalhes (recrutadores + vagas). */
export default function Empresas() {
  const [termo, setTermo] = useState("");
  const [termoAplicado, setTermoAplicado] = useState("");
  const [pagina, setPagina] = useState(0);
  const [resultado, setResultado] = useState(null);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState("");
  const [tentativa, setTentativa] = useState(0);

  const [criando, setCriando] = useState(false);
  const [empresaSelecionada, setEmpresaSelecionada] = useState(null);

  // Ver Vagas.jsx para o motivo do `if`: sem ele, este efeito religa
  // `carregando` 400ms após montar mesmo sem digitação nenhuma, e nada o
  // desliga de volta porque termoAplicado não muda de valor de fato.
  useEffect(() => {
    const timer = setTimeout(() => {
      const termoLimpo = termo.trim();
      if (termoLimpo === termoAplicado) return;
      setCarregando(true);
      setTermoAplicado(termoLimpo);
      setPagina(0);
    }, 400);
    return () => clearTimeout(timer);
  }, [termo, termoAplicado]);

  useEffect(() => {
    let ativo = true;
    empresasApi
      .listar({ page: pagina, size: 10, termo: termoAplicado })
      .then((dados) => {
        if (ativo) {
          setResultado(dados);
          setErro("");
        }
      })
      .catch((err) => {
        if (ativo) setErro(mensagemDeErro(err, "Não foi possível carregar as empresas."));
      })
      .finally(() => {
        if (ativo) setCarregando(false);
      });
    return () => {
      ativo = false;
    };
  }, [pagina, termoAplicado, tentativa]);

  if (empresaSelecionada) {
    return <DetalheEmpresa empresa={empresaSelecionada} onFechar={() => setEmpresaSelecionada(null)} />;
  }

  if (criando) {
    return (
      <div>
        <h1 className="formulario-titulo">Cadastrar Empresa</h1>
        <p className="formulario-subtitulo">
          Toda empresa cadastrada aparece publicamente e pode receber recrutadores vinculados.
        </p>
        <FormularioNovaEmpresa
          onCriada={() => {
            setCriando(false);
            recarregar();
          }}
          onCancelar={() => setCriando(false)}
        />
      </div>
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

  const empresasLista = resultado?.itens ?? [];

  return (
    <div>
      <div
        style={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "flex-start",
          flexWrap: "wrap",
          gap: 16,
        }}
      >
        <div>
          <h1 className="formulario-titulo">Empresas</h1>
          <p className="formulario-subtitulo">Empresas cadastradas na plataforma.</p>
        </div>
        <button
          type="button"
          className="botao-pequeno botao-pequeno-primario"
          onClick={() => setCriando(true)}
        >
          + Cadastrar empresa
        </button>
      </div>

      <div className="form-inline">
        <input
          type="text"
          placeholder="Buscar empresa..."
          value={termo}
          onChange={(e) => setTermo(e.target.value)}
          style={{ flex: 1, minWidth: 240 }}
        />
      </div>

      {erro && <AvisoErro mensagem={erro} onTentarNovamente={recarregar} />}
      {carregando && <Aviso>Carregando empresas...</Aviso>}
      {!carregando && !erro && empresasLista.length === 0 && (
        <Aviso>Nenhuma empresa encontrada.</Aviso>
      )}

      <div className="lista-cards">
        {empresasLista.map((emp) => (
          <ItemCard
            key={emp.id}
            titulo={emp.nome}
            meta={localidadeTexto(emp)}
            acoes={
              <button
                type="button"
                className="botao-pequeno"
                onClick={() => setEmpresaSelecionada(emp)}
              >
                Ver detalhes
              </button>
            }
          />
        ))}
      </div>

      <Paginacao
        pagina={pagina}
        totalPaginas={resultado?.totalPaginas}
        carregando={carregando}
        onMudarPagina={irParaPagina}
      />
    </div>
  );
}
