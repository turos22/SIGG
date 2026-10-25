import { Outlet } from 'react-router'
import Cabecalho from '@/components/template/Cabecalho'

// Igual ao layout.tsx do Next: envolve todas as páginas. Cabeçalho, menu e providers
// (contexts) entram aqui; <Outlet /> é onde a página da rota atual aparece.
export default function Layout() {
  return (
    <>
      <Cabecalho />
      <Outlet />
    </>
  )
}
