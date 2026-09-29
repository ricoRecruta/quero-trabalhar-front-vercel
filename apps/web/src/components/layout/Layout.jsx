import "./Layout.css";

function LogoutIcon() {
  return (
    <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" strokeWidth="2">
      <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4M16 17l5-5-5-5M21 12H9" />
    </svg>
  );
}

const ABAS = [
  { id: "vagas", label: "Vagas" },
  { id: "criarVaga", label: "Criar Vaga" },
  { id: "recrutador", label: "Recrutador" },
  { id: "empresas", label: "Empresas" },
  { id: "indicacoes", label: "Indicações" },
  { id: "perfil", label: "Perfil" },
  { id: "admin", label: "Admin" },
];

const ABAS_EXCLUSIVAS_ADMIN = new Set(["criarVaga", "admin"]);

export default function Layout({
  abaAtiva,
  onMudarAba,
  usuario = "Usuário",
  administrador = false,
  onSair,
  children,
}) {
  const abasVisiveis = administrador
    ? ABAS
    : ABAS.filter((aba) => !ABAS_EXCLUSIVAS_ADMIN.has(aba.id));

  return (
    <div className="layout-page">
      {/* Barra superior */}
      <header className="layout-topbar">
        <span className="layout-topbar-usuario">{usuario}</span>
        <button type="button" className="layout-sair-botao" onClick={onSair}>
          <LogoutIcon /> Sair
        </button>
      </header>

      {/* Navegação */}
      <nav className="layout-nav">
        {abasVisiveis.map((aba) => (
          <button
            key={aba.id}
            type="button"
            className={`layout-nav-item ${abaAtiva === aba.id ? "layout-nav-item-ativo" : ""}`}
            onClick={() => onMudarAba(aba.id)}
          >
            {aba.label}
          </button>
        ))}
      </nav>

      {/* Conteúdo da página ativa */}
      <main className="layout-conteudo-fundo">
        <div className="layout-conteudo-card">{children}</div>
      </main>
    </div>
  );
}
