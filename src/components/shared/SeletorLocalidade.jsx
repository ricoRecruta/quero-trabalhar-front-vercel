import { useEffect, useRef, useState } from "react";
import { localidades } from "../../api";

/**
 * Campo de autocomplete: digita, espera 350ms, busca na API, mostra opções.
 *
 * A API de localidades (/api/localidades/paises|estados|cidades) não expõe
 * "listar tudo" — ela exige um termo de busca com pelo menos 2 caracteres e
 * devolve lista vazia abaixo disso (confirmado lendo LocalidadeController).
 * Por isso este campo não é um <select> estático: é busca mesmo.
 */
function CampoAutocomplete({
  label,
  obrigatorio,
  disabled,
  placeholderDesabilitado,
  valorInicial,
  buscar,
  onSelecionar,
}) {
  const [termo, setTermo] = useState(valorInicial ?? "");
  const [opcoes, setOpcoes] = useState([]);
  const [buscando, setBuscando] = useState(false);
  // Guarda o nome do último item efetivamente selecionado (ou o valor
  // inicial, em modo edição). Enquanto `termo` for igual a isso, o efeito
  // não busca — sem essa guarda, escolher uma opção (ou só abrir a tela em
  // modo edição já preenchida) dispararia uma busca pelo próprio nome
  // escolhido, reabrindo o dropdown sozinho.
  //
  // Importante: isso precisa ser uma COMPARAÇÃO estável, não uma flag
  // "de uso único" — o Strict Mode do React roda efeitos duas vezes em
  // desenvolvimento, e uma flag que se apaga na primeira passada deixa a
  // segunda passada seguir livre e buscar de qualquer jeito.
  const ultimoSelecionadoRef = useRef(valorInicial ?? null);

  useEffect(() => {
    if (termo === ultimoSelecionadoRef.current) return;
    if (termo.trim().length < 2) return;
    let ativo = true;
    const timer = setTimeout(() => {
      setBuscando(true);
      buscar(termo.trim())
        .then((dados) => {
          if (ativo) setOpcoes(dados);
        })
        .catch(() => {
          if (ativo) setOpcoes([]);
        })
        .finally(() => {
          if (ativo) setBuscando(false);
        });
    }, 350);
    return () => {
      ativo = false;
      clearTimeout(timer);
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [termo]);

  function handleDigitar(e) {
    const v = e.target.value;
    setTermo(v);
    if (v.trim().length < 2) setOpcoes([]);
  }

  function selecionar(opcao) {
    ultimoSelecionadoRef.current = opcao.nome;
    setTermo(opcao.nome);
    setOpcoes([]);
    onSelecionar(opcao);
  }

  return (
    <div className="formulario-campo" style={{ position: "relative" }}>
      <label className="formulario-label">
        {label}
        {obrigatorio && " *"}
      </label>
      <input
        type="text"
        className="formulario-input"
        value={termo}
        onChange={handleDigitar}
        disabled={disabled}
        placeholder={disabled ? placeholderDesabilitado : "Digite pelo menos 2 letras..."}
        autoComplete="off"
        required={obrigatorio}
      />
      {buscando && (
        <span style={{ fontSize: "0.78rem", color: "#9ca3af", marginTop: 4 }}>Buscando...</span>
      )}
      {opcoes.length > 0 && (
        <ul
          style={{
            position: "absolute",
            top: "100%",
            left: 0,
            right: 0,
            zIndex: 10,
            margin: "4px 0 0",
            padding: 6,
            listStyle: "none",
            background: "#ffffff",
            borderRadius: 12,
            boxShadow: "0 4px 16px rgba(0,0,0,0.12)",
            maxHeight: 220,
            overflowY: "auto",
          }}
        >
          {opcoes.map((opcao) => (
            <li key={opcao.id}>
              <button
                type="button"
                onMouseDown={() => selecionar(opcao)}
                style={{
                  display: "block",
                  width: "100%",
                  textAlign: "left",
                  padding: "8px 10px",
                  border: "none",
                  background: "transparent",
                  borderRadius: 8,
                  cursor: "pointer",
                  font: "inherit",
                }}
                onMouseEnter={(e) => (e.currentTarget.style.background = "#f3f4f6")}
                onMouseLeave={(e) => (e.currentTarget.style.background = "transparent")}
              >
                {opcao.nome}
                {opcao.sigla ? ` (${opcao.sigla})` : ""}
              </button>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}

/**
 * País → Estado → Cidade em autocomplete encadeado.
 *
 * value: { paisId, paisNome, estadoId, estadoNome, cidadeId, cidadeNome }
 * onChange(novoValue) — a troca de país/estado remonta os campos abaixo
 * (via key), o que já limpa a seleção anterior sem precisar de efeito
 * algum sincronizando estado entre os três campos.
 */
export default function SeletorLocalidade({ value, onChange, obrigatorio = false }) {
  return (
    <div className="formulario-linha formulario-linha-3">
      <CampoAutocomplete
        key="pais"
        label="País"
        obrigatorio={obrigatorio}
        valorInicial={value.paisNome}
        buscar={(termo) => localidades.listarPaises(termo)}
        onSelecionar={(opcao) =>
          onChange({
            paisId: opcao.id,
            paisNome: opcao.nome,
            estadoId: null,
            estadoNome: "",
            cidadeId: null,
            cidadeNome: "",
          })
        }
      />

      <CampoAutocomplete
        key={`estado-${value.paisId ?? "none"}`}
        label="Estado"
        obrigatorio={obrigatorio}
        disabled={!value.paisId}
        placeholderDesabilitado="Escolha um país primeiro"
        valorInicial={value.estadoNome}
        buscar={(termo) => localidades.listarEstados(value.paisId, termo)}
        onSelecionar={(opcao) =>
          onChange({
            ...value,
            estadoId: opcao.id,
            estadoNome: opcao.nome,
            cidadeId: null,
            cidadeNome: "",
          })
        }
      />

      <CampoAutocomplete
        key={`cidade-${value.estadoId ?? "none"}`}
        label="Cidade"
        obrigatorio={obrigatorio}
        disabled={!value.estadoId}
        placeholderDesabilitado="Escolha um estado primeiro"
        valorInicial={value.cidadeNome}
        buscar={(termo) => localidades.listarCidades(value.estadoId, termo)}
        onSelecionar={(opcao) =>
          onChange({ ...value, cidadeId: opcao.id, cidadeNome: opcao.nome })
        }
      />
    </div>
  );
}
