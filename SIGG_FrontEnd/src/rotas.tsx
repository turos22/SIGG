import { createBrowserRouter } from 'react-router'
import Layout from '@/app/layout'
import PaginaLogin from '@/app/login/page'
import RotaProtegida from '@/components/autenticacao/RotaProtegida'
import { PERMISSOES } from '@/data/utils/permissoes'
import PaginaInicial from '@/app/page'
import PaginaMembros from '@/app/membros/page'
import PaginaTropa from '@/app/tropa/page'
import PaginaVoluntarios from '@/app/voluntarios/page'
import PaginaMensalidades from '@/app/mensalidades/page'
import PaginaContribuicoes from '@/app/contribuicoes/page'
import PaginaContas from '@/app/contas/page'
import PaginaTiposAtividade from '@/app/tipos-atividade/page'
import PaginaCategoriasMensalidade from '@/app/categorias-mensalidade/page'
import PaginaPagamentosMensalidade from '@/app/pagamentos-mensalidade/page'
import PaginaPagamentosAtividade from '@/app/pagamentos-atividade/page'
import PaginaAtividadesPagas from '@/app/atividades-pagas/page'
import PaginaParametrizacao from '@/app/parametrizacao/page'

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
      { path: 'voluntarios', element: <RotaProtegida perfis={PERMISSOES['/voluntarios']}><PaginaVoluntarios /></RotaProtegida> },
      { path: 'tropa', element: <RotaProtegida perfis={PERMISSOES['/tropa']}><PaginaTropa /></RotaProtegida> },
      { path: 'mensalidades', element: <PaginaMensalidades /> },
      { path: 'contas', element: <RotaProtegida perfis={PERMISSOES['/contas']}><PaginaContas /></RotaProtegida> },
      { path: 'contribuicoes', element: <RotaProtegida perfis={PERMISSOES['/contribuicoes']}><PaginaContribuicoes /></RotaProtegida> },
      { path: 'tipos-atividade', element: <RotaProtegida perfis={PERMISSOES['/tipos-atividade']}><PaginaTiposAtividade /></RotaProtegida> },
      { path: 'categorias-mensalidade', element: <RotaProtegida perfis={PERMISSOES['/categorias-mensalidade']}><PaginaCategoriasMensalidade /></RotaProtegida> },
      { path: 'pagamentos-mensalidade', element: <RotaProtegida perfis={PERMISSOES['/pagamentos-mensalidade']}><PaginaPagamentosMensalidade /></RotaProtegida> },
      { path: 'pagamentos-atividade', element: <RotaProtegida perfis={PERMISSOES['/pagamentos-atividade']}><PaginaPagamentosAtividade /></RotaProtegida> },
      { path: 'atividades-pagas', element: <RotaProtegida perfis={PERMISSOES['/atividades-pagas']}><PaginaAtividadesPagas /></RotaProtegida> },
      { path: 'parametrizacao', element: <RotaProtegida perfis={PERMISSOES['/parametrizacao']}><PaginaParametrizacao /></RotaProtegida> },
    ],
  },
  { path: '/login', element: <PaginaLogin /> },
])
