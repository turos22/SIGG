import { useState } from 'react'
import { Navigate, useLocation, useNavigate, useSearchParams } from 'react-router'
import logoEscoteiros from '@/assets/logo_escoteiros.png'
import ConfirmacaoDeConta from '@/components/autenticacao/ConfirmacaoDeConta'
import FormCriarConta from '@/components/autenticacao/FormCriarConta'
import FormEntrar from '@/components/autenticacao/FormEntrar'
import FormRecuperarSenha from '@/components/autenticacao/FormRecuperarSenha'
import FormRedefinirSenha from '@/components/autenticacao/FormRedefinirSenha'
import { useSessao } from '@/data/contexts/SessaoContexto'
import { AUTENTICACAO_ATIVA } from '@/data/services/sessao'
import './page.css'

type Modo = 'entrar' | 'criar' | 'recuperar' | 'redefinir' | 'confirmar'

const TITULO_DO_MODO: Record<Modo, string> = {
  entrar: 'Acesse sua conta',
  criar: 'Criar conta',
  recuperar: 'Recuperar senha',
  redefinir: 'Definir nova senha',
  confirmar: 'Confirmação de cadastro',
}

export default function PaginaLogin() {
  const [parametros] = useSearchParams()
  const tokenDeRedefinicao = parametros.get('token')
  const tokenDeConfirmacao = parametros.get('confirmar')
  const [modo, setModo] = useState<Modo>(tokenDeRedefinicao ? 'redefinir' : tokenDeConfirmacao ? 'confirmar' : 'entrar')
  const { usuario } = useSessao()
  const navegar = useNavigate()
  const local = useLocation()
  const destino = (local.state as { de?: string } | null)?.de ?? '/'

  if (!AUTENTICACAO_ATIVA || usuario) return <Navigate to={destino} replace />

  return (
    <main className="login">
      <div className="login-cartao">
        <header className="login-marca">
          <div className="login-logo">
            <img src={logoEscoteiros} alt="Escoteiros do Brasil" />
          </div>
          <h1>SIGG</h1>
          <p>Sistema Integrado de Gestão Guayporé · Sempre Alerta</p>
        </header>

        <section className="cartao" style={{ display: 'grid', gap: 16 }}>
          <h2 className="login-titulo">{TITULO_DO_MODO[modo]}</h2>
          {modo === 'entrar' && (
            <FormEntrar
              aoEntrar={() => navegar(destino, { replace: true })}
              aoCriarConta={() => setModo('criar')}
              aoEsquecerSenha={() => setModo('recuperar')}
            />
          )}
          {modo === 'criar' && <FormCriarConta aoVoltar={() => setModo('entrar')} />}
          {modo === 'recuperar' && <FormRecuperarSenha aoVoltar={() => setModo('entrar')} />}
          {modo === 'confirmar' && tokenDeConfirmacao && (
            <ConfirmacaoDeConta
              token={tokenDeConfirmacao}
              aoVoltar={() => {
                navegar('/login', { replace: true })
                setModo('entrar')
              }}
            />
          )}
          {modo === 'redefinir' && tokenDeRedefinicao && (
            <FormRedefinirSenha
              token={tokenDeRedefinicao}
              aoVoltar={() => {
                navegar('/login', { replace: true })
                setModo('entrar')
              }}
            />
          )}
        </section>
      </div>
    </main>
  )
}
