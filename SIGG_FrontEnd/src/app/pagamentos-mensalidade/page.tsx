import { useEffect, useState } from 'react'
import Alert from '@mui/material/Alert'
import Box from '@mui/material/Box'
import Button from '@mui/material/Button'
import Card from '@mui/material/Card'
import CardContent from '@mui/material/CardContent'
import Chip from '@mui/material/Chip'
import Dialog from '@mui/material/Dialog'
import DialogActions from '@mui/material/DialogActions'
import DialogContent from '@mui/material/DialogContent'
import DialogTitle from '@mui/material/DialogTitle'
import MenuItem from '@mui/material/MenuItem'
import Stack from '@mui/material/Stack'
import TextField from '@mui/material/TextField'
import Typography from '@mui/material/Typography'
import Grade, { type ColunaGrade } from '@/components/grade/Grade'
import { BotaoCancelar, BotaoOk } from '@/components/formulario/Botoes'
import CampoTexto from '@/components/formulario/CampoTexto'
import DialogoConfirmacao from '@/components/formulario/DialogoConfirmacao'
import type Escoteiro from '@/data/models/Escoteiro'
import type {
    FormaPagamento,
    PagamentoMensalidade,
    ReciboPagamentoMensalidade,
    SituacaoFinanceiraEscoteiro,
    SituacaoParcelaMensalidade,
} from '@/data/models/PagamentoMensalidade'
import {
    buscarFormasPagamento,
    consultarSituacaoFinanceiraEscoteiro,
    estornarPagamentoMensalidade,
    pesquisarEscoteirosPagamento,
    registrarPagamentoMensalidade,
} from '@/data/services/pagamentosMensalidade'
import { formatarData } from '@/data/utils/datas'
import { mensagemDeErro } from '@/data/utils/erros'
import { formatarCpf, formatarMoeda } from '@/data/utils/formatadores'

const COR_SITUACAO: Record<SituacaoParcelaMensalidade, 'success' | 'error' | 'warning' | 'default'> = {
    PAGA: 'success',
    ATRASADA: 'error',
    PARCIAL: 'warning',
    ABERTA: 'default',
}

const ROTULO_SITUACAO: Record<SituacaoParcelaMensalidade, string> = {
    PAGA: 'Paga',
    ATRASADA: 'Atrasada',
    PARCIAL: 'Parcial',
    ABERTA: 'Em aberto',
}

const colunasEscoteiros: ColunaGrade<Escoteiro>[] = [
    { id: 'nome', titulo: 'Nome', valor: (e) => e.membro.nome },
    { id: 'cpf', titulo: 'CPF', valor: (e) => e.membro.CPF, renderizar: (e) => formatarCpf(e.membro.CPF) },
    { id: 'matricula', titulo: 'Matrícula', valor: (e) => e.esc_cod, largura: 110 },
    { id: 'patrulha', titulo: 'Patrulha', valor: (e) => e.patrulha.nome },
    {
        id: 'situacao',
        titulo: 'Situação Financeira',
        valor: (e) => e.esc_sit_fin,
        renderizar: (e) => (
            <Chip
                size="small"
                color={e.esc_sit_fin === 1 ? 'error' : 'success'}
                label={e.esc_sit_fin === 1 ? 'Inadimplente' : 'Em dia'}
            />
        ),
    },
]

