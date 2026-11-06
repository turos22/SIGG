import type DadosNovaConta from '@/data/models/DadosNovaConta'
import type MensagemApi from '@/data/models/MensagemApi'
import type Usuario from '@/data/models/Usuario'
import { requisitar } from '@/data/services/api'
import { definirTokenCsrf } from '@/data/services/sessao'

function enviar<T>(caminho: string, corpo?: unknown): Promise<T> {
  return requisitar<T>(caminho, {
    method: 'POST',
    body: corpo === undefined ? undefined : JSON.stringify(corpo),
  })
}

export async function entrarNoSistema(cpf: string, senha: string): Promise<Usuario> {
  const usuario = await enviar<Usuario>('/auth/login', { cpf, senha })
  definirTokenCsrf(null)
  return usuario
}

export function buscarUsuarioAtual(): Promise<Usuario> {
  return requisitar<Usuario>('/auth/me')
}

export async function encerrarSessao(): Promise<MensagemApi> {
  try {
    return await enviar<MensagemApi>('/auth/logout')
  } finally {
    definirTokenCsrf(null)
  }
}

export function criarConta(dados: DadosNovaConta): Promise<MensagemApi> {
  return enviar<MensagemApi>('/auth/registro', dados)
}

export function confirmarConta(token: string): Promise<MensagemApi> {
  return enviar<MensagemApi>('/auth/confirmar-conta', { token })
}

export function solicitarRecuperacaoDeSenha(cpf: string, email: string): Promise<MensagemApi> {
  return enviar<MensagemApi>('/auth/esqueci-senha', { cpf, email })
}

export function redefinirSenha(token: string, senha: string): Promise<MensagemApi> {
  return enviar<MensagemApi>('/auth/redefinir-senha', { token, senha })
}
