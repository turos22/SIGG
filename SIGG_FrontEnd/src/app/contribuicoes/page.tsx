import { useEffect, useState } from 'react'
import Alert from '@mui/material/Alert'
import Box from '@mui/material/Box'
import ResumoVerificacao from '@/components/contribuicoes/ResumoVerificacao'
import CampoCpf from '@/components/formulario/CampoCpf'
import DialogoConfirmacao from '@/components/formulario/DialogoConfirmacao'
import { BotaoCancelar, BotaoOk } from '@/components/formulario/Botoes'
import Grade, { type ColunaGrade } from '@/components/grade/Grade'
import type Contribuicao from '@/data/models/Contribuicao'
import type Escoteiro from '@/data/models/Escoteiro'
import type VerificacaoContribuicao from '@/data/models/VerificacaoContribuicao'
import { buscarContribuicoes, registrarContribuicoes, verificarContribuicao } from '@/data/services/contribuicao'
import { formatarData } from '@/data/utils/datas'
import { mensagemDeErro } from '@/data/utils/erros'
import { formatarCpf } from '@/data/utils/formatadores'
import { cpfValido } from '@/data/utils/validadores'

const colunasContribuicoes: ColunaGrade<Contribuicao>[] = [
  { id: 'anjo', titulo: 'Anjo', valor: (c) => c.nome_anjo },
  { id: 'adotado', titulo: 'Adotado', valor: (c) => c.nome_adotado },
  { id: 'desde', titulo: 'Desde', valor: (c) => c.con_dt, renderizar: (c) => formatarData(c.con_dt) },
]

const colunasAdotados: ColunaGrade<Escoteiro>[] = [
  { id: 'nome', titulo: 'Adotado', valor: (e) => e.membro.nome },
  { id: 'cpf', titulo: 'CPF', valor: (e) => e.membro.CPF, renderizar: (e) => formatarCpf(e.membro.CPF) },
  { id: 'patrulha', titulo: 'Patrulha', valor: (e) => e.patrulha.nome },
]

