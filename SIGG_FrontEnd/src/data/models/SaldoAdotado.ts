import type Mensalidade from '@/data/models/Mensalidade'

export default interface SaldoAdotado {
  mem_cod: number
  esc_cod: number
  nome: string
  abertas: Mensalidade[]
  subtotal: number
}
