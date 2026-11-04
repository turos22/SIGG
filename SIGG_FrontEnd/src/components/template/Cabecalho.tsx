import { NavLink } from 'react-router'
import './Cabecalho.css'

export default function Cabecalho() {
  return (
    <header className="cabecalho">
      <strong>SIGG</strong>
      <nav>
        <NavLink to="/" end>Início</NavLink>
        <NavLink to="/escoteiros">Escoteiros</NavLink>
      </nav>
    </header>
  )
}
