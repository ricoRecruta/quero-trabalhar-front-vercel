import { useState } from "react";
import "../criarVaga/Formulario.css";
import AdminExperiencias from "./AdminExperiencias";
import AdminRecrutadores from "./AdminRecrutadores";
import AdminTiposDeEmprego from "./AdminTiposDeEmprego";
import AdminUsuarios from "./AdminUsuarios";

const ABAS = [
  { id: "usuarios", label: "Usuários", Componente: AdminUsuarios },
  { id: "tipos", label: "Tipos de Emprego", Componente: AdminTiposDeEmprego },
  { id: "experiencias", label: "Experiências", Componente: AdminExperiencias },
  { id: "recrutadores", label: "Vínculos de Recrutador", Componente: AdminRecrutadores },
];

/**
 * Painel de administração. Todas as chamadas exigem papel ADMIN
 * (@PreAuthorize("hasRole('ADMIN')") nos *AdminController do backend) — um
 * usuário sem esse papel recebe 403 em qualquer ação daqui, já tratado pelo
 * ApiError de cada tela.
 */
export default function Admin() {
  const [abaAtiva, setAbaAtiva] = useState("usuarios");
  const AbaAtual = ABAS.find((a) => a.id === abaAtiva)?.Componente ?? AdminUsuarios;

  return (
    <div>
      <h1 className="formulario-titulo">Administração</h1>
      <p className="formulario-subtitulo">
        Exige perfil de administrador. Se alguma ação aqui der "acesso negado",
        é porque sua conta não tem esse papel.
      </p>

      <div className="abas-internas">
        {ABAS.map((aba) => (
          <button
            key={aba.id}
            type="button"
            className={`aba-interna-item ${abaAtiva === aba.id ? "aba-interna-item-ativa" : ""}`}
            onClick={() => setAbaAtiva(aba.id)}
          >
            {aba.label}
          </button>
        ))}
      </div>

      <AbaAtual />
    </div>
  );
}
