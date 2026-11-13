import type Escoteiro from '@/data/models/Escoteiro'
import type Responsavel from '@/data/models/Responsavel'
import { requisitar } from '@/data/services/api'

// O que se define ao tornar alguém escoteiro: a categoria de mensalidade e a patrulha (a tropa vem dela)
export interface DadosEscoteiro {
  cat_mensalidade: number
  patrulha: { cod: number }
}

export function listarEscoteiros(tropa?: number): Promise<Escoteiro[]> {
  return requisitar<Escoteiro[]>(tropa ? `/escoteiros?tropa=${tropa}` : '/escoteiros')
}

export function buscarEscoteiro(memCod: number): Promise<Escoteiro> {
  return requisitar<Escoteiro>(`/escoteiros/${memCod}`)
}

export function tornarEscoteiro(memCod: number, dados: DadosEscoteiro): Promise<Escoteiro> {
  return requisitar<Escoteiro>(`/membros/${memCod}/escoteiro`, { method: 'POST', body: JSON.stringify(dados) })
}

export function alterarEscoteiro(memCod: number, dados: DadosEscoteiro): Promise<Escoteiro> {
  return requisitar<Escoteiro>(`/escoteiros/${memCod}`, { method: 'PUT', body: JSON.stringify(dados) })
}

export function vincularResponsavel(memCodEscoteiro: number, memCodResponsavel: number): Promise<Responsavel[]> {
  return requisitar<Responsavel[]>(`/escoteiros/${memCodEscoteiro}/responsaveis/${memCodResponsavel}`, { method: 'POST' })
}

export function desvincularResponsavel(memCodEscoteiro: number, memCodResponsavel: number): Promise<Responsavel[]> {
  return requisitar<Responsavel[]>(`/escoteiros/${memCodEscoteiro}/responsaveis/${memCodResponsavel}`, { method: 'DELETE' })
}
