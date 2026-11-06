import type { Perfil } from '@/data/models/Perfil'
import { AUTENTICACAO_ATIVA } from '@/data/services/sessao'

export const DIRETORIA: Perfil[] = ['ADMIN', 'PRESIDENTE', 'CHEFE', 'FINANCEIRO']

export const PERMISSOES: Record<string, Perfil[] | undefined> = {
  '/membros': DIRETORIA,
  '/tropa': ['ADMIN', 'PRESIDENTE', 'CHEFE'],
  '/contribuicoes': DIRETORIA,
}

export function podeAcessar(perfis: Perfil[] | undefined, rota: string): boolean {
  if (!AUTENTICACAO_ATIVA) return true
  const permitidos = PERMISSOES[rota]
  if (!permitidos) return true
  return perfis !== undefined && permitidos.some((perfil) => perfis.includes(perfil))
}