export default function PaginaContribuicoes() {
  const [cpfAnjo, setCpfAnjo] = useState('')
  const [cpfAdotado, setCpfAdotado] = useState('')
  const [verificacao, setVerificacao] = useState<VerificacaoContribuicao | null>(null)
  const [anjo, setAnjo] = useState<Escoteiro | null>(null)
  const [adotados, setAdotados] = useState<Escoteiro[]>([])
  const [contribuicoes, setContribuicoes] = useState<Contribuicao[]>([])
  const [carregando, setCarregando] = useState(true)
  const [confirmando, setConfirmando] = useState(false)
  const [erro, setErro] = useState('')
  const [aviso, setAviso] = useState('')

  const verificar = async () => {
    if (!(anjo ? true : cpfValido(cpfAnjo)) || !cpfValido(cpfAdotado)) {
      setErro('Informe CPFs válidos para o Anjo e para o adotado.')
      return
    }
    setErro('')
    setAviso('')
    setVerificacao(null)
    setCarregando(true)
    try {
      setVerificacao(await verificarContribuicao(anjo ? anjo.membro.CPF : cpfAnjo, cpfAdotado))
    } catch (falha) {
      setErro(mensagemDeErro(falha, 'Não foi possível verificar os cadastros.'))
    } finally {
      setCarregando(false)
    }
  }

  const adicionarALista = () => {
    if (!verificacao) return
    if (adotados.some((a) => a.esc_cod === verificacao.adotado.esc_cod)) {
      setErro(`${verificacao.adotado.membro.nome} já está na lista de adotados.`)
      return
    }
    setErro('')
    setAnjo(verificacao.anjo)
    setAdotados([...adotados, verificacao.adotado])
    setVerificacao(null)
    setCpfAdotado('')
  }

  const removerDaLista = (escCod: number) => {
    const restantes = adotados.filter((a) => a.esc_cod !== escCod)
    setAdotados(restantes)
    if (restantes.length === 0) setAnjo(null)
  }

  const cancelar = () => {
    setAnjo(null)
    setAdotados([])
    setVerificacao(null)
    setCpfAnjo('')
    setCpfAdotado('')
    setErro('')
  }

  const registrar = async () => {
    if (!anjo) return
    setCarregando(true)
    try {
      const registradas = await registrarContribuicoes({
        cpf_anjo: anjo.membro.CPF,
        cpfs_adotados: adotados.map((a) => a.membro.CPF),
      })
      setConfirmando(false)
      cancelar()
      setAviso(`${registradas.length} contribuição(ões) registrada(s) com sucesso.`)
      setContribuicoes(await buscarContribuicoes())
    } catch (falha) {
      setConfirmando(false)
      setErro(mensagemDeErro(falha, 'Não foi possível registrar a contribuição.'))
    } finally {
      setCarregando(false)
    }
  }

  useEffect(() => {
    let ativo = true
    buscarContribuicoes()
      .then((lista) => ativo && setContribuicoes(lista))
      .catch((falha: unknown) => ativo && setErro(mensagemDeErro(falha, 'Não foi possível carregar as contribuições.')))
      .finally(() => ativo && setCarregando(false))
    return () => {
      ativo = false
    }
  }, [])

  return (
    <main className="pagina">
      <header>
        <h1>Contribuições (Anjos)</h1>
        <p className="subtitulo">Registre quais escoteiros terão as mensalidades custeadas por um Anjo</p>
      </header>

      {erro && <Alert severity="error">{erro}</Alert>}
      {aviso && <Alert severity="success">{aviso}</Alert>}

      <Box
        component="form"
        className="cartao"
        onSubmit={(evento) => {
          evento.preventDefault()
          verificar()
        }}
        sx={{ display: 'grid', gap: 2 }}
      >
        <h2>Nova contribuição</h2>
        <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', md: '1fr 1fr' }, gap: 2 }}>
          <CampoCpf
            rotulo="CPF do Anjo"
            valor={anjo ? anjo.membro.CPF : cpfAnjo}
            aoMudar={setCpfAnjo}
            desabilitado={anjo !== null}
            ajuda={anjo ? anjo.membro.nome : undefined}
            obrigatorio
          />
          <CampoCpf rotulo="CPF do adotado" valor={cpfAdotado} aoMudar={setCpfAdotado} obrigatorio />
        </Box>
        <Box sx={{ display: 'flex', justifyContent: 'flex-end' }}>
          <BotaoOk rotulo="Verificar cadastros" carregando={carregando} />
        </Box>

        {verificacao && (
          <>
            <ResumoVerificacao anjo={verificacao.anjo} adotado={verificacao.adotado} />
            <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 1 }}>
              <BotaoCancelar rotulo="Descartar" aoClicar={() => setVerificacao(null)} />
              <BotaoOk rotulo="Adicionar à lista" tipo="button" aoClicar={adicionarALista} />
            </Box>
          </>
        )}
      </Box>

      {adotados.length > 0 && anjo && (
        <Grade
          titulo={`Adotados de ${anjo.membro.nome}`}
          subtitulo="Confirme a lista para registrar a contribuição"
          colunas={colunasAdotados}
          linhas={adotados}
          chave={(a) => a.esc_cod}
          acoes={(a) => <BotaoCancelar rotulo="Remover" aoClicar={() => removerDaLista(a.esc_cod)} />}
          rodape={(
            <Box sx={{ display: 'flex', gap: 1 }}>
              <BotaoCancelar rotulo="Cancelar registro" aoClicar={cancelar} />
              <BotaoOk rotulo="Registrar contribuição" tipo="button" aoClicar={() => setConfirmando(true)} />
            </Box>
          )}
          linhasPorPagina={5}
        />
      )}

      <DialogoConfirmacao
        aberto={confirmando}
        titulo="Confirmar contribuição"
        rotuloConfirmar="Registrar"
        mensagem={`${anjo?.membro.nome ?? ''} passará a custear as mensalidades de: ${adotados.map((a) => a.membro.nome).join(', ')}. Confirma a operação?`}
        aoConfirmar={registrar}
        aoCancelar={() => setConfirmando(false)}
        carregando={carregando}
      />

      <Grade
        titulo="Contribuições registradas"
        subtitulo={`${contribuicoes.length} ${contribuicoes.length === 1 ? 'adotado' : 'adotados'} com Anjo`}
        colunas={colunasContribuicoes}
        linhas={contribuicoes}
        chave={(c) => c.con_cod}
        carregando={carregando}
        textoVazio="Nenhuma contribuição registrada."
        linhasPorPagina={10}
      />
    </main>
  )
}
