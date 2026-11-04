import { Outlet } from 'react-router'
import MenuLateral from '@/components/template/MenuLateral'
import './page.css'

export default function Layout() {
  return (
    <div className="app-layout">
      <MenuLateral />
      <div className="app-conteudo">
        <Outlet />
      </div>
    </div>
  )
}
