import { requisitar } from '@/data/services/api'
import type CategoriaMensalidade from '@/data/models/CategoriaMensalidade'

export function buscarCategoriasMensalidade(filtro?: string): Promise<CategoriaMensalidade[]> {
    const query = filtro && filtro.trim() !== '' ? `?filtro=${encodeURIComponent(filtro.trim())}` : ''
    return requisitar<CategoriaMensalidade[]>(`/categorias-mensalidade${query}`)
}

export function cadastrarCategoriaMensalidade(
    categoria: Omit<CategoriaMensalidade, 'cat_mens_cod'>
): Promise<unknown> {
    return requisitar('/categorias-mensalidade', {
        method: 'POST',
        body: JSON.stringify({
            cat_mens_cod: 0,
            ...categoria,
        }),
    })
}

export function alterarCategoriaMensalidade(
    id: number,
    categoria: CategoriaMensalidade
): Promise<unknown> {
    return requisitar(`/categorias-mensalidade/${id}`, {
        method: 'PUT',
        body: JSON.stringify(categoria),
    })
}

export function apagarCategoriaMensalidade(id: number): Promise<unknown> {
    return requisitar(`/categorias-mensalidade/${id}`, {
        method: 'DELETE',
    })
}