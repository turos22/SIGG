import { requisitar } from '@/data/services/api'
import type Escoteiro from '@/data/models/Escoteiro'
import type { SituacaoEscoteiro } from '@/data/models/SituacaoEscoteiro'
import type {
    FormaPagamento,
    ReciboPagamentoMensalidade,
    SituacaoFinanceiraEscoteiro,
} from '@/data/models/PagamentoMensalidade'

export function buscarFormasPagamento(): Promise<FormaPagamento[]> {
    return requisitar<FormaPagamento[]>('/pagamentos-mensalidade/formas-pagamento')
}

export function pesquisarEscoteirosPagamento(busca: string): Promise<Escoteiro[]> {
    return requisitar<Escoteiro[]>(`/pagamentos-mensalidade/escoteiros?busca=${encodeURIComponent(busca)}`)
}

export function consultarSituacaoFinanceiraEscoteiro(escCod: number): Promise<SituacaoFinanceiraEscoteiro> {
    return requisitar<SituacaoFinanceiraEscoteiro>(`/pagamentos-mensalidade/escoteiros/${escCod}`)
}

export function registrarPagamentoMensalidade(
    crCod: number,
    dados: { valorRecebido: number; formaPagamentoCod: number; dataPagamento: string }
): Promise<{ mensagem: string; recibo: ReciboPagamentoMensalidade }> {
    return requisitar(`/pagamentos-mensalidade/${crCod}/registrar`, {
        method: 'POST',
        body: JSON.stringify(dados),
    })
}

export function estornarPagamentoMensalidade(crCod: number): Promise<{ mensagem: string }> {
    return requisitar(`/pagamentos-mensalidade/${crCod}/estornar`, {
        method: 'DELETE',
    })
}

export function listarSituacoes(tropa?: number): Promise<SituacaoEscoteiro[]> {
    return requisitar<SituacaoEscoteiro[]>(tropa ? `/pagamentos-mensalidade/situacoes?tropa=${tropa}` : '/pagamentos-mensalidade/situacoes')
}

export function marcarInadimplente(escCod: number): Promise<{ mensagem: string }> {
    return requisitar(`/pagamentos-mensalidade/escoteiros/${escCod}/inadimplente`, { method: 'POST' })
}
