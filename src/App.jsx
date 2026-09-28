import { useEffect, useState } from "react";
import Layout from "./components/layout/Layout";
import Vagas from "./components/vagas/Vagas";
import CriarVaga from "./components/criarVaga/CriarVaga";
import Recrutador from "./components/recrutador/Recrutador";
import Empresas from "./components/empresas/Empresas";
import Indicacoes from "./components/indicacoes/Indicacoes";
import Perfil from "./components/perfil/Perfil";
import Admin from "./components/admin/Admin";
import Login from "./components/login/Login";
import Cadastro from "./components/cadastro/Cadastro";
import { auth, estaAutenticado } from "./api";

export default function App() {
  const [abaAtiva, setAbaAtiva] = useState("vagas");
  const [vagaEditando, setVagaEditando] = useState(null);

  const [telaAuth, setTelaAuth] = useState("login"); // "login" | "cadastro"
  const [autenticado, setAutenticado] = useState(estaAutenticado);
  const [usuario, setUsuario] = useState(null);

  // Se já existe um token de uma sessão anterior (localStorage), tenta
  // recuperar os dados do usuário. Se o token estiver vencido ou inválido,
  // a API responde 401 e voltamos pra tela de login.
  useEffect(() => {
    if (!estaAutenticado()) return;
    auth
      .usuarioAtual()
      .then(setUsuario)
      .catch(() => {
        auth.logout();
        setAutenticado(false);
      });
  }, []);

  function handleEntrar(dadosUsuario) {
    setUsuario(dadosUsuario);
    setAutenticado(true);
    setAbaAtiva("vagas");
  }

  function handleSair() {
    auth.logout();
    setUsuario(null);
    setAutenticado(false);
    setTelaAuth("login");
  }

  /** Navegação por clique no menu: sempre sai do modo de edição de vaga. */
  function handleMudarAba(novaAba) {
    setVagaEditando(null);
    setAbaAtiva(novaAba);
  }

  /** Abre a tela de Criar Vaga — vazia (nova) ou pré-preenchida (edição). */
  function abrirCriarVaga(vaga) {
    setVagaEditando(vaga ?? null);
    setAbaAtiva("criarVaga");
  }

  function handleConcluirVaga() {
    setVagaEditando(null);
    setAbaAtiva("recrutador");
  }

  function renderConteudo() {
    switch (abaAtiva) {
      case "criarVaga":
        return (
          <CriarVaga
            key={vagaEditando?.id ?? "novo"}
            vagaEditando={vagaEditando}
            onConcluir={handleConcluirVaga}
            onCancelar={handleConcluirVaga}
          />
        );
      case "recrutador":
        return (
          <Recrutador
            onNovaVaga={() => abrirCriarVaga(null)}
            onEditarVaga={(vaga) => abrirCriarVaga(vaga)}
            onIrParaPerfil={() => handleMudarAba("perfil")}
          />
        );
      case "empresas":
        return <Empresas />;
      case "indicacoes":
        return <Indicacoes />;
      case "perfil":
        return <Perfil />;
      case "admin":
        return <Admin />;
      case "vagas":
      default:
        return <Vagas />;
    }
  }

  if (!autenticado) {
    return telaAuth === "cadastro" ? (
      <Cadastro onIrParaLogin={() => setTelaAuth("login")} />
    ) : (
      <Login onEntrar={handleEntrar} onIrParaCadastro={() => setTelaAuth("cadastro")} />
    );
  }

  return (
    <Layout
      abaAtiva={abaAtiva}
      onMudarAba={handleMudarAba}
      usuario={usuario?.nome ?? "Usuário"}
      onSair={handleSair}
    >
      {renderConteudo()}
    </Layout>
  );
}
