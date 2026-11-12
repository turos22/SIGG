import BemVindo from '@/components/template/BemVindo'
import { useParametrizacao } from '@/data/contexts/ParametrizacaoContexto'
const mesAtual = new Date()
  .toLocaleDateString('pt-BR', { month: 'long', year: 'numeric' })
  .replace(/^./, (letra) => letra.toUpperCase())

export default function PaginaInicial() {
  const { parametrizacao, logo } = useParametrizacao()
  return (
    <main className="pagina">
      <BemVindo
        titulo="Bem-vindo de volta, Chefe!"
        descricao={`Resumo do ${parametrizacao.nomeGrupo}: presença, mensalidades e escoteiros.`}
        selo={mesAtual}
      />
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', flexDirection: 'row', gap: '30px', marginTop: '20px', height: '50vh' }}>
        <img src={logo} alt={`Logo ${parametrizacao.nomeGrupo}`} style={{ width: '200px', height: 'auto' }} />
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', textAlign: 'center' }}>
          <h1 style={{ fontSize: '4em', fontWeight: 'bold' }}>{parametrizacao.sigla}</h1>
          <h1 style={{ fontSize: '2em', fontWeight: 'normal' }}>{parametrizacao.nomeSistema}</h1>
        </div>
      </div>
    </main>
  )
}
