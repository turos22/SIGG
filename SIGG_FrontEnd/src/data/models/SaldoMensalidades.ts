import type Mensalidade from '@/data/models/Mensalidade'
import type SaldoAdotado from '@/data/models/SaldoAdotado'

export default interface SaldoMensalidades {
  mem_cod: number
  esc_cod: number
  nome: string
  cpf: string
  anjo: boolean
  adotado: boolean
  nome_anjo: string | null
  isento: boolean
  observacao: string | null
  abertas: Mensalidade[]
  pagas: Mensalidade[]
  adotados: SaldoAdotado[]
  total_proprio: number
  total_adotados: number
  total_a_pagar: number
}
