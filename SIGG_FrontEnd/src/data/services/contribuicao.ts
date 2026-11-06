import type Contribuicao from '@/data/models/Contribuicao'
import type RegistroContribuicao from '@/data/models/RegistroContribuicao'
import type VerificacaoContribuicao from '@/data/models/VerificacaoContribuicao'
import { requisitar } from '@/data/services/api'

export function buscarContribuicoes(): Promise<Contribuicao[]> {
  return requisitar<Contribuicao[]>('/contribuicoes')
}

export function verificarContribuicao(cpfAnjo: string, cpfAdotado: string): Promise<VerificacaoContribuicao> {
  const parametros = new URLSearchParams({ cpfAnjo, cpfAdotado })
  return requisitar<VerificacaoContribuicao>(`/contribuicoes/verificacao?${parametros.toString()}`)
}

export function registrarContribuicoes(registro: RegistroContribuicao): Promise<Contribuicao[]> {
  return requisitar<Contribuicao[]>('/contribuicoes', {
    method: 'POST',
    body: JSON.stringify(registro),
  })
}