export default function PaginaPagamentosMensalidade() {
    const [busca, setBusca] = useState('')
    const [encontrados, setEncontrados] = useState<Escoteiro[]>([])
    const [situacao, setSituacao] = useState<SituacaoFinanceiraEscoteiro | null>(null)
    const [formasPagamento, setFormasPagamento] = useState<FormaPagamento[]>([
        { tp_cod: 1, tp_desc: 'Dinheiro' },
        { tp_cod: 2, tp_desc: 'PIX' },
        { tp_cod: 3, tp_desc: 'Cartão' },
        { tp_cod: 4, tp_desc: 'Apadrinhamento (Anjo)' },
    ])

    const [carregando, setCarregando] = useState(false)
    const [erro, setErro] = useState('')
    const [sucesso, setSucesso] = useState('')

    const [parcelaPagar, setParcelaPagar] = useState<PagamentoMensalidade | null>(null)
    const [valorRecebido, setValorRecebido] = useState('')
    const [formaPagamentoCod, setFormaPagamentoCod] = useState<number>(2)
    const [dataPagamento, setDataPagamento] = useState(() => new Date().toISOString().slice(0, 10))
    const [confirmouDivergencia, setConfirmouDivergencia] = useState(false)

    const [parcelaEstornar, setParcelaEstornar] = useState<PagamentoMensalidade | null>(null)
    const [recibo, setRecibo] = useState<ReciboPagamentoMensalidade | null>(null)

    const carregarListaInicial = () => {
        pesquisarEscoteirosPagamento('')
            .then((lista) => setEncontrados(lista))
            .catch(() => {})
    }

    useEffect(() => {
        buscarFormasPagamento()
            .then((lista) => {
                if (lista.length > 0) setFormasPagamento(lista)
            })
            .catch(() => {})

        carregarListaInicial()
    }, [])

    const carregarSituacao = async (escCod: number) => {
        setCarregando(true)
        setErro('')
        try {
            const dados = await consultarSituacaoFinanceiraEscoteiro(escCod)
            setSituacao(dados)
        } catch (err) {
            setSituacao(null)
            setErro(mensagemDeErro(err, 'Erro ao consultar situação financeira do escoteiro.'))
        } finally {
            setCarregando(false)
        }
    }

    const handlePesquisar = async (e: React.FormEvent) => {
        e.preventDefault()
        setErro('')
        setSucesso('')
        setSituacao(null)
        setCarregando(true)

        try {
            const lista = await pesquisarEscoteirosPagamento(busca)
            setEncontrados(lista)
            if (lista.length === 1) {
                await carregarSituacao(lista[0].esc_cod)
            }
        } catch (err) {
            setEncontrados([])
            setErro(mensagemDeErro(err, 'Escoteiro não localizado no sistema.'))
        } finally {
            setCarregando(false)
        }
    }

    const handleLimparBusca = () => {
        setBusca('')
        setSituacao(null)
        setErro('')
        setSucesso('')
        carregarListaInicial()
    }

    const abrirModalPagamento = (item: PagamentoMensalidade) => {
        setParcelaPagar(item)
        setValorRecebido(Number(item.saldo).toFixed(2))
        setFormaPagamentoCod(2)
        setDataPagamento(new Date().toISOString().slice(0, 10))
        setConfirmouDivergencia(false)
        setErro('')
        setSucesso('')
    }

    const valorNumerico = parseFloat(valorRecebido) || 0
    const saldoDevido = parcelaPagar ? Number(parcelaPagar.saldo) : 0
    const diferenca = Number((valorNumerico - saldoDevido).toFixed(2))
    const temDivergencia = parcelaPagar !== null && Math.abs(diferenca) >= 0.01

    const handleConfirmarPagamento = async (e: React.FormEvent) => {
        e.preventDefault()
        if (!parcelaPagar || !situacao) return

        if (temDivergencia && !confirmouDivergencia) {
            setConfirmouDivergencia(true)
            return
        }

        setCarregando(true)
        setErro('')
        try {
            const resp = await registrarPagamentoMensalidade(parcelaPagar.crCod, {
                valorRecebido: valorNumerico,
                formaPagamentoCod,
                dataPagamento,
            })
            setSucesso(resp.mensagem)
            setRecibo(resp.recibo)
            setParcelaPagar(null)
            await carregarSituacao(situacao.esc_cod)
            carregarListaInicial()
        } catch (err) {
            setErro(mensagemDeErro(err, 'Não foi possível registrar o pagamento.'))
        } finally {
            setCarregando(false)
        }
    }

    const handleConfirmarEstorno = async () => {
        if (!parcelaEstornar || !situacao) return
        const alvo = parcelaEstornar
        setParcelaEstornar(null)
        setCarregando(true)
        setErro('')
        setSucesso('')

        try {
            const resp = await estornarPagamentoMensalidade(alvo.crCod)
            setSucesso(resp.mensagem)
            await carregarSituacao(situacao.esc_cod)
            carregarListaInicial()
        } catch (err) {
            setErro(mensagemDeErro(err, 'Não foi possível estornar o pagamento.'))
        } finally {
            setCarregando(false)
        }
    }

    const colunasMensalidades: ColunaGrade<PagamentoMensalidade>[] = [
        { id: 'referencia', titulo: 'Referência', valor: (m) => m.referencia },
        {
            id: 'vencimento',
            titulo: 'Vencimento',
            valor: (m) => m.dataVencimento,
            renderizar: (m) => (m.dataVencimento ? formatarData(m.dataVencimento) : '—'),
        },
        {
            id: 'valorDevido',
            titulo: 'Valor Devido',
            valor: (m) => m.valorDevido,
            renderizar: (m) => formatarMoeda(m.valorDevido),
            alinhamento: 'right',
        },
        {
            id: 'valorPago',
            titulo: 'Valor Pago',
            valor: (m) => m.valorPago,
            renderizar: (m) => formatarMoeda(m.valorPago),
            alinhamento: 'right',
        },
        {
            id: 'saldo',
            titulo: 'Saldo',
            valor: (m) => m.saldo,
            renderizar: (m) => formatarMoeda(m.saldo),
            alinhamento: 'right',
        },
        {
            id: 'forma',
            titulo: 'Forma / Data Pgto',
            valor: (m) => m.formaPagamentoDesc ?? '',
            renderizar: (m) =>
                m.dataPagamento ? `${m.formaPagamentoDesc ?? 'Pago'} (${formatarData(m.dataPagamento)})` : '—',
        },
        {
            id: 'situacao',
            titulo: 'Situação',
            valor: (m) => m.situacao,
            renderizar: (m) => (
                <Chip size="small" variant="outlined" color={COR_SITUACAO[m.situacao]} label={ROTULO_SITUACAO[m.situacao]} />
            ),
        },
        {
            id: 'acoes',
            titulo: 'Ações',
            valor: (m) => m.crCod,
            renderizar: (m) => (
                <Stack direction="row" spacing={1}>
                    {Number(m.saldo) > 0 && (
                        <Button size="small" variant="contained" color="success" onClick={() => abrirModalPagamento(m)}>
                            Registrar Pagamento
                        </Button>
                    )}
                    {Number(m.valorPago) > 0 && (
                        <Button size="small" variant="outlined" color="error" onClick={() => setParcelaEstornar(m)}>
                            Estornar
                        </Button>
                    )}
                </Stack>
            ),
        },
    ]

    const abertas = situacao?.mensalidades.filter((m) => m.situacao !== 'PAGA' && Number(m.saldo ?? 0) > 0) ?? []
    const pagas = situacao?.mensalidades.filter((m) => m.situacao === 'PAGA' || Number(m.saldo ?? 0) <= 0) ?? []

    return (
        <main className="pagina">
            <header>
                <h1>Registrar Pagamento de Mensalidade</h1>
                <p className="subtitulo">Baixa de mensalidades, regularização de inadimplência, estornos e recibos</p>
            </header>

            {erro && <Alert severity="error" sx={{ mb: 2 }}>{erro}</Alert>}
            {sucesso && <Alert severity="success" sx={{ mb: 2 }}>{sucesso}</Alert>}

            <Box
                component="form"
                onSubmit={handlePesquisar}
                sx={{ display: 'flex', gap: 1.5, alignItems: 'flex-start', mb: 2 }}
            >
                <CampoTexto rotulo="Filtrar por Nome, CPF ou Matrícula" valor={busca} aoMudar={setBusca} />
                <BotaoOk rotulo="Buscar" carregando={carregando} />
                {(busca || situacao) && (
                    <BotaoCancelar rotulo="Ver Todos" aoClicar={handleLimparBusca} />
                )}
            </Box>

            {encontrados.length > 0 && (
                <Grade
                    titulo="Escoteiros"
                    subtitulo="Clique sobre o escoteiro para consultar e registrar pagamentos"
                    colunas={colunasEscoteiros}
                    linhas={encontrados}
                    chave={(e) => e.esc_cod}
                    chaveSelecionada={situacao?.esc_cod ?? null}
                    aoClicarLinha={(e) => carregarSituacao(e.esc_cod)}
                    linhasPorPagina={5}
                />
            )}

            {situacao && (
                <Stack spacing={2.5} sx={{ mt: 2 }}>
                    <Card variant="outlined">
                        <CardContent sx={{ display: 'flex', justifyContent: 'space-between', flexWrap: 'wrap', gap: 2 }}>
                            <div>
                                <Typography variant="h6">{situacao.nome}</Typography>
                                <Typography variant="body2" color="text.secondary">
                                    CPF: {formatarCpf(situacao.cpf)} | Matrícula: {situacao.esc_cod} | Patrulha: {situacao.patrulha}
                                </Typography>
                                <Typography variant="body2" color="text.secondary">
                                    Categoria: <strong>{situacao.categoria}</strong> ({formatarMoeda(situacao.valorCategoria)}/mês)
                                </Typography>
                            </div>
                            <Box sx={{ display: 'flex', alignItems: 'center' }}>
                                <Chip
                                    color={situacao.inadimplente ? 'error' : 'success'}
                                    label={situacao.inadimplente ? 'Situação: INADIMPLENTE' : 'Situação: EM DIA'}
                                />
                            </Box>
                        </CardContent>
                    </Card>

                    {situacao.inadimplente && (
                        <Alert severity="warning">
                            Este escoteiro está inadimplente. O pagamento das parcelas vencidas regularizará automaticamente a situação.
                        </Alert>
                    )}

                    {abertas.length === 0 ? (
                        <Alert severity="info">Não há mensalidades pendentes para este membro.</Alert>
                    ) : (
                        <Grade
                            titulo="Mensalidades em Aberto"
                            subtitulo={`${abertas.length} pendente(s)`}
                            colunas={colunasMensalidades}
                            linhas={abertas}
                            chave={(m) => m.crCod}
                            linhasPorPagina={10}
                        />
                    )}

                    <Grade
                        titulo="Histórico de Mensalidades Quitadas"
                        subtitulo={`${pagas.length} quitada(s)`}
                        colunas={colunasMensalidades}
                        linhas={pagas}
                        chave={(m) => m.crCod}
                        textoVazio="Nenhuma mensalidade quitada registrada."
                        linhasPorPagina={10}
                    />
                </Stack>
            )}

            <Dialog open={parcelaPagar !== null} onClose={() => setParcelaPagar(null)} maxWidth="xs" fullWidth>
                <form onSubmit={handleConfirmarPagamento}>
                    <DialogTitle>Registrar Pagamento — {parcelaPagar?.referencia}</DialogTitle>
                    <DialogContent>
                        <Stack spacing={2} sx={{ mt: 1 }}>
                            <Typography variant="body2">
                                Escoteiro: <strong>{parcelaPagar?.escoteiroNome}</strong>
                                <br />
                                Valor Devido: <strong>{formatarMoeda(saldoDevido)}</strong>
                            </Typography>

                            <TextField
                                label="Valor Recebido (R$)"
                                type="number"
                                inputProps={{ step: '0.01', min: '0.01' }}
                                value={valorRecebido}
                                onChange={(e) => {
                                    setValorRecebido(e.target.value)
                                    setConfirmouDivergencia(false)
                                }}
                                required
                                fullWidth
                            />

                            {temDivergencia && (
                                <Alert severity={diferenca < 0 ? 'warning' : 'info'}>
                                    Valor recebido: <strong>{formatarMoeda(valorNumerico)}</strong> | Valor devido:{' '}
                                    <strong>{formatarMoeda(saldoDevido)}</strong> | Diferença:{' '}
                                    <strong>{formatarMoeda(Math.abs(diferenca))}</strong>.
                                    <br />
                                    {diferenca < 0
                                        ? `O valor recebido é inferior ao valor devido. A diferença de ${formatarMoeda(Math.abs(diferenca))} será mantida em aberto. Deseja continuar?`
                                        : `O valor recebido é superior ao valor devido. O excedente de ${formatarMoeda(diferenca)} será registrado como crédito. Deseja continuar?`}
                                </Alert>
                            )}

                            <TextField
                                select
                                label="Forma de Pagamento"
                                value={formaPagamentoCod}
                                onChange={(e) => setFormaPagamentoCod(Number(e.target.value))}
                                fullWidth
                            >
                                {formasPagamento.map((fp) => (
                                    <MenuItem key={fp.tp_cod} value={fp.tp_cod}>
                                        {fp.tp_desc}
                                    </MenuItem>
                                ))}
                            </TextField>

                            <TextField
                                label="Data do Pagamento"
                                type="date"
                                InputLabelProps={{ shrink: true }}
                                value={dataPagamento}
                                onChange={(e) => setDataPagamento(e.target.value)}
                                required
                                fullWidth
                            />
                        </Stack>
                    </DialogContent>
                    <DialogActions sx={{ px: 3, pb: 2 }}>
                        <BotaoCancelar rotulo="Cancelar" aoClicar={() => setParcelaPagar(null)} />
                        <BotaoOk
                            rotulo={temDivergencia && !confirmouDivergencia ? 'Confirmar com divergência' : 'Confirmar Pagamento'}
                            tipo="submit"
                            carregando={carregando}
                        />
                    </DialogActions>
                </form>
            </Dialog>

            <DialogoConfirmacao
                aberto={parcelaEstornar !== null}
                mensagem={`Deseja realmente estornar o pagamento de ${parcelaEstornar ? formatarMoeda(parcelaEstornar.valorPago) : ''} referente à mensalidade de ${parcelaEstornar?.referencia ?? ''}? Esta ação não pode ser desfeita.`}
                aoConfirmar={handleConfirmarEstorno}
                aoCancelar={() => setParcelaEstornar(null)}
                carregando={carregando}
            />

            <Dialog open={recibo !== null} onClose={() => setRecibo(null)} maxWidth="sm" fullWidth>
                <DialogTitle>Recibo de Pagamento — {recibo?.numero}</DialogTitle>
                <DialogContent>
                    {recibo && (
                        <Box sx={{ p: 2, border: '1px solid #ccc', borderRadius: 1, bgcolor: '#fafafa' }}>
                            <Typography variant="subtitle1" fontWeight="bold" gutterBottom>
                                Grupo de Escoteiros Guayporé 186°/SP — Recibo Oficial
                            </Typography>
                            <Typography variant="body2"><strong>Escoteiro:</strong> {recibo.escoteiro} (Matrícula {recibo.matricula})</Typography>
                            <Typography variant="body2"><strong>CPF:</strong> {formatarCpf(recibo.cpf)}</Typography>
                            <Typography variant="body2"><strong>Referência:</strong> Mensalidade {recibo.referencia}</Typography>
                            <Typography variant="body2"><strong>Data do Pagamento:</strong> {formatarData(recibo.dataPagamento)}</Typography>
                            <Typography variant="body2"><strong>Valor Devido:</strong> {formatarMoeda(recibo.valorDevido)}</Typography>
                            <Typography variant="body2"><strong>Valor Pago:</strong> {formatarMoeda(recibo.valorPago)}</Typography>
                            {recibo.observacao && (
                                <Typography variant="body2" color="warning.main" sx={{ mt: 1 }}>
                                    <strong>Nota Explicativa:</strong> {recibo.observacao}
                                </Typography>
                            )}
                            {recibo.regularizado && (
                                <Typography variant="body2" color="success.main" sx={{ mt: 1 }}>
                                    <strong>Status Financeiro:</strong> Regularizado (Em dia)
                                </Typography>
                            )}
                        </Box>
                    )}
                </DialogContent>
                <DialogActions sx={{ px: 3, pb: 2 }}>
                    <Button variant="outlined" onClick={() => window.print()}>
                        Imprimir / Salvar PDF
                    </Button>
                    <BotaoOk rotulo="Fechar" tipo="button" aoClicar={() => setRecibo(null)} />
                </DialogActions>
            </Dialog>
        </main>
    )
}