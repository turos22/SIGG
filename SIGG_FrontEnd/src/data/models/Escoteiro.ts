// Formato que o backend devolve em GET /escoteiros (EscoteiroController)
export default interface Escoteiro {
  id: number
  nome: string
  dataNascimento: string // "AAAA-MM-DD"
  patrulha: string
  inadimplente: boolean
}
