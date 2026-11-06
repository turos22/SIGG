import type Escoteiro from '@/data/models/Escoteiro'
import type ResultadoLancamento from '@/data/models/ResultadoLancamento'
import type SaldoMensalidades from '@/data/models/SaldoMensalidades'
import { requisitar } from '@/data/services/api'

export function pesquisarEscoteirosParaMensalidade(busca: string): Promise<Escoteiro[]> {
  return requisitar<Escoteiro[]>(`/mensalidades/escoteiros?busca=${encodeURIComponent(busca)}`)
}

export function buscarSaldoDoEscoteiro(memCod: number): Promise<SaldoMensalidades> {
  return requisitar<SaldoMensalidades>(`/mensalidades/escoteiros/${memCod}/saldo`)
}

export function buscarSaldoDoResponsavel(memCod: number): Promise<SaldoMensalidades[]> {
  return requisitar<SaldoMensalidades[]>(`/mensalidades/responsaveis/${memCod}/saldo`)
}

export function lancarMensalidadesDoMes(mes: number, ano: number): Promise<ResultadoLancamento> {
  return requisitar<ResultadoLancamento>(`/mensalidades/lancamentos?mes=${mes}&ano=${ano}`, { method: 'POST' })
}
