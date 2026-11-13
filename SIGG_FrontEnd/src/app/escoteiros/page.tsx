import { useEffect, useState } from 'react'
import Alert from '@mui/material/Alert'
import Box from '@mui/material/Box'
import { BotaoOk } from '@/components/formulario/Botoes'
import CampoSelecao from '@/components/formulario/CampoSelecao'
import FormEscoteiro, { type PedidoEscoteiro } from '@/components/escoteiros/FormEscoteiro'
import Grade, { type ColunaGrade } from '@/components/grade/Grade'
import type CategoriaMensalidade from '@/data/models/CategoriaMensalidade'
import type Escoteiro from '@/data/models/Escoteiro'
import type Membro from '@/data/models/Membro'
import type Tropa from '@/data/models/Tropa'
import { alterarEscoteiro, listarEscoteiros, tornarEscoteiro } from '@/data/services/escoteiro'
import { buscarCategoriasMensalidade } from '@/data/services/categoriasMensalidade'
import { listarMembros } from '@/data/services/membro'
import { listarTropas } from '@/data/services/tropa'
import { mensagemDeErro } from '@/data/utils/erros'

const TODAS = 0

// Cadastro de escoteiro: separado do cadastro da pessoa. Aqui se escolhe categoria, tropa e patrulha.
export default function PaginaEscoteiros() {
  const [escoteiros, setEscoteiros] = useState<Escoteiro[]>([])
  const [tropas, setTropas] = useState<Tropa[]>([])
  const [categorias, setCategorias] = useState<CategoriaMensalidade[]>([])
  const [pessoas, setPessoas] = useState<Membro[]>([])
  const [filtroTropa, setFiltroTropa] = useState<number>(TODAS)
  const [selecionadoCod, setSelecionadoCod] = useState<number | null>(null)
  const [formulario, setFormulario] = useState<'novo' | 'edicao' | null>(null)
  const [carregando, setCarregando] = useState(true)
  const [salvando, setSalvando] = useState(false)
  const [erro, setErro] = useState('')
  const [aviso, setAviso] = useState('')

  const selecionado = escoteiros.find((e) => e.membro.mem_cod === selecionadoCod) ?? null

  const colunas: ColunaGrade<Escoteiro>[] = [
    { id: 'nome', titulo: 'Nome', valor: (e) => e.membro.nome },
    { id: 'tropa', titulo: 'Tropa', valor: (e) => e.patrulha.tropa?.nome },
    { id: 'patrulha', titulo: 'Patrulha', valor: (e) => e.patrulha.nome },
    {
      id: 'categoria',
      titulo: 'Categoria',
      valor: (e) => categorias.find((c) => c.cat_mens_cod === e.cat_mensalidade)?.descricao ?? String(e.cat_mensalidade),
    },
  ]

  useEffect(() => {
    let ativo = true
    Promise.all([listarTropas(), buscarCategoriasMensalidade()])
      .then(([listaTropas, listaCategorias]) => {
        if (!ativo) return
        setTropas(listaTropas)
        setCategorias(listaCategorias)
      })
      .catch((falha: unknown) => ativo && setErro(mensagemDeErro(falha, 'Não foi possível carregar tropas e categorias.')))
    return () => {
      ativo = false
    }
  }, [])

  useEffect(() => {
    let ativo = true
    listarEscoteiros(filtroTropa === TODAS ? undefined : filtroTropa)
      .then((lista) => ativo && setEscoteiros(lista))
      .catch((falha: unknown) => ativo && setErro(mensagemDeErro(falha, 'Não foi possível carregar os escoteiros.')))
      .finally(() => ativo && setCarregando(false))
    return () => {
      ativo = false
    }
  }, [filtroTropa])

  async function abrirNovo() {
    setErro('')
    try {
      const todas = await listarMembros()
      setPessoas(todas.filter((m) => !m.papeis.includes('Escoteiro')))
      setFormulario('novo')
    } catch (falha) {
      setErro(mensagemDeErro(falha, 'Não foi possível carregar as pessoas.'))
    }
  }

  async function salvar(pedido: PedidoEscoteiro) {
    setSalvando(true)
    setErro('')
    setAviso('')
    const dados = { cat_mensalidade: pedido.categoria, patrulha: { cod: pedido.patrulha } }
    try {
      if (formulario === 'edicao') await alterarEscoteiro(pedido.memCod, dados)
      else await tornarEscoteiro(pedido.memCod, dados)
      setEscoteiros(await listarEscoteiros(filtroTropa === TODAS ? undefined : filtroTropa))
      setAviso(formulario === 'edicao' ? 'Escoteiro alterado.' : 'Escoteiro cadastrado.')
      setFormulario(null)
    } catch (falha) {
      setErro(mensagemDeErro(falha, 'Não foi possível salvar o escoteiro.'))
    } finally {
      setSalvando(false)
    }
  }

  return (
    <main className="pagina">
      <header>
        <h1>Escoteiros</h1>
        <p className="subtitulo">Pessoas que fazem parte de uma tropa e de uma patrulha</p>
      </header>

      {erro && <Alert severity="error" onClose={() => setErro('')}>{erro}</Alert>}
      {aviso && <Alert severity="success" onClose={() => setAviso('')}>{aviso}</Alert>}

      <Box sx={{ maxWidth: 320 }}>
        <CampoSelecao
          rotulo="Tropa"
          valor={filtroTropa}
          opcoes={[{ valor: TODAS, rotulo: 'Todas as tropas' }, ...tropas.map((t) => ({ valor: t.cod, rotulo: t.nome }))]}
          aoMudar={(cod) => setFiltroTropa(cod ?? TODAS)}
        />
      </Box>

      {formulario && (
        <FormEscoteiro
          escoteiro={formulario === 'edicao' ? (selecionado ?? undefined) : undefined}
          pessoas={pessoas}
          tropas={tropas}
          categorias={categorias}
          aoConfirmar={salvar}
          aoCancelar={() => setFormulario(null)}
          carregando={salvando}
        />
      )}

      <Grade
        titulo="Escoteiros ativos"
        subtitulo={`${escoteiros.length} ${escoteiros.length === 1 ? 'escoteiro' : 'escoteiros'}`}
        colunas={colunas}
        linhas={escoteiros}
        chave={(e) => e.membro.mem_cod}
        carregando={carregando}
        textoVazio="Nenhum escoteiro encontrado."
        aoClicarLinha={(e) => setSelecionadoCod(e.membro.mem_cod)}
        aoDuploClique={(e) => {
          setSelecionadoCod(e.membro.mem_cod)
          setFormulario('edicao')
        }}
        chaveSelecionada={selecionadoCod}
        linhasPorPagina={10}
        rodape={(
          <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: '10px' }}>
            <BotaoOk rotulo="Novo escoteiro" tipo="button" aoClicar={abrirNovo} />
            <BotaoOk rotulo="Editar" tipo="button" desabilitado={!selecionado} aoClicar={() => setFormulario('edicao')} />
          </Box>
        )}
      />
    </main>
  )
}
