import { requisitar } from '@/data/services/api'
import type TipoAtividade from '@/data/models/TipoAtividade'

export function buscarTiposAtividade(filtro?: string): Promise<TipoAtividade[]> {
    const query = filtro ? `?filtro=${encodeURIComponent(filtro)}` : ''
    return requisitar<TipoAtividade[]>(`/tipos-atividade${query}`)
}

export function cadastrarTipoAtividade(tipo: Omit<TipoAtividade, 'ta_cod'>): Promise<unknown> {
    return requisitar('/tipos-atividade', {
        method: 'POST',
        body: JSON.stringify(tipo),
    })
}

export function alterarTipoAtividade(id: number, tipo: TipoAtividade): Promise<unknown> {
    return requisitar(`/tipos-atividade/${id}`, {
        method: 'PUT',
        body: JSON.stringify(tipo),
    })
}

export function apagarTipoAtividade(id: number): Promise<unknown> {
    return requisitar(`/tipos-atividade/${id}`, {
        method: 'DELETE',
    })
}