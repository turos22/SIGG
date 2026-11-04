import type Escoteiro from '@/data/models/Escoteiro'
import type Tropa from '@/data/models/Tropa'

// Espelha models/Patrulha.java. Nas listagens de escoteiros o backend preenche só cod e nome;
// lider, sublider e tropa chegam como null.
export default interface Patrulha {
  cod: number
  nome: string
  lider: Escoteiro | null
  sublider: Escoteiro | null
  tropa: Tropa | null
}
