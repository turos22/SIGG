import type Usuario from '@/data/models/Usuario'

export default interface SessaoAutenticada {
  token_acesso: string
  expira_em: number
  usuario: Usuario
}
