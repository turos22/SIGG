import { useState } from 'react'
import Alert from '@mui/material/Alert'
import Box from '@mui/material/Box'
import Tab from '@mui/material/Tab'
import Tabs from '@mui/material/Tabs'
import DetalheMensalidades from '@/components/mensalidades/DetalheMensalidades'
import MensalidadesPessoais from '@/components/mensalidades/MensalidadesPessoais'
import CampoInteiro from '@/components/formulario/CampoInteiro'
import CampoTexto from '@/components/formulario/CampoTexto'
import Formulario from '@/components/formulario/Formulario'
import { BotaoOk } from '@/components/formulario/Botoes'
import Grade, { type ColunaGrade } from '@/components/grade/Grade'
import { useSessao } from '@/data/contexts/SessaoContexto'
import type Escoteiro from '@/data/models/Escoteiro'
import type Responsavel from '@/data/models/Responsavel'
import type SaldoMensalidades from '@/data/models/SaldoMensalidades'
import {
  buscarSaldoDoEscoteiro,
  buscarSaldoDoResponsavel,
  lancarMensalidadesDoMes,
  pesquisarEscoteirosParaMensalidade,
} from '@/data/services/mensalidade'
import { buscarResponsaveis } from '@/data/services/membro'
import { AUTENTICACAO_ATIVA } from '@/data/services/sessao'
import { mensagemDeErro } from '@/data/utils/erros'
import { formatarCpf, formatarReferencia } from '@/data/utils/formatadores'
import { DIRETORIA } from '@/data/utils/permissoes'

type Aba = 'escoteiro' | 'responsavel'

const colunasEscoteiros: ColunaGrade<Escoteiro>[] = [
  { id: 'nome', titulo: 'Nome', valor: (e) => e.membro.nome },
  { id: 'cpf', titulo: 'CPF', valor: (e) => e.membro.CPF, renderizar: (e) => formatarCpf(e.membro.CPF) },
  { id: 'matricula', titulo: 'Matrícula', valor: (e) => e.esc_cod, largura: 120 },
  { id: 'patrulha', titulo: 'Patrulha', valor: (e) => e.patrulha.nome },
]

const colunasResponsaveis: ColunaGrade<Responsavel>[] = [
  { id: 'nome', titulo: 'Nome', valor: (r) => r.membro.nome },
  { id: 'cpf', titulo: 'CPF', valor: (r) => r.membro.CPF, renderizar: (r) => formatarCpf(r.membro.CPF) },
]

