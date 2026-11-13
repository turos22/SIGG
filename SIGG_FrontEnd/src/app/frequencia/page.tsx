import { useEffect, useState } from 'react'
import Alert from '@mui/material/Alert'
import Box from '@mui/material/Box'
import { BotaoCancelar, BotaoOk } from '@/components/formulario/Botoes'
import CampoInteiro from '@/components/formulario/CampoInteiro'
import CampoSelecao from '@/components/formulario/CampoSelecao'
import DialogoConfirmacao from '@/components/formulario/DialogoConfirmacao'
import FormChamada from '@/components/frequencia/FormChamada'
import Grade, { type ColunaGrade } from '@/components/grade/Grade'
import { useSessao } from '@/data/contexts/SessaoContexto'
import type { ResumoChamada } from '@/data/models/Chamada'
import type Tropa from '@/data/models/Tropa'
import {
  atualizarChamada, buscarMinhaTropa, excluirChamada, listarChamadas, registrarChamada, type ItemParaGravar,
} from '@/data/services/frequencia'
import { AUTENTICACAO_ATIVA } from '@/data/services/sessao'
import { listarTropas } from '@/data/services/tropa'
import { formatarData } from '@/data/utils/datas'
import { mensagemDeErro } from '@/data/utils/erros'

const TODAS = 0
const ANO_ATUAL = new Date().getFullYear()

const colunas: ColunaGrade<ResumoChamada>[] = [
  { id: 'data', titulo: 'Data', valor: (c) => c.data, renderizar: (c) => formatarData(c.data) },
  { id: 'tropa', titulo: 'Tropa', valor: (c) => c.tropa.nome },
  { id: 'presentes', titulo: 'Presentes', valor: (c) => c.presentes, renderizar: (c) => `${c.presentes} de ${c.total}` },
]

const chaveDe = (c: ResumoChamada) => `${c.tropa.cod}|${c.data}`

