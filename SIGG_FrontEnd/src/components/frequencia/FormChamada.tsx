import { useEffect, useState } from 'react'
import Alert from '@mui/material/Alert'
import Box from '@mui/material/Box'
import ToggleButton from '@mui/material/ToggleButton'
import ToggleButtonGroup from '@mui/material/ToggleButtonGroup'
import CampoData from '@/components/formulario/CampoData'
import Formulario from '@/components/formulario/Formulario'
import type { ItemChamada, StatusPresenca } from '@/data/models/Chamada'
import type Tropa from '@/data/models/Tropa'
import { consultarChamada, type ItemParaGravar } from '@/data/services/frequencia'
import { mensagemDeErro } from '@/data/utils/erros'

const HOJE = new Date().toLocaleDateString('sv-SE')

interface FormChamadaProps {
  tropa: Tropa
  dataInicial?: string // presente = edição: a data não muda
  aoConfirmar: (data: string, itens: ItemParaGravar[]) => void
  aoCancelar: () => void
  carregando?: boolean
}

export default function FormChamada({ tropa, dataInicial, aoConfirmar, aoCancelar, carregando }: FormChamadaProps) {
  const edicao = dataInicial !== undefined
  const [data, setData] = useState(dataInicial ?? HOJE)
  const [itens, setItens] = useState<ItemChamada[]>([])
  const [status, setStatus] = useState<Record<number, StatusPresenca | null>>({})
  const [jaExiste, setJaExiste] = useState(false)
  const [erro, setErro] = useState('')

  useEffect(() => {
    if (!data || data > HOJE) return
    let ativo = true
    consultarChamada(tropa.cod, data)
      .then((chamada) => {
        if (!ativo) return
        const existente = chamada.itens.some((i) => i.status !== null)
        setItens(chamada.itens)
        setJaExiste(!edicao && existente)
        // chamada nova começa com todos presentes; na edição, quem não foi marcado fica sem status
        setStatus(Object.fromEntries(chamada.itens.map((i) => [i.esc_cod, edicao ? i.status : (i.status ?? 'P')])))
        setErro('')
      })
      .catch((falha: unknown) => ativo && setErro(mensagemDeErro(falha, 'Não foi possível carregar os escoteiros.')))
    return () => {
      ativo = false
    }
  }, [tropa.cod, data, edicao])

  function confirmar() {
    const marcados = itens.flatMap((i) => (status[i.esc_cod] ? [{ esc_cod: i.esc_cod, status: status[i.esc_cod] as StatusPresenca }] : []))
    if (marcados.length > 0) aoConfirmar(data, marcados)
  }

  const erroData = data > HOJE ? 'A data não pode ser futura' : undefined

  return (
    <Formulario
      titulo={edicao ? `Chamada de ${tropa.nome}` : `Nova chamada: ${tropa.nome}`}
      aoConfirmar={confirmar}
      aoCancelar={aoCancelar}
      carregando={carregando}
      desabilitarOk={jaExiste || !!erroData || itens.length === 0}
      largura="md"
    >
      {erro && <Alert severity="error">{erro}</Alert>}
      {jaExiste && <Alert severity="warning">Já existe frequência desta tropa nesta data. Edite a chamada existente.</Alert>}
      <CampoData rotulo="Data" valor={data} aoMudar={setData} maximo={HOJE} desabilitado={edicao} obrigatorio erro={erroData} />
      {itens.length === 0 && !erro && !erroData && <span>Nenhum escoteiro ativo nesta tropa.</span>}
      {itens.map((item) => (
        <Box key={item.esc_cod} sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 2, flexWrap: 'wrap' }}>
          <Box>
            <strong>{item.nome}</strong>
            <Box component="span" sx={{ ml: 1, opacity: 0.75, fontSize: '0.85rem' }}>
              {item.patrulha} · presença no ano: {item.percentual_ano.toLocaleString('pt-BR', { minimumFractionDigits: 1 })}%
            </Box>
          </Box>
          <ToggleButtonGroup
            exclusive
            size="small"
            value={status[item.esc_cod] ?? null}
            onChange={(_, novo: StatusPresenca | null) => setStatus((atual) => ({ ...atual, [item.esc_cod]: novo ?? atual[item.esc_cod] }))}
            aria-label={`Presença de ${item.nome}`}
          >
            <ToggleButton value="P" color="success">Presente</ToggleButton>
            <ToggleButton value="F" color="error">Falta</ToggleButton>
            <ToggleButton value="J" color="warning">Justificada</ToggleButton>
          </ToggleButtonGroup>
        </Box>
      ))}
    </Formulario>
  )
}
