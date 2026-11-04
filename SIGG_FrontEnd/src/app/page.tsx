import BemVindo from '@/components/template/BemVindo'
import logoEscoteiros from '@/assets/logo_escoteiros.png'
const mesAtual = new Date()
  .toLocaleDateString('pt-BR', { month: 'long', year: 'numeric' })
  .replace(/^./, (letra) => letra.toUpperCase())

export default function PaginaInicial() {
  return (
    <main className="pagina">
      <BemVindo
        titulo="Bem-vindo de volta, Chefe!"
        descricao="Resumo do Grupo Escoteiro Guayporé: presença, mensalidades e escoteiros."
        selo={mesAtual}
      />
      <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', flexDirection: 'row', gap: '30px', marginTop: '20px', height: '50vh' }}>
        <img src={logoEscoteiros} alt="Descrição da imagem" style={{ width: '200px', height: 'auto' }} />
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', textAlign: 'center' }}>
          <h1 style={{ fontSize: '4em', fontWeight: 'bold' }}>SIGG</h1>          
          <h1 style={{ fontSize: '2em', fontWeight: 'normal' }}>Sistema de Gestão do Grupo Escoteiro Guayporé</h1>  
        </div>        
      </div>
    </main>
  )
}
