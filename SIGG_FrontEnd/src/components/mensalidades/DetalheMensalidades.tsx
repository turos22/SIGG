import Alert from '@mui/material/Alert'
import Chip from '@mui/material/Chip'
import Stack from '@mui/material/Stack'
import Grade, { type ColunaGrade } from '@/components/grade/Grade'
import type Mensalidade from '@/data/models/Mensalidade'
import type { SituacaoMensalidade } from '@/data/models/Mensalidade'
import type SaldoMensalidades from '@/data/models/SaldoMensalidades'
import { formatarData } from '@/data/utils/datas'
import { formatarMoeda, formatarReferencia } from '@/data/utils/formatadores'
import './DetalheMensalidades.css'

interface DetalheMensalidadesProps {
  saldo: SaldoMensalidades
}

interface IndicadorProps {
  rotulo: string
  valor: number
  destaque?: boolean
}

const COR_DA_SITUACAO: Record<SituacaoMensalidade, 'success' | 'error' | 'warning' | 'default'> = {
  PAGA: 'success',
  ATRASADA: 'error',
  PARCIAL: 'warning',
  ABERTA: 'default',
}

const ROTULO_DA_SITUACAO: Record<SituacaoMensalidade, string> = {
  PAGA: 'Paga',
  ATRASADA: 'Atrasada',
  PARCIAL: 'Pagamento parcial',
  ABERTA: 'Em aberto',
}

const colunas: ColunaGrade<Mensalidade>[] = [
  {
    id: 'referencia',
    titulo: 'Referência',
    valor: (m) => m.mens_ano * 100 + m.mens_mes,
    renderizar: (m) => formatarReferencia(m.mens_mes, m.mens_ano),
  },
  {
    id: 'vencimento',
    titulo: 'Vencimento',
    valor: (m) => m.dt_vencimento,
    renderizar: (m) => (m.dt_vencimento ? formatarData(m.dt_vencimento) : '—'),
  },
  { id: 'valor', titulo: 'Valor', valor: (m) => m.mens_valor, renderizar: (m) => formatarMoeda(m.mens_valor), alinhamento: 'right' },
  { id: 'pago', titulo: 'Pago', valor: (m) => m.valor_pago, renderizar: (m) => formatarMoeda(m.valor_pago), alinhamento: 'right' },
  { id: 'saldo', titulo: 'Saldo', valor: (m) => m.saldo, renderizar: (m) => formatarMoeda(m.saldo), alinhamento: 'right' },
  {
    id: 'situacao',
    titulo: 'Situação',
    valor: (m) => m.situacao,
    renderizar: (m) => (
      <Chip
        size="small"
        variant="outlined"
        color={COR_DA_SITUACAO[m.situacao]}
        label={m.apadrinhada ? 'Paga pelo Anjo' : ROTULO_DA_SITUACAO[m.situacao]}
      />
    ),
  },
]

function Indicador({ rotulo, valor, destaque }: IndicadorProps) {
  return (
    <div className={destaque ? 'detalhe-mensalidades-total detalhe-mensalidades-total-destaque' : 'detalhe-mensalidades-total'}>
      <span>{rotulo}</span>
      <strong>{formatarMoeda(valor)}</strong>
    </div>
  )
}

export default function DetalheMensalidades({ saldo }: DetalheMensalidadesProps) {
  return (
    <section className="detalhe-mensalidades">
      <header className="detalhe-mensalidades-cabecalho">
        <div>
          <h2>{saldo.nome}</h2>
          <p className="subtitulo">Matrícula {saldo.esc_cod}</p>
        </div>
        <Stack direction="row" spacing={0.5}>
          {saldo.anjo && <Chip size="small" color="warning" label="Anjo" />}
          {saldo.adotado && <Chip size="small" color="secondary" label="Adotado" />}
        </Stack>
      </header>

      {saldo.observacao && <Alert severity="info">{saldo.observacao}</Alert>}

      <div className="detalhe-mensalidades-totais">
        <Indicador rotulo="Mensalidades próprias em aberto" valor={saldo.total_proprio} />
        {saldo.anjo && <Indicador rotulo="Mensalidades dos adotados" valor={saldo.total_adotados} />}
        <Indicador rotulo="Total a pagar" valor={saldo.total_a_pagar} destaque />
      </div>

      <Grade
        titulo="Mensalidades em aberto"
        colunas={colunas}
        linhas={saldo.abertas}
        chave={(m) => m.mens_cod}
        textoVazio="Nenhuma mensalidade em aberto."
        linhasPorPagina={5}
      />

      {saldo.adotados.map((adotado) => (
        <Grade
          key={adotado.esc_cod}
          titulo={`Adotado: ${adotado.nome}`}
          subtitulo={`Subtotal em aberto: ${formatarMoeda(adotado.subtotal)}`}
          colunas={colunas}
          linhas={adotado.abertas}
          chave={(m) => m.mens_cod}
          textoVazio="Nenhuma mensalidade em aberto para este adotado."
          linhasPorPagina={5}
        />
      ))}

      <Grade
        titulo="Histórico de mensalidades pagas"
        colunas={colunas}
        linhas={saldo.pagas}
        chave={(m) => m.mens_cod}
        textoVazio="Nenhuma mensalidade paga."
        linhasPorPagina={5}
      />
    </section>
  )
}
