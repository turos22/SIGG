import type Escoteiro from '@/data/models/Escoteiro'
import type Tropa from '@/data/models/Tropa'

// Espelha models/Chefe.java (Chefe extends Escoteiro: os campos do escoteiro vêm no mesmo objeto)
export default interface Chefe extends Escoteiro {
  chefe_cod: number
  gestao_ini: string // "2024-02-01"
  gestao_fim: string | null
  tropa: Tropa
}
