// "18999998888" → "(18) 99999-8888"; "1833334444" → "(18) 3333-4444"; outros formatos voltam como vieram
export function formatarTelefone(telefone: string): string {
  const numeros = telefone.replace(/\D/g, '')
  if (numeros.length === 11) return `(${numeros.slice(0, 2)}) ${numeros.slice(2, 7)}-${numeros.slice(7)}`
  if (numeros.length === 10) return `(${numeros.slice(0, 2)}) ${numeros.slice(2, 6)}-${numeros.slice(6)}`
  return telefone
}

export function formatarMoeda(valor: number): string {
  return valor.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' })
}

export function formatarCpf(cpf: string): string {
  const numeros = cpf.replace(/\D/g, '')
  if (numeros.length !== 11) return cpf
  return `${numeros.slice(0, 3)}.${numeros.slice(3, 6)}.${numeros.slice(6, 9)}-${numeros.slice(9)}`
}

export function formatarReferencia(mes: number, ano: number): string {
  return `${String(mes).padStart(2, '0')}/${ano}`
}

export function formatarCpfParcial(digitos: string): string {
  const numeros = digitos.replace(/\D/g, '').slice(0, 11)
  const partes = [numeros.slice(0, 3), numeros.slice(3, 6), numeros.slice(6, 9)].filter(Boolean)
  const resto = numeros.slice(9)
  return partes.join('.') + (resto ? `-${resto}` : '')
}
