import { createBrowserRouter } from 'react-router'
import Layout from '@/app/layout'
import PaginaLogin from '@/app/login/page'
import RotaProtegida from '@/components/autenticacao/RotaProtegida'
import { PERMISSOES } from '@/data/utils/permissoes'
import PaginaInicial from '@/app/page'
import PaginaMembros from '@/app/membros/page'
import PaginaTropa from '@/app/tropa/page'
import PaginaMensalidades from '@/app/mensalidades/page'
import PaginaContribuicoes from '@/app/contribuicoes/page'

export const rotas = createBrowserRouter([
  {
    path: '/',
    element: (
      <RotaProtegida>
        <Layout />
      </RotaProtegida>
    ),
    children: [
      { index: true, element: <PaginaInicial /> },
      { path: 'membros', element: <RotaProtegida perfis={PERMISSOES['/membros']}><PaginaMembros /></RotaProtegida> },
      { path: 'tropa', element: <RotaProtegida perfis={PERMISSOES['/tropa']}><PaginaTropa /></RotaProtegida> },
      { path: 'mensalidades', element: <PaginaMensalidades /> },
      { path: 'contribuicoes', element: <RotaProtegida perfis={PERMISSOES['/contribuicoes']}><PaginaContribuicoes /></RotaProtegida> },
    ],
  },
  { path: '/login', element: <PaginaLogin /> },
])
