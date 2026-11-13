import { useEffect, useState } from 'react'
import Alert from '@mui/material/Alert'
import Button from '@mui/material/Button'
import CampoTexto from '@/components/formulario/CampoTexto'
import DialogoConfirmacao from '@/components/formulario/DialogoConfirmacao'
import Formulario from '@/components/formulario/Formulario'
import Grade, { type ColunaGrade } from '@/components/grade/Grade'
import FormPatrulha from '@/components/tropa/FormPatrulha'
import { useSessao } from '@/data/contexts/SessaoContexto'
import type Patrulha from '@/data/models/Patrulha'
import type Tropa from '@/data/models/Tropa'
import { AUTENTICACAO_ATIVA } from '@/data/services/sessao'
import {
  alterarPatrulha, alterarTropa, criarPatrulha, criarTropa, excluirPatrulha, excluirTropa, listarPatrulhas, listarTropas,
  type DadosPatrulha,
} from '@/data/services/tropa'
import { mensagemDeErro } from '@/data/utils/erros'
import { semDigitos } from '@/data/utils/filtros'

const colunasTropa: ColunaGrade<Tropa>[] = [
  { id: 'cod', titulo: 'Código', valor: (t) => t.cod, largura: 120 },
  { id: 'nome', titulo: 'Nome', valor: (t) => t.nome },
]

const colunasPatrulha: ColunaGrade<Patrulha>[] = [
  { id: 'nome', titulo: 'Patrulha', valor: (p) => p.nome },
  { id: 'lider', titulo: 'Líder', valor: (p) => p.lider?.membro.nome ?? '—' },
  { id: 'sublider', titulo: 'Sublíder', valor: (p) => p.sublider?.membro.nome ?? '—' },
]

type Formulario_ = 'tropa-nova' | 'tropa-edicao' | 'patrulha-nova' | 'patrulha-edicao' | null
type Exclusao = 'tropa' | 'patrulha' | null

