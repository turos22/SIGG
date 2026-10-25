// "2016-05-20" (como vem do backend) → "20/05/2016"
export function formatarData(dataIso: string): string {
  const [ano, mes, dia] = dataIso.split('-')
  return `${dia}/${mes}/${ano}`
}
