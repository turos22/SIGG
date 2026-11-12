export type SituacaoPagamentoAtividade = 'ABERTA' | 'PARCIAL' | 'PAGA'

export interface PagamentoAtividade {
    id: number
    crCod: number
    atvCod: number
    atividadeNome: string
    atividadeDesc: string | null
    atividadeData: string | null
    escCod: number
    escoteiroNome: string
    escoteiroCpf: string
    dataVencimento: string | null
    dataPagamento: string | null
    valorAtividade: number
    valorPago: number
    saldo: number
    formaPagamentoCod: number
    formaPagamentoDesc: string | null
    situacao: SituacaoPagamentoAtividade
    inadimplente: boolean
    observacao: string | null
}

export interface AtividadesFinanceiroEscoteiro {
    esc_cod: number
    mem_cod: number
    nome: string
    cpf: string
    patrulha: string
    inadimplente: boolean
    atividades: PagamentoAtividade[]
}

export interface ReciboPagamentoAtividade {
    numero: string
    escoteiro: string
    cpf: string
    matricula: number
    atividade: string
    dataAtividade: string
    valorAtividade: number
    valorPago: number
    observacao: string
    dataPagamento: string
}