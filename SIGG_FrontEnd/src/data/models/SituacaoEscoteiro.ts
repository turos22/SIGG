export type SituacaoFinanceira = 'EM_DIA' | 'PENDENTE' | 'INADIMPLENTE'

// Espelha models/SituacaoEscoteiro.java: em dia e pendente são calculados; inadimplente só o financeiro marca
export interface SituacaoEscoteiro {
  esc_cod: number
  mem_cod: number
  nome: string
  cpf: string
  tropa: string
  patrulha: string
  mensalidades_abertas: number
  situacao: SituacaoFinanceira
}
