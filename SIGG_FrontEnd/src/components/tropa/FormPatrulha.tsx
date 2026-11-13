import { useEffect, useState } from 'react'
import CampoSelecao from '@/components/formulario/CampoSelecao'
import CampoTexto from '@/components/formulario/CampoTexto'
import Formulario from '@/components/formulario/Formulario'
import type Escoteiro from '@/data/models/Escoteiro'
import type Patrulha from '@/data/models/Patrulha'
import type Tropa from '@/data/models/Tropa'
import { listarEscoteiros } from '@/data/services/escoteiro'
import type { DadosPatrulha } from '@/data/services/tropa'
import { semDigitos } from '@/data/utils/filtros'

const SEM_ESCOTEIRO = 0

interface FormPatrulhaProps {
  tropa: Tropa
  patrulha?: Patrulha // presente = edição
  aoConfirmar: (dados: DadosPatrulha) => void
  aoCancelar: () => void
  carregando?: boolean
}

export default function FormPatrulha({ tropa, patrulha, aoConfirmar, aoCancelar, carregando }: FormPatrulhaProps) {
  const [nome, setNome] = useState(patrulha?.nome ?? '')
  const [lider, setLider] = useState<number>(patrulha?.lider?.esc_cod ?? SEM_ESCOTEIRO)
  const [sublider, setSublider] = useState<number>(patrulha?.sublider?.esc_cod ?? SEM_ESCOTEIRO)
  const [candidatos, setCandidatos] = useState<Escoteiro[]>([])
  const [tentou, setTentou] = useState(false)

  useEffect(() => {
    let ativo = true
    listarEscoteiros(tropa.cod)
      .then((lista) => ativo && setCandidatos(lista))
      .catch(() => ativo && setCandidatos([]))
    return () => {
      ativo = false
    }
  }, [tropa.cod])

  const opcoes = [
    { valor: SEM_ESCOTEIRO, rotulo: 'Sem escoteiro' },
    ...candidatos.map((e) => ({ valor: e.esc_cod, rotulo: e.membro.nome })),
  ]
  const erroNome = tentou && !nome.trim() ? 'Obrigatório' : undefined
  const erroLider = lider !== SEM_ESCOTEIRO && lider === sublider ? 'Líder e sublíder devem ser pessoas diferentes' : undefined

  function confirmar() {
    setTentou(true)
    if (nome.trim() && !erroLider) aoConfirmar({ nome: nome.trim(), tropa: tropa.cod, lider, sublider })
  }

  return (
    <Formulario
      titulo={patrulha ? `Patrulha ${patrulha.nome}` : `Nova patrulha da ${tropa.nome}`}
      aoConfirmar={confirmar}
      aoCancelar={aoCancelar}
      carregando={carregando}
    >
      <CampoTexto rotulo="Nome da patrulha" valor={nome} aoMudar={setNome} filtro={semDigitos} tamanhoMaximo={20} autoFocus obrigatorio erro={erroNome} />
      <CampoSelecao rotulo="Líder" valor={lider} opcoes={opcoes} aoMudar={(v) => setLider(v ?? SEM_ESCOTEIRO)} ajuda="Opcional: escoteiro da tropa" />
      <CampoSelecao rotulo="Sublíder" valor={sublider} opcoes={opcoes} aoMudar={(v) => setSublider(v ?? SEM_ESCOTEIRO)} erro={erroLider} ajuda="Opcional" />
    </Formulario>
  )
}
