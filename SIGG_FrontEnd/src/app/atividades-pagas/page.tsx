import { useEffect, useState, type FormEvent } from 'react'
import Alert from '@mui/material/Alert'
import Box from '@mui/material/Box'
import Chip from '@mui/material/Chip'
import Dialog from '@mui/material/Dialog'
import DialogActions from '@mui/material/DialogActions'
import DialogContent from '@mui/material/DialogContent'
import DialogTitle from '@mui/material/DialogTitle'
import FormControlLabel from '@mui/material/FormControlLabel'
import Stack from '@mui/material/Stack'
import Switch from '@mui/material/Switch'
import Grade, { type ColunaGrade } from '@/components/grade/Grade'
import { BotaoCancelar, BotaoOk } from '@/components/formulario/Botoes'
import CampoData from '@/components/formulario/CampoData'
import CampoInteiro from '@/components/formulario/CampoInteiro'
import CampoMoeda from '@/components/formulario/CampoMoeda'
import CampoTexto from '@/components/formulario/CampoTexto'
import CampoTextoLongo from '@/components/formulario/CampoTextoLongo'
import type AtividadePaga from '@/data/models/AtividadePaga'
import { buscarAtividadesPagas, ofertarAtividadePaga } from '@/data/services/atividadesPagas'
import { formatarData } from '@/data/utils/datas'
import { mensagemDeErro } from '@/data/utils/erros'
import { formatarMoeda } from '@/data/utils/formatadores'
import { lerMoeda } from '@/data/utils/validadores'
import {
  errosDoBackendAtividadePaga,
  validarAtividadePaga,
  type CampoAtividadePaga,
  type DadosFormAtividadePaga,
  type ErrosAtividadePaga,
} from '@/data/utils/validacaoAtividadePaga'

// Data de hoje no fuso do navegador, no formato do backend ("AAAA-MM-DD")
function hojeIso(): string {
  const agora = new Date()
  const mes = String(agora.getMonth() + 1).padStart(2, '0')
  const dia = String(agora.getDate()).padStart(2, '0')
  return `${agora.getFullYear()}-${mes}-${dia}`
}

function estaAberta(atividade: AtividadePaga): boolean {
  return (atividade.prazoInteresse ?? atividade.data) >= hojeIso()
}

const FORM_VAZIO: DadosFormAtividadePaga = {
  nome: '',
  descricao: '',
  data: '',
  prazoInteresse: '',
  local: '',
  valor: '',
  vagas: null,
}

const colunas: ColunaGrade<AtividadePaga>[] = [
  { id: 'nome', titulo: 'Atividade', valor: (a) => a.nome },
  { id: 'data', titulo: 'Data', valor: (a) => a.data, renderizar: (a) => formatarData(a.data) },
  { id: 'local', titulo: 'Local', valor: (a) => a.local ?? '—' },
  { id: 'valor', titulo: 'Valor', valor: (a) => a.valor, renderizar: (a) => formatarMoeda(a.valor), alinhamento: 'right' },
  { id: 'vagas', titulo: 'Vagas', valor: (a) => a.vagas, renderizar: (a) => a.vagas ?? '—', alinhamento: 'center' },
  {
    id: 'prazo',
    titulo: 'Interesse até',
    valor: (a) => a.prazoInteresse,
    renderizar: (a) => (a.prazoInteresse ? formatarData(a.prazoInteresse) : '—'),
  },
  {
    id: 'situacao',
    titulo: 'Situação',
    valor: (a) => (estaAberta(a) ? 'Aberta' : 'Encerrada'),
    renderizar: (a) =>
      estaAberta(a) ? <Chip size="small" color="success" label="Aberta" /> : <Chip size="small" label="Encerrada" />,
  },
]

