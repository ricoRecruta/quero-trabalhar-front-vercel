import { useEffect, useState } from "react";
import "./Formulario.css";
import { empresas, mensagemDeErro, oportunidades } from "../../api";
import SeletorLocalidade from "../shared/SeletorLocalidade";
import SeletorTipoDeEmprego from "../shared/SeletorTipoDeEmprego";

const MODALIDADES = [
  { valor: "REMOTO", rotulo: "Remoto" },
  { valor: "PRESENCIAL", rotulo: "Presencial" },
  { valor: "HIBRIDO", rotulo: "Híbrido" },
];

function estadoInicial(vagaEditando) {
  if (!vagaEditando) {
    return {
      descricao: "",
      tipoDeEmpregoId: null,
      modalidade: "REMOTO",
      localidade: {
        paisId: null,
        paisNome: "",
        estadoId: null,
        estadoNome: "",
        cidadeId: null,
        cidadeNome: "",
      },
      usarTextoLivre: false,
      localidadeTexto: "",
      empresaId: null,
    };
  }
  return {
    descricao: vagaEditando.descricao ?? "",
    tipoDeEmpregoId: vagaEditando.tipoDeEmpregoId ?? null,
    modalidade: vagaEditando.modalidade ?? "REMOTO",
    localidade: {
      paisId: vagaEditando.paisId ?? null,
      paisNome: vagaEditando.pais ?? "",
      estadoId: vagaEditando.estadoId ?? null,
      estadoNome: vagaEditando.estado ?? "",
      cidadeId: vagaEditando.cidadeId ?? null,
      cidadeNome: vagaEditando.cidade ?? "",
    },
    usarTextoLivre: false,
    localidadeTexto: vagaEditando.localidadeTextoOriginal ?? "",
    empresaId: vagaEditando.empresaId ?? null,
  };
}

/**
 * Cria ou edita uma oportunidade — payload real de OportunidadeDeEmpregoRequestDTO:
 * descricao, tipoDeEmpregoId, modalidade, paisId/estadoId/cidadeId OU
 * localidadeTexto, publicarComoEmpresa. Não existem os campos "remuneração",
 * "e-mail de contato", "endereço/número" nem "requisitos" no backend — por
 * isso não aparecem aqui, ao contrário da versão anterior desta tela.
 */
