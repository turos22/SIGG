import { useEffect, useState } from 'react'
import Alert from '@mui/material/Alert'
import Box from '@mui/material/Box'
import Dialog from '@mui/material/Dialog'
import DialogActions from '@mui/material/DialogActions'
import DialogContent from '@mui/material/DialogContent'
import DialogTitle from '@mui/material/DialogTitle'
import Stack from '@mui/material/Stack'
import TextField from '@mui/material/TextField'
import Grade, { type ColunaGrade } from '@/components/grade/Grade'
import { BotaoCancelar, BotaoOk } from '@/components/formulario/Botoes'
import DialogoConfirmacao from '@/components/formulario/DialogoConfirmacao'
import type CategoriaMensalidade from '@/data/models/CategoriaMensalidade'
import { ErroDeApi } from '@/data/services/api'
import {
    buscarCategoriasMensalidade,
    cadastrarCategoriaMensalidade,
    alterarCategoriaMensalidade,
    apagarCategoriaMensalidade,
} from '@/data/services/categoriasMensalidade'

const colunas: ColunaGrade<CategoriaMensalidade>[] = [
    { id: 'codigo', titulo: 'Código', valor: (c) => c.cat_mens_cod },
    { id: 'descricao', titulo: 'Descrição', valor: (c) => c.descricao },
    {
        id: 'valor',
        titulo: 'Valor',
        valor: (c) => c.valor,
        renderizar: (c) => `R$ ${Number(c.valor).toFixed(2)}`,
    },
    { id: 'qtd_irmao', titulo: 'Qtd. Irmãos', valor: (c) => c.qtd_irmao },
]

function extrairMensagemErro(err: unknown, padrao: string): string {
    if (err instanceof ErroDeApi) {
        try {
            const obj = JSON.parse(err.message)
            return obj.erro || obj.message || err.message
        } catch {
            return err.message || `O backend respondeu ${err.status}.`
        }
    }
    return padrao
}