export default function PaginaTropa() {
  const { usuario } = useSessao()
  const [tropas, setTropas] = useState<Tropa[]>([])
  const [patrulhas, setPatrulhas] = useState<Patrulha[]>([])
  const [tropaCod, setTropaCod] = useState<number | null>(null)
  const [patrulhaCod, setPatrulhaCod] = useState<number | null>(null)
  const [formulario, setFormulario] = useState<Formulario_>(null)
  const [exclusao, setExclusao] = useState<Exclusao>(null)
  const [nomeTropa, setNomeTropa] = useState('')
  const [carregando, setCarregando] = useState(true)
  const [salvando, setSalvando] = useState(false)
  const [erro, setErro] = useState('')

  const tropa = tropas.find((t) => t.cod === tropaCod) ?? null
  const patrulha = patrulhas.find((p) => p.cod === patrulhaCod) ?? null
  // Só o presidente (ou o admin) mexe em tropa e patrulha; o chefe só consulta
  const podeGerir = !AUTENTICACAO_ATIVA || !!usuario?.perfis.some((p) => p === 'PRESIDENTE' || p === 'ADMIN')

  useEffect(() => {
    let ativo = true
    listarTropas()
      .then((lista) => ativo && setTropas(lista))
      .catch((falha: unknown) => ativo && setErro(mensagemDeErro(falha, 'Não foi possível carregar as tropas.')))
      .finally(() => ativo && setCarregando(false))
    return () => {
      ativo = false
    }
  }, [])

  useEffect(() => {
    let ativo = true
    if (tropaCod === null) return
    listarPatrulhas(tropaCod)
      .then((lista) => ativo && setPatrulhas(lista))
      .catch((falha: unknown) => ativo && setErro(mensagemDeErro(falha, 'Não foi possível carregar as patrulhas.')))
    return () => {
      ativo = false
    }
  }, [tropaCod])

  async function executar(acao: () => Promise<unknown>, falhaPadrao: string) {
    setSalvando(true)
    setErro('')
    try {
      await acao()
      setTropas(await listarTropas())
      setPatrulhas(tropaCod === null ? [] : await listarPatrulhas(tropaCod))
      setFormulario(null)
    } catch (falha) {
      setErro(mensagemDeErro(falha, falhaPadrao))
    } finally {
      setSalvando(false)
      setExclusao(null)
    }
  }

  const salvarTropa = () =>
    executar(() => (formulario === 'tropa-edicao' && tropa ? alterarTropa(tropa.cod, nomeTropa) : criarTropa(nomeTropa)), 'Não foi possível salvar a tropa.')

  const salvarPatrulha = (dados: DadosPatrulha) =>
    executar(() => (formulario === 'patrulha-edicao' && patrulha ? alterarPatrulha(patrulha.cod, dados) : criarPatrulha(dados)), 'Não foi possível salvar a patrulha.')

  async function confirmarExclusao() {
    if (exclusao === 'tropa' && tropa) {
      await executar(() => excluirTropa(tropa.cod), 'Não foi possível excluir a tropa.')
      setTropaCod(null)
      setPatrulhas([])
    } else if (exclusao === 'patrulha' && patrulha) {
      await executar(() => excluirPatrulha(patrulha.cod), 'Não foi possível excluir a patrulha.')
      setPatrulhaCod(null)
    }
  }

  function abrirTropa(edicao: boolean) {
    setNomeTropa(edicao && tropa ? tropa.nome : '')
    setFormulario(edicao ? 'tropa-edicao' : 'tropa-nova')
  }

  return (
    <main className="pagina">
      <header>
        <h1>Tropa</h1>
        <p className="subtitulo">Clique numa tropa para ver as patrulhas dela; duplo clique para editar</p>
      </header>

      {erro && <Alert severity="error" onClose={() => setErro('')}>{erro}</Alert>}

      {(formulario === 'tropa-nova' || formulario === 'tropa-edicao') && (
        <Formulario
          titulo={formulario === 'tropa-edicao' ? 'Editar tropa' : 'Nova tropa'}
          aoConfirmar={salvarTropa}
          aoCancelar={() => setFormulario(null)}
          carregando={salvando}
          desabilitarOk={!nomeTropa.trim()}
        >
          <CampoTexto rotulo="Nome da tropa" valor={nomeTropa} aoMudar={setNomeTropa} filtro={semDigitos} tamanhoMaximo={20} autoFocus obrigatorio />
        </Formulario>
      )}
      {tropa && (formulario === 'patrulha-nova' || formulario === 'patrulha-edicao') && (
        <FormPatrulha
          tropa={tropa}
          patrulha={formulario === 'patrulha-edicao' ? (patrulha ?? undefined) : undefined}
          aoConfirmar={salvarPatrulha}
          aoCancelar={() => setFormulario(null)}
          carregando={salvando}
        />
      )}

      <Grade
        titulo="Tropas cadastradas"
        subtitulo={`${tropas.length} ${tropas.length === 1 ? 'tropa' : 'tropas'}`}
        colunas={colunasTropa}
        linhas={tropas}
        chave={(t) => t.cod}
        carregando={carregando}
        textoVazio="Nenhuma tropa cadastrada ainda."
        aoClicarLinha={(t) => {
          setTropaCod(t.cod)
          setPatrulhaCod(null)
        }}
        aoDuploClique={podeGerir ? (t) => { setTropaCod(t.cod); setNomeTropa(t.nome); setFormulario('tropa-edicao') } : undefined}
        chaveSelecionada={tropaCod}
        rodape={podeGerir && (
          <>
            <Button variant="contained" onClick={() => abrirTropa(false)}>Nova tropa</Button>
            <Button variant="outlined" color="error" disabled={!tropa} onClick={() => setExclusao('tropa')}>Excluir</Button>
          </>
        )}
      />

      {tropa && (
        <Grade
          titulo={`Patrulhas da ${tropa.nome}`}
          subtitulo={`${patrulhas.length} ${patrulhas.length === 1 ? 'patrulha' : 'patrulhas'}`}
          colunas={colunasPatrulha}
          linhas={patrulhas}
          chave={(p) => p.cod}
          textoVazio="Nenhuma patrulha nesta tropa."
          aoClicarLinha={(p) => setPatrulhaCod(p.cod)}
          aoDuploClique={podeGerir ? (p) => { setPatrulhaCod(p.cod); setFormulario('patrulha-edicao') } : undefined}
          chaveSelecionada={patrulhaCod}
          rodape={podeGerir && (
            <>
              <Button variant="contained" onClick={() => setFormulario('patrulha-nova')}>Nova patrulha</Button>
              <Button variant="outlined" disabled={!patrulha} onClick={() => setFormulario('patrulha-edicao')}>Editar</Button>
              <Button variant="outlined" color="error" disabled={!patrulha} onClick={() => setExclusao('patrulha')}>Excluir</Button>
            </>
          )}
        />
      )}

      <DialogoConfirmacao
        aberto={exclusao !== null}
        mensagem={<>Excluir {exclusao === 'tropa' ? 'a tropa' : 'a patrulha'} <strong>{exclusao === 'tropa' ? tropa?.nome : patrulha?.nome}</strong>? Essa ação não pode ser desfeita.</>}
        aoConfirmar={confirmarExclusao}
        aoCancelar={() => setExclusao(null)}
        carregando={salvando}
      />
    </main>
  )
}
