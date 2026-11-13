import { requisitar } from '@/data/services/api'
import type AtividadePaga from '@/data/models/AtividadePaga'

export function buscarAtividadesPagas(somenteAbertas = false): Promise<AtividadePaga[]> {
  return requisitar<AtividadePaga[]>(`/atividades-pagas${somenteAbertas ? '?abertas=true' : ''}`)
}

export function ofertarAtividadePaga(atividade: Omit<AtividadePaga, 'codigo'>): Promise<AtividadePaga> {
  return requisitar<AtividadePaga>('/atividades-pagas', {
    method: 'POST',
    body: JSON.stringify(atividade),
  })
}
