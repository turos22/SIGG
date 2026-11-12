import { useState } from 'react'
import { NavLink } from 'react-router'
import logoEscoteiros from '@/assets/logo_escoteiros.png'
import { useSessao } from '@/data/contexts/SessaoContexto'
import { AUTENTICACAO_ATIVA } from '@/data/services/sessao'
import { podeAcessar } from '@/data/utils/permissoes'
import Icone, { type NomeIcone } from './Icone'
import './MenuLateral.css'

interface Subitem {
  rotulo: string
  to?: string
}

interface Grupo {
  id: string
  rotulo: string
  icone: NomeIcone
  sub: Subitem[]
}

const GRUPOS: Grupo[] = [
  { id: 'inicio', rotulo: 'Início', icone: 'inicio', sub: [{ rotulo: 'Painel do grupo', to: '/' }, { rotulo: 'Avisos' }, { rotulo: 'Aniversariantes' }] },
  { id: 'membros', rotulo: 'Membros', icone: 'responsaveis', sub: [{ rotulo: 'Listar membros', to: '/membros' }] },
  { id: 'tropa', rotulo: 'Tropa', icone: 'tropa', sub: [{ rotulo: 'Gerenciar tropas', to: '/tropa' }] },
  { id: 'financeiro', rotulo: 'Financeiro', icone: 'financeiro', sub: [{ rotulo: 'Mensalidades', to: '/mensalidades' }, { rotulo: 'Contribuições (Anjos)', to: '/contribuicoes' }, { rotulo: 'Tipos de Atividade', to: '/tipos-atividade' }, { rotulo: 'Categorias de Mensalidade', to: '/categorias-mensalidade' }, { rotulo: 'Pagamento de Mensalidades', to: '/pagamentos-mensalidade' }, { rotulo: 'Pagamento de Atividades', to: '/pagamentos-atividade' }, ], },
  { id: 'administracao', rotulo: 'Administração', icone: 'responsaveis', sub: [{ rotulo: 'Contas de acesso', to: '/contas' }] },
]

export default function MenuLateral() {
  const [aberto, setAberto] = useState<string | null>(null)
  const [expandido, setExpandido] = useState(false)
  const { usuario, sair } = useSessao()
  const grupos = GRUPOS.map((grupo) => ({
    ...grupo,
    sub: grupo.sub.filter((sub) => !sub.to || podeAcessar(usuario?.perfis, sub.to)),
  })).filter((grupo) => grupo.sub.length > 0)

  function alternarGrupo(id: string) {
    const novo = aberto === id ? null : id
    setAberto(novo)
    if (novo) setExpandido(true)
  }

  function alternarMenu() {
    if (expandido) setAberto(null)
    setExpandido(!expandido)
  }

  return (
    <aside className={expandido ? 'menu-lateral expandido' : 'menu-lateral'} aria-label="Menu principal">
      <div className="menu-marca">
        <div className="menu-logo">
          <img src={logoEscoteiros} alt="Escoteiros do Brasil" />
        </div>
        <strong>
          SIGG<small>Sempre Alerta</small>
        </strong>
      </div>

      {grupos.map((grupo) => {
        const ativo = aberto === grupo.id
        return (
          <div key={grupo.id}>
            <button
              type="button"
              className={ativo ? 'menu-item ativo' : 'menu-item'}
              data-rotulo={grupo.rotulo}
              aria-expanded={ativo}
              aria-label={grupo.rotulo}
              onClick={() => alternarGrupo(grupo.id)}
            >
              <Icone nome={grupo.icone} />
              <span className="menu-rotulo">{grupo.rotulo}</span>
              <Icone nome="seta" classe="menu-seta" />
            </button>
            <div className={ativo ? 'menu-sub aberto' : 'menu-sub'} inert={!ativo}>
              <div>
                {grupo.sub.map((sub) =>
                  sub.to ? (
                    <NavLink key={sub.rotulo} to={sub.to} end className={({ isActive }) => (isActive ? 'atual' : undefined)}>
                      {sub.rotulo}
                    </NavLink>
                  ) : (
                    <span key={sub.rotulo} className="em-breve" aria-disabled="true" title="Em breve">
                      {sub.rotulo}
                    </span>
                  ),
                )}
              </div>
            </div>
          </div>
        )
      })}

      <div className="menu-espaco" />
      {AUTENTICACAO_ATIVA && usuario && (
        <button type="button" className="menu-item" data-rotulo={`Sair (${usuario.nome})`} aria-label="Sair do sistema" onClick={() => sair()}>
          <Icone nome="sair" />
          <span className="menu-rotulo">Sair</span>
        </button>
      )}
      <button type="button" className="menu-item menu-recolher" data-rotulo="Expandir menu" aria-label="Expandir ou recolher menu" onClick={alternarMenu}>
        <Icone nome="seta" />
        <span className="menu-rotulo">Recolher menu</span>
      </button>
    </aside>
  )
}
