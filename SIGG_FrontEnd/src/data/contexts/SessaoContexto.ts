import { createContext, useContext } from 'react'
import type Usuario from '@/data/models/Usuario'

export interface ValorDaSessao {
  usuario: Usuario | null
  carregando: boolean
  entrar: (cpf: string, senha: string) => Promise<void>
  sair: () => Promise<void>
}

export const SessaoContexto = createContext<ValorDaSessao>({
  usuario: null,
  carregando: false,
  entrar: async () => {},
  sair: async () => {},
})

export function useSessao(): ValorDaSessao {
  return useContext(SessaoContexto)
}
