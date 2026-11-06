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
