import { UFS } from '@/data/utils/filtros'
import CampoSelecao from './CampoSelecao'
import type { CampoBaseProps } from './tipos'

interface CampoUfProps extends CampoBaseProps {
  valor: string
  aoMudar: (sigla: string) => void
}

const OPCOES = UFS.map((sigla) => ({ valor: sigla, rotulo: sigla }))

export default function CampoUf({ valor, aoMudar, ...base }: CampoUfProps) {
  return <CampoSelecao {...base} valor={valor || null} opcoes={OPCOES} aoMudar={(sigla) => aoMudar(sigla ?? '')} />
}
