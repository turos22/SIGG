import type Tropa from '@/data/models/Tropa'

// P presente, F falta, J falta justificada; null = ainda não marcado nessa data
export type StatusPresenca = 'P' | 'F' | 'J'

// Espelha models/ItemChamada.java
export interface ItemChamada {
  esc_cod: number
  mem_cod: number
  nome: string
  patrulha: string
  status: StatusPresenca | null
  percentual_ano: number // 0 a 100: presenças sobre os registros do escoteiro no ano
}

// Espelha models/Chamada.java: os escoteiros atuais da tropa com o status gravado na data
export interface Chamada {
  tropa: Tropa
  data: string
  itens: ItemChamada[]
}

// Espelha models/ResumoChamada.java: uma linha da grade de chamadas já feitas
export interface ResumoChamada {
  tropa: Tropa
  data: string
  presentes: number
  total: number
}
