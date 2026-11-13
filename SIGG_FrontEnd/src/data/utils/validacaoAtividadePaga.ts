import { lerMoeda } from '@/data/utils/validadores'

export interface DadosFormAtividadePaga {
  nome: string
  descricao: string
  data: string
  prazoInteresse: string
  local: string
  valor: string
  vagas: number | null
}

export type CampoAtividadePaga = keyof DadosFormAtividadePaga
export type ErrosAtividadePaga = Partial<Record<CampoAtividadePaga, string>>

// Mesmas regras do AtividadePaga.validar() / AtividadePagaFacade.ofertar() no backend, campo a campo
export function validarAtividadePaga(dados: DadosFormAtividadePaga, hoje: string): ErrosAtividadePaga {
  const erros: ErrosAtividadePaga = {}
  const nome = dados.nome.trim()
  const local = dados.local.trim()
  const valor = lerMoeda(dados.valor)

  if (!nome) erros.nome = 'Informe o nome da atividade.'
  else if (nome.length > 30) erros.nome = 'O nome deve ter até 30 caracteres.'

  if (dados.descricao.trim().length > 200) erros.descricao = 'A descrição deve ter até 200 caracteres.'

  if (!dados.data) erros.data = 'Informe a data de realização.'
  else if (dados.data < hoje) erros.data = 'A data de realização não pode estar no passado.'

  if (!dados.prazoInteresse) erros.prazoInteresse = 'Informe o prazo para manifestação de interesse.'
  else if (dados.prazoInteresse < hoje) erros.prazoInteresse = 'O prazo não pode estar no passado.'
  else if (dados.data && dados.prazoInteresse > dados.data)
    erros.prazoInteresse = 'O prazo não pode ser depois da data da atividade.'

  if (!local) erros.local = 'Informe o local da atividade.'
  else if (local.length > 100) erros.local = 'O local deve ter até 100 caracteres.'

  if (dados.valor.trim() === '') erros.valor = 'Informe o valor da atividade.'
  else if (valor === null || valor <= 0) erros.valor = 'O valor deve ser maior que zero.'

  if (dados.vagas === null) erros.vagas = 'Informe o número de vagas.'
  else if (dados.vagas <= 0) erros.vagas = 'O número de vagas deve ser maior que zero.'

  return erros
}

// Erros que só o backend detecta, ligados ao(s) campo(s) a que se referem
export function errosDoBackendAtividadePaga(mensagem: string): ErrosAtividadePaga | null {
  if (mensagem.startsWith('Já existe uma atividade paga'))
    return { nome: mensagem, data: 'Já existe uma atividade com este nome nesta data.' }
  return null
}
