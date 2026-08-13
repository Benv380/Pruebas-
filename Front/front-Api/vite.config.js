import { defineConfig } from 'vite'
import react, { reactCompilerPreset } from '@vitejs/plugin-react'
import babel from '@rolldown/plugin-babel'

export default defineConfig({
  plugins: [
    react(),
    babel({ presets: [reactCompilerPreset()] })
  ],
  server: {
    proxy: {
      // Reenvia al backend compra-service (puerto 8082) para evitar
      // problemas de CORS en desarrollo. El front llama a rutas relativas
      // como fetch('/compra/agil/1234-5-COT26').
      //
      // Nota: usamos una regex con "/" final en vez de la key string '/compra'
      // porque Vite matchea por PREFIJO ("/compra-agil".startsWith("/compra")
      // también es true) Con la key string, la ruta de la SPA /compra-agil
      // quedaba interceptada por el proxy: al recargar esa página el navegador
      // pedía GET /compra-agil al servidor y en vez de recibir el index.html
      // de React recibía el 404 JSON del backend Spring -> "se caía" la página.
      '^/compra/': {
        target: 'http://localhost:8082',
        changeOrigin: true,
      },
    },
  },
})
