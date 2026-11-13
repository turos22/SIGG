// Formato de GET/PUT /parametrizacao (ParametrizacaoController)
export default interface Parametrizacao {
  sigla: string
  nomeSistema: string
  slogan: string | null
  nomeGrupo: string
  endereco: string | null
  cidade: string | null
  estado: string | null
  telefone: string | null
  email: string | null
  logo: string | null // data URL (data:image/png;base64,...) ou null para usar o logo padrão
}
