import { useEffect, useState } from 'react'
import Alert from '@mui/material/Alert'
import Button from '@mui/material/Button'
import Chip from '@mui/material/Chip'
import Stack from '@mui/material/Stack'
import FormSenhaDaConta from '@/components/contas/FormSenhaDaConta'
import DialogoConfirmacao from '@/components/formulario/DialogoConfirmacao'
import Grade, { type ColunaGrade } from '@/components/grade/Grade'
import { useSessao } from '@/data/contexts/SessaoContexto'
import type ContaAcesso from '@/data/models/ContaAcesso'
import type { SituacaoConta } from '@/data/models/ContaAcesso'
import {
  alterarAdministradorDaConta,
  alterarSituacaoDaConta,
  buscarContas,
  redefinirSenhaDaConta,
} from '@/data/services/conta'
import { mensagemDeErro } from '@/data/utils/erros'
import { formatarCpf } from '@/data/utils/formatadores'

type Operacao = 'senha' | 'situacao' | 'admin'

const ROTULO_DA_SITUACAO: Record<SituacaoConta, string> = {
  A: 'Ativa',
  P: 'Aguardando confirmação',
  I: 'Inativa',
}

const COR_DA_SITUACAO: Record<SituacaoConta, 'success' | 'warning' | 'default'> = {
  A: 'success',
  P: 'warning',
  I: 'default',
}

const colunas: ColunaGrade<ContaAcesso>[] = [
  { id: 'nome', titulo: 'Nome', valor: (c) => c.nome },
  { id: 'cpf', titulo: 'CPF', valor: (c) => c.cpf, renderizar: (c) => formatarCpf(c.cpf) },
  {
    id: 'perfis',
    titulo: 'Perfis',
    valor: (c) => c.perfis.join(','),
    renderizar: (c) => (
      <Stack direction="row" spacing={0.5} useFlexGap sx={{ flexWrap: 'wrap' }}>
        {c.perfis.map((perfil) => (
          <Chip key={perfil} size="small" variant="outlined" color={perfil === 'ADMIN' ? 'warning' : 'default'} label={perfil} />
        ))}
      </Stack>
    ),
  },
  {
    id: 'situacao',
    titulo: 'Situação',
    valor: (c) => c.situacao,
    renderizar: (c) => (
      <Chip
        size="small"
        color={COR_DA_SITUACAO[c.situacao]}
        label={c.bloqueio_ate && new Date(c.bloqueio_ate) > new Date() ? 'Bloqueada (tentativas)' : ROTULO_DA_SITUACAO[c.situacao]}
      />
    ),
  },
  { id: 'email', titulo: 'E-mail', valor: (c) => c.email ?? '—' },
]

export default function PaginaContas() {
  const { usuario } = useSessao()
  const [contas, setContas] = useState<ContaAcesso[]>([])
  const [carregando, setCarregando] = useState(true)
  const [alvo, setAlvo] = useState<ContaAcesso | null>(null)
  const [operacao, setOperacao] = useState<Operacao | null>(null)
  const [erro, setErro] = useState('')
  const [aviso, setAviso] = useState('')

  const iniciar = (conta: ContaAcesso, nova: Operacao) => {
    setErro('')
    setAviso('')
    setAlvo(conta)
    setOperacao(nova)
  }

  const fechar = () => {
    setAlvo(null)
    setOperacao(null)
  }

  const executar = async (acao: () => Promise<{ mensagem: string }>) => {
    setCarregando(true)
    try {
      const resposta = await acao()
      fechar()
      setErro('')
      setAviso(resposta.mensagem)
      setContas(await buscarContas())
    } catch (falha) {
      fechar()
      setErro(mensagemDeErro(falha, 'Não foi possível concluir a operação.'))
    } finally {
      setCarregando(false)
    }
  }

  useEffect(() => {
    let ativo = true
    buscarContas()
      .then((lista) => ativo && setContas(lista))
      .catch((falha: unknown) => ativo && setErro(mensagemDeErro(falha, 'Não foi possível carregar as contas.')))
      .finally(() => ativo && setCarregando(false))
    return () => {
      ativo = false
    }
  }, [])

  const inativando = alvo?.situacao === 'A'
  const removendoAdmin = alvo?.admin ?? false

  return (
    <main className="pagina">
      <header>
        <h1>Contas de acesso</h1>
        <p className="subtitulo">Redefina senhas, ative ou inative contas e gerencie administradores</p>
      </header>

      {erro && <Alert severity="error">{erro}</Alert>}
      {aviso && <Alert severity="success">{aviso}</Alert>}

      <Grade
        titulo="Contas"
        subtitulo={`${contas.length} ${contas.length === 1 ? 'conta' : 'contas'} cadastradas`}
        colunas={colunas}
        linhas={contas}
        chave={(c) => c.cta_cod}
        carregando={carregando}
        textoVazio="Nenhuma conta cadastrada."
        linhasPorPagina={10}
        acoes={(conta) => (
          <Stack direction="row" spacing={0.5} sx={{ justifyContent: 'flex-end' }}>
            <Button size="small" onClick={() => iniciar(conta, 'senha')}>Senha</Button>
            <Button size="small" color={conta.situacao === 'A' ? 'warning' : 'success'} onClick={() => iniciar(conta, 'situacao')}>
              {conta.situacao === 'A' ? 'Inativar' : 'Ativar'}
            </Button>
            <Button size="small" disabled={conta.situacao !== 'A' && !conta.admin} onClick={() => iniciar(conta, 'admin')}>
              {conta.admin ? 'Remover admin' : 'Tornar admin'}
            </Button>
          </Stack>
        )}
      />

      {alvo && operacao === 'senha' && (
        <FormSenhaDaConta
          conta={alvo}
          carregando={carregando}
          aoConfirmar={(senha) => executar(() => redefinirSenhaDaConta(alvo.mem_cod, senha))}
          aoCancelar={fechar}
        />
      )}

      <DialogoConfirmacao
        aberto={alvo !== null && operacao === 'situacao'}
        titulo={inativando ? 'Inativar conta' : 'Ativar conta'}
        rotuloConfirmar={inativando ? 'Inativar' : 'Ativar'}
        mensagem={
          inativando
            ? `${alvo?.nome} não conseguirá mais entrar no sistema e as sessões abertas serão encerradas. Confirma?`
            : `${alvo?.nome} poderá entrar no sistema novamente. Confirma?`
        }
        aoConfirmar={() => alvo && executar(() => alterarSituacaoDaConta(alvo.mem_cod, !inativando))}
        aoCancelar={fechar}
        carregando={carregando}
      />

      <DialogoConfirmacao
        aberto={alvo !== null && operacao === 'admin'}
        titulo={removendoAdmin ? 'Remover administrador' : 'Tornar administrador'}
        rotuloConfirmar={removendoAdmin ? 'Remover' : 'Confirmar'}
        mensagem={
          removendoAdmin
            ? `${alvo?.nome} perderá o acesso de administrador${alvo?.mem_cod === usuario?.mem_cod ? ' (é a sua própria conta)' : ''}. Sempre deve restar ao menos um administrador ativo. Confirma?`
            : `${alvo?.nome} poderá gerenciar contas e senhas de todos os usuários. Confirma?`
        }
        aoConfirmar={() => alvo && executar(() => alterarAdministradorDaConta(alvo.mem_cod, !removendoAdmin))}
        aoCancelar={fechar}
        carregando={carregando}
      />
    </main>
  )
}
