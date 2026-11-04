import { useEffect, useState } from 'react'
import Alert from '@mui/material/Alert'
import Chip from '@mui/material/Chip'
import Stack from '@mui/material/Stack'
import Grade, { type ColunaGrade } from '@/components/grade/Grade'
import type Membro from '@/data/models/Membro'
import {
  buscarChefes,
  buscarEscoteiros,
  buscarFinanceiros,
  buscarPresidentes,
  buscarResponsaveis,
  ExcluirMembro,
  PostMembro,
  } from '@/data/services/membro'
import { ErroDeApi } from '@/data/services/api'
import { formatarData } from '@/data/utils/datas'
import { formatarTelefone } from '@/data/utils/formatadores'
import FormsEscoteiros,  { type DadosEscoteiro } from '@/components/escoteiros/FormsEscoteiros';
import { BotaoCancelar, BotaoOk } from '@/components/formulario/Botoes';
import { Box } from '@mui/material';
import { membroParaDados } from '@/data/utils/conversores';
import DialogoConfirmacao from '@/components/formulario/DialogoConfirmacao';

type Papel = 'Presidente' | 'Chefe' | 'Escoteiro' | 'Responsável' | 'Financeiro'

interface LinhaMembro {
  membro: Membro
  papeis: Papel[]
}

const COR_DO_PAPEL: Record<Papel, 'primary' | 'secondary' | 'success' | 'default' | 'warning'> = {
  Presidente: 'primary',
  Chefe: 'secondary',
  Escoteiro: 'success',
  Responsável: 'default',
  Financeiro: 'warning',
}

function juntarMembros(fontes: [Papel, Membro[]][]): LinhaMembro[] {
  const porCodigo = new Map<number, LinhaMembro>()
  for (const [papel, membros] of fontes) {
    for (const membro of membros) {
      const existente = porCodigo.get(membro.mem_cod)
      if (existente) existente.papeis.push(papel)
      else porCodigo.set(membro.mem_cod, { membro, papeis: [papel] })
    }
  }
  return [...porCodigo.values()]
}

const colunas: ColunaGrade<LinhaMembro>[] = [
  { id: 'nome', titulo: 'Nome', valor: (linha) => linha.membro.nome },
  { id: 'nascimento', titulo: 'Nascimento', valor: (linha) => linha.membro.dt_nascto, renderizar: (linha) => formatarData(linha.membro.dt_nascto) },
  { id: 'cidade', titulo: 'Cidade', valor: (linha) => `${linha.membro.cidade}/${linha.membro.estado}` },
  { id: 'telefone', titulo: 'Telefone', valor: (linha) => linha.membro.telefone && formatarTelefone(linha.membro.telefone),},
  { id: 'email', titulo: 'E-mail', valor: (linha) => linha.membro.email },
  { id: 'papeis', titulo: 'Papéis', renderizar: (linha) => (
      <Stack direction="row" spacing={0.5} useFlexGap sx={{ flexWrap: 'wrap' }}>
        {linha.papeis.map((papel) => (
          <Chip key={papel} size="small" label={papel} color={COR_DO_PAPEL[papel]} variant="outlined" />
        ))}
      </Stack>
    ),
  },
]

async function buscarTodosOsMembros(): Promise<LinhaMembro[]> {
  const [presidentes, chefes, escoteiros, responsaveis, financeiros] = await Promise.all([
    buscarPresidentes(),
    buscarChefes(),
    buscarEscoteiros(),
    buscarResponsaveis(),
    buscarFinanceiros(),
  ])
  return juntarMembros([
    ['Presidente', presidentes.map((p) => p.membro)],
    ['Chefe', chefes.map((c) => c.membro)],
    ['Escoteiro', escoteiros.map((e) => e.membro)],
    ['Responsável', responsaveis.map((r) => r.membro)],
    ['Financeiro', financeiros.map((f) => f.membro)],
  ])
}

function mensagemDeErroDeCarga(falha: unknown): string {
  return falha instanceof ErroDeApi
    ? `O backend respondeu ${falha.status}.`
    : 'Não foi possível carregar os membros. O backend está rodando em localhost:8080 e o front em localhost:5173 (origem liberada no CORS)?'
}

function mensagemDeErroAoSalvar(falha: unknown): string {
  if (!(falha instanceof ErroDeApi)) return 'Não foi possível salvar o membro. O backend está rodando em localhost:8080?'
  let detalhe: string | undefined
  try {
    detalhe = /RuntimeException: ([^\n]+)/.exec(JSON.parse(falha.message).trace ?? '')?.[1]
  } catch {
    // corpo da resposta não é JSON: fica só o código
  }
  return detalhe ? `Não foi possível salvar o membro: ${detalhe}` : `Não foi possível salvar o membro (o backend respondeu ${falha.status}).`
}

