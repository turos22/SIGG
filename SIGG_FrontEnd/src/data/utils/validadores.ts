export function somenteDigitos(valor: string): string {
  return valor.replace(/\D/g, '')
}

export function cpfValido(cpf: string): boolean {
  const numeros = somenteDigitos(cpf)
  if (numeros.length !== 11 || /^(\d)\1{10}$/.test(numeros)) return false
  const digito = (base: string): number => {
    let soma = 0
    for (let i = 0; i < base.length; i++) soma += Number(base[i]) * (base.length + 1 - i)
    const resto = (soma * 10) % 11
    return resto === 10 ? 0 : resto
  }
  return digito(numeros.slice(0, 9)) === Number(numeros[9]) && digito(numeros.slice(0, 10)) === Number(numeros[10])
}

export function emailValido(email: string): boolean {
  return email.length <= 40 && /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)
}

export function erroDeSenha(senha: string): string | undefined {
  if (senha.length < 8) return 'Mínimo de 8 caracteres'
  if (senha.length > 72) return 'Máximo de 72 caracteres'
  if (!/[A-Za-z]/.test(senha)) return 'Inclua ao menos uma letra'
  if (!/\d/.test(senha)) return 'Inclua ao menos um número'
  return undefined
}

export function dataDeNascimentoValida(data: string, hoje: string): boolean {
  return /^\d{4}-\d{2}-\d{2}$/.test(data) && data >= '1900-01-01' && data <= hoje
}

export function gerarSenhaTemporaria(): string {
  const letras = 'ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz'
  const numeros = '23456789'
  const simbolos = '@#$%&*!?'
  const todos = letras + numeros + simbolos
  const sortear = (alfabeto: string): string => {
    const limite = Math.floor(256 / alfabeto.length) * alfabeto.length
    const buffer = new Uint8Array(1)
    do {
      crypto.getRandomValues(buffer)
    } while (buffer[0] >= limite)
    return alfabeto[buffer[0] % alfabeto.length]
  }
  const caracteres = [sortear(letras), sortear(numeros), sortear(simbolos)]
  while (caracteres.length < 14) caracteres.push(sortear(todos))
  for (let i = caracteres.length - 1; i > 0; i--) {
    const buffer = new Uint8Array(1)
    const limite = Math.floor(256 / (i + 1)) * (i + 1)
    do {
      crypto.getRandomValues(buffer)
    } while (buffer[0] >= limite)
    const j = buffer[0] % (i + 1)
    ;[caracteres[i], caracteres[j]] = [caracteres[j], caracteres[i]]
  }
  return caracteres.join('')
}

// "150,50" → 150.5; texto vazio ou inválido → null
export function lerMoeda(texto: string): number | null {
  if (texto.trim() === '') return null
  const numero = Number(texto.replace(',', '.'))
  return Number.isFinite(numero) ? numero : null
}
