import type Escoteiro from '@/data/models/Escoteiro'
import { formatarData } from '@/data/utils/datas'
import './CartaoEscoteiro.css'

interface CartaoEscoteiroProps {
  escoteiro: Escoteiro
}

// Componente: só mostra um escoteiro que recebeu por props. Não busca dados nem guarda estado.
export default function CartaoEscoteiro({ escoteiro }: CartaoEscoteiroProps) {
  return (
    <article className="cartao-escoteiro">
      <h2>{escoteiro.nome}</h2>
      <p>Patrulha: {escoteiro.patrulha}</p>
      <p>Nascimento: {formatarData(escoteiro.dataNascimento)}</p>
      <span className={escoteiro.inadimplente ? 'situacao inadimplente' : 'situacao em-dia'}>
        {escoteiro.inadimplente ? 'Inadimplente' : 'Em dia'}
      </span>
    </article>
  )
}
