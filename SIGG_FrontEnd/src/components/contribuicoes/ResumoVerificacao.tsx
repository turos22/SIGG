import type Escoteiro from '@/data/models/Escoteiro'
import { formatarCpf } from '@/data/utils/formatadores'
import './ResumoVerificacao.css'

interface ResumoVerificacaoProps {
  anjo: Escoteiro
  adotado: Escoteiro
}

interface PessoaProps {
  papel: string
  escoteiro: Escoteiro
}

function Pessoa({ papel, escoteiro }: PessoaProps) {
  return (
    <div className="resumo-verificacao-pessoa">
      <span>{papel}</span>
      <strong>{escoteiro.membro.nome}</strong>
      <span>CPF {formatarCpf(escoteiro.membro.CPF)}</span>
      <span>Matrícula {escoteiro.esc_cod} · {escoteiro.patrulha.nome}</span>
    </div>
  )
}

export default function ResumoVerificacao({ anjo, adotado }: ResumoVerificacaoProps) {
  return (
    <div className="resumo-verificacao">
      <Pessoa papel="Anjo (contribuinte)" escoteiro={anjo} />
      <Pessoa papel="Adotado" escoteiro={adotado} />
    </div>
  )
}
