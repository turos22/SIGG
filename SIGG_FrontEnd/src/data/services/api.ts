const URL_BASE = import.meta.env.VITE_API_URL ?? 'http://localhost:8080'

export class ErroDeApi extends Error {
  readonly status: number

  constructor(status: number, mensagem: string) {
    super(mensagem)
    this.name = 'ErroDeApi'
    this.status = status
  }
}

export async function requisitar<T>(caminho: string, opcoes?: RequestInit): Promise<T> {
  const resposta = await fetch(`${URL_BASE}${caminho}`, {
    ...opcoes,
    headers: { 'Content-Type': 'application/json', ...opcoes?.headers },
  })
  if (!resposta.ok)
    throw new ErroDeApi(resposta.status, await resposta.text())
  return resposta.json() as Promise<T>
}