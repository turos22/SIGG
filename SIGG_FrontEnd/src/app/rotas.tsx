import { createBrowserRouter } from 'react-router'
import Layout from '@/app/layout'
import PaginaInicial from '@/app/page'
import PaginaEscoteiros from '@/app/escoteiros/page'
import NaoEncontrada from '@/app/not-found'

// No Next, as pastas de app/ viram rotas sozinhas. Aqui (Vite + React Router) a tabela é explícita:
// cada página nova entra como mais uma linha em children, ex.: { path: 'escoteiros', element: <PaginaEscoteiros /> }
export const rotas = createBrowserRouter([
  {
    path: '/',
    element: <Layout />,
    children: [
      { index: true, element: <PaginaInicial /> },
      { path: 'escoteiros', element: <PaginaEscoteiros /> },
      { path: '*', element: <NaoEncontrada /> },
    ],
  },
])
