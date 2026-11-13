import { useEffect, useState } from 'react'
import Alert from '@mui/material/Alert'
import Box from '@mui/material/Box'
import Chip from '@mui/material/Chip'
import Stack from '@mui/material/Stack'
import { BotaoCancelar, BotaoOk } from '@/components/formulario/Botoes'
import DialogoConfirmacao from '@/components/formulario/DialogoConfirmacao'
import Grade, { type ColunaGrade } from '@/components/grade/Grade'
import FormPessoa from '@/components/membros/FormPessoa'
import FormPromocao, { type PedidoPromocao } from '@/components/membros/FormPromocao'
import { useSessao } from '@/data/contexts/SessaoContexto'
import type { DadosPessoa } from '@/data/models/DadosPessoa'
import type Membro from '@/data/models/Membro'
import type Tropa from '@/data/models/Tropa'
import {
  alterarMembro, cadastrarMembro, encerrarChefe, excluirMembro, listarMembros,
  promoverAChefe, promoverAFinanceiro, promoverAPresidente, removerFinanceiro,
} from '@/data/services/membro'
import { AUTENTICACAO_ATIVA } from '@/data/services/sessao'
import { listarTropas } from '@/data/services/tropa'
import { membroParaDados } from '@/data/utils/conversores'
import { mensagemDeErro } from '@/data/utils/erros'
import { formatarData } from '@/data/utils/datas'
import { formatarTelefone } from '@/data/utils/formatadores'

type CorChip = 'primary' | 'secondary' | 'success' | 'default' | 'warning' | 'info'

const COR_DO_PAPEL: Record<string, CorChip> = {
  Presidente: 'primary',
  Chefe: 'secondary',
  Escoteiro: 'success',
  'Responsável': 'default',
  Financeiro: 'warning',
  'Voluntário': 'info',
}

const colunas: ColunaGrade<Membro>[] = [
  { id: 'nome', titulo: 'Nome', valor: (m) => m.nome },
  { id: 'nascimento', titulo: 'Nascimento', valor: (m) => m.dt_nascto, renderizar: (m) => formatarData(m.dt_nascto) },
  { id: 'cidade', titulo: 'Cidade', valor: (m) => `${m.cidade}/${m.estado}` },
  { id: 'telefone', titulo: 'Telefone', valor: (m) => m.telefone && formatarTelefone(m.telefone) },
  { id: 'email', titulo: 'E-mail', valor: (m) => m.email },
  {
    id: 'papeis',
    titulo: 'Papéis',
    renderizar: (m) => (
      <Stack direction="row" spacing={0.5} useFlexGap sx={{ flexWrap: 'wrap' }}>
        {m.papeis.map((papel) => (
          <Chip key={papel} size="small" label={papel} color={COR_DO_PAPEL[papel] ?? 'default'} variant="outlined" />
        ))}
      </Stack>
    ),
  },
]

type Confirmacao = 'excluir' | 'chefe' | 'financeiro' | null