export default function PaginaMembros() {
  const [membros, setMembros] = useState<LinhaMembro[]>([])
  const [carregando, setCarregando] = useState(true)
  const [erro, setErro] = useState('')
  const [editando, setEditando] = useState(false)
  const [membroSelecionado, setMembroSelecionado] = useState<LinhaMembro | null>(null)
  const [confirmandoExclusao, setConfirmandoExclusao] = useState(false)



  const recarregar = async () => {
    setCarregando(true)
    try {
      setMembros(await buscarTodosOsMembros())
      setErro('')
    } catch (falha) {
      setErro(mensagemDeErroDeCarga(falha))
    } finally {
      setCarregando(false)
    }
  }

  const salvarMembro = async (DadosMembro: DadosEscoteiro) => {
    setEditando(false)
    setMembroSelecionado(null)
    try {
      await PostMembro(DadosMembro)
    } catch (falha) {
      console.error('Erro ao salvar membro:', falha)
      setErro(mensagemDeErroAoSalvar(falha))
      return
    }
    await recarregar()
  }

  const NovoMembro = () => {
    setEditando(true)
    setMembroSelecionado(null)
  }

  const excluirMembro = async () => {
    if (!membroSelecionado) return
    setConfirmandoExclusao(false)
    try {
      await ExcluirMembro(membroSelecionado.membro.mem_cod)
      setMembros((atual) => atual.filter((linha) => linha.membro.mem_cod !== membroSelecionado.membro.mem_cod))
      setMembroSelecionado(null)
    } catch (erro) {
      if (erro instanceof ErroDeApi) {
        console.error(`Erro ao excluir membro: ${erro.status} - ${erro.message}`)
      } else {
        console.error('Erro desconhecido ao excluir membro:', erro)
      }
      throw erro
    }
  }

  useEffect(() => {
    let ativo = true 
    buscarTodosOsMembros()
      .then((lista) => ativo && setMembros(lista))
      .catch((falha: unknown) => ativo && setErro(mensagemDeErroDeCarga(falha)))
      .finally(() => ativo && setCarregando(false))
    return () => {
      ativo = false
    }
  }, [])

  return (
    <main className="pagina">
      <header>
        <h1>Membros</h1>
        <p className="subtitulo">Todas as pessoas do grupo, com os papéis de cada uma</p>
      </header>

      {erro && <Alert severity="error">{erro}</Alert>}

      {editando && (
        <FormsEscoteiros
          inicial={membroSelecionado ? membroParaDados(membroSelecionado.membro) : undefined}
          aoConfirmar={(dados) => {salvarMembro(dados)}}
          aoCancelar={() => {
            setEditando(false)
            setMembroSelecionado(null)
          }}
          carregando={carregando}
        >

        </FormsEscoteiros>
      )}
      
      <DialogoConfirmacao
        aberto={confirmandoExclusao}
        mensagem={"Deseja Realmente Excluir o membro " + (membroSelecionado?.membro.nome ?? '') + "?"}
        aoConfirmar={excluirMembro}                 
        aoCancelar={() => setConfirmandoExclusao(false)}
        carregando={false}                      
      />

      <Grade
        titulo="Membros ativos"
        subtitulo={`${membros.length} ${membros.length === 1 ? 'membro' : 'membros'}`}
        colunas={colunas}
        linhas={membros}
        chave={(linha) => linha.membro.mem_cod}
        carregando={carregando}
        textoVazio="Nenhum membro encontrado."
        aoClicarLinha={(linha) => setMembroSelecionado(linha)}
        aoDuploClique={(linha) => {
          setMembroSelecionado(linha)
          setEditando(true)
        }}
        chaveSelecionada={membroSelecionado?.membro.mem_cod ?? null}
        linhasPorPagina={10}
        rodape={(
          <Box style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px' }}>
            <BotaoOk
            rotulo="Adicionar Membro"
            aoClicar={NovoMembro}
            tipo="button"
            >
            </BotaoOk>
          
            <BotaoCancelar
            rotulo="Deletar Membro"
            aoClicar={() => setConfirmandoExclusao(true)}
            desabilitado={membroSelecionado === null}
            >
            </BotaoCancelar>
          </Box>                  
        )}
      />
    </main>
  )
}
