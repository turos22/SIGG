import { createContext, useContext } from 'react'
import logoPadrao from '@/assets/logo_escoteiros.png'
import type Parametrizacao from '@/data/models/Parametrizacao'

// Usados enquanto o backend não respondeu (ou se estiver fora do ar), para a tela nunca ficar sem nome.
export const PARAMETRIZACAO_PADRAO: Parametrizacao = {
  sigla: 'SIGG',
  nomeSistema: 'Sistema Integrado de Gestão Guayporé',
  slogan: 'Sempre Alerta',
  nomeGrupo: 'Grupo de Escoteiros Guayporé 186º/SP',
  endereco: null,
  cidade: null,
  estado: null,
  telefone: null,
  email: null,
  logo: null,
}

export const LOGO_PADRAO = logoPadrao

export interface ValorDaParametrizacao {
  parametrizacao: Parametrizacao
  logo: string // o logo do banco, ou o padrão do projeto
  definir: (nova: Parametrizacao) => void
}

export const ParametrizacaoContexto = createContext<ValorDaParametrizacao>({
  parametrizacao: PARAMETRIZACAO_PADRAO,
  logo: LOGO_PADRAO,
  definir: () => {},
})

export function useParametrizacao(): ValorDaParametrizacao {
  return useContext(ParametrizacaoContexto)
}
