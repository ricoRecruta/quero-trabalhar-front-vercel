import { useState } from "react";
import "./Cadastro.css";
import { ApiError, usuarios } from "../../api";
import cadastroPhoto from "../../images/login-quero-trabalhar.png";

function LogoIcon({ className = "" }) {
  return (
    <svg viewBox="0 0 40 40" className={className} fill="none" xmlns="http://www.w3.org/2000/svg">
      <circle cx="20" cy="20" r="20" fill="currentColor" />
      <path
        d="M20 10c-3.3 0-6 2.7-6 6 0 4.5 6 11 6 11s6-6.5 6-11c0-3.3-2.7-6-6-6z"
        fill="white"
      />
      <circle cx="20" cy="16" r="2.2" fill="currentColor" />
    </svg>
  );
}

/** Remove máscara: "123.456.789-09" -> "12345678909" */
function apenasDigitos(valor) {
  return valor.replace(/\D/g, "");
}

export default function Cadastro({ onIrParaLogin }) {
  const [nome, setNome] = useState("");
  const [cpf, setCpf] = useState("");
  const [telefone, setTelefone] = useState("");
  const [email, setEmail] = useState("");
  const [senha, setSenha] = useState("");
  const [confirmarSenha, setConfirmarSenha] = useState("");
  const [loading, setLoading] = useState(false);
  const [erro, setErro] = useState("");
  const [sucesso, setSucesso] = useState(false);

  async function handleSubmit(e) {
    e.preventDefault();
    setErro("");

    if (senha !== confirmarSenha) {
      setErro("As senhas não coincidem.");
      return;
    }

    setLoading(true);
    try {
      // A API exige cpf e telefone além de nome, email e senha.
      // Enviamos cpf e telefone apenas com dígitos.
      await usuarios.cadastrar({
        cpf: apenasDigitos(cpf),
        nome,
        telefone: apenasDigitos(telefone),
        email,
        senha,
      });
      setSucesso(true);
    } catch (err) {
      setErro(
        err instanceof ApiError
          ? err.message
          : "Não foi possível concluir o cadastro. Tente novamente."
      );
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="cadastro-page">
      <div className="cadastro-container">
        {/* Painel esquerdo com foto */}
        <div className="cadastro-photo-panel">
          <img
            src={cadastroPhoto}
            alt="Pessoa trabalhando em um laptop"
            className="cadastro-photo"
          />
          <div className="cadastro-logo">
            <LogoIcon className="cadastro-logo-icon" />
          </div>
        </div>

        {/* Painel direito com formulário */}
        <div className="cadastro-form-panel">
          <h1 className="cadastro-title">Crie sua conta</h1>
          <p className="cadastro-subtitle">
            Preencha os dados abaixo para começar
          </p>

          <form onSubmit={handleSubmit} className="cadastro-form">
            <div className="cadastro-field">
              <label htmlFor="nome" className="cadastro-label">
                Nome completo
              </label>
              <input
                id="nome"
                type="text"
                value={nome}
                onChange={(e) => setNome(e.target.value)}
                required
                autoComplete="name"
                className="cadastro-input"
              />
            </div>

            <div className="cadastro-field">
              <label htmlFor="cpf" className="cadastro-label">
                CPF
              </label>
              <input
                id="cpf"
                type="text"
                inputMode="numeric"
                value={cpf}
                onChange={(e) => setCpf(e.target.value)}
                required
                maxLength={14}
                placeholder="000.000.000-00"
                className="cadastro-input"
              />
            </div>

            <div className="cadastro-field">
              <label htmlFor="telefone" className="cadastro-label">
                Telefone
              </label>
              <input
                id="telefone"
                type="tel"
                value={telefone}
                onChange={(e) => setTelefone(e.target.value)}
                required
                autoComplete="tel"
                placeholder="(83) 99999-0000"
                className="cadastro-input"
              />
            </div>

            <div className="cadastro-field">
              <label htmlFor="email" className="cadastro-label">
                E-mail
              </label>
              <input
                id="email"
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                autoComplete="email"
                className="cadastro-input"
              />
            </div>

            <div className="cadastro-field">
              <label htmlFor="senha" className="cadastro-label">
                Senha
              </label>
              <input
                id="senha"
                type="password"
                value={senha}
                onChange={(e) => setSenha(e.target.value)}
                required
                autoComplete="new-password"
                className="cadastro-input"
              />
            </div>

            <div className="cadastro-field">
              <label htmlFor="confirmarSenha" className="cadastro-label">
                Confirmar senha
              </label>
              <input
                id="confirmarSenha"
                type="password"
                value={confirmarSenha}
                onChange={(e) => setConfirmarSenha(e.target.value)}
                required
                autoComplete="new-password"
                className="cadastro-input"
              />
            </div>

            {erro && (
              <p className="cadastro-error" role="alert">
                {erro}
              </p>
            )}

            {sucesso && (
              <p className="cadastro-sucesso" role="status">
                Cadastro criado com sucesso! Já dá para entrar com seu e-mail e senha.
              </p>
            )}

            <button type="submit" disabled={loading} className="cadastro-button">
              {loading ? "Cadastrando..." : "Cadastrar"}
            </button>

            <p className="cadastro-login-link">
              Já tem uma conta?{" "}
              <button type="button" onClick={onIrParaLogin} className="cadastro-link-button">
                Entrar
              </button>
            </p>
          </form>
        </div>
      </div>
    </div>
  );
}