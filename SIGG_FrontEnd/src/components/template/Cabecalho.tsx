import { NavLink } from 'react-router'
import './Cabecalho.css'

// NavLink é um link que ganha a classe "active" quando a página dele está aberta
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
