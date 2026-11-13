export default interface Membro {
  mem_cod: number
  nome: string
  dt_nascto: string 
  CPF: string
  RG: string
  CEP: string
  endereco: string
  endereco_num: number
  endereco_bairro: string
  cidade: string
  estado: string
  telefone: string | null
  email: string | null
  dt_ini: string
  dt_fim: string | null
  papeis: string[] // só vem preenchido em GET /membros: Presidente, Chefe, Financeiro, Escoteiro, Responsável, Voluntário
}
