import { requisitar } from '@/data/services/api'
import type Escoteiro from '@/data/models/Escoteiro'
import type { FormaPagamento } from '@/data/models/PagamentoMensalidade'
import type {
    AtividadesFinanceiroEscoteiro,
    ReciboPagamentoAtividade,
} from '@/data/models/PagamentoAtividade'

export function buscarFormasPagamentoAtividade(): Promise<FormaPagamento[]> {
    return requisitar<FormaPagamento[]>('/pagamentos-atividade/formas-pagamento')
}

export function pesquisarEscoteirosAtividade(busca: string): Promise<Escoteiro[]> {
    return requisitar<Escoteiro[]>(`/pagamentos-atividade/escoteiros?busca=${encodeURIComponent(busca)}`)
}

export function consultarAtividadesEscoteiro(escCod: number): Promise<AtividadesFinanceiroEscoteiro> {
    return requisitar<AtividadesFinanceiroEscoteiro>(`/pagamentos-atividade/escoteiros/${escCod}`)
}

export function registrarPagamentoAtividade(
    crCod: number,
    dados: {
        valorRecebido: number
        formaPagamentoCod: number
        dataPagamento: string
        confirmarDuplicado?: boolean
    }
): Promise<{ mensagem: string; recibo: ReciboPagamentoAtividade }> {
    return requisitar(`/pagamentos-atividade/${crCod}/registrar`, {
        method: 'POST',
        body: JSON.stringify(dados),
    })
}

export function estornarPagamentoAtividade(crCod: number): Promise<{ mensagem: string }> {
    return requisitar(`/pagamentos-atividade/${crCod}/estornar`, {
        method: 'DELETE',
    })
}