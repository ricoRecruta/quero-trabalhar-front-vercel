import { useEffect, useState } from "react";
import "../criarVaga/Formulario.css";
import "./Perfil.css";
import { candidatos, experiencias as experienciasApi, mensagemDeErro, tiposDeEmprego, usuarios } from "../../api";
import { vagaParaCartao } from "../../api/mapeadores";
import { Aviso, AvisoErro } from "../shared/Estado";
import { ItemCard, Secao } from "../shared/Painel";
import SeletorTipoDeEmprego from "../shared/SeletorTipoDeEmprego";

/** Seção 1: nome/telefone/email — o único dado editável em Usuario. */
function SecaoDados({ usuario, onAtualizado }) {
  const [nome, setNome] = useState(usuario.nome ?? "");
  const [telefone, setTelefone] = useState(usuario.telefone ?? "");
  const [email, setEmail] = useState(usuario.email ?? "");
  const [salvando, setSalvando] = useState(false);
  const [mensagem, setMensagem] = useState("");

  async function handleSubmit(e) {
    e.preventDefault();
    setSalvando(true);
    setMensagem("");
    try {
      const atualizado = await usuarios.atualizarMe({ nome, telefone, email });
      onAtualizado(atualizado);
      setMensagem("Dados atualizados.");
    } catch (err) {
      setMensagem(mensagemDeErro(err, "Não foi possível atualizar seus dados."));
    } finally {
      setSalvando(false);
    }
  }

  return (
    <form onSubmit={handleSubmit}>
      <div className="formulario-linha formulario-linha-2">
        <div className="formulario-campo">
          <label htmlFor="p-nome" className="formulario-label">
            Nome completo *
          </label>
          <input
            id="p-nome"
            className="formulario-input"
            value={nome}
            onChange={(e) => setNome(e.target.value)}
            required
          />
        </div>
        <div className="formulario-campo">
          <label htmlFor="p-email" className="formulario-label">
            E-mail *
          </label>
          <input
            id="p-email"
            type="email"
            className="formulario-input"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />
        </div>
      </div>
      <div className="formulario-campo" style={{ marginBottom: 20, maxWidth: 320 }}>
        <label htmlFor="p-telefone" className="formulario-label">
          Telefone *
        </label>
        <input
          id="p-telefone"
          className="formulario-input"
          value={telefone}
          onChange={(e) => setTelefone(e.target.value)}
          required
        />
      </div>
      {mensagem && <p style={{ fontSize: "0.88rem", marginBottom: 12 }}>{mensagem}</p>}
      <button type="submit" className="botao-pequeno botao-pequeno-primario" disabled={salvando}>
        {salvando ? "Salvando..." : "Salvar dados"}
      </button>
    </form>
  );
}

/** Seção 2: trocar senha (senhaAtual, novaSenha, confirmacaoNovaSenha). */
function SecaoSenha() {
  const [senhaAtual, setSenhaAtual] = useState("");
  const [novaSenha, setNovaSenha] = useState("");
  const [confirmacaoNovaSenha, setConfirmacaoNovaSenha] = useState("");
  const [salvando, setSalvando] = useState(false);
  const [mensagem, setMensagem] = useState("");

  async function handleSubmit(e) {
    e.preventDefault();
    setSalvando(true);
    setMensagem("");
    try {
      await usuarios.alterarSenha({ senhaAtual, novaSenha, confirmacaoNovaSenha });
      setMensagem("Senha alterada com sucesso.");
      setSenhaAtual("");
      setNovaSenha("");
      setConfirmacaoNovaSenha("");
    } catch (err) {
      setMensagem(mensagemDeErro(err, "Não foi possível alterar a senha."));
    } finally {
      setSalvando(false);
    }
  }

  return (
    <form onSubmit={handleSubmit}>
      <div className="formulario-linha formulario-linha-3">
        <div className="formulario-campo">
          <label htmlFor="senha-atual" className="formulario-label">
            Senha atual *
          </label>
          <input
            id="senha-atual"
            type="password"
            className="formulario-input"
            value={senhaAtual}
            onChange={(e) => setSenhaAtual(e.target.value)}
            required
          />
        </div>
        <div className="formulario-campo">
          <label htmlFor="senha-nova" className="formulario-label">
            Nova senha *
          </label>
          <input
            id="senha-nova"
            type="password"
            className="formulario-input"
            value={novaSenha}
            onChange={(e) => setNovaSenha(e.target.value)}
            required
          />
        </div>
        <div className="formulario-campo">
          <label htmlFor="senha-confirmacao" className="formulario-label">
            Confirmar nova senha *
          </label>
          <input
            id="senha-confirmacao"
            type="password"
            className="formulario-input"
            value={confirmacaoNovaSenha}
            onChange={(e) => setConfirmacaoNovaSenha(e.target.value)}
            required
          />
        </div>
      </div>
      {mensagem && <p style={{ fontSize: "0.88rem", marginBottom: 12 }}>{mensagem}</p>}
      <button type="submit" className="botao-pequeno botao-pequeno-primario" disabled={salvando}>
        {salvando ? "Salvando..." : "Alterar senha"}
      </button>
    </form>
  );
}

