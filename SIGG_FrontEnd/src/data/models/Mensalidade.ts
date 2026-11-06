export type SituacaoMensalidade = 'PAGA' | 'ATRASADA' | 'PARCIAL' | 'ABERTA'

export default interface Mensalidade {
  mens_cod: number
  mens_mes: number
  mens_ano: number
  mens_dt_lancto: string
  mens_valor: number
  esc_cod: number
  cat_mensalidade: number
  cr_cod: number
  dt_vencimento: string | null
  valor_pago: number
  apadrinhada: boolean
  saldo: number
  situacao: SituacaoMensalidade
}