export default function PaginaMensalidades() {
  const { usuario } = useSessao()
  const hoje = new Date()
  const [aba, setAba] = useState<Aba>('escoteiro')
  const [busca, setBusca] = useState('')
  const [encontrados, setEncontrados] = useState<Escoteiro[]>([])
  const [responsaveis, setResponsaveis] = useState<Responsavel[]>([])
  const [memCodSelecionado, setMemCodSelecionado] = useState<number | null>(null)
  const [saldos, setSaldos] = useState<SaldoMensalidades[]>([])
  const [carregando, setCarregando] = useState(false)
  const [erro, setErro] = useState('')
  const [aviso, setAviso] = useState('')
  const [lancandoMes, setLancandoMes] = useState(false)
  const [mes, setMes] = useState<number | null>(hoje.getMonth() + 1)
  const [ano, setAno] = useState<number | null>(hoje.getFullYear())

  const limparResultado = () => {
    setMemCodSelecionado(null)
    setSaldos([])
    setErro('')
    setAviso('')
  }

  const carregarSaldoDoEscoteiro = async (memCod: number) => {
    setMemCodSelecionado(memCod)
    setCarregando(true)
    try {
      setSaldos([await buscarSaldoDoEscoteiro(memCod)])
      setErro('')
    } catch (falha) {
      setSaldos([])
      setErro(mensagemDeErro(falha, 'Não foi possível calcular as mensalidades.'))
    } finally {
      setCarregando(false)
    }
  }

  const carregarSaldoDoResponsavel = async (memCod: number) => {
    setMemCodSelecionado(memCod)
    setCarregando(true)
    setAviso('')
    try {
      const lista = await buscarSaldoDoResponsavel(memCod)
      setSaldos(lista)
      setErro('')
      if (lista.length === 0) setAviso('Este responsável não possui dependentes vinculados ao seu cadastro.')
    } catch (falha) {
      setSaldos([])
      setErro(mensagemDeErro(falha, 'Não foi possível calcular as mensalidades.'))
    } finally {
      setCarregando(false)
    }
  }

  const pesquisar = async () => {
    limparResultado()
    setEncontrados([])
    setCarregando(true)
    try {
      const lista = await pesquisarEscoteirosParaMensalidade(busca)
      setEncontrados(lista)
      if (lista.length === 1) await carregarSaldoDoEscoteiro(lista[0].membro.mem_cod)
    } catch (falha) {
      setErro(mensagemDeErro(falha, 'Não foi possível pesquisar o escoteiro.'))
    } finally {
      setCarregando(false)
    }
  }

  const trocarAba = async (nova: Aba) => {
    setAba(nova)
    limparResultado()
    if (nova !== 'responsavel' || responsaveis.length > 0) return
    setCarregando(true)
    try {
      setResponsaveis(await buscarResponsaveis())
    } catch (falha) {
      setErro(mensagemDeErro(falha, 'Não foi possível carregar os responsáveis.'))
    } finally {
      setCarregando(false)
    }
  }

  const lancarMes = async () => {
    if (mes === null || ano === null) return
    setLancandoMes(false)
    setCarregando(true)
    try {
      const resultado = await lancarMensalidadesDoMes(mes, ano)
      setErro('')
      setAviso(
        `Mensalidades de ${formatarReferencia(resultado.mes, resultado.ano)}: ${resultado.lancadas} lançadas e ${resultado.ignoradas} ignoradas (já lançadas ou isentas).`,
      )
    } catch (falha) {
      setAviso('')
      setErro(mensagemDeErro(falha, 'Não foi possível lançar as mensalidades.'))
    } finally {
      setCarregando(false)
    }
  }

  const diretoria = !AUTENTICACAO_ATIVA || (usuario?.perfis.some((perfil) => DIRETORIA.includes(perfil)) ?? false)
  const podeLancar = !AUTENTICACAO_ATIVA || (usuario?.perfis.some((perfil) => perfil === 'ADMIN' || perfil === 'FINANCEIRO') ?? false)

  if (!diretoria && usuario) return <MensalidadesPessoais usuario={usuario} />

  return (
    <main className="pagina">
      <header>
        <h1>Mensalidades</h1>
        <p className="subtitulo">Cálculo das mensalidades em aberto e histórico de pagamentos</p>
      </header>

      {erro && <Alert severity="error">{erro}</Alert>}
      {aviso && <Alert severity="info">{aviso}</Alert>}

      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 1 }}>
        <Tabs value={aba} onChange={(_, valor: Aba) => trocarAba(valor)}>
          <Tab value="escoteiro" label="Consultar escoteiro" />
          <Tab value="responsavel" label="Consultar responsável" />
        </Tabs>
        {podeLancar && (
          <BotaoOk rotulo="Lançar mensalidades do mês" tipo="button" aoClicar={() => setLancandoMes(true)} desabilitado={carregando} />
        )}
      </Box>

      {aba === 'escoteiro' && (
        <Box
          component="form"
          onSubmit={(evento) => {
            evento.preventDefault()
            pesquisar()
          }}
          sx={{ display: 'flex', gap: 1, alignItems: 'flex-start' }}
        >
          <CampoTexto rotulo="CPF, nome ou matrícula" valor={busca} aoMudar={setBusca} obrigatorio />
          <BotaoOk rotulo="Buscar" carregando={carregando} />
        </Box>
      )}

      {aba === 'escoteiro' && encontrados.length > 1 && (
        <Grade
          titulo="Escoteiros encontrados"
          subtitulo="Selecione o escoteiro para calcular as mensalidades"
          colunas={colunasEscoteiros}
          linhas={encontrados}
          chave={(e) => e.membro.mem_cod}
          chaveSelecionada={memCodSelecionado}
          aoClicarLinha={(e) => carregarSaldoDoEscoteiro(e.membro.mem_cod)}
          linhasPorPagina={5}
        />
      )}

      {aba === 'responsavel' && (
        <Grade
          titulo="Responsáveis"
          subtitulo="Selecione o responsável para calcular as mensalidades de seus dependentes"
          colunas={colunasResponsaveis}
          linhas={responsaveis}
          chave={(r) => r.membro.mem_cod}
          chaveSelecionada={memCodSelecionado}
          aoClicarLinha={(r) => carregarSaldoDoResponsavel(r.membro.mem_cod)}
          carregando={carregando}
          textoVazio="Nenhum responsável cadastrado."
          linhasPorPagina={5}
        />
      )}

      {saldos.map((saldo) => (
        <DetalheMensalidades key={saldo.esc_cod} saldo={saldo} />
      ))}

      <Formulario
        titulo="Lançar mensalidades do mês"
        aberto={lancandoMes}
        aoConfirmar={lancarMes}
        aoCancelar={() => setLancandoMes(false)}
        rotuloOk="Lançar"
        desabilitarOk={mes === null || ano === null}
      >
        <CampoInteiro rotulo="Mês" valor={mes} aoMudar={setMes} minimo={1} maximo={12} obrigatorio />
        <CampoInteiro rotulo="Ano" valor={ano} aoMudar={setAno} minimo={2000} maximo={2100} obrigatorio />
      </Formulario>
    </main>
  )
}
