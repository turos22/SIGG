// Campos que o formulário de pessoa edita (sem código, datas de entrada/saída e papéis)
export interface DadosPessoa {
  nome: string
  dt_nascto: string
  CPF: string
  RG: string
  CEP: string
  endereco: string
  endereco_num: number | null
  endereco_bairro: string
  cidade: string
  estado: string
  telefone: string
  email: string
}
