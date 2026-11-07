import type ContaAcesso from '@/data/models/ContaAcesso'
import type MensagemApi from '@/data/models/MensagemApi'
import { requisitar } from '@/data/services/api'

export function buscarContas(): Promise<ContaAcesso[]> {
  return requisitar<ContaAcesso[]>('/contas')
}

export function redefinirSenhaDaConta(memCod: number, senha: string): Promise<MensagemApi> {
  return requisitar<MensagemApi>(`/contas/${memCod}/senha`, { method: 'PUT', body: JSON.stringify({ senha }) })
}

export function alterarSituacaoDaConta(memCod: number, ativa: boolean): Promise<MensagemApi> {
  return requisitar<MensagemApi>(`/contas/${memCod}/situacao`, { method: 'PUT', body: JSON.stringify({ ativa }) })
}

export function alterarAdministradorDaConta(memCod: number, admin: boolean): Promise<MensagemApi> {
  return requisitar<MensagemApi>(`/contas/${memCod}/admin`, { method: 'PUT', body: JSON.stringify({ admin }) })
}
