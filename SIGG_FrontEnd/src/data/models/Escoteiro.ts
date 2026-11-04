import type Membro from '@/data/models/Membro'
import type Patrulha from '@/data/models/Patrulha'
import type Responsavel from '@/data/models/Responsavel'

// Espelha models/Escoteiro.java. "responsaveis" só vem preenchido no detalhe (GET /escoteiros/{id});
// nas listagens chega como lista vazia.
export default interface Escoteiro {
  membro: Membro
  esc_cod: number
  cat_mensalidade: number
  anjo: number // código do escoteiro "adotado"; 0 quando não há
  patrulha: Patrulha
  esc_sit_fin: number // 1 = inadimplente
  responsaveis: Responsavel[]
}
