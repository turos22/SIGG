export { somenteDigitos } from '@/data/utils/validadores'

// Filtros de digitação: tiram do texto (digitado ou colado) o que o campo não aceita
export function semDigitos(valor: string): string {
  return valor.replace(/\d/g, '')
}

// RG: dígitos e a letra X (maiúscula)
export function filtroRg(valor: string): string {
  return valor.toUpperCase().replace(/[^0-9X]/g, '')
}

export const UFS: readonly string[] = [
  'AC', 'AL', 'AP', 'AM', 'BA', 'CE', 'DF', 'ES', 'GO', 'MA', 'MT', 'MS', 'MG', 'PA',
  'PB', 'PR', 'PE', 'PI', 'RJ', 'RN', 'RS', 'RO', 'RR', 'SC', 'SP', 'SE', 'TO',
]