/**
 * Seção 3: ativar/desativar os perfis de candidato e recrutador.
 *
 * A API não devolve, em nenhuma resposta de /api/usuarios/me, quais perfis
 * o usuário já tem — por isso não dá pra mostrar "você já é candidato" com
 * certeza. Em vez de simular esse estado, oferecemos as duas ações (ativar
 * e remover) para os dois perfis e deixamos a mensagem de erro da própria
 * API explicar quando a ação não se aplica (ex.: tentar remover um perfil
 * que não existe).
 */
function SecaoPerfis() {
  const [nomeDaEmpresa, setNomeDaEmpresa] = useState("");
  const [carregandoAcao, setCarregandoAcao] = useState("");
  const [mensagens, setMensagens] = useState({ candidato: "", recrutador: "" });

  async function executar(chave, fn) {
    setCarregandoAcao(chave);
    setMensagens((m) => ({ ...m, [chave.startsWith("candidato") ? "candidato" : "recrutador"]: "" }));
    try {
      await fn();
      const grupo = chave.startsWith("candidato") ? "candidato" : "recrutador";
      setMensagens((m) => ({ ...m, [grupo]: "Feito." }));
    } catch (err) {
      const grupo = chave.startsWith("candidato") ? "candidato" : "recrutador";
      setMensagens((m) => ({ ...m, [grupo]: mensagemDeErro(err) }));
    } finally {
      setCarregandoAcao("");
    }
  }

  return (
    <div className="lista-cards">
      <ItemCard
        titulo="Perfil de candidato"
        meta="Permite demonstrar interesse em vagas e cadastrar experiências."
        acoes={
          <>
            <button
              type="button"
              className="botao-pequeno botao-pequeno-primario"
              onClick={() => executar("candidato-add", () => usuarios.adicionarPerfilCandidato())}
              disabled={carregandoAcao === "candidato-add"}
            >
              Ativar
            </button>
            <button
              type="button"
              className="botao-pequeno botao-pequeno-perigo"
              onClick={() => executar("candidato-rm", () => usuarios.removerPerfilCandidato())}
              disabled={carregandoAcao === "candidato-rm"}
            >
              Remover
            </button>
          </>
        }
      >
        {mensagens.candidato && (
          <p style={{ fontSize: "0.82rem", marginTop: 6 }}>{mensagens.candidato}</p>
        )}
      </ItemCard>

      <ItemCard
        titulo="Perfil de recrutador"
        meta="Permite publicar vagas e vincular sua conta a uma empresa."
        acoes={
          <>
            <button
              type="button"
              className="botao-pequeno botao-pequeno-primario"
              onClick={() =>
                executar("recrutador-add", () => usuarios.adicionarPerfilRecrutador({ nomeDaEmpresa }))
              }
              disabled={carregandoAcao === "recrutador-add" || !nomeDaEmpresa.trim()}
            >
              Ativar
            </button>
            <button
              type="button"
              className="botao-pequeno botao-pequeno-perigo"
              onClick={() => executar("recrutador-rm", () => usuarios.removerPerfilRecrutador())}
              disabled={carregandoAcao === "recrutador-rm"}
            >
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
        {mensagens.recrutador && (
          <p style={{ fontSize: "0.82rem", marginTop: 6 }}>{mensagens.recrutador}</p>
        )}
      </ItemCard>
    </div>
  );
}

/** Seção 4: experiências profissionais — listar, adicionar, remover. */
function SecaoExperiencias() {
  const [lista, setLista] = useState(null);
  const [tituloPorTipo, setTituloPorTipo] = useState({});
  const [erro, setErro] = useState("");
  const [tentativa, setTentativa] = useState(0);

  const [tipoDeEmpregoId, setTipoDeEmpregoId] = useState(null);
  const [descricao, setDescricao] = useState("");
  const [dataInicio, setDataInicio] = useState("");
  const [dataFim, setDataFim] = useState("");
  const [salvando, setSalvando] = useState(false);
  const [erroForm, setErroForm] = useState("");
  const [removendoId, setRemovendoId] = useState(null);

  useEffect(() => {
    let ativo = true;
    Promise.allSettled([experienciasApi.listar(), tiposDeEmprego.listarAprovados()]).then(
      ([r1, r2]) => {
        if (!ativo) return;
        if (r1.status === "fulfilled") {
          setLista(r1.value);
          setErro("");
        } else {
          setErro(mensagemDeErro(r1.reason, "Não foi possível carregar suas experiências."));
        }
        if (r2.status === "fulfilled") {
          setTituloPorTipo(Object.fromEntries(r2.value.map((t) => [t.id, t.titulo])));
        }
      }
    );
    return () => {
      ativo = false;
    };
  }, [tentativa]);

  async function handleAdicionar(e) {
    e.preventDefault();
    setErroForm("");
    setSalvando(true);
    try {
      await experienciasApi.criar({
        tipoDeEmpregoId,
        descricao,
        dataInicio,
        dataFim: dataFim || null,
      });
      setDescricao("");
      setDataInicio("");
      setDataFim("");
      setTipoDeEmpregoId(null);
      setTentativa((n) => n + 1);
    } catch (err) {
      setErroForm(mensagemDeErro(err, "Não foi possível salvar a experiência."));
    } finally {
      setSalvando(false);
    }
  }

  async function handleRemover(id) {
    if (!window.confirm("Remover esta experiência?")) return;
    setRemovendoId(id);
    try {
      await experienciasApi.remover(id);
      setTentativa((n) => n + 1);
    } catch (err) {
      setErro(mensagemDeErro(err, "Não foi possível remover a experiência."));
    } finally {
      setRemovendoId(null);
    }
  }

  return (
    <div>
      {erro && <AvisoErro mensagem={erro} onTentarNovamente={() => setTentativa((n) => n + 1)} />}
      {lista === null && !erro && <Aviso>Carregando...</Aviso>}
      {lista?.length === 0 && <Aviso>Você ainda não cadastrou experiências.</Aviso>}

      <div className="lista-cards" style={{ marginBottom: 20 }}>
        {lista?.map((exp) => (
          <ItemCard
            key={exp.id}
            titulo={tituloPorTipo[exp.tipoDeEmprego] ?? `Tipo #${exp.tipoDeEmprego}`}
            meta={`${exp.descricao} · ${exp.dataInicio}${exp.dataFim ? ` até ${exp.dataFim}` : " (atual)"}`}
            acoes={
              <button
                type="button"
                className="botao-pequeno botao-pequeno-perigo"
                onClick={() => handleRemover(exp.id)}
                disabled={removendoId === exp.id}
              >
                {removendoId === exp.id ? "Removendo..." : "Remover"}
              </button>
            }
          />
        ))}
      </div>

      <form onSubmit={handleAdicionar}>
        <div className="formulario-linha formulario-linha-3">
          <SeletorTipoDeEmprego value={tipoDeEmpregoId} onChange={setTipoDeEmpregoId} obrigatorio />
          <div className="formulario-campo">
            <label htmlFor="exp-inicio" className="formulario-label">
              Início *
            </label>
            <input
              id="exp-inicio"
              type="date"
              className="formulario-input"
              value={dataInicio}
              onChange={(e) => setDataInicio(e.target.value)}
              required
            />
          </div>
          <div className="formulario-campo">
            <label htmlFor="exp-fim" className="formulario-label">
              Fim (deixe em branco se atual)
            </label>
            <input
              id="exp-fim"
              type="date"
              className="formulario-input"
              value={dataFim}
              onChange={(e) => setDataFim(e.target.value)}
            />
          </div>
        </div>
        <div className="formulario-campo" style={{ marginBottom: 16 }}>
          <label htmlFor="exp-descricao" className="formulario-label">
            Descrição *
          </label>
          <textarea
            id="exp-descricao"
            className="formulario-textarea"
            style={{ height: 90 }}
            value={descricao}
            onChange={(e) => setDescricao(e.target.value)}
            required
          />
        </div>
        {erroForm && (
          <p style={{ color: "#b42318", fontSize: "0.88rem", marginBottom: 12 }} role="alert">
            {erroForm}
          </p>
        )}
        <button
          type="submit"
          className="botao-pequeno botao-pequeno-primario"
          disabled={salvando || !tipoDeEmpregoId || !dataInicio || !descricao}
        >
          {salvando ? "Salvando..." : "+ Adicionar experiência"}
        </button>
      </form>
    </div>
  );
}

/** Seção 5: vagas em que demonstrei interesse. */
function SecaoInteresses() {
  const [resultado, setResultado] = useState(null);
  const [erro, setErro] = useState("");
  const [tentativa, setTentativa] = useState(0);
  const [removendoId, setRemovendoId] = useState(null);

  useEffect(() => {
    let ativo = true;
    candidatos
      .listarInteresses({ size: 20 })
      .then((dados) => {
        if (ativo) {
          setResultado(dados);
          setErro("");
        }
      })
      .catch((err) => {
        if (ativo) setErro(mensagemDeErro(err, "Não foi possível carregar suas vagas de interesse."));
      });
    return () => {
      ativo = false;
    };
  }, [tentativa]);

  async function handleRemover(vagaId) {
    setRemovendoId(vagaId);
    try {
      await candidatos.removerInteresse(vagaId);
      setTentativa((n) => n + 1);
    } catch (err) {
      setErro(mensagemDeErro(err, "Não foi possível remover o interesse."));
    } finally {
      setRemovendoId(null);
    }
  }

  const vagas = (resultado?.itens ?? []).map(vagaParaCartao);

  return (
    <div>
      {erro && <AvisoErro mensagem={erro} onTentarNovamente={() => setTentativa((n) => n + 1)} />}
      {resultado === null && !erro && <Aviso>Carregando...</Aviso>}
      {resultado && vagas.length === 0 && <Aviso>Você ainda não demonstrou interesse em nenhuma vaga.</Aviso>}

      <div className="lista-cards">
        {vagas.map((vaga) => (
          <ItemCard
            key={vaga.id}
            titulo={vaga.cargo}
            meta={`${vaga.publicador} · ${vaga.localidade}`}
            acoes={
              <button
                type="button"
                className="botao-pequeno botao-pequeno-perigo"
                onClick={() => handleRemover(vaga.id)}
                disabled={removendoId === vaga.id}
              >
                {removendoId === vaga.id ? "Removendo..." : "Remover interesse"}
              </button>
            }
          />
        ))}
      </div>
    </div>
  );
}

/**
 * Perfil do usuário autenticado. Reescrita completa: a versão anterior tinha
 * campos (CPF editável, escolaridade, endereço, "sobre você") que não
 * existem em nenhuma entidade do backend — foram removidos. O que resta é
 * exatamente o que a API sustenta: dados de conta, senha, perfis,
 * experiências e vagas de interesse.
 */
export default function Perfil() {
  const [usuario, setUsuario] = useState(null);
  const [erro, setErro] = useState("");

  useEffect(() => {
    let ativo = true;
    usuarios
      .buscarMe()
      .then((dados) => {
        if (ativo) setUsuario(dados);
      })
      .catch((err) => {
        if (ativo) setErro(mensagemDeErro(err, "Não foi possível carregar seu perfil."));
      });
    return () => {
      ativo = false;
    };
  }, []);

  if (erro) return <AvisoErro mensagem={erro} />;
  if (!usuario) return <Aviso>Carregando...</Aviso>;

  return (
    <div>
      <h1 className="formulario-titulo">Meu Perfil</h1>
      <p className="formulario-subtitulo">Gerencie sua conta, seus perfis e suas informações.</p>

      <Secao titulo="Meus dados">
        <SecaoDados usuario={usuario} onAtualizado={setUsuario} />
      </Secao>

      <Secao titulo="Alterar senha">
        <SecaoSenha />
      </Secao>

      <Secao
        titulo="Meus perfis"
        subtitulo="Ative ou remova seus perfis de candidato e recrutador."
      >
        <SecaoPerfis />
      </Secao>

      <Secao titulo="Minhas experiências profissionais">
        <SecaoExperiencias />
      </Secao>

      <Secao titulo="Vagas de interesse">
        <SecaoInteresses />
      </Secao>
    </div>
  );
}
