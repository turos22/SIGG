import type ResumoEscoteiro from '@/data/models/ResumoEscoteiro'

// Dados de exemplo para o painel inicial, até o backend expor frequência e arrecadação
export const escoteirosExemplo: ResumoEscoteiro[] = [
  { cod: 1, nome: 'Lucas Andrade', patrulha: 'Lobos', dt_nascto: '2014-03-12', inadimplente: false },
  { cod: 2, nome: 'Marina Souza', patrulha: 'Águias', dt_nascto: '2013-07-02', inadimplente: false },
  { cod: 3, nome: 'Pedro Henrique Lima', patrulha: 'Lobos', dt_nascto: '2012-11-25', inadimplente: true },
  { cod: 4, nome: 'Júlia Ferreira', patrulha: 'Onças', dt_nascto: '2014-01-30', inadimplente: false },
  { cod: 5, nome: 'Rafael Gomes', patrulha: 'Águias', dt_nascto: '2011-09-18', inadimplente: true },
  { cod: 6, nome: 'Beatriz Martins', patrulha: 'Tucanos', dt_nascto: '2013-05-09', inadimplente: false },
  { cod: 7, nome: 'Gabriel Rocha', patrulha: 'Onças', dt_nascto: '2012-02-14', inadimplente: false },
  { cod: 8, nome: 'Sofia Carvalho', patrulha: 'Tucanos', dt_nascto: '2014-08-21', inadimplente: false },
  { cod: 9, nome: 'Davi Nunes', patrulha: 'Lobos', dt_nascto: '2011-12-03', inadimplente: false },
  { cod: 10, nome: 'Helena Barros', patrulha: 'Águias', dt_nascto: '2013-10-27', inadimplente: true },
]

export const frequenciaExemplo = [
  { rotulo: 'Jun', valor: 78 },
  { rotulo: 'Jul', valor: 64 },
  { rotulo: 'Ago', valor: 82 },
  { rotulo: 'Set', valor: 86 },
  { rotulo: 'Out', valor: 79 },
  { rotulo: 'Nov', valor: 91 },
]
