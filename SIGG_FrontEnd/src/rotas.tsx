import { createBrowserRouter } from 'react-router'
import Layout from '@/app/layout'
import PaginaInicial from '@/app/page'
import PaginaMembros from '@/app/membros/page'
import PaginaTropa from '@/app/tropa/page'

export const rotas = createBrowserRouter([
  {
    path: '/',
    element: <Layout />,
    children: [
      { index: true, element: <PaginaInicial /> },
      { path: 'membros', element: <PaginaMembros /> },
      { path: 'tropa', element: <PaginaTropa /> },
    ],
  },
])
