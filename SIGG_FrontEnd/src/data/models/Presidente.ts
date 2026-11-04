import type Escoteiro from '@/data/models/Escoteiro'

// Espelha models/Presidente.java (Presidente extends Escoteiro)
export default interface Presidente extends Escoteiro {
  pres_cod: number
  gestao_ini: string // "2024-02-01"
  gestao_fim: string | null
}
