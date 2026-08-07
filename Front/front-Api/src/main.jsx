import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter, Routes, Route } from 'react-router-dom'
import './index.css'
import 'bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap/dist/js/bootstrap.bundle.min.js'
import 'bootstrap-icons/font/bootstrap-icons.css'
import Layout from './components/Layout.jsx'
import Home from './pages/Home.jsx'
import CompraRapida from './pages/CompraRapida.jsx'
import Licitacion from './pages/Licitacion.jsx'

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <BrowserRouter>
      <Routes>
        <Route element={<Layout />}>
          <Route path="/" element={<Home />} />
          <Route path="/compra-agil" element={<CompraRapida />} />
          <Route path="/licitacion" element={<Licitacion />} />
        </Route>
      </Routes>
    </BrowserRouter>
  </StrictMode>,
)
