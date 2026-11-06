import type { Perfil } from '@/data/models/Perfil'

export default interface Usuario {
  mem_cod: number
  nome: string
  perfis: Perfil[]
}