export default function PaginaMembros() {
  const { usuario } = useSessao()
  const [membros, setMembros] = useState<Membro[]>([])
  const [tropas, setTropas] = useState<Tropa[]>([])
  const [carregando, setCarregando] = useState(true)
  const [salvando, setSalvando] = useState(false)
  const [erro, setErro] = useState('')
  const [aviso, setAviso] = useState('')
  const [selecionadoCod, setSelecionadoCod] = useState<number | null>(null)
  const [formulario, setFormulario] = useState<'pessoa' | 'promocao' | null>(null)
  const [editando, setEditando] = useState(false)
  const [confirmacao, setConfirmacao] = useState<Confirmacao>(null)

  const selecionado = membros.find((m) => m.mem_cod === selecionadoCod) ?? null
  // Só o presidente (ou o admin) promove; com a autenticação desligada (desenvolvimento) tudo é liberado
  const podePromover = !AUTENTICACAO_ATIVA || !!usuario?.perfis.some((p) => p === 'PRESIDENTE' || p === 'ADMIN')

  async function recarregar() {
    setMembros(await listarMembros())
  }

  useEffect(() => {
    let ativo = true
    Promise.all([listarMembros(), listarTropas()])
      .then(([lista, listaTropas]) => {
        if (!ativo) return
        setMembros(lista)
        setTropas(listaTropas)
      })
      .catch((falha: unknown) => ativo && setErro(mensagemDeErro(falha, 'Não foi possível carregar os membros.')))
      .finally(() => ativo && setCarregando(false))
    return () => {
      ativo = false
    }
  }, [])

  // Executa a ação, recarrega a lista e mostra o resultado; qualquer falha do backend vira mensagem na tela
  async function executar(acao: () => Promise<unknown>, sucesso: string, falhaPadrao: string) {
    setSalvando(true)
    setErro('')
    setAviso('')
    try {
      await acao()
      await recarregar()
      setAviso(sucesso)
      setFormulario(null)
      setConfirmacao(null)
    } catch (falha) {
      setErro(mensagemDeErro(falha, falhaPadrao))
      setConfirmacao(null)
    } finally {
      setSalvando(false)
    }
  }

  const salvarPessoa = (dados: DadosPessoa) =>
    executar(
      () => (editando && selecionado ? alterarMembro(selecionado.mem_cod, dados) : cadastrarMembro(dados)),
      editando ? 'Pessoa alterada.' : 'Pessoa cadastrada.',
      'Não foi possível salvar a pessoa.',
    )

  const promover = (pedido: PedidoPromocao) => {
    if (!selecionado) return Promise.resolve()
    const cod = selecionado.mem_cod
    const chamada =
      pedido.cargo === 'CHEFE' ? () => promoverAChefe(cod, { tropa: { cod: pedido.tropa }, gestao_ini: pedido.inicio })
      : pedido.cargo === 'PRESIDENTE' ? () => promoverAPresidente(cod, { gestao_ini: pedido.posse, gestao_fim: pedido.fim })
      : () => promoverAFinanceiro(cod)
    return executar(chamada, `${selecionado.nome} foi promovido(a). O novo cargo vale a partir do próximo login da pessoa.`, 'Não foi possível promover.')
  }

  const confirmar = () => {
    if (!selecionado) return Promise.resolve()
    const cod = selecionado.mem_cod
    if (confirmacao === 'excluir') return executar(() => excluirMembro(cod), 'Pessoa removida do grupo.', 'Não foi possível excluir a pessoa.')
    if (confirmacao === 'chefe') return executar(() => encerrarChefe(cod), 'Chefia encerrada.', 'Não foi possível encerrar a chefia.')
    return executar(() => removerFinanceiro(cod), 'Cargo de financeiro removido.', 'Não foi possível remover o cargo.')
  }

  const textoConfirmacao: Record<Exclude<Confirmacao, null>, { titulo: string; rotulo: string; mensagem: string }> = {
    excluir: { titulo: 'Confirmar exclusão', rotulo: 'Excluir', mensagem: `Deseja realmente excluir ${selecionado?.nome ?? ''} do grupo?` },
    chefe: { titulo: 'Encerrar chefia', rotulo: 'Encerrar', mensagem: `Encerrar a gestão de ${selecionado?.nome ?? ''} como chefe?` },
    financeiro: { titulo: 'Remover financeiro', rotulo: 'Remover', mensagem: `Retirar o cargo de financeiro de ${selecionado?.nome ?? ''}?` },
  }

  return (
    <main className="pagina">
      <header>
        <h1>Membros</h1>
        <p className="subtitulo">Todas as pessoas do grupo, com os papéis de cada uma</p>
      </header>

      {erro && <Alert severity="error" onClose={() => setErro('')}>{erro}</Alert>}
      {aviso && <Alert severity="success" onClose={() => setAviso('')}>{aviso}</Alert>}

      {formulario === 'pessoa' && (
        <FormPessoa
          inicial={editando && selecionado ? membroParaDados(selecionado) : undefined}
          aoConfirmar={salvarPessoa}
          aoCancelar={() => setFormulario(null)}
          carregando={salvando}
        />
      )}
      {formulario === 'promocao' && selecionado && (
        <FormPromocao pessoa={selecionado} tropas={tropas} aoConfirmar={promover} aoCancelar={() => setFormulario(null)} carregando={salvando} />
      )}
      {confirmacao && (
        <DialogoConfirmacao
          aberto
          titulo={textoConfirmacao[confirmacao].titulo}
          rotuloConfirmar={textoConfirmacao[confirmacao].rotulo}
          mensagem={textoConfirmacao[confirmacao].mensagem}
          aoConfirmar={confirmar}
          aoCancelar={() => setConfirmacao(null)}
          carregando={salvando}
        />
      )}

      <Grade
        titulo="Pessoas ativas"
        subtitulo={`${membros.length} ${membros.length === 1 ? 'pessoa' : 'pessoas'}`}
        colunas={colunas}
        linhas={membros}
        chave={(m) => m.mem_cod}
        carregando={carregando}
        textoVazio="Nenhuma pessoa encontrada."
        aoClicarLinha={(m) => setSelecionadoCod(m.mem_cod)}
        aoDuploClique={(m) => {
          setSelecionadoCod(m.mem_cod)
          setEditando(true)
          setFormulario('pessoa')
        }}
        chaveSelecionada={selecionadoCod}
        linhasPorPagina={10}
        rodape={(
          <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: '10px', flexWrap: 'wrap' }}>
            <BotaoOk rotulo="Adicionar Membro" tipo="button" aoClicar={() => { setEditando(false); setFormulario('pessoa') }} />
            <BotaoOk rotulo="Editar" tipo="button" desabilitado={!selecionado} aoClicar={() => { setEditando(true); setFormulario('pessoa') }} />
            {podePromover && (
              <BotaoOk rotulo="Promover" tipo="button" desabilitado={!selecionado} aoClicar={() => setFormulario('promocao')} />
            )}
            {podePromover && selecionado?.papeis.includes('Chefe') && (
              <BotaoCancelar rotulo="Encerrar chefia" aoClicar={() => setConfirmacao('chefe')} />
            )}
            {podePromover && selecionado?.papeis.includes('Financeiro') && (
              <BotaoCancelar rotulo="Remover financeiro" aoClicar={() => setConfirmacao('financeiro')} />
            )}
            <BotaoCancelar rotulo="Deletar Membro" desabilitado={!selecionado} aoClicar={() => setConfirmacao('excluir')} />
          </Box>
        )}
      />
    </main>
  )
}
