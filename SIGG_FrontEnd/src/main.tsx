import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { RouterProvider } from 'react-router/dom'
import { ThemeProvider } from '@mui/material/styles'
import ProvedorSessao from '@/components/autenticacao/ProvedorSessao'
import { rotas } from '@/rotas'
import { tema } from '@/tema'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <ThemeProvider theme={tema}>
      <ProvedorSessao>
        <RouterProvider router={rotas} />
      </ProvedorSessao>
    </ThemeProvider>
  </StrictMode>,
)
