import type Escoteiro from '@/data/models/Escoteiro'
import type SaudeApi from '@/data/models/SaudeApi'

const URL_BASE = import.meta.env.VITE_API_URL ?? 'http://localhost:8080'

export class ErroDeApi extends Error {
  readonly status: number

  constructor(status: number, mensagem: string) {
    super(mensagem)
    this.name = 'ErroDeApi'
    this.status = status
  }
}

// Toda chamada ao backend passa por aqui: monta a URL, envia JSON e trata resposta de erro
async function requisitar<T>(caminho: string, opcoes?: RequestInit): Promise<T> {
  const resposta = await fetch(`${URL_BASE}${caminho}`, {
    ...opcoes,
    headers: { 'Content-Type': 'application/json', ...opcoes?.headers },
  })
  if (!resposta.ok)
    throw new ErroDeApi(resposta.status, await resposta.text())
  return resposta.json() as Promise<T>
}

export function buscarSaudeDoBanco(): Promise<SaudeApi> {
  return requisitar<SaudeApi>('/health/db')
}

export function buscarEscoteiros(): Promise<Escoteiro[]> {
  return requisitar<Escoteiro[]>('/escoteiros')
}
