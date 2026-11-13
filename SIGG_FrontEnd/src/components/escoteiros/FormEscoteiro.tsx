import { useEffect, useState } from 'react'
import Alert from '@mui/material/Alert'
import Box from '@mui/material/Box'
import Button from '@mui/material/Button'
import Chip from '@mui/material/Chip'
import Stack from '@mui/material/Stack'
import CampoSelecao from '@/components/formulario/CampoSelecao'
import Formulario from '@/components/formulario/Formulario'
import type CategoriaMensalidade from '@/data/models/CategoriaMensalidade'
import type Escoteiro from '@/data/models/Escoteiro'
import type Membro from '@/data/models/Membro'
import type Patrulha from '@/data/models/Patrulha'
import type Responsavel from '@/data/models/Responsavel'
import type Tropa from '@/data/models/Tropa'
import { buscarEscoteiro, desvincularResponsavel, vincularResponsavel } from '@/data/services/escoteiro'
import { listarMembros } from '@/data/services/membro'
import { listarPatrulhas } from '@/data/services/tropa'
import { mensagemDeErro } from '@/data/utils/erros'

export interface PedidoEscoteiro {
  memCod: number
  categoria: number
  patrulha: number
}

interface FormEscoteiroProps {
  escoteiro?: Escoteiro // presente = edição (a pessoa não muda)
  pessoas: Membro[] // candidatas ao cadastro: quem ainda não é escoteiro
  tropas: Tropa[]
  categorias: CategoriaMensalidade[]
  aoConfirmar: (pedido: PedidoEscoteiro) => void
  aoCancelar: () => void
  carregando?: boolean
}

