import { useEffect, useState } from "react";
import { mensagemDeErro, tiposDeEmprego } from "../../api";

/**
 * Select alimentado por /api/tipos-de-emprego/aprovados, com um atalho pra
 * sugerir um tipo novo (POST /sugerir) quando o desejado ainda não existe
 * no catálogo — a sugestão só aparece pra todo mundo depois que um admin
 * aprova (ver telas de Admin), então aqui só avisamos que foi enviada.
 */
export default function SeletorTipoDeEmprego({ value, onChange, obrigatorio = false }) {
  const [tipos, setTipos] = useState([]);
  const [sugerindo, setSugerindo] = useState(false);
  const [novoTitulo, setNovoTitulo] = useState("");
  const [novaDescricao, setNovaDescricao] = useState("");
  const [enviandoSugestao, setEnviandoSugestao] = useState(false);
  const [mensagemSugestao, setMensagemSugestao] = useState("");

  function carregarTipos() {
    tiposDeEmprego
      .listarAprovados()
      .then(setTipos)
      .catch(() => setTipos([]));
  }

  useEffect(() => {
    carregarTipos();
  }, []);

  async function handleSugerir(e) {
    e.preventDefault();
    setEnviandoSugestao(true);
    setMensagemSugestao("");
    try {
      await tiposDeEmprego.sugerir({ titulo: novoTitulo, descricao: novaDescricao });
      setMensagemSugestao(
        "Sugestão enviada! Ela só aparece nesta lista depois que um admin aprovar."
      );
      setNovoTitulo("");
      setNovaDescricao("");
      setSugerindo(false);
    } catch (err) {
      setMensagemSugestao(mensagemDeErro(err, "Não foi possível enviar a sugestão."));
    } finally {
      setEnviandoSugestao(false);
    }
  }

  return (
    <div className="formulario-campo">
      <label htmlFor="tipo-emprego" className="formulario-label">
        Tipo de emprego{obrigatorio && " *"}
      </label>
      <select
        id="tipo-emprego"
        className="formulario-select"
        value={value ?? ""}
        onChange={(e) => onChange(e.target.value ? Number(e.target.value) : null)}
        required={obrigatorio}
      >
        <option value="">Selecione</option>
        {tipos.map((t) => (
          <option key={t.id} value={t.id}>
            {t.titulo}
          </option>
        ))}
      </select>

      {!sugerindo ? (
        <button
          type="button"
          className="botao-pequeno"
          style={{ marginTop: 10, alignSelf: "flex-start" }}
          onClick={() => setSugerindo(true)}
        >
          Não achei o tipo que eu queria — sugerir novo
        </button>
      ) : (
        <div style={{ marginTop: 12, padding: 14, background: "#f9fafb", borderRadius: 12 }}>
          <input
            type="text"
            placeholder="Título do novo tipo (ex.: Pedreiro)"
            value={novoTitulo}
            onChange={(e) => setNovoTitulo(e.target.value)}
            className="formulario-input"
            style={{ marginBottom: 10, width: "100%" }}
          />
          <textarea
            placeholder="Descrição breve"
            value={novaDescricao}
            onChange={(e) => setNovaDescricao(e.target.value)}
            className="formulario-textarea"
            style={{ height: 80, width: "100%", marginBottom: 10 }}
          />
          <div style={{ display: "flex", gap: 8 }}>
            <button
              type="button"
              className="botao-pequeno botao-pequeno-primario"
              onClick={handleSugerir}
              disabled={enviandoSugestao || !novoTitulo.trim()}
            >
              {enviandoSugestao ? "Enviando..." : "Enviar sugestão"}
            </button>
            <button type="button" className="botao-pequeno" onClick={() => setSugerindo(false)}>
              Cancelar
            </button>
          </div>
          {mensagemSugestao && (
            <p style={{ marginTop: 8, fontSize: "0.85rem" }}>{mensagemSugestao}</p>
          )}
        </div>
      )}
    </div>
  );
}
