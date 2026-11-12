// Formato de GET/POST /atividades-pagas (AtividadePagaController) — RF_F5
export default interface AtividadePaga {
  codigo: number
  nome: string
  descricao: string | null
  data: string // "AAAA-MM-DD"
  local: string | null // atividades antigas podem não ter
  valor: number
  vagas: number | null
  prazoInteresse: string | null // "AAAA-MM-DD"
}
