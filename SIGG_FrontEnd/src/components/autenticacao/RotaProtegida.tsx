import type { ReactNode } from 'react'
import Alert from '@mui/material/Alert'
import LinearProgress from '@mui/material/LinearProgress'
import { Navigate, Outlet, useLocation } from 'react-router'
import { useSessao } from '@/data/contexts/SessaoContexto'
import type { Perfil } from '@/data/models/Perfil'
import { AUTENTICACAO_ATIVA } from '@/data/services/sessao'

interface RotaProtegidaProps {
  perfis?: Perfil[]
  children?: ReactNode
}

export default function RotaProtegida({ perfis, children }: RotaProtegidaProps) {
  const { usuario, carregando } = useSessao()
  const local = useLocation()

  if (!AUTENTICACAO_ATIVA) return children ?? <Outlet />
  if (carregando) return <LinearProgress />
  if (!usuario) return <Navigate to="/login" replace state={{ de: local.pathname }} />
  if (perfis && !perfis.some((perfil) => usuario.perfis.includes(perfil))) {
    return (
      <main className="pagina">
        <Alert severity="warning">Seu perfil não tem permissão para acessar esta página.</Alert>
      </main>
    )
  }
  return children ?? <Outlet />
}
