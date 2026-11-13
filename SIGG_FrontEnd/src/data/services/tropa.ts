import type Patrulha from '@/data/models/Patrulha'
import type Tropa from '@/data/models/Tropa'
import { requisitar } from '@/data/services/api'

export function listarTropas(): Promise<Tropa[]> {
  return requisitar<Tropa[]>('/tropas')
}

export function listarPatrulhas(tropa?: number): Promise<Patrulha[]> {
  return requisitar<Patrulha[]>(tropa ? `/patrulhas?tropa=${tropa}` : '/patrulhas')
}

export function criarTropa(nome: string): Promise<Tropa> {
  return requisitar<Tropa>('/tropas', { method: 'POST', body: JSON.stringify({ nome }) })
}

export function alterarTropa(cod: number, nome: string): Promise<Tropa> {
  return requisitar<Tropa>(`/tropas/${cod}`, { method: 'PUT', body: JSON.stringify({ nome }) })
}

export function excluirTropa(cod: number): Promise<{ mensagem: string }> {
  return requisitar(`/tropas/${cod}`, { method: 'DELETE' })
}

// Líder e sublíder são opcionais: 0 = sem escoteiro nesse posto
export interface DadosPatrulha {
  nome: string
  tropa: number
  lider: number
  sublider: number
}

function corpoDaPatrulha(d: DadosPatrulha) {
  return {
    nome: d.nome,
    tropa: { cod: d.tropa },
    lider: d.lider ? { esc_cod: d.lider } : null,
    sublider: d.sublider ? { esc_cod: d.sublider } : null,
  }
}

export function criarPatrulha(dados: DadosPatrulha): Promise<Patrulha> {
  return requisitar<Patrulha>('/patrulhas', { method: 'POST', body: JSON.stringify(corpoDaPatrulha(dados)) })
}

export function alterarPatrulha(cod: number, dados: DadosPatrulha): Promise<Patrulha> {
  return requisitar<Patrulha>(`/patrulhas/${cod}`, { method: 'PUT', body: JSON.stringify(corpoDaPatrulha(dados)) })
}

export function excluirPatrulha(cod: number): Promise<{ mensagem: string }> {
  return requisitar(`/patrulhas/${cod}`, { method: 'DELETE' })
}
