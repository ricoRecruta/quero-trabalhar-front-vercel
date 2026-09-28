/**
 * Tradução entre o contrato da API e o que a interface consome.
 *
 * Manter isso separado dos módulos de endpoint deixa claro o que é contrato
 * do backend e o que é decisão de apresentação.
 */

export const ROTULOS_MODALIDADE = {
  REMOTO: "Remoto",
  PRESENCIAL: "Presencial",
  HIBRIDO: "Híbrido",
};

export function rotuloModalidade(modalidade) {
  return ROTULOS_MODALIDADE[modalidade] ?? "Não informada";
}

/** "João Pessoa - PB", com queda para estado, país ou aviso. */
export function formatarLocalidade(vaga) {
  const cidade = vaga?.cidade;
  const uf = vaga?.estadoSigla ?? vaga?.estado;

  if (cidade && uf) return `${cidade} - ${uf}`;
  if (cidade) return cidade;
  if (uf) return uf;
  if (vaga?.pais) return vaga.pais;
  return "Localidade não informada";
}

/** Quem publicou: a empresa, quando houver; senão o recrutador. */
export function formatarPublicador(vaga) {
  return vaga?.empresaNome || vaga?.recrutadorNome || "Publicação independente";
}

/**
 * Achata OportunidadeDeEmpregoPublicaResponseDTO no formato do card.
 *
 * Observação: a API não expõe salário nem data de publicação, então o card
 * não exibe esses campos — eles existiam apenas nos dados de exemplo.
 */
export function vagaParaCartao(vaga) {
  return {
    id: vaga.id,
    cargo: vaga.tipoDeEmprego || "Vaga sem tipo definido",
    descricao: vaga.descricao || "",
    publicador: formatarPublicador(vaga),
    localidade: formatarLocalidade(vaga),
    modalidade: rotuloModalidade(vaga.modalidade),
    empresaId: vaga.empresaId ?? null,
  };
}
