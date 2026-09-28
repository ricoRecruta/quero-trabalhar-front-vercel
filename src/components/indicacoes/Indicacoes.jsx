import { useEffect, useState } from "react";
import "../criarVaga/Formulario.css";
import { indicacoes, mensagemDeErro } from "../../api";
import { Aviso, AvisoErro } from "../shared/Estado";
import { ItemCard, Secao } from "../shared/Painel";

/**
 * Entidade Indicacao: criar, listar dadas/recebidas e remover.
 *
 * Limitação real da API: IndicacaoRequestDTO exige usuarioIndicadoId, mas
 * não existe nenhum endpoint público de busca de usuário por nome/e-mail
 * (só admin consegue listar usuários). Por isso o formulário pede o ID
 * diretamente — não dá pra fazer uma busca por nome sem esse endpoint.
 */
export default function Indicacoes() {
  const [usuarioIndicadoId, setUsuarioIndicadoId] = useState("");
  const [mensagemTexto, setMensagemTexto] = useState("");
  const [enviando, setEnviando] = useState(false);
  const [erroForm, setErroForm] = useState("");

  const [dadas, setDadas] = useState(null);
  const [recebidas, setRecebidas] = useState(null);
  const [erroLista, setErroLista] = useState("");
  const [tentativa, setTentativa] = useState(0);
  const [removendoId, setRemovendoId] = useState(null);

  useEffect(() => {
    let ativo = true;
    Promise.allSettled([indicacoes.listarDadas(), indicacoes.listarRecebidas()]).then(
      ([r1, r2]) => {
        if (!ativo) return;
        if (r1.status === "fulfilled") setDadas(r1.value);
        if (r2.status === "fulfilled") setRecebidas(r2.value);
        if (r1.status === "rejected" && r2.status === "rejected") {
          setErroLista(mensagemDeErro(r1.reason, "Não foi possível carregar as indicações."));
        }
      }
    );
    return () => {
      ativo = false;
    };
  }, [tentativa]);

  async function handleSubmit(e) {
    e.preventDefault();
    setErroForm("");
    setEnviando(true);
    try {
      await indicacoes.criar({
        usuarioIndicadoId: Number(usuarioIndicadoId),
        mensagem: mensagemTexto,
      });
      setUsuarioIndicadoId("");
      setMensagemTexto("");
      setTentativa((n) => n + 1);
    } catch (err) {
      setErroForm(mensagemDeErro(err, "Não foi possível criar a indicação."));
    } finally {
      setEnviando(false);
    }
  }

  async function handleRemover(id) {
    if (!window.confirm("Remover esta indicação?")) return;
    setRemovendoId(id);
    try {
      await indicacoes.remover(id);
      setTentativa((n) => n + 1);
    } catch (err) {
      setErroLista(mensagemDeErro(err, "Não foi possível remover a indicação."));
    } finally {
      setRemovendoId(null);
    }
  }

  return (
    <div>
      <h1 className="formulario-titulo">Indicações</h1>
      <p className="formulario-subtitulo">Indique profissionais e acompanhe quem te indicou.</p>

      <Secao titulo="Nova indicação" subtitulo="Você precisa saber o ID do usuário que quer indicar.">
        <form onSubmit={handleSubmit} className="formulario-linha formulario-linha-2" style={{ marginBottom: 0 }}>
          <div className="formulario-campo">
            <label htmlFor="ind-usuario" className="formulario-label">
              ID do usuário indicado *
            </label>
            <input
              id="ind-usuario"
              type="number"
              min="1"
              className="formulario-input"
              value={usuarioIndicadoId}
              onChange={(e) => setUsuarioIndicadoId(e.target.value)}
              required
            />
          </div>
          <div className="formulario-campo">
            <label htmlFor="ind-mensagem" className="formulario-label">
              Mensagem
            </label>
            <input
              id="ind-mensagem"
              className="formulario-input"
              value={mensagemTexto}
              onChange={(e) => setMensagemTexto(e.target.value)}
              placeholder="Por que você está indicando essa pessoa?"
            />
          </div>
        </form>
        {erroForm && (
          <p style={{ color: "#b42318", fontSize: "0.9rem", margin: "12px 0" }} role="alert">
            {erroForm}
          </p>
        )}
        <button
          type="button"
          className="botao-pequeno botao-pequeno-primario"
          onClick={handleSubmit}
          disabled={enviando || !usuarioIndicadoId}
          style={{ marginTop: 12 }}
        >
          {enviando ? "Enviando..." : "Enviar indicação"}
        </button>
      </Secao>

      {erroLista && <AvisoErro mensagem={erroLista} onTentarNovamente={() => setTentativa((n) => n + 1)} />}

      <Secao titulo="Indicações que eu dei">
        {dadas === null && !erroLista && <Aviso>Carregando...</Aviso>}
        {dadas?.length === 0 && <Aviso>Você ainda não indicou ninguém.</Aviso>}
        <div className="lista-cards">
          {dadas?.map((ind) => (
            <ItemCard
              key={ind.id}
              titulo={ind.usuarioIndicadoNome ?? `Usuário #${ind.usuarioIndicadoId}`}
              meta={ind.mensagem}
              acoes={
                <button
                  type="button"
                  className="botao-pequeno botao-pequeno-perigo"
                  onClick={() => handleRemover(ind.id)}
                  disabled={removendoId === ind.id}
                >
                  {removendoId === ind.id ? "Removendo..." : "Remover"}
                </button>
              }
            />
          ))}
        </div>
      </Secao>

      <Secao titulo="Indicações que eu recebi">
        {recebidas === null && !erroLista && <Aviso>Carregando...</Aviso>}
        {recebidas?.length === 0 && <Aviso>Você ainda não recebeu indicações.</Aviso>}
        <div className="lista-cards">
          {recebidas?.map((ind) => (
            <ItemCard
              key={ind.id}
              titulo={ind.autorNome ?? `Usuário #${ind.autorId}`}
              meta={ind.mensagem}
            />
          ))}
        </div>
      </Secao>
    </div>
  );
}
