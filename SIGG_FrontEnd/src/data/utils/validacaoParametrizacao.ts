export interface DadosFormParametrizacao {
  sigla: string
  nomeSistema: string
  slogan: string
  nomeGrupo: string
  endereco: string
  cidade: string
  estado: string
  telefone: string
  email: string
}

export type ErrosParametrizacao = Partial<Record<keyof DadosFormParametrizacao | 'logo', string>>

// Mesmas regras do Parametrizacao.validar() no backend, campo a campo
export function validarParametrizacao(dados: DadosFormParametrizacao): ErrosParametrizacao {
  const erros: ErrosParametrizacao = {}
  const obrigatorio = (campo: 'sigla' | 'nomeSistema' | 'nomeGrupo', rotulo: string, maximo: number) => {
    const texto = dados[campo].trim()
    if (!texto) erros[campo] = `Informe ${rotulo}.`
    else if (texto.length > maximo) erros[campo] = `Máximo de ${maximo} caracteres.`
  }
  const opcional = (campo: 'slogan' | 'endereco' | 'cidade', maximo: number) => {
    if (dados[campo].trim().length > maximo) erros[campo] = `Máximo de ${maximo} caracteres.`
  }

  obrigatorio('sigla', 'a sigla do sistema', 20)
  obrigatorio('nomeSistema', 'o nome do sistema', 80)
  obrigatorio('nomeGrupo', 'o nome do grupo', 80)
  opcional('slogan', 50)
  opcional('endereco', 150)
  opcional('cidade', 50)

  const estado = dados.estado.trim()
  if (estado && !/^[A-Za-z]{2}$/.test(estado)) erros.estado = 'Use a sigla com 2 letras (ex.: SP).'

  const telefone = dados.telefone.replace(/\D/g, '')
  if (dados.telefone.trim() && !/^\d{10,11}$/.test(telefone))
    erros.telefone = 'Informe DDD + número (10 ou 11 dígitos).'

  const email = dados.email.trim()
  if (email && (email.length > 60 || !/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(email)))
    erros.email = 'Informe um e-mail válido (até 60 caracteres).'

  return erros
}

// Liga a mensagem do backend ao campo correspondente (caso a validação do front deixe passar algo)
export function errosDoBackendParametrizacao(mensagem: string): ErrosParametrizacao | null {
  const mapa: [string, keyof ErrosParametrizacao][] = [
    ['sigla', 'sigla'],
    ['nome do sistema', 'nomeSistema'],
    ['nome do grupo', 'nomeGrupo'],
    ['slogan', 'slogan'],
    ['endereço', 'endereco'],
    ['cidade', 'cidade'],
    ['estado', 'estado'],
    ['telefone', 'telefone'],
    ['E-mail', 'email'],
    ['logo', 'logo'],
  ]
  const achado = mapa.find(([trecho]) => mensagem.toLowerCase().includes(trecho.toLowerCase()))
  return achado ? { [achado[1]]: mensagem } : null
}
