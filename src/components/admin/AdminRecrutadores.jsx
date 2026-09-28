import { useState } from "react";
import { admin, mensagemDeErro } from "../../api";
import { Aviso } from "../shared/Estado";
import { Badge, Secao } from "../shared/Painel";

const TOM_VINCULO = {
  APROVADO: "sucesso",
  PENDENTE: "alerta",
  RECUSADO: "erro",
  REMOVIDO: "neutro",
};

/**
 * Admin → Vínculos de recrutador com empresa.
 *
 * Limitação real da API: PerfilRecrutadorAdminController só tem
 * PATCH .../aprovar e PATCH .../recusar por ID — não existe nenhum endpoint
 * que liste os vínculos pendentes. Pra aprovar/recusar você precisa saber o
 * ID do recrutador de antemão (ex.: via Admin → Usuários, olhando o ID do
 * usuário que solicitou o vínculo).
 */
export default function AdminRecrutadores() {
  const [recrutadorId, setRecrutadorId] = useState("");
  const [processando, setProcessando] = useState(false);
  const [resultado, setResultado] = useState(null);
  const [erro, setErro] = useState("");

  async function executar(acao) {
    setProcessando(true);
    setErro("");
    setResultado(null);
    try {
      const fn = acao === "aprovar" ? admin.aprovarVinculoRecrutador : admin.recusarVinculoRecrutador;
      const resposta = await fn(Number(recrutadorId));
      setResultado(resposta);
    } catch (err) {
      setErro(mensagemDeErro(err, "Não foi possível processar o vínculo."));
    } finally {
      setProcessando(false);
    }
  }

  return (
    <div>
      <Secao
        titulo="Aprovar ou recusar vínculo de recrutador"
        subtitulo="A API não lista vínculos pendentes — informe o ID do recrutador diretamente."
      >
        <div className="form-inline">
          <input
            type="number"
            min="1"
            placeholder="ID do recrutador"
            value={recrutadorId}
            onChange={(e) => setRecrutadorId(e.target.value)}
          />
          <button
            type="button"
            className="botao-pequeno botao-pequeno-primario"
            onClick={() => executar("aprovar")}
            disabled={processando || !recrutadorId}
          >
            {processando ? "Processando..." : "Aprovar"}
          </button>
          <button
            type="button"
            className="botao-pequeno botao-pequeno-perigo"
            onClick={() => executar("recusar")}
            disabled={processando || !recrutadorId}
          >
            {processando ? "Processando..." : "Recusar"}
          </button>
        </div>

        {erro && <Aviso>{erro}</Aviso>}

        {resultado && (
          <div className="lista-cards">
            <div className="item-card">
              <div className="item-card-info">
                <div className="item-card-titulo-linha">
                  <p className="item-card-titulo">
                    {resultado.empresa?.nome ?? "Sem empresa vinculada"}
                  </p>
                  <Badge tom={TOM_VINCULO[resultado.statusVinculoEmpresa] ?? "neutro"}>
                    {resultado.statusVinculoEmpresa}
                  </Badge>
                </div>
              </div>
            </div>
          </div>
        )}
      </Secao>
    </div>
  );
}
