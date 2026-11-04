import { useState, type KeyboardEvent, type ReactNode } from 'react'
import LinearProgress from '@mui/material/LinearProgress'
import Stack from '@mui/material/Stack'
import Table from '@mui/material/Table'
import TableBody from '@mui/material/TableBody'
import TableCell from '@mui/material/TableCell'
import TableContainer from '@mui/material/TableContainer'
import TableHead from '@mui/material/TableHead'
import TablePagination from '@mui/material/TablePagination'
import TableRow from '@mui/material/TableRow'
import TableSortLabel from '@mui/material/TableSortLabel'

type Valor = string | number | null | undefined

export interface ColunaGrade<T> {
  id: string
  titulo: string
  valor?: (linha: T) => Valor
  renderizar?: (linha: T) => ReactNode 
  alinhamento?: 'left' | 'center' | 'right'
  largura?: number | string
  ordenavel?: boolean 
}

interface GradeProps<T> {
  colunas: ColunaGrade<T>[]
  linhas: T[]
  chave: (linha: T) => string | number
  titulo?: string
  subtitulo?: string
  acoes?: (linha: T) => ReactNode 
  aoClicarLinha?: (linha: T) => void 
  aoDuploClique?: (linha: T) => void 
  chaveSelecionada?: string | number | null
  rodape?: ReactNode 
  textoVazio?: string
  linhasPorPagina?: number
  carregando?: boolean
}

type Ordem = { coluna: string; direcao: 'asc' | 'desc' }

function comparar(a: Valor, b: Valor): number {
  if (a == null && b == null) return 0
  if (a == null) return 1
  if (b == null) return -1
  if (typeof a === 'number' && typeof b === 'number') return a - b
  return String(a).localeCompare(String(b), 'pt-BR', { numeric: true, sensitivity: 'base' })
}

export default function Grade<T>({
  colunas,
  linhas,
  chave,
  titulo,
  subtitulo,
  acoes,
  aoClicarLinha,
  aoDuploClique,
  chaveSelecionada,
  rodape,
  textoVazio = 'Nenhum registro.',
  linhasPorPagina = 10,
  carregando = false,
}: GradeProps<T>) {
  const [ordem, setOrdem] = useState<Ordem | null>(null)
  const [pagina, setPagina] = useState(0)
  const [porPagina, setPorPagina] = useState(linhasPorPagina)

  const interativa = !!(aoClicarLinha || aoDuploClique)
  const colunaOrdenada = colunas.find((coluna) => coluna.id === ordem?.coluna)
  const ordenadas =
    ordem && colunaOrdenada?.valor
      ? [...linhas].sort((a, b) => {
          const valorA = colunaOrdenada.valor!(a)
          const valorB = colunaOrdenada.valor!(b)
          const resultado = comparar(valorA, valorB)
          const vazio = valorA == null || valorB == null
          return ordem.direcao === 'asc' || vazio ? resultado : -resultado
        })
      : linhas

  const ultimaPagina = Math.max(0, Math.ceil(linhas.length / porPagina) - 1)
  const paginaAtual = Math.min(pagina, ultimaPagina) 
  const visiveis = ordenadas.slice(paginaAtual * porPagina, paginaAtual * porPagina + porPagina)
  const opcoes = [...new Set([5, 10, 25, linhasPorPagina])].sort((a, b) => a - b)

  function alternarOrdem(id: string) {
    setOrdem(ordem?.coluna === id && ordem.direcao === 'asc' ? { coluna: id, direcao: 'desc' } : { coluna: id, direcao: 'asc' })
  }

  return (
    <article className="cartao">
      {titulo && <h2>{titulo}</h2>}
      {subtitulo && <p className="subtitulo">{subtitulo}</p>}
      {carregando && <LinearProgress />}

      <TableContainer>
        <Table
          size="small"
          sx={{
            '& .MuiTableCell-root': { borderColor: 'var(--border)', color: 'var(--text-h)', py: 1.4 },
            '& .MuiTableCell-head': {
              color: 'var(--text)',
              fontSize: 12,
              fontWeight: 600,
              letterSpacing: '0.05em',
              textTransform: 'uppercase',
            },
            '& tbody tr:last-child td': { borderBottom: 0 },
          }}
        >
          <TableHead>
            <TableRow>
              {colunas.map((coluna) => (
                <TableCell
                  key={coluna.id}
                  align={coluna.alinhamento}
                  sx={{ width: coluna.largura }}
                  sortDirection={ordem?.coluna === coluna.id ? ordem.direcao : false}
                >
                  {(coluna.ordenavel ?? !!coluna.valor) ? (
                    <TableSortLabel
                      active={ordem?.coluna === coluna.id}
                      direction={ordem?.coluna === coluna.id ? ordem.direcao : 'asc'}
                      onClick={() => alternarOrdem(coluna.id)}
                    >
                      {coluna.titulo}
                    </TableSortLabel>
                  ) : (
                    coluna.titulo
                  )}
                </TableCell>
              ))}
              {acoes && <TableCell align="right" aria-label="Ações" />}
            </TableRow>
          </TableHead>
          <TableBody>
            {visiveis.map((linha) => (
              <TableRow
                key={chave(linha)}
                hover={interativa}
                selected={chaveSelecionada != null && chave(linha) === chaveSelecionada}
                sx={interativa ? { cursor: 'pointer', userSelect: 'none' } : undefined}
                tabIndex={interativa ? 0 : undefined}
                onClick={aoClicarLinha ? () => aoClicarLinha(linha) : undefined}
                onDoubleClick={aoDuploClique ? () => aoDuploClique(linha) : undefined}
                onKeyDown={
                  interativa
                    ? (evento: KeyboardEvent<HTMLTableRowElement>) => {
                        if (evento.key !== 'Enter') return
                        if (aoDuploClique) aoDuploClique(linha)
                        else aoClicarLinha?.(linha)
                      }
                    : undefined
                }
              >
                {colunas.map((coluna) => (
                  <TableCell key={coluna.id} align={coluna.alinhamento}>
                    {coluna.renderizar ? coluna.renderizar(linha) : (coluna.valor?.(linha) ?? '—')}
                  </TableCell>
                ))}
                {acoes && (
                  <TableCell align="right" sx={{ whiteSpace: 'nowrap' }} onClick={(evento) => evento.stopPropagation()}>
                    {acoes(linha)}
                  </TableCell>
                )}
              </TableRow>
            ))}
            {linhas.length === 0 && (
              <TableRow>
                <TableCell colSpan={colunas.length + (acoes ? 1 : 0)} align="center">
                  {textoVazio}
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </TableContainer>

      {(rodape || linhas.length > 0) && (
        <Stack direction="row" sx={{ alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: 1, mt: 1 }}>
          <Stack direction="row" spacing={1}>
            {rodape}
          </Stack>
          {linhas.length > 0 && (
            <TablePagination
              component="div"
              count={linhas.length}
              page={paginaAtual}
              rowsPerPage={porPagina}
              rowsPerPageOptions={opcoes}
              onPageChange={(_, novaPagina) => setPagina(novaPagina)}
              onRowsPerPageChange={(evento) => {
                setPorPagina(Number(evento.target.value))
                setPagina(0)
              }}
              labelRowsPerPage="Linhas por página"
              labelDisplayedRows={({ from, to, count }) => `${from}–${to} de ${count}`}
              sx={{ color: 'var(--text)', borderBottom: 0 }}
            />
          )}
        </Stack>
      )}
    </article>
  )
}
