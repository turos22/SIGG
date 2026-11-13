import type Tropa from '@/data/models/Tropa'
import { requisitar } from '@/data/services/api'

export function listarTropas(): Promise<Tropa[]> {
  return requisitar<Tropa[]>('/tropas')
}
