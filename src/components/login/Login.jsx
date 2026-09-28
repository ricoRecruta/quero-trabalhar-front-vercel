import { useState } from "react";
import "./Login.css";
import { ApiError, auth } from "../../api";
import loginPhoto from "../../images/login-quero-trabalhar.png";

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

export default function Login({ onEntrar, onIrParaCadastro }) {
  const [email, setEmail] = useState("");
  const [senha, setSenha] = useState("");
  const [loading, setLoading] = useState(false);
  const [erro, setErro] = useState("");

  async function handleSubmit(e) {
    e.preventDefault();
    setErro("");
    setLoading(true);
    try {
      // A API espera { email, password } e devolve o JWT no cabeçalho
      // Authorization da resposta — ver src/api/auth.js.
      await auth.login({ email, password: senha });
      const usuario = await auth.usuarioAtual();
      onEntrar?.(usuario);
    } catch (err) {
      setErro(
        err instanceof ApiError
          ? err.message
          : "Não foi possível entrar. Verifique se a API está no ar."
      );
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="login-page">
      <div className="login-container">
        {/* Painel esquerdo com foto */}
        <div className="login-photo-panel">
          <img
            src={loginPhoto}
            alt="Pessoa trabalhando em um laptop"
            className="login-photo"
          />
          <div className="login-logo">
            <LogoIcon className="login-logo-icon" />
          </div>
        </div>

        {/* Painel direito com formulário */}
        <div className="login-form-panel">
          <h1 className="login-title">Bem-Vindo!</h1>

          <form onSubmit={handleSubmit} className="login-form">
            <div className="login-field">
              <label htmlFor="email" className="login-label">
                E-mail
              </label>
              <input
                id="email"
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                autoComplete="email"
                className="login-input"
              />
            </div>

            <div className="login-field">
              <label htmlFor="senha" className="login-label">
                Senha
              </label>
              <input
                id="senha"
                type="password"
                value={senha}
                onChange={(e) => setSenha(e.target.value)}
                required
                autoComplete="current-password"
                className="login-input"
              />
            </div>

            {erro && (
              <p className="login-error" role="alert">
                {erro}
              </p>
            )}

            <button type="submit" disabled={loading} className="login-button">
              {loading ? "Entrando..." : "Entrar"}
            </button>

            <p className="login-cadastro-link">
              Ainda não tem uma conta?{" "}
              <button type="button" onClick={onIrParaCadastro} className="login-link-button">
                Cadastre-se
              </button>
            </p>
          </form>
        </div>
      </div>
    </div>
  );
}