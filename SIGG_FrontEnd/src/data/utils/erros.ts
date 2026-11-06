import { ErroDeApi } from '@/data/services/api'

export function mensagemDeErro(falha: unknown, padrao: string): string {
  if (!(falha instanceof ErroDeApi))
    return `${padrao} O backend está rodando em localhost:8080?`
  try {
    const corpo = JSON.parse(falha.message) as { erro?: string }
    if (corpo.erro)
      return corpo.erro
  } catch {
    return `${padrao} (o backend respondeu ${falha.status}).`
  }
  return `${padrao} (o backend respondeu ${falha.status}).`
}
