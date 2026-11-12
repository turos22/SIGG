import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router'
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
import type { FormaPagamento } from '@/data/models/PagamentoMensalidade'
import type {
    AtividadesFinanceiroEscoteiro,
    PagamentoAtividade,
    ReciboPagamentoAtividade,
    SituacaoPagamentoAtividade,
} from '@/data/models/PagamentoAtividade'
import {
    buscarFormasPagamentoAtividade,
    consultarAtividadesEscoteiro,
    estornarPagamentoAtividade,
    pesquisarEscoteirosAtividade,
    registrarPagamentoAtividade,
} from '@/data/services/pagamentosAtividade'
import { formatarData } from '@/data/utils/datas'
import { mensagemDeErro } from '@/data/utils/erros'
import { formatarCpf, formatarMoeda } from '@/data/utils/formatadores'

const COR_SITUACAO: Record<SituacaoPagamentoAtividade, 'success' | 'warning' | 'default'> = {
    PAGA: 'success',
    PARCIAL: 'warning',
    ABERTA: 'default',
}

const ROTULO_SITUACAO: Record<SituacaoPagamentoAtividade, string> = {
    PAGA: 'Paga',
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

export default function PaginaPagamentosAtividade() {
    const navegar = useNavigate()
    const [busca, setBusca] = useState('')
    const [encontrados, setEncontrados] = useState<Escoteiro[]>([])
    const [dadosEscoteiro, setDadosEscoteiro] = useState<AtividadesFinanceiroEscoteiro | null>(null)
    const [formasPagamento, setFormasPagamento] = useState<FormaPagamento[]>([
        { tp_cod: 1, tp_desc: 'Dinheiro' },
        { tp_cod: 2, tp_desc: 'PIX' },
        { tp_cod: 3, tp_desc: 'Cartão' },
    ])

    const [carregando, setCarregando] = useState(false)
    const [erro, setErro] = useState('')
    const [sucesso, setSucesso] = useState('')

    const [atividadePagar, setAtividadePagar] = useState<PagamentoAtividade | null>(null)
    const [valorRecebido, setValorRecebido] = useState('')
    const [formaPagamentoCod, setFormaPagamentoCod] = useState<number>(2)
    const [dataPagamento, setDataPagamento] = useState(() => new Date().toISOString().slice(0, 10))
    const [confirmouDivergencia, setConfirmouDivergencia] = useState(false)
    const [confirmouDuplicado, setConfirmouDuplicado] = useState(false)

    const [atividadeEstornar, setAtividadeEstornar] = useState<PagamentoAtividade | null>(null)
    const [recibo, setRecibo] = useState<ReciboPagamentoAtividade | null>(null)

    const carregarListaInicial = () => {
        pesquisarEscoteirosAtividade('')
            .then((lista) => setEncontrados(lista))
            .catch(() => {})
    }

    useEffect(() => {
        buscarFormasPagamentoAtividade()
            .then((lista) => {
                if (lista.length > 0) setFormasPagamento(lista)
            })
            .catch(() => {})

        carregarListaInicial()
    }, [])

    const carregarAtividades = async (escCod: number) => {
        setCarregando(true)
        setErro('')
        try {
            const dados = await consultarAtividadesEscoteiro(escCod)
            setDadosEscoteiro(dados)
        } catch (err) {
            setDadosEscoteiro(null)
            setErro(mensagemDeErro(err, 'Erro ao consultar atividades do escoteiro.'))
        } finally {
            setCarregando(false)
        }
    }

    const handlePesquisar = async (e: React.FormEvent) => {
        e.preventDefault()
        setErro('')
        setSucesso('')
        setDadosEscoteiro(null)
        setCarregando(true)

        try {
            const lista = await pesquisarEscoteirosAtividade(busca)
            setEncontrados(lista)
            if (lista.length === 1) {
                await carregarAtividades(lista[0].esc_cod)
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
        setDadosEscoteiro(null)
        setErro('')
        setSucesso('')
        carregarListaInicial()
    }

    const abrirModalPagamento = (item: PagamentoAtividade) => {
        setAtividadePagar(item)
        const valorSugerido = Number(item.saldo) > 0 ? Number(item.saldo) : Number(item.valorAtividade)
        setValorRecebido(valorSugerido.toFixed(2))
        setFormaPagamentoCod(2)
        setDataPagamento(new Date().toISOString().slice(0, 10))
        setConfirmouDivergencia(false)
        setConfirmouDuplicado(false)
        setErro('')
        setSucesso('')
    }

    const jaEstaPaga = atividadePagar !== null && Number(atividadePagar.saldo) === 0
    const valorNumerico = parseFloat(valorRecebido) || 0
    const valorBase = atividadePagar
        ? Number(atividadePagar.saldo) > 0
            ? Number(atividadePagar.saldo)
            : Number(atividadePagar.valorAtividade)
        : 0
    const diferenca = Number((valorNumerico - valorBase).toFixed(2))
    const temDivergencia = atividadePagar !== null && !jaEstaPaga && Math.abs(diferenca) >= 0.01

    const handleConfirmarPagamento = async (e: React.FormEvent) => {
        e.preventDefault()
        if (!atividadePagar || !dadosEscoteiro) return

        if (jaEstaPaga && !confirmouDuplicado) {
            setConfirmouDuplicado(true)
            return
        }

        if (temDivergencia && !confirmouDivergencia) {
            setConfirmouDivergencia(true)
            return
        }

        setCarregando(true)
        setErro('')
        try {
            const resp = await registrarPagamentoAtividade(atividadePagar.crCod, {
                valorRecebido: valorNumerico,
                formaPagamentoCod,
                dataPagamento,
                confirmarDuplicado: jaEstaPaga && confirmouDuplicado,
            })
            setSucesso(resp.mensagem)
            setRecibo(resp.recibo)
            setAtividadePagar(null)
            await carregarAtividades(dadosEscoteiro.esc_cod)
        } catch (err) {
            setErro(mensagemDeErro(err, 'Não foi possível registrar o pagamento da atividade.'))
        } finally {
            setCarregando(false)
        }
    }

    const handleConfirmarEstorno = async () => {
        if (!atividadeEstornar || !dadosEscoteiro) return
        const alvo = atividadeEstornar
        setAtividadeEstornar(null)
        setCarregando(true)
        setErro('')
        setSucesso('')

        try {
            const resp = await estornarPagamentoAtividade(alvo.crCod)
            setSucesso(resp.mensagem)
            await carregarAtividades(dadosEscoteiro.esc_cod)
        } catch (err) {
            setErro(mensagemDeErro(err, 'Não foi possível estornar o pagamento da atividade.'))
        } finally {
            setCarregando(false)
        }
    }

    const bloqueadoPorInadimplencia = dadosEscoteiro?.inadimplente ?? false

    const colunasAtividades: ColunaGrade<PagamentoAtividade>[] = [
        { id: 'atividade', titulo: 'Atividade', valor: (a) => a.atividadeNome },
        {
            id: 'data',
            titulo: 'Data da Atividade',
            valor: (a) => a.atividadeData,
            renderizar: (a) => (a.atividadeData ? formatarData(a.atividadeData) : '—'),
        },
        {
            id: 'valorAtividade',
            titulo: 'Valor',
            valor: (a) => a.valorAtividade,
            renderizar: (a) => formatarMoeda(a.valorAtividade),
            alinhamento: 'right',
        },
        {
            id: 'valorPago',
            titulo: 'Valor Pago',
            valor: (a) => a.valorPago,
            renderizar: (a) => formatarMoeda(a.valorPago),
            alinhamento: 'right',
        },
        {
            id: 'saldo',
            titulo: 'Saldo',
            valor: (a) => a.saldo,
            renderizar: (a) => formatarMoeda(a.saldo),
            alinhamento: 'right',
        },
        {
            id: 'forma',
            titulo: 'Forma / Data Pgto',
            valor: (a) => a.formaPagamentoDesc ?? '',
            renderizar: (a) =>
                a.dataPagamento ? `${a.formaPagamentoDesc ?? 'Pago'} (${formatarData(a.dataPagamento)})` : '—',
        },
        {
            id: 'situacao',
            titulo: 'Situação',
            valor: (a) => a.situacao,
            renderizar: (a) => (
                <Chip size="small" variant="outlined" color={COR_SITUACAO[a.situacao]} label={ROTULO_SITUACAO[a.situacao]} />
            ),
        },
        {
            id: 'acoes',
            titulo: 'Ações',
            valor: (a) => a.crCod,
            renderizar: (a) => (
                <Stack direction="row" spacing={1}>
                    <Button
                        size="small"
                        variant={Number(a.saldo) > 0 ? 'contained' : 'outlined'}
                        color="success"
                        disabled={bloqueadoPorInadimplencia}
                        onClick={() => abrirModalPagamento(a)}
                    >
                        {Number(a.saldo) > 0 ? 'Registrar Pagamento' : 'Novo Pgto'}
                    </Button>
                    {Number(a.valorPago) > 0 && (
                        <Button size="small" variant="outlined" color="error" onClick={() => setAtividadeEstornar(a)}>
                            Estornar
                        </Button>
                    )}
                </Stack>
            ),
        },
    ]

    return (
        <main className="pagina">
            <header>
                <h1>Registrar Pagamento de Atividade</h1>
                <p className="subtitulo">Controle de pagamentos de acampamentos, passeios e eventos especiais</p>
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
                {(busca || dadosEscoteiro) && (
                    <BotaoCancelar rotulo="Ver Todos" aoClicar={handleLimparBusca} />
                )}
            </Box>

            {encontrados.length > 0 && (
                <Grade
                    titulo="Escoteiros (Inadimplentes bloqueados para atividades pagas)"
                    subtitulo="Clique sobre o escoteiro para consultar suas inscrições em atividades"
                    colunas={colunasEscoteiros}
                    linhas={encontrados}
                    chave={(e) => e.esc_cod}
                    chaveSelecionada={dadosEscoteiro?.esc_cod ?? null}
                    aoClicarLinha={(e) => carregarAtividades(e.esc_cod)}
                    linhasPorPagina={5}
                />
            )}

            {dadosEscoteiro && (
                <Stack spacing={2.5} sx={{ mt: 2 }}>
                    <Card variant="outlined">
                        <CardContent sx={{ display: 'flex', justifyContent: 'space-between', flexWrap: 'wrap', gap: 2 }}>
                            <div>
                                <Typography variant="h6">{dadosEscoteiro.nome}</Typography>
                                <Typography variant="body2" color="text.secondary">
                                    CPF: {formatarCpf(dadosEscoteiro.cpf)} | Matrícula: {dadosEscoteiro.esc_cod} | Patrulha: {dadosEscoteiro.patrulha}
                                </Typography>
                            </div>
                            <Box sx={{ display: 'flex', alignItems: 'center' }}>
                                <Chip
                                    color={dadosEscoteiro.inadimplente ? 'error' : 'success'}
                                    label={dadosEscoteiro.inadimplente ? 'Mensalidades: INADIMPLENTE' : 'Mensalidades: EM DIA'}
                                />
                            </Box>
                        </CardContent>
                    </Card>

                    {dadosEscoteiro.inadimplente && (
                        <Alert
                            severity="error"
                            action={
                                <Button color="inherit" size="small" onClick={() => navegar('/pagamentos-mensalidade')}>
                                    Regularizar Mensalidades
                                </Button>
                            }
                        >
                            Este escoteiro está inadimplente. Não é permitido registrar pagamento de atividades pagas enquanto houver
                            mensalidades pendentes. Regularize a situação financeira primeiro.
                        </Alert>
                    )}

                    {dadosEscoteiro.atividades.length === 0 ? (
                        <Alert severity="info">Este escoteiro não possui inscrições em atividades pagas.</Alert>
                    ) : (
                        <Grade
                            titulo="Inscrições em Atividades Pagas"
                            subtitulo={`${dadosEscoteiro.atividades.length} atividade(s) vinculada(s)`}
                            colunas={colunasAtividades}
                            linhas={dadosEscoteiro.atividades}
                            chave={(a) => a.crCod}
                            linhasPorPagina={10}
                        />
                    )}
                </Stack>
            )}

            <Dialog open={atividadePagar !== null} onClose={() => setAtividadePagar(null)} maxWidth="xs" fullWidth>
                <form onSubmit={handleConfirmarPagamento}>
                    <DialogTitle>Pagamento de Atividade — {atividadePagar?.atividadeNome}</DialogTitle>
                    <DialogContent>
                        <Stack spacing={2} sx={{ mt: 1 }}>
                            <Typography variant="body2">
                                Escoteiro: <strong>{atividadePagar?.escoteiroNome}</strong>
                                <br />
                                Valor da Atividade: <strong>{formatarMoeda(atividadePagar?.valorAtividade ?? 0)}</strong>
                                {Number(atividadePagar?.saldo ?? 0) > 0 && (
                                    <>
                                        <br />
                                        Saldo Pendente: <strong>{formatarMoeda(atividadePagar?.saldo ?? 0)}</strong>
                                    </>
                                )}
                            </Typography>

                            {jaEstaPaga && (
                                <Alert severity="warning">
                                    Este escoteiro já realizou o pagamento para esta atividade. Deseja registrar um novo pagamento
                                    duplicado/complementar?
                                </Alert>
                            )}

                            <TextField
                                label="Valor Recebido (R$)"
                                type="number"
                                slotProps={{ htmlInput: { step: '0.01', min: '0.01' } }}
                                value={valorRecebido}
                                onChange={(e) => {
                                    setValorRecebido(e.target.value)
                                    setConfirmouDivergencia(false)
                                }}
                                required
                                fullWidth
                            />

                            {temDivergencia && (
                                <Alert severity="warning">
                                    O valor informado ({formatarMoeda(valorNumerico)}) é diferente do valor pendente (
                                    {formatarMoeda(valorBase)}). Clique novamente em Confirmar para continuar.
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
                                slotProps={{ inputLabel: { shrink: true } }}
                                value={dataPagamento}
                                onChange={(e) => setDataPagamento(e.target.value)}
                                required
                                fullWidth
                            />
                        </Stack>
                    </DialogContent>
                    <DialogActions sx={{ px: 3, pb: 2 }}>
                        <BotaoCancelar rotulo="Cancelar" aoClicar={() => setAtividadePagar(null)} />
                        <BotaoOk
                            rotulo={
                                (jaEstaPaga && !confirmouDuplicado) || (temDivergencia && !confirmouDivergencia)
                                    ? 'Confirmar mesmo assim'
                                    : 'Confirmar Pagamento'
                            }
                            tipo="submit"
                            carregando={carregando}
                        />
                    </DialogActions>
                </form>
            </Dialog>

            <DialogoConfirmacao
                aberto={atividadeEstornar !== null}
                mensagem={`Deseja realmente estornar o pagamento de ${atividadeEstornar ? formatarMoeda(atividadeEstornar.valorPago) : ''} referente à atividade "${atividadeEstornar?.atividadeNome ?? ''}"? Esta ação não pode ser desfeita.`}
                aoConfirmar={handleConfirmarEstorno}
                aoCancelar={() => setAtividadeEstornar(null)}
                carregando={carregando}
            />

            <Dialog open={recibo !== null} onClose={() => setRecibo(null)} maxWidth="sm" fullWidth>
                <DialogTitle>Recibo de Pagamento de Atividade — {recibo?.numero}</DialogTitle>
                <DialogContent>
                    {recibo && (
                        <Box sx={{ p: 2, border: '1px solid #ccc', borderRadius: 1, bgcolor: '#fafafa' }}>
                            <Typography variant="subtitle1" gutterBottom sx={{ fontWeight: 'bold' }}>
                                Grupo de Escoteiros Guayporé 186°/SP — Recibo de Atividade
                            </Typography>
                            <Typography variant="body2"><strong>Escoteiro:</strong> {recibo.escoteiro} (Matrícula {recibo.matricula})</Typography>
                            <Typography variant="body2"><strong>CPF:</strong> {formatarCpf(recibo.cpf)}</Typography>
                            <Typography variant="body2"><strong>Atividade:</strong> {recibo.atividade}</Typography>
                            <Typography variant="body2">
                                <strong>Data da Atividade:</strong> {recibo.dataAtividade !== '—' ? formatarData(recibo.dataAtividade) : '—'}
                            </Typography>
                            <Typography variant="body2"><strong>Data do Pagamento:</strong> {formatarData(recibo.dataPagamento)}</Typography>
                            <Typography variant="body2"><strong>Valor da Atividade:</strong> {formatarMoeda(recibo.valorAtividade)}</Typography>
                            <Typography variant="body2"><strong>Valor Pago:</strong> {formatarMoeda(recibo.valorPago)}</Typography>
                            {recibo.observacao && (
                                <Typography variant="body2" color="warning.main" sx={{ mt: 1 }}>
                                    <strong>Observação:</strong> {recibo.observacao}
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