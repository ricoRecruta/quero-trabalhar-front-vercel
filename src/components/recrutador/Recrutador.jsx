import { useEffect, useState } from "react";
import "../criarVaga/Formulario.css";
import "./Recrutador.css";
import { empresas as empresasApi, mensagemDeErro, recrutadores } from "../../api";
import { Aviso, AvisoErro, Paginacao } from "../shared/Estado";
import { Badge, ItemCard, Secao } from "../shared/Painel";
import { vagaParaCartao } from "../../api/mapeadores";

const TOM_VINCULO = {
  APROVADO: "sucesso",
  PENDENTE: "alerta",
  RECUSADO: "erro",
  REMOVIDO: "neutro",
};

const TEXTO_VINCULO = {
  APROVADO: "Vínculo aprovado",
  PENDENTE: "Aguardando aprovação",
  RECUSADO: "Vínculo recusado",
  REMOVIDO: "Vínculo removido",
};

function BuscaEmpresa({ onSolicitado }) {
  const [termo, setTermo] = useState("");
  const [resultados, setResultados] = useState([]);
  const [buscando, setBuscando] = useState(false);
  const [solicitandoId, setSolicitandoId] = useState(null);
  const [mensagem, setMensagem] = useState("");

  useEffect(() => {
    const timer = setTimeout(() => {
      if (!termo.trim()) {
        setResultados([]);
        return;
      }
      setBuscando(true);
      empresasApi
        .listar({ termo, size: 5 })
        .then((r) => setResultados(r.itens))
        .catch(() => setResultados([]))
        .finally(() => setBuscando(false));
    }, 400);
    return () => clearTimeout(timer);
  }, [termo]);

  async function solicitar(empresa) {
    setSolicitandoId(empresa.id);
    setMensagem("");
    try {
      await recrutadores.solicitarVinculo(empresa.id);
      setMensagem(`Vínculo solicitado com "${empresa.nome}". Aguarde a aprovação de um admin.`);
      setResultados([]);
      setTermo("");
      onSolicitado?.();
    } catch (err) {
      setMensagem(mensagemDeErro(err, "Não foi possível solicitar o vínculo."));
    } finally {
      setSolicitandoId(null);
    }
  }

  return (
    <div>
      <div className="form-inline">
        <input
          type="text"
          placeholder="Buscar empresa pelo nome..."
          value={termo}
          onChange={(e) => setTermo(e.target.value)}
        />
        {buscando && <span style={{ fontSize: "0.85rem", color: "#6b7280" }}>Buscando...</span>}
      </div>

      {resultados.length > 0 && (
        <div className="lista-cards" style={{ marginBottom: 12 }}>
          {resultados.map((emp) => (
            <ItemCard
              key={emp.id}
              titulo={emp.nome}
              meta={emp.cidade ? `${emp.cidade} - ${emp.estadoSigla ?? emp.estado ?? ""}` : null}
              acoes={
                <button
                  type="button"
                  className="botao-pequeno botao-pequeno-primario"
                  onClick={() => solicitar(emp)}
                  disabled={solicitandoId === emp.id}
                >
                  {solicitandoId === emp.id ? "Enviando..." : "Solicitar vínculo"}
                </button>
              }
            />
          ))}
        </div>
      )}

      {mensagem && <Aviso>{mensagem}</Aviso>}
    </div>
  );
}

/**
 * Tela do recrutador: meu perfil, vínculo com empresa e gestão das próprias
 * oportunidades. Nada aqui é mock — todas as chamadas usam src/api/recrutadores.js
 * e src/api/oportunidades.js.
 */
