import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  // Diretório do projeto; evita depender do global 'process' no config.
  const env = loadEnv(mode, '.', '')

  // Para onde o dev server encaminha as chamadas da API.
  // Ajuste em .env se o backend não estiver em localhost:8080.
  const target = env.VITE_API_PROXY_TARGET || 'http://localhost:8080'

  const proxy = { target, changeOrigin: true }

  return {
    plugins: [react()],
    server: {
      host: true, // necessário para acessar de fora do container
      port: 5173,
      proxy: {
        // Mantém front e API na mesma origem durante o desenvolvimento,
        // o que elimina qualquer problema de CORS e deixa o cabeçalho
        // Authorization da resposta de login chegar intacto.
        '/login': proxy,
        '/api': proxy,
        '/v3/api-docs': proxy,
        '/swagger-ui': proxy,
        // A API antiga (branch main) expõe o login em /auth/login.
        '/auth': proxy,
      },
    },
    preview: {
      host: true,
      port: 4173,
    },
  }
})