export default function PaginaFrequencia() {
  const { usuario } = useSessao()
  // O chefe só registra a tropa dele (o combo vem travado); presidente e admin escolhem qualquer uma
  const restrito = AUTENTICACAO_ATIVA && !usuario?.perfis.some((p) => p === 'PRESIDENTE' || p === 'ADMIN')
  const [tropas, setTropas] = useState<Tropa[]>([])
  const [tropaCod, setTropaCod] = useState<number>(TODAS)
  const [ano, setAno] = useState<number | null>(ANO_ATUAL)
  const [chamadas, setChamadas] = useState<ResumoChamada[]>([])
  const [selecionada, setSelecionada] = useState<string | null>(null)
  const [formulario, setFormulario] = useState<'nova' | 'edicao' | null>(null)
  const [excluindo, setExcluindo] = useState(false)
  const [pronto, setPronto] = useState(false)
  const [carregando, setCarregando] = useState(true)
  const [salvando, setSalvando] = useState(false)
  const [erro, setErro] = useState('')
  const [aviso, setAviso] = useState('')

  const tropa = tropas.find((t) => t.cod === tropaCod) ?? null
  const chamada = chamadas.find((c) => chaveDe(c) === selecionada) ?? null

  useEffect(() => {
    let ativo = true
    const trava = restrito ? buscarMinhaTropa().then((t) => t.cod) : Promise.resolve(TODAS)
    Promise.all([listarTropas(), trava])
      .then(([lista, inicial]) => {
        if (!ativo) return
        setTropas(lista)
        setTropaCod(inicial)
        setPronto(true)
      })
      .catch((falha: unknown) => {
        if (!ativo) return
        setErro(mensagemDeErro(falha, 'Não foi possível carregar as tropas.'))
        setCarregando(false)
      })
    return () => {
      ativo = false
    }
  }, [restrito])

  useEffect(() => {
    if (!pronto) return
    let ativo = true
    listarChamadas(tropaCod === TODAS ? undefined : tropaCod, ano ?? ANO_ATUAL)
      .then((lista) => ativo && setChamadas(lista))
      .catch((falha: unknown) => ativo && setErro(mensagemDeErro(falha, 'Não foi possível carregar as chamadas.')))
      .finally(() => ativo && setCarregando(false))
    return () => {
      ativo = false
    }
  }, [pronto, tropaCod, ano])

  async function executar(acao: () => Promise<unknown>, sucesso: string, falhaPadrao: string) {
    setSalvando(true)
    setErro('')
    setAviso('')
    try {
      await acao()
      setChamadas(await listarChamadas(tropaCod === TODAS ? undefined : tropaCod, ano ?? ANO_ATUAL))
      setAviso(sucesso)
      setFormulario(null)
    } catch (falha) {
      setErro(mensagemDeErro(falha, falhaPadrao))
    } finally {
      setSalvando(false)
      setExcluindo(false)
    }
  }

  const salvar = (data: string, itens: ItemParaGravar[]) => {
    const alvo = formulario === 'edicao' && chamada ? chamada.tropa.cod : tropaCod
    return executar(
      () => (formulario === 'edicao' ? atualizarChamada(alvo, data, itens) : registrarChamada(alvo, data, itens)),
      'Frequência registrada.',
      'Não foi possível registrar a frequência.',
    )
  }

  const excluir = () => chamada
    ? executar(() => excluirChamada(chamada.tropa.cod, chamada.data), 'Chamada excluída.', 'Não foi possível excluir a chamada.')
    : Promise.resolve()

  const tropaDoFormulario = formulario === 'edicao' ? chamada?.tropa ?? null : tropa

  return (
    <main className="pagina">
      <header>
        <h1>Registro de frequência</h1>
        <p className="subtitulo">Chamada por tropa e data: um registro por escoteiro em cada dia</p>
      </header>

      {erro && <Alert severity="error" onClose={() => setErro('')}>{erro}</Alert>}
      {aviso && <Alert severity="success" onClose={() => setAviso('')}>{aviso}</Alert>}

      <Box sx={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 320px))', gap: 2 }}>
        <CampoSelecao
          rotulo="Tropa"
          valor={tropaCod}
          opcoes={[...(restrito ? [] : [{ valor: TODAS, rotulo: 'Todas as tropas' }]), ...tropas.map((t) => ({ valor: t.cod, rotulo: t.nome }))]}
          aoMudar={(cod) => setTropaCod(cod ?? TODAS)}
          desabilitado={restrito}
          ajuda={restrito ? 'Você registra a frequência da sua tropa' : undefined}
        />
        <CampoInteiro rotulo="Ano" valor={ano} aoMudar={setAno} minimo={2000} maximo={2100} />
      </Box>

      {formulario && tropaDoFormulario && (
        <FormChamada
          tropa={tropaDoFormulario}
          dataInicial={formulario === 'edicao' ? chamada?.data : undefined}
          aoConfirmar={salvar}
          aoCancelar={() => setFormulario(null)}
          carregando={salvando}
        />
      )}

      <DialogoConfirmacao
        aberto={excluindo}
        mensagem={<>Excluir a chamada de <strong>{chamada?.tropa.nome}</strong> em <strong>{chamada ? formatarData(chamada.data) : ''}</strong>?</>}
        aoConfirmar={excluir}
        aoCancelar={() => setExcluindo(false)}
        carregando={salvando}
      />

      <Grade
        titulo="Chamadas registradas"
        subtitulo={`${chamadas.length} ${chamadas.length === 1 ? 'chamada' : 'chamadas'}`}
        colunas={colunas}
        linhas={chamadas}
        chave={chaveDe}
        carregando={carregando}
        textoVazio="Nenhuma chamada registrada neste período."
        aoClicarLinha={(c) => setSelecionada(chaveDe(c))}
        aoDuploClique={(c) => {
          setSelecionada(chaveDe(c))
          setFormulario('edicao')
        }}
        chaveSelecionada={selecionada}
        linhasPorPagina={10}
        rodape={(
          <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: '10px', flexWrap: 'wrap' }}>
            <BotaoOk rotulo="Nova chamada" tipo="button" desabilitado={!tropa} aoClicar={() => setFormulario('nova')} />
            <BotaoOk rotulo="Editar" tipo="button" desabilitado={!chamada} aoClicar={() => setFormulario('edicao')} />
            <BotaoCancelar rotulo="Excluir" desabilitado={!chamada} aoClicar={() => setExcluindo(true)} />
          </Box>
        )}
      />
      {!tropa && !restrito && <p className="subtitulo">Escolha uma tropa para fazer uma nova chamada.</p>}
    </main>
  )
}
