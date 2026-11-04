import type Membro from '@/data/models/Membro'

// Espelha models/Financeiro.java
export default interface Financeiro {
  membro: Membro
  fin_cod: number
}
