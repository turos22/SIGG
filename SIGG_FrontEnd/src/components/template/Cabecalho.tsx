import { NavLink } from 'react-router'
import { useParametrizacao } from '@/data/contexts/ParametrizacaoContexto'
import './Cabecalho.css'

export default function Cabecalho() {
  const { parametrizacao } = useParametrizacao()
  return (
    <header className="cabecalho">
      <strong>{parametrizacao.sigla}</strong>
      <nav>
        <NavLink to="/" end>Início</NavLink>
        <NavLink to="/escoteiros">Escoteiros</NavLink>
          <NavLink to="/tipos-atividade">Tipos de Atividade</NavLink>
      </nav>
    </header>
  )
}