export default function PaginaAtividadesPagas() {
  const [atividades, setAtividades] = useState<AtividadePaga[]>([])
  const [somenteAbertas, setSomenteAbertas] = useState(false)
  const [carregando, setCarregando] = useState(true)
  const [erro, setErro] = useState('')
  const [sucesso, setSucesso] = useState('')

  const [modalAberto, setModalAberto] = useState(false)
  const [salvando, setSalvando] = useState(false)
  const [erroModal, setErroModal] = useState('')
  const [dados, setDados] = useState<DadosFormAtividadePaga>(FORM_VAZIO)
  // Padrão de validação do Bootstrap: os erros aparecem ao tentar enviar e, a partir daí, acompanham a digitação
  const [validado, setValidado] = useState(false)
  const [errosBackend, setErrosBackend] = useState<ErrosAtividadePaga>({})

  // Passo 2: busca e exibe as atividades pagas já cadastradas
  useEffect(() => {
    let ativo = true
    buscarAtividadesPagas(somenteAbertas)
      .then((lista) => {
        if (!ativo) return
        setAtividades(lista)
        setErro('')
      })
      .catch((falha: unknown) => ativo && setErro(mensagemDeErro(falha, 'Não foi possível carregar as atividades pagas.')))
      .finally(() => ativo && setCarregando(false))
    return () => {
      ativo = false
    }
  }, [somenteAbertas])

  async function recarregar() {
    try {
      setAtividades(await buscarAtividadesPagas(somenteAbertas))
    } catch (falha) {
      setErro(mensagemDeErro(falha, 'Não foi possível carregar as atividades pagas.'))
    }
  }

  // Passos 3 e 4: solicita o cadastro e apresenta os campos
  function abrirNovaOferta() {
    setDados(FORM_VAZIO)
    setValidado(false)
    setErrosBackend({})
    setErroModal('')
    setSucesso('')
    setModalAberto(true)
  }

  // Passos 5 e 6: confirma a oferta; o backend valida e registra
  async function ofertar(evento: FormEvent) {
    evento.preventDefault()
    setErroModal('')
    setErrosBackend({})
    setValidado(true)
    if (Object.keys(validarAtividadePaga(dados, hojeIso())).length > 0) return
    setSalvando(true)
    try {
      const nova = await ofertarAtividadePaga({
        nome: dados.nome.trim(),
        descricao: dados.descricao.trim() || null,
        data: dados.data,
        local: dados.local.trim(),
        valor: lerMoeda(dados.valor) ?? 0,
        vagas: dados.vagas,
        prazoInteresse: dados.prazoInteresse,
      })
      setModalAberto(false)
      setSucesso(`Atividade "${nova.nome}" ofertada com sucesso! Ela já está disponível para manifestação de interesse.`)
      await recarregar()
    } catch (falha) {
      // Fluxos alternativos 6.1 e 6.3: mostra a mensagem e continua no formulário (passo 4)
      const mensagem = mensagemDeErro(falha, 'Não foi possível ofertar a atividade.')
      const porCampo = errosDoBackendAtividadePaga(mensagem)
      if (porCampo) setErrosBackend(porCampo)
      else setErroModal(mensagem)
    } finally {
      setSalvando(false)
    }
  }

  const hoje = hojeIso()
  const erros: ErrosAtividadePaga = { ...(validado ? validarAtividadePaga(dados, hoje) : {}), ...errosBackend }

  function mudar<C extends CampoAtividadePaga>(campo: C) {
    return (valor: DadosFormAtividadePaga[C]) => {
      setDados((atual) => ({ ...atual, [campo]: valor }))
      // o erro do backend (ex.: atividade repetida) some quando o campo é corrigido
      if (errosBackend.nome || errosBackend.data) setErrosBackend({})
    }
  }

  return (
    <main className="pagina">
      <header>
        <h1>Atividades pagas</h1>
        <p className="subtitulo">Oferta de novas atividades pagas (acampamentos, passeios, eventos) para manifestação de interesse dos escoteiros</p>
      </header>

      {erro && <Alert severity="error" sx={{ mb: 2 }}>{erro}</Alert>}
      {sucesso && <Alert severity="success" sx={{ mb: 2 }} onClose={() => setSucesso('')}>{sucesso}</Alert>}

      <FormControlLabel
        sx={{ mb: 1 }}
        control={<Switch checked={somenteAbertas} onChange={(e) => setSomenteAbertas(e.target.checked)} />}
        label="Mostrar só as abertas para manifestação de interesse"
      />

      <Grade
        titulo="Atividades pagas cadastradas"
        subtitulo={`${atividades.length} ${atividades.length === 1 ? 'atividade' : 'atividades'}`}
        colunas={colunas}
        linhas={atividades}
        chave={(a) => a.codigo}
        carregando={carregando}
        textoVazio="Nenhuma atividade paga encontrada."
        linhasPorPagina={10}
        rodape={
          <Box sx={{ display: 'flex', justifyContent: 'flex-end' }}>
            <BotaoOk rotulo="Ofertar nova atividade" tipo="button" aoClicar={abrirNovaOferta} />
          </Box>
        }
      />

      <Dialog open={modalAberto} onClose={() => !salvando && setModalAberto(false)} maxWidth="sm" fullWidth>
        <form onSubmit={ofertar} noValidate>
          <DialogTitle>Ofertar nova atividade paga</DialogTitle>
          <DialogContent>
            <Stack spacing={2} sx={{ mt: 1 }}>
              {erroModal && <Alert severity="error">{erroModal}</Alert>}
              <CampoTexto rotulo="Nome" valor={dados.nome} aoMudar={mudar('nome')} tamanhoMaximo={30} obrigatorio autoFocus erro={erros.nome} />
              <CampoTextoLongo
                rotulo="Descrição"
                valor={dados.descricao}
                aoMudar={mudar('descricao')}
                tamanhoMaximo={200}
                linhas={2}
                erro={erros.descricao}
              />
              <Box sx={{ display: 'grid', gap: 2, gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr' } }}>
                <CampoData rotulo="Data de realização" valor={dados.data} aoMudar={mudar('data')} minimo={hoje} obrigatorio erro={erros.data} />
                <CampoData
                  rotulo="Interesse até"
                  valor={dados.prazoInteresse}
                  aoMudar={mudar('prazoInteresse')}
                  minimo={hoje}
                  maximo={dados.data || undefined}
                  obrigatorio
                  erro={erros.prazoInteresse}
                  ajuda="Prazo final para manifestação de interesse"
                />
              </Box>
              <CampoTexto rotulo="Local" valor={dados.local} aoMudar={mudar('local')} tamanhoMaximo={100} obrigatorio erro={erros.local} />
              <Box sx={{ display: 'grid', gap: 2, gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr' } }}>
                <CampoMoeda rotulo="Valor" valor={dados.valor} aoMudar={mudar('valor')} obrigatorio erro={erros.valor} />
                <CampoInteiro rotulo="Número de vagas" valor={dados.vagas} aoMudar={mudar('vagas')} obrigatorio erro={erros.vagas} />
              </Box>
            </Stack>
          </DialogContent>
          <DialogActions sx={{ px: 3, pb: 2 }}>
            <BotaoCancelar aoClicar={() => setModalAberto(false)} desabilitado={salvando} />
            <BotaoOk rotulo="Confirmar oferta" carregando={salvando} />
          </DialogActions>
        </form>
      </Dialog>
    </main>
  )
}
