import { useState } from 'react'
import CampoData from '@/components/formulario/CampoData'
import CampoSelecao from '@/components/formulario/CampoSelecao'
import Formulario from '@/components/formulario/Formulario'
import type Membro from '@/data/models/Membro'
import type Tropa from '@/data/models/Tropa'

export type PedidoPromocao =
  | { cargo: 'CHEFE'; tropa: number; inicio: string }
  | { cargo: 'PRESIDENTE'; posse: string; fim: string }
  | { cargo: 'FINANCEIRO' }

type Cargo = PedidoPromocao['cargo']

interface FormPromocaoProps {
  pessoa: Membro
  tropas: Tropa[]
  aoConfirmar: (pedido: PedidoPromocao) => void
  aoCancelar: () => void
  carregando?: boolean
}

const HOJE = new Date().toLocaleDateString('sv-SE')

function maisAnos(data: string, anos: number): string {
  const [ano, resto] = [Number(data.slice(0, 4)), data.slice(4)]
  return `${ano + anos}${resto}`
}

export default function FormPromocao({ pessoa, tropas, aoConfirmar, aoCancelar, carregando }: FormPromocaoProps) {
  const eEscoteiro = pessoa.papeis.includes('Escoteiro')
  const [cargo, setCargo] = useState<Cargo | null>(null)
  const [tropa, setTropa] = useState<number | null>(null)
  const [inicio, setInicio] = useState(HOJE)
  const [posse, setPosse] = useState(HOJE)
  const [fim, setFim] = useState(maisAnos(HOJE, 2))
  const [tentou, setTentou] = useState(false)

  // Chefe e presidente saem de escoteiro (regra do backend); financeiro, de qualquer pessoa
  const opcoes: { valor: Cargo; rotulo: string }[] = [
    ...(eEscoteiro ? [{ valor: 'CHEFE' as const, rotulo: 'Chefe de tropa' }, { valor: 'PRESIDENTE' as const, rotulo: 'Presidente (troca de posse)' }] : []),
    { valor: 'FINANCEIRO', rotulo: 'Financeiro' },
  ]

  const erroCargo = tentou && !cargo ? 'Escolha o cargo' : undefined
  const erroTropa = tentou && cargo === 'CHEFE' && tropa === null ? 'Escolha a tropa' : undefined
  const erroFim = tentou && cargo === 'PRESIDENTE' && fim < posse ? 'O fim não pode ser antes da posse' : undefined

  function confirmar() {
    setTentou(true)
    if (!cargo) return
    if (cargo === 'CHEFE' && tropa !== null && inicio) aoConfirmar({ cargo, tropa, inicio })
    else if (cargo === 'PRESIDENTE' && posse && fim && fim >= posse) aoConfirmar({ cargo, posse, fim })
    else if (cargo === 'FINANCEIRO') aoConfirmar({ cargo })
  }

  return (
    <Formulario titulo={`Promover ${pessoa.nome}`} aoConfirmar={confirmar} aoCancelar={aoCancelar} carregando={carregando} rotuloOk="Promover">
      <CampoSelecao
        rotulo="Cargo"
        valor={cargo}
        opcoes={opcoes}
        aoMudar={setCargo}
        obrigatorio
        erro={erroCargo}
        ajuda={eEscoteiro ? undefined : 'Só escoteiros podem ser chefe ou presidente. Cadastre esta pessoa como escoteira primeiro.'}
      />
      {cargo === 'CHEFE' && (
        <>
          <CampoSelecao rotulo="Tropa" valor={tropa} opcoes={tropas.map((t) => ({ valor: t.cod, rotulo: t.nome }))} aoMudar={setTropa} obrigatorio erro={erroTropa} />
          <CampoData rotulo="Início da gestão" valor={inicio} aoMudar={setInicio} obrigatorio />
        </>
      )}
      {cargo === 'PRESIDENTE' && (
        <>
          <CampoData rotulo="Data da posse" valor={posse} aoMudar={setPosse} obrigatorio ajuda="O presidente atual termina na véspera da posse" />
          <CampoData rotulo="Fim do mandato" valor={fim} aoMudar={setFim} minimo={posse} obrigatorio erro={erroFim} />
        </>
      )}
      <p style={{ margin: 0, fontSize: '0.85rem', opacity: 0.8 }}>O novo cargo vale a partir do próximo login da pessoa.</p>
    </Formulario>
  )
}
