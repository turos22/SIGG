import type Membro from '@/data/models/Membro'

// Espelha models/Responsavel.java
export default interface Responsavel {
  membro: Membro
  resp_cod: number
}