export default function CriarVaga({ vagaEditando, onConcluir, onCancelar }) {
  const editando = Boolean(vagaEditando);
  const [form, setForm] = useState(() => estadoInicial(vagaEditando));
  const [empresasDisponiveis, setEmpresasDisponiveis] = useState([]);
  const [salvando, setSalvando] = useState(false);
  const [excluindo, setExcluindo] = useState(false);
  const [erro, setErro] = useState("");

  // Não há efeito para sincronizar `form` com `vagaEditando`: o componente é
  // montado com `key={vagaEditando?.id ?? "novo"}` em App.jsx, então trocar
  // de vaga (ou ir de editar pra criar) sempre remonta o componente do zero
  // e o useState(() => estadoInicial(...)) acima já parte do valor certo.

  useEffect(() => {
    let ativo = true;
    empresas
      .listar({ size: 100 })
      .then((dados) => {
        if (ativo) setEmpresasDisponiveis(dados.itens);
      })
      .catch(() => {
        if (ativo) setEmpresasDisponiveis([]);
      });
    return () => {
      ativo = false;
    };
  }, []);

  function atualizarCampo(campo, valor) {
    setForm((prev) => ({ ...prev, [campo]: valor }));
  }

  function montarPayload() {
    const base = {
      descricao: form.descricao,
      tipoDeEmpregoId: form.tipoDeEmpregoId,
      modalidade: form.modalidade,
      empresaId: form.empresaId,
      publicarComoEmpresa: false,
    };

    if (form.usarTextoLivre) {
      return { ...base, localidadeTexto: form.localidadeTexto || null };
    }
    return {
      ...base,
      paisId: form.localidade.paisId,
      estadoId: form.localidade.estadoId,
      cidadeId: form.localidade.cidadeId,
    };
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setErro("");
    setSalvando(true);
    try {
      const payload = montarPayload();
      const salva = editando
        ? await oportunidades.atualizar(vagaEditando.id, payload)
        : await oportunidades.criar(payload);
      onConcluir?.(
        editando ? "Vaga atualizada com sucesso." : "Vaga cadastrada com sucesso.",
        salva
      );
    } catch (err) {
      setErro(mensagemDeErro(err, "Não foi possível salvar a vaga."));
    } finally {
      setSalvando(false);
    }
  }

  async function handleExcluir() {
    if (!editando) return;
    if (!window.confirm("Remover esta vaga? Essa ação não pode ser desfeita.")) return;
    setExcluindo(true);
    setErro("");
    try {
      await oportunidades.remover(vagaEditando.id);
      onConcluir?.("Vaga removida.", null);
    } catch (err) {
      setErro(mensagemDeErro(err, "Não foi possível remover a vaga."));
    } finally {
      setExcluindo(false);
    }
  }

  return (
    <form onSubmit={handleSubmit}>
      <h1 className="formulario-titulo">
        {editando ? "Editar Vaga" : "Cadastrar Nova Vaga"}
      </h1>
      <p className="formulario-subtitulo">
        {editando
          ? "Atualize os dados da oportunidade."
          : "Publique uma oportunidade de emprego."}
      </p>

      <div className="formulario-linha formulario-linha-2">
        <SeletorTipoDeEmprego
          value={form.tipoDeEmpregoId}
          onChange={(v) => atualizarCampo("tipoDeEmpregoId", v)}
          obrigatorio
        />

        <div className="formulario-campo">
          <label htmlFor="modalidade" className="formulario-label">
            Modalidade *
          </label>
          <select
            id="modalidade"
            className="formulario-select"
            value={form.modalidade}
            onChange={(e) => atualizarCampo("modalidade", e.target.value)}
            required
          >
            {MODALIDADES.map((m) => (
              <option key={m.valor} value={m.valor}>
                {m.rotulo}
              </option>
            ))}
          </select>
        </div>
      </div>

      <div className="formulario-campo" style={{ marginBottom: 24 }}>
        <label htmlFor="descricao" className="formulario-label">
          Descrição da vaga *
        </label>
        <textarea
          id="descricao"
          className="formulario-textarea"
          value={form.descricao}
          onChange={(e) => atualizarCampo("descricao", e.target.value)}
          maxLength={500}
          required
        />
      </div>

      <div className="formulario-campo" style={{ marginBottom: 24 }}>
        <label htmlFor="empresa" className="formulario-label">
          Empresa
        </label>
        <select
          id="empresa"
          className="formulario-select"
          value={form.empresaId ?? ""}
          onChange={(e) => atualizarCampo("empresaId", e.target.value ? Number(e.target.value) : null)}
          disabled={editando}
        >
          <option value="">Publicação sem empresa</option>
          {empresasDisponiveis.map((empresa) => (
            <option key={empresa.id} value={empresa.id}>
              {empresa.nome}
            </option>
          ))}
        </select>
      </div>

      <div className="formulario-toggle-linha">
        <label className="formulario-toggle">
          <input
            type="checkbox"
            checked={form.usarTextoLivre}
            onChange={(e) => atualizarCampo("usarTextoLivre", e.target.checked)}
          />
          <span className="formulario-toggle-trilho" />
        </label>
        <span className="formulario-toggle-texto">
          Não sei a cidade cadastrada — vou descrever a localidade em texto
        </span>
      </div>

      {form.usarTextoLivre ? (
        <div className="formulario-campo" style={{ marginBottom: 24 }}>
          <label htmlFor="localidadeTexto" className="formulario-label">
            Localidade (texto livre)
          </label>
          <input
            id="localidadeTexto"
            className="formulario-input"
            value={form.localidadeTexto}
            onChange={(e) => atualizarCampo("localidadeTexto", e.target.value)}
            placeholder="Ex.: João Pessoa e região"
          />
          <p style={{ fontSize: "0.82rem", color: "#6b7280", marginTop: 8 }}>
            Se o sistema não conseguir validar esse texto automaticamente, a
            vaga fica pendente e não aparece na listagem pública até ser
            resolvida.
          </p>
        </div>
      ) : (
        <SeletorLocalidade
          value={form.localidade}
          onChange={(v) => atualizarCampo("localidade", v)}
        />
      )}

      {erro && (
        <p style={{ color: "#b42318", fontSize: "0.9rem", marginBottom: 16 }} role="alert">
          {erro}
        </p>
      )}

      <div className="formulario-botoes">
        <button type="submit" className="formulario-botao-primario" disabled={salvando}>
          {salvando ? "Salvando..." : editando ? "Salvar alterações" : "Cadastrar Vaga"}
        </button>
        {editando && (
          <button
            type="button"
            className="formulario-botao-secundario"
            onClick={handleExcluir}
            disabled={excluindo}
          >
            {excluindo ? "Removendo..." : "Excluir vaga"}
          </button>
        )}
        <button type="button" className="formulario-botao-secundario" onClick={onCancelar}>
          Cancelar
        </button>
      </div>
    </form>
  );
}
