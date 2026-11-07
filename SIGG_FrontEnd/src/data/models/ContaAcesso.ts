import type { Perfil } from '@/data/models/Perfil'

export type SituacaoConta = 'A' | 'P' | 'I'

export default interface ContaAcesso {
  cta_cod: number
  mem_cod: number
  nome: string
  cpf: string
  email: string | null
  situacao: SituacaoConta
  admin: boolean
  perfis: Perfil[]
  tentativas: number
  bloqueio_ate: string | null
  dt_criacao: string
}
