import {
  AUTENTICACAO_ATIVA,
  definirTokenCsrf,
  lerTokenCsrf,
  notificarSessaoExpirada,
} from '@/data/services/sessao'

const URL_BASE = import.meta.env.VITE_API_URL ?? 'http://localhost:8080'
const METODOS_SEGUROS = ['GET', 'HEAD', 'OPTIONS']

export class ErroDeApi extends Error {
  readonly status: number

  constructor(status: number, mensagem: string) {
    super(mensagem)
    this.name = 'ErroDeApi'
    this.status = status
  }
}

async function obterTokenCsrf(): Promise<string> {
  const atual = lerTokenCsrf()
  if (atual) return atual
  const resposta = await fetch(`${URL_BASE}/auth/csrf`, { credentials: 'include' })
  if (!resposta.ok) throw new ErroDeApi(resposta.status, await resposta.text())
  const corpo = (await resposta.json()) as { token: string }
  definirTokenCsrf(corpo.token)
  return corpo.token
}

export async function requisitar<T>(caminho: string, opcoes?: RequestInit): Promise<T> {
  const metodo = (opcoes?.method ?? 'GET').toUpperCase()
  const precisaDeCsrf = AUTENTICACAO_ATIVA && !METODOS_SEGUROS.includes(metodo)
  const resposta = await fetch(`${URL_BASE}${caminho}`, {
    credentials: 'include',
    ...opcoes,
    headers: {
      'Content-Type': 'application/json',
      ...(precisaDeCsrf ? { 'X-CSRF-TOKEN': await obterTokenCsrf() } : {}),
      ...opcoes?.headers,
    },
  })
  if (!resposta.ok) {
    if (resposta.status === 401 && !caminho.startsWith('/auth/')) notificarSessaoExpirada()
    if (resposta.status === 403) definirTokenCsrf(null)
    throw new ErroDeApi(resposta.status, await resposta.text())
  }
  return resposta.json() as Promise<T>
}