export default function FormEscoteiro({ escoteiro, pessoas, tropas, categorias, aoConfirmar, aoCancelar, carregando }: FormEscoteiroProps) {
  const [memCod, setMemCod] = useState<number | null>(escoteiro?.membro.mem_cod ?? null)
  const [categoria, setCategoria] = useState<number | null>(escoteiro?.cat_mensalidade ?? null)
  const [tropa, setTropa] = useState<number | null>(escoteiro?.patrulha.tropa?.cod ?? null)
  const [patrulha, setPatrulha] = useState<number | null>(escoteiro?.patrulha.cod ?? null)
  const [patrulhas, setPatrulhas] = useState<Patrulha[]>([])
  const [tentou, setTentou] = useState(false)

  // Responsáveis só existem para quem já é escoteiro: a seção aparece na edição
  const [responsaveis, setResponsaveis] = useState<Responsavel[]>([])
  const [adultos, setAdultos] = useState<Membro[]>([])
  const [novoResponsavel, setNovoResponsavel] = useState<number | null>(null)
  const [erro, setErro] = useState('')
  const editando = !!escoteiro

  useEffect(() => {
    let ativo = true
    if (tropa !== null) {
      listarPatrulhas(tropa)
        .then((lista) => ativo && setPatrulhas(lista))
        .catch((falha: unknown) => ativo && setErro(mensagemDeErro(falha, 'Não foi possível carregar as patrulhas.')))
    }
    return () => {
      ativo = false
    }
  }, [tropa])

  useEffect(() => {
    if (!escoteiro) return
    let ativo = true
    Promise.all([buscarEscoteiro(escoteiro.membro.mem_cod), listarMembros(true)])
      .then(([atual, lista]) => {
        if (!ativo) return
        setResponsaveis(atual.responsaveis)
        setAdultos(lista)
      })
      .catch((falha: unknown) => ativo && setErro(mensagemDeErro(falha, 'Não foi possível carregar os responsáveis.')))
    return () => {
      ativo = false
    }
  }, [escoteiro])

  function aoMudarTropa(cod: number | null) {
    setTropa(cod)
    setPatrulha(null) // a patrulha antiga pode não ser da tropa nova
    setPatrulhas([])
  }

  async function alterarVinculo(acao: () => Promise<Responsavel[]>) {
    setErro('')
    try {
      setResponsaveis(await acao())
      setNovoResponsavel(null)
    } catch (falha) {
      setErro(mensagemDeErro(falha, 'Não foi possível alterar o responsável.'))
    }
  }

  function confirmar() {
    setTentou(true)
    if (memCod !== null && categoria !== null && patrulha !== null) aoConfirmar({ memCod, categoria, patrulha })
  }

  const candidatos = adultos.filter(
    (m) => m.mem_cod !== escoteiro?.membro.mem_cod && !responsaveis.some((r) => r.membro.mem_cod === m.mem_cod),
  )

  return (
    <Formulario titulo={editando ? `Escoteiro: ${escoteiro.membro.nome}` : 'Novo escoteiro'} aoConfirmar={confirmar} aoCancelar={aoCancelar} carregando={carregando} largura="md">
      {erro && <Alert severity="error" onClose={() => setErro('')}>{erro}</Alert>}
      <CampoSelecao
        rotulo="Pessoa"
        valor={memCod}
        opcoes={(editando ? [escoteiro.membro] : pessoas).map((m) => ({ valor: m.mem_cod, rotulo: m.nome }))}
        aoMudar={setMemCod}
        desabilitado={editando}
        obrigatorio
        erro={tentou && memCod === null ? 'Escolha a pessoa' : undefined}
        ajuda={!editando && pessoas.length === 0 ? 'Todas as pessoas cadastradas já são escoteiras' : undefined}
      />
      <Box sx={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: 2 }}>
        <CampoSelecao
          rotulo="Categoria de mensalidade"
          valor={categoria}
          opcoes={categorias.map((c) => ({ valor: c.cat_mens_cod, rotulo: `${c.cat_mens_cod} - ${c.descricao}` }))}
          aoMudar={setCategoria}
          obrigatorio
          erro={tentou && categoria === null ? 'Escolha a categoria' : undefined}
        />
        <CampoSelecao
          rotulo="Tropa"
          valor={tropa}
          opcoes={tropas.map((t) => ({ valor: t.cod, rotulo: t.nome }))}
          aoMudar={aoMudarTropa}
          obrigatorio
          erro={tentou && tropa === null ? 'Escolha a tropa' : undefined}
        />
        <CampoSelecao
          rotulo="Patrulha"
          valor={patrulha}
          opcoes={patrulhas.map((p) => ({ valor: p.cod, rotulo: p.nome }))}
          aoMudar={setPatrulha}
          desabilitado={tropa === null}
          obrigatorio
          erro={tentou && patrulha === null ? 'Escolha a patrulha' : undefined}
          ajuda={tropa === null ? 'Escolha a tropa primeiro' : undefined}
        />
      </Box>

      {editando && (
        <>
          <h3>Responsáveis</h3>
          <Stack direction="row" spacing={1} useFlexGap sx={{ flexWrap: 'wrap' }}>
            {responsaveis.length === 0 && <span>Nenhum responsável vinculado.</span>}
            {responsaveis.map((r) => (
              <Chip
                key={r.membro.mem_cod}
                label={r.membro.nome}
                onDelete={() => alterarVinculo(() => desvincularResponsavel(escoteiro.membro.mem_cod, r.membro.mem_cod))}
              />
            ))}
          </Stack>
          <Box sx={{ display: 'flex', gap: 1, alignItems: 'flex-start' }}>
            <Box sx={{ flex: 1 }}>
              <CampoSelecao
                rotulo="Adicionar responsável"
                valor={novoResponsavel}
                opcoes={candidatos.map((m) => ({ valor: m.mem_cod, rotulo: m.nome }))}
                aoMudar={setNovoResponsavel}
                ajuda="Só pessoas com 18 anos ou mais"
              />
            </Box>
            <Button
              variant="outlined"
              disabled={novoResponsavel === null}
              onClick={() => novoResponsavel !== null && alterarVinculo(() => vincularResponsavel(escoteiro.membro.mem_cod, novoResponsavel))}
            >
              Adicionar
            </Button>
          </Box>
        </>
      )}
    </Formulario>
  )
}