export default function Recrutador({ onEditarVaga, onNovaVaga, onIrParaPerfil }) {
  const [perfil, setPerfil] = useState(null);
  const [vinculo, setVinculo] = useState(null);
  const [semPerfilRecrutador, setSemPerfilRecrutador] = useState(false);
  const [carregandoPerfil, setCarregandoPerfil] = useState(true);
  const [erroPerfil, setErroPerfil] = useState("");

  const [oportunidadesState, setOportunidadesState] = useState(null);
  const [paginaVagas, setPaginaVagas] = useState(0);
  const [carregandoVagas, setCarregandoVagas] = useState(true);
  const [erroVagas, setErroVagas] = useState("");
  const [tentativa, setTentativa] = useState(0);

  useEffect(() => {
    let ativo = true;
    Promise.allSettled([recrutadores.buscarMe(), recrutadores.buscarMinhaEmpresa()]).then(
      ([resPerfil, resVinculo]) => {
        if (!ativo) return;
        if (resPerfil.status === "fulfilled") {
          setPerfil(resPerfil.value);
          setSemPerfilRecrutador(false);
        } else if (resPerfil.reason?.naoAutorizado || resPerfil.reason?.status === 422) {
          // GET /api/recrutadores/me devolve 422 (regra de negócio) quando o
          // usuário autenticado não tem perfil de recrutador — não é um erro
          // de permissão (401/403), é a forma que essa API usa para dizer
          // "esse recurso não existe pra você".
          setSemPerfilRecrutador(true);
        } else {
          setErroPerfil(mensagemDeErro(resPerfil.reason, "Não foi possível carregar seu perfil."));
        }
        setVinculo(resVinculo.status === "fulfilled" ? resVinculo.value : null);
        setCarregandoPerfil(false);
      }
    );
    return () => {
      ativo = false;
    };
  }, [tentativa]);

  useEffect(() => {
    if (semPerfilRecrutador) return;
    let ativo = true;
    recrutadores
      .listarMinhasOportunidades({ page: paginaVagas, size: 10 })
      .then((dados) => {
        if (ativo) {
          setOportunidadesState(dados);
          setErroVagas("");
        }
      })
      .catch((err) => {
        if (ativo) setErroVagas(mensagemDeErro(err, "Não foi possível carregar suas vagas."));
      })
      .finally(() => {
        if (ativo) setCarregandoVagas(false);
      });
    return () => {
      ativo = false;
    };
  }, [paginaVagas, semPerfilRecrutador, tentativa]);

  function irParaPaginaVagas(p) {
    setCarregandoVagas(true);
    setPaginaVagas(p);
  }

  function recarregar() {
    setCarregandoPerfil(true);
    setCarregandoVagas(true);
    setTentativa((n) => n + 1);
  }

  if (carregandoPerfil) return <Aviso>Carregando...</Aviso>;

  if (semPerfilRecrutador) {
    return (
      <div>
        <h1 className="formulario-titulo">Área do Recrutador</h1>
        <Aviso>
          Sua conta ainda não tem o perfil de recrutador ativado.{" "}
          <button
            type="button"
            className="botao-pequeno botao-pequeno-primario"
            onClick={onIrParaPerfil}
            style={{ marginLeft: 8 }}
          >
            Ativar em Meu Perfil
          </button>
        </Aviso>
      </div>
    );
  }

  const vagas = (oportunidadesState?.itens ?? []).map((v) => ({
    ...vagaParaCartao(v),
    statusLocalidade: v.statusLocalidade,
  }));

  return (
    <div>
      <h1 className="formulario-titulo">Área do Recrutador</h1>
      <p className="formulario-subtitulo">Gerencie seu perfil, sua empresa e suas vagas.</p>

      <Secao titulo="Meu perfil">
        {erroPerfil && <AvisoErro mensagem={erroPerfil} onTentarNovamente={recarregar} />}
        {perfil && (
          <div className="lista-cards">
            <ItemCard
              titulo={perfil.nome}
              meta={perfil.empresaLegada ? `Empresa (cadastro legado): ${perfil.empresaLegada}` : null}
              badge={
                vinculo?.statusVinculoEmpresa && (
                  <Badge tom={TOM_VINCULO[vinculo.statusVinculoEmpresa] ?? "neutro"}>
                    {TEXTO_VINCULO[vinculo.statusVinculoEmpresa] ?? vinculo.statusVinculoEmpresa}
                  </Badge>
                )
              }
            >
              {vinculo?.empresa && (
                <p className="item-card-meta">Empresa vinculada: {vinculo.empresa.nome}</p>
              )}
            </ItemCard>
          </div>
        )}
      </Secao>

      <Secao
        titulo="Vínculo com empresa"
        subtitulo={
          vinculo?.statusVinculoEmpresa === "APROVADO"
            ? "Você já está vinculado a uma empresa."
            : "Busque uma empresa cadastrada e solicite o vínculo. Um admin precisa aprovar."
        }
      >
        {vinculo?.statusVinculoEmpresa !== "APROVADO" && (
          <BuscaEmpresa onSolicitado={recarregar} />
        )}
      </Secao>

      <Secao
        titulo="Minhas vagas"
        acoes={
          <button type="button" className="botao-pequeno botao-pequeno-primario" onClick={onNovaVaga}>
            + Nova vaga
          </button>
        }
      >
        {erroVagas && <AvisoErro mensagem={erroVagas} onTentarNovamente={recarregar} />}
        {carregandoVagas && <Aviso>Carregando suas vagas...</Aviso>}
        {!carregandoVagas && !erroVagas && vagas.length === 0 && (
          <Aviso>Você ainda não publicou nenhuma vaga.</Aviso>
        )}

        <div className="lista-cards">
          {vagas.map((vaga) => (
            <ItemCard
              key={vaga.id}
              titulo={vaga.cargo}
              meta={`${vaga.modalidade} · ${vaga.localidade}`}
              badge={
                <Badge tom={vaga.statusLocalidade === "VALIDADA" ? "sucesso" : "alerta"}>
                  {vaga.statusLocalidade === "VALIDADA" ? "Publicada" : "Localidade pendente"}
                </Badge>
              }
              acoes={
                <button
                  type="button"
                  className="botao-pequeno"
                  onClick={() => onEditarVaga(oportunidadesState.itens.find((v) => v.id === vaga.id))}
                >
                  Editar / excluir
                </button>
              }
            />
          ))}
        </div>

        <Paginacao
          pagina={paginaVagas}
          totalPaginas={oportunidadesState?.totalPaginas}
          carregando={carregandoVagas}
          onMudarPagina={irParaPaginaVagas}
        />
      </Secao>
    </div>
  );
}