export default function PaginaCategoriasMensalidade() {
    const [categorias, setCategorias] = useState<CategoriaMensalidade[]>([])
    const [carregando, setCarregando] = useState(true)
    const [erro, setErro] = useState('')
    const [sucesso, setSucesso] = useState('')

    const [selecionada, setSelecionada] = useState<CategoriaMensalidade | null>(null)
    const [modalAberto, setModalAberto] = useState(false)
    const [confirmandoExclusao, setConfirmandoExclusao] = useState(false)

    const [descricao, setDescricao] = useState('')
    const [valor, setValor] = useState('')
    const [qtdIrmao, setQtdIrmao] = useState('0')
    const [filtro, setFiltro] = useState('')

    const carregar = async (termo = filtro) => {
        setCarregando(true)
        try {
            const lista = await buscarCategoriasMensalidade(termo)
            setCategorias(lista)
            setErro('')
        } catch (err) {
            setErro(extrairMensagemErro(err, 'Não foi possível carregar as categorias de mensalidade.'))
        } finally {
            setCarregando(false)
        }
    }

    // Carga inicial. Não chama carregar() aqui: ele faz setCarregando(true) de forma síncrona dentro do efeito
    // (o estado já nasce true). O estado só é atualizado quando a resposta chega.
    useEffect(() => {
        let ativo = true // evita atualizar o estado depois que a página foi fechada
        buscarCategoriasMensalidade('')
            .then((lista) => {
                if (ativo) {
                    setCategorias(lista)
                    setErro('')
                }
            })
            .catch((err: unknown) => {
                if (ativo) setErro(extrairMensagemErro(err, 'Não foi possível carregar as categorias de mensalidade.'))
            })
            .finally(() => {
                if (ativo) setCarregando(false)
            })
        return () => {
            ativo = false
        }
    }, [])

    const abrirNova = () => {
        setSelecionada(null)
        setDescricao('')
        setValor('')
        setQtdIrmao('0')
        setErro('')
        setSucesso('')
        setModalAberto(true)
    }

    const abrirEdicao = (item: CategoriaMensalidade) => {
        setSelecionada(item)
        setDescricao(item.descricao)
        setValor(item.valor.toString())
        setQtdIrmao(item.qtd_irmao.toString())
        setErro('')
        setSucesso('')
        setModalAberto(true)
    }

    const handleSalvar = async (e: React.FormEvent) => {
        e.preventDefault()
        setErro('')
        setSucesso('')

        try {
            if (selecionada) {
                await alterarCategoriaMensalidade(selecionada.cat_mens_cod, {
                    cat_mens_cod: selecionada.cat_mens_cod,
                    descricao,
                    valor: parseFloat(valor),
                    qtd_irmao: parseInt(qtdIrmao, 10) || 0,
                })
                setSucesso('Categoria atualizada com sucesso!')
            } else {
                await cadastrarCategoriaMensalidade({
                    descricao,
                    valor: parseFloat(valor),
                    qtd_irmao: parseInt(qtdIrmao, 10) || 0,
                })
                setSucesso('Categoria cadastrada com sucesso!')
            }
            setModalAberto(false)
            setSelecionada(null)
            await carregar()
        } catch (err) {
            setErro(extrairMensagemErro(err, 'Erro inesperado ao salvar categoria.'))
        }
    }

    const handleExcluir = async () => {
        if (!selecionadoValido()) return
        const alvo = selecionada!
        setConfirmandoExclusao(false)
        setErro('')
        setSucesso('')

        try {
            await apagarCategoriaMensalidade(alvo.cat_mens_cod)
            setSucesso('Categoria excluída com sucesso!')
            setSelecionada(null)
            await carregar()
        } catch (err) {
            setErro(extrairMensagemErro(err, 'Erro ao excluir categoria.'))
        }
    }

    const selecionadoValido = () => selecionada !== null

    return (
        <main className="pagina">
            <header>
                <h1>Categorias de Mensalidade</h1>
                <p className="subtitulo">Configuração de faixas de mensalidade e descontos por irmãos</p>
            </header>

            {erro && <Alert severity="error" sx={{ mb: 2 }}>{erro}</Alert>}
            {sucesso && <Alert severity="success" sx={{ mb: 2 }}>{sucesso}</Alert>}

            <Box
                component="form"
                onSubmit={(e) => {
                    e.preventDefault()
                    carregar(filtro)
                }}
                sx={{ display: 'flex', gap: 1.5, mb: 2, alignItems: 'center' }}
            >
                <TextField
                    size="small"
                    label="Filtrar por descrição"
                    value={filtro}
                    onChange={(e) => setFiltro(e.target.value)}
                />
                <BotaoOk rotulo="Buscar" tipo="submit" />
                {filtro && (
                    <BotaoCancelar
                        rotulo="Limpar"
                        aoClicar={() => {
                            setFiltro('')
                            carregar('')
                        }}
                    />
                )}
            </Box>

            <Dialog open={modalAberto} onClose={() => setModalAberto(false)} maxWidth="xs" fullWidth>
                <form onSubmit={handleSalvar}>
                    <DialogTitle>{selecionada ? 'Editar Categoria de Mensalidade' : 'Nova Categoria de Mensalidade'}</DialogTitle>
                    <DialogContent>
                        <Stack spacing={2} sx={{ mt: 1 }}>
                            <TextField
                                label="Descrição"
                                value={descricao}
                                onChange={(e) => setDescricao(e.target.value)}
                                required
                                fullWidth
                            />
                            <TextField
                                label="Valor (R$)"
                                type="number"
                                slotProps={{ htmlInput: { step: '0.01', min: '0' } }}
                                value={valor}
                                onChange={(e) => setValor(e.target.value)}
                                required
                                fullWidth
                            />
                            <TextField
                                label="Quantidade de Irmãos"
                                type="number"
                                slotProps={{ htmlInput: { step: '1', min: '0' } }}
                                value={qtdIrmao}
                                onChange={(e) => setQtdIrmao(e.target.value)}
                                required
                                fullWidth
                            />
                        </Stack>
                    </DialogContent>
                    <DialogActions sx={{ px: 3, pb: 2 }}>
                        <BotaoCancelar rotulo="Cancelar" aoClicar={() => setModalAberto(false)} />
                        <BotaoOk rotulo={selecionada ? 'Atualizar' : 'Cadastrar'} tipo="submit" />
                    </DialogActions>
                </form>
            </Dialog>

            <DialogoConfirmacao
                aberto={confirmandoExclusao}
                mensagem={`Deseja realmente excluir a categoria "${selecionada?.descricao ?? ''}"?`}
                aoConfirmar={handleExcluir}
                aoCancelar={() => setConfirmandoExclusao(false)}
                carregando={false}
            />

            <Grade
                titulo="Categorias cadastradas"
                subtitulo={`${categorias.length} ${categorias.length === 1 ? 'categoria' : 'categorias'}`}
                colunas={colunas}
                linhas={categorias}
                chave={(c) => c.cat_mens_cod}
                carregando={carregando}
                textoVazio="Nenhuma categoria de mensalidade encontrada."
                aoClicarLinha={(linha) => setSelecionada(linha)}
                aoDuploClique={(linha) => abrirEdicao(linha)}
                chaveSelecionada={selecionada?.cat_mens_cod ?? null}
                linhasPorPagina={10}
                rodape={
                    <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: '10px' }}>
                        <BotaoOk rotulo="Adicionar Categoria" aoClicar={abrirNova} tipo="button" />
                        <BotaoOk
                            rotulo="Editar"
                            aoClicar={() => selecionada && abrirEdicao(selecionada)}
                            desabilitado={selecionada === null}
                            tipo="button"
                        />
                        <BotaoCancelar
                            rotulo="Excluir"
                            aoClicar={() => setConfirmandoExclusao(true)}
                            desabilitado={selecionada === null}
                        />
                    </Box>
                }
            />
        </main>
    )
}