import { useEffect, useState } from "react";
import { admin, mensagemDeErro } from "../../api";
import { Aviso, AvisoErro, Paginacao } from "../shared/Estado";
import { ItemCard, Secao } from "../shared/Painel";

function DetalheUsuario({ usuario, onFechar, onAlterado }) {
  const [nome, setNome] = useState(usuario.nome ?? "");
  const [telefone, setTelefone] = useState(usuario.telefone ?? "");
  const [email, setEmail] = useState(usuario.email ?? "");
  const [nomeDaEmpresa, setNomeDaEmpresa] = useState("");
  const [carregando, setCarregando] = useState("");
  const [mensagem, setMensagem] = useState("");

  async function executar(chave, fn, textoOk) {
    setCarregando(chave);
    setMensagem("");
    try {
      await fn();
      setMensagem(textoOk);
      onAlterado?.();
    } catch (err) {
      setMensagem(mensagemDeErro(err));
    } finally {
      setCarregando("");
    }
  }

  async function handleSalvar(e) {
    e.preventDefault();
    executar("salvar", () => admin.atualizarUsuario(usuario.id, { nome, telefone, email }), "Dados atualizados.");
  }

  async function handleRemoverUsuario() {
    if (!window.confirm(`Remover o usuário "${usuario.nome}" definitivamente?`)) return;
    try {
      await admin.removerUsuario(usuario.id);
      onFechar();
      onAlterado?.();
    } catch (err) {
      setMensagem(mensagemDeErro(err));
    }
  }

  return (
    <div>
      <button type="button" className="botao-pequeno" onClick={onFechar} style={{ marginBottom: 20 }}>
        ← Voltar para a lista
      </button>

      <h1 className="formulario-titulo">{usuario.nome}</h1>
      <p className="formulario-subtitulo">Usuário #{usuario.id}</p>

      <Secao titulo="Dados cadastrais">
        <form onSubmit={handleSalvar} className="formulario-linha formulario-linha-3" style={{ marginBottom: 12 }}>
          <div className="formulario-campo">
            <label className="formulario-label" htmlFor="au-nome">Nome</label>
            <input id="au-nome" className="formulario-input" value={nome} onChange={(e) => setNome(e.target.value)} />
          </div>
          <div className="formulario-campo">
            <label className="formulario-label" htmlFor="au-telefone">Telefone</label>
            <input id="au-telefone" className="formulario-input" value={telefone} onChange={(e) => setTelefone(e.target.value)} />
          </div>
          <div className="formulario-campo">
            <label className="formulario-label" htmlFor="au-email">E-mail</label>
            <input id="au-email" type="email" className="formulario-input" value={email} onChange={(e) => setEmail(e.target.value)} />
          </div>
        </form>
        <button type="button" className="botao-pequeno botao-pequeno-primario" onClick={handleSalvar} disabled={carregando === "salvar"}>
          {carregando === "salvar" ? "Salvando..." : "Salvar"}
        </button>
      </Secao>

      <Secao titulo="Perfis">
        <div className="lista-cards">
          <ItemCard
            titulo="Perfil de candidato"
            acoes={
              <>
                <button type="button" className="botao-pequeno botao-pequeno-primario"
                  onClick={() => executar("cand-add", () => admin.adicionarPerfilCandidato(usuario.id), "Perfil de candidato ativado.")}
                  disabled={carregando === "cand-add"}>
                  Ativar
                </button>
                <button type="button" className="botao-pequeno botao-pequeno-perigo"
                  onClick={() => executar("cand-rm", () => admin.removerPerfilCandidato(usuario.id), "Perfil de candidato removido.")}
                  disabled={carregando === "cand-rm"}>
                  Remover
                </button>
              </>
            }
          />
          <ItemCard
            titulo="Perfil de recrutador"
            acoes={
              <>
                <button type="button" className="botao-pequeno botao-pequeno-primario"
                  onClick={() => executar("rec-add", () => admin.adicionarPerfilRecrutador(usuario.id, { nomeDaEmpresa }), "Perfil de recrutador ativado.")}
                  disabled={carregando === "rec-add" || !nomeDaEmpresa.trim()}>
                  Ativar
                </button>
                <button type="button" className="botao-pequeno botao-pequeno-perigo"
                  onClick={() => executar("rec-rm", () => admin.removerPerfilRecrutador(usuario.id), "Perfil de recrutador removido.")}
                  disabled={carregando === "rec-rm"}>
                  Remover
                </button>
              </>
            }
          >
            <input
              type="text"
              placeholder="Nome da empresa (obrigatório para ativar)"
              value={nomeDaEmpresa}
              onChange={(e) => setNomeDaEmpresa(e.target.value)}
              className="formulario-input"
              style={{ marginTop: 8, height: 40, maxWidth: 320 }}
            />
          </ItemCard>
        </div>
      </Secao>

      {mensagem && <Aviso>{mensagem}</Aviso>}

      <Secao titulo="Zona de risco">
        <button type="button" className="botao-pequeno botao-pequeno-perigo" onClick={handleRemoverUsuario}>
          Remover usuário definitivamente
        </button>
      </Secao>
    </div>
  );
}

/** Admin → Usuários: listar/buscar, editar dados, gerenciar perfis, remover. */
export default function AdminUsuarios() {
  const [termo, setTermo] = useState("");
  const [termoAplicado, setTermoAplicado] = useState("");
  const [pagina, setPagina] = useState(0);
  const [resultado, setResultado] = useState(null);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState("");
  const [tentativa, setTentativa] = useState(0);
  const [selecionado, setSelecionado] = useState(null);

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
    admin
      .listarUsuarios({ page: pagina, size: 10, termo: termoAplicado })
      .then((dados) => {
        if (ativo) {
          setResultado(dados);
          setErro("");
        }
      })
      .catch((err) => {
        if (ativo) setErro(mensagemDeErro(err, "Não foi possível carregar os usuários."));
      })
      .finally(() => {
        if (ativo) setCarregando(false);
      });
    return () => {
      ativo = false;
    };
  }, [pagina, termoAplicado, tentativa]);

  function irParaPagina(p) {
    setCarregando(true);
    setPagina(p);
  }

  function recarregar() {
    setCarregando(true);
    setTentativa((n) => n + 1);
  }

  if (selecionado) {
    return (
      <DetalheUsuario
        usuario={selecionado}
        onFechar={() => setSelecionado(null)}
        onAlterado={recarregar}
      />
    );
  }

  const usuariosLista = resultado?.itens ?? [];

  return (
    <div>
      <div className="form-inline">
        <input
          type="text"
          placeholder="Buscar por nome ou e-mail..."
          value={termo}
          onChange={(e) => setTermo(e.target.value)}
          style={{ flex: 1, minWidth: 240 }}
        />
      </div>

      {erro && <AvisoErro mensagem={erro} onTentarNovamente={recarregar} />}
      {carregando && <Aviso>Carregando usuários...</Aviso>}
      {!carregando && !erro && usuariosLista.length === 0 && <Aviso>Nenhum usuário encontrado.</Aviso>}

      <div className="lista-cards">
        {usuariosLista.map((u) => (
          <ItemCard
            key={u.id}
            titulo={u.nome}
            meta={`${u.email} · ${u.telefone}`}
            acoes={
              <button type="button" className="botao-pequeno" onClick={() => setSelecionado(u)}>
                Gerenciar
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
