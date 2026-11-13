import type Chefe from '@/data/models/Chefe'
import type Escoteiro from '@/data/models/Escoteiro'
import type Financeiro from '@/data/models/Financeiro'
import type Presidente from '@/data/models/Presidente'
import type Responsavel from '@/data/models/Responsavel'
import type SaudeApi from '@/data/models/SaudeApi'
import { requisitar } from '@/data/services/api'
import type { DadosPessoa } from '@/data/models/DadosPessoa'
import type Membro from '@/data/models/Membro'

export function buscarSaudeDoBanco(): Promise<SaudeApi> {
  return requisitar<SaudeApi>('/health/db')
}

export function buscarEscoteiros(): Promise<Escoteiro[]> {
  return requisitar<Escoteiro[]>('/escoteiros')
}

export function buscarChefes(): Promise<Chefe[]> {
  return requisitar<Chefe[]>('/chefes')
}

export function buscarPresidentes(): Promise<Presidente[]> {
  return requisitar<Presidente[]>('/presidentes')
}

export function buscarResponsaveis(): Promise<Responsavel[]> {
  return requisitar<Responsavel[]>('/responsaveis')
}

export function buscarFinanceiros(): Promise<Financeiro[]> {
  return requisitar<Financeiro[]>('/financeiros')
}

export function listarMembros(somenteVoluntarios = false): Promise<Membro[]> {
  return requisitar<Membro[]>(somenteVoluntarios ? '/membros?voluntarios=true' : '/membros')
}

function corpoDaPessoa(dados: DadosPessoa) {
  return { ...dados, telefone: dados.telefone || null, email: dados.email || null }
}

export function cadastrarMembro(dados: DadosPessoa): Promise<Membro> {
  return requisitar<Membro>('/membros', { method: 'POST', body: JSON.stringify(corpoDaPessoa(dados)) })
}

export function alterarMembro(memCod: number, dados: DadosPessoa): Promise<Membro> {
  return requisitar<Membro>(`/membros/${memCod}`, { method: 'PUT', body: JSON.stringify(corpoDaPessoa(dados)) })
}

export function excluirMembro(memCod: number): Promise<{ mensagem: string }> {
  return requisitar(`/membros/${memCod}`, { method: 'DELETE' })
}

// Promoções: só presidente (ou admin) pode. Chefe e presidente exigem que a pessoa já seja escoteira.
export function promoverAChefe(memCod: number, dados: { tropa: { cod: number }; gestao_ini: string }): Promise<Chefe> {
  return requisitar<Chefe>(`/escoteiros/${memCod}/chefe`, { method: 'POST', body: JSON.stringify(dados) })
}

export function promoverAPresidente(memCod: number, dados: { gestao_ini: string; gestao_fim: string }): Promise<Presidente> {
  return requisitar<Presidente>(`/escoteiros/${memCod}/presidente`, { method: 'POST', body: JSON.stringify(dados) })
}

export function promoverAFinanceiro(memCod: number): Promise<Financeiro> {
  return requisitar<Financeiro>(`/membros/${memCod}/financeiro`, { method: 'POST' })
}

export function encerrarChefe(memCod: number): Promise<{ mensagem: string }> {
  return requisitar(`/chefes/${memCod}`, { method: 'DELETE' })
}

export function removerFinanceiro(memCod: number): Promise<{ mensagem: string }> {
  return requisitar(`/financeiros/${memCod}`, { method: 'DELETE' })
}
