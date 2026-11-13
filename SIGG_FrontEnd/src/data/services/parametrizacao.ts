import { requisitar } from '@/data/services/api'
import type Parametrizacao from '@/data/models/Parametrizacao'

export function buscarParametrizacao(): Promise<Parametrizacao> {
  return requisitar<Parametrizacao>('/parametrizacao')
}

export function alterarParametrizacao(parametrizacao: Parametrizacao): Promise<Parametrizacao> {
  return requisitar<Parametrizacao>('/parametrizacao', {
    method: 'PUT',
    body: JSON.stringify(parametrizacao),
  })
}
