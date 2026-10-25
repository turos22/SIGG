import { useEffect, useState } from 'react'
import CartaoEscoteiro from '@/components/escoteiro/CartaoEscoteiro'
import type Escoteiro from '@/data/models/Escoteiro'
import { buscarEscoteiros } from '@/data/services/api'
import './page.css'

export default function PaginaEscoteiros() {
  // null = ainda carregando
  const [escoteiros, setEscoteiros] = useState<Escoteiro[] | null>(null)
  const [erro, setErro] = useState('')

  // Roda uma vez, quando a página aparece: busca os dados no backend
  useEffect(() => {
    buscarEscoteiros()
      .then(setEscoteiros)
      .catch(() => setErro('Não foi possível carregar os escoteiros. O backend está rodando?'))
  }, [])

  if (erro) return <p className="aviso-erro">{erro}</p>
  if (escoteiros === null) return <p className="carregando">Carregando...</p>

  const inadimplentes = escoteiros.filter((escoteiro) => escoteiro.inadimplente).length

  return (
    <section className="pagina-escoteiros">
      <h1>Escoteiros</h1>
      <p>{escoteiros.length} escoteiros ativos · {inadimplentes} inadimplentes</p>
      <div className="grade-escoteiros">
        {escoteiros.map((escoteiro) => (
          <CartaoEscoteiro key={escoteiro.id} escoteiro={escoteiro} />
        ))}
      </div>
    </section>
  )
}
