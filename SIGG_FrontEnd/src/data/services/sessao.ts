export const AUTENTICACAO_ATIVA = import.meta.env.VITE_AUTH_ATIVA !== 'false'

let tokenCsrf: string | null = null
let aoSessaoExpirar: (() => void) | null = null

export function lerTokenCsrf(): string | null {
  return tokenCsrf
}

export function definirTokenCsrf(token: string | null): void {
  tokenCsrf = token
}

export function registrarAoSessaoExpirar(acao: (() => void) | null): void {
  aoSessaoExpirar = acao
}

export function notificarSessaoExpirada(): void {
  aoSessaoExpirar?.()
}
