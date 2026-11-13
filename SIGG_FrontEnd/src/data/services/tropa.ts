import type Patrulha from '@/data/models/Patrulha'
import type Tropa from '@/data/models/Tropa'
import { requisitar } from '@/data/services/api'

export function listarTropas(): Promise<Tropa[]> {
  return requisitar<Tropa[]>('/tropas')
}

export function listarPatrulhas(tropa?: number): Promise<Patrulha[]> {
  return requisitar<Patrulha[]>(tropa ? `/patrulhas?tropa=${tropa}` : '/patrulhas')
}
