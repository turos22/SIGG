import './BemVindo.css'

interface BemVindoProps {
  titulo: string
  descricao: string
  selo?: string
}

export default function BemVindo({ titulo, descricao, selo }: BemVindoProps) {
  return (
    <section className="bem-vindo">
      <div>
        <h1>{titulo}</h1>
        <p>{descricao}</p>
      </div>
      {selo && <span className="bem-vindo-selo">{selo}</span>}
    </section>
  )
}
