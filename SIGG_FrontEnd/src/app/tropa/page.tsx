import { useState } from 'react'
import Button from '@mui/material/Button'
import Grade, { type ColunaGrade } from '@/components/grade/Grade'
import CampoTexto from '@/components/formulario/CampoTexto'
import DialogoConfirmacao from '@/components/formulario/DialogoConfirmacao'
import Formulario from '@/components/formulario/Formulario'
import type Tropa from '@/data/models/Tropa'

const TAMANHO_MAXIMO_NOME = 20 // tro_nome VARCHAR(20) no banco

const colunas: ColunaGrade<Tropa>[] = [
  { id: 'cod', titulo: 'Código', valor: (tropa) => tropa.cod, largura: 120 },
  { id: 'nome', titulo: 'Nome', valor: (tropa) => tropa.nome },
]

// CRUD em memória por enquanto: o backend ainda não tem endpoint de tropa
export default function PaginaTropa() {
  const [tropas, setTropas] = useState<Tropa[]>([])
  const [nome, setNome] = useState('')
  const [editando, setEditando] = useState<Tropa | null>(null)
  const [formularioAberto, setFormularioAberto] = useState(false)
  const [confirmandoExclusao, setConfirmandoExclusao] = useState(false)
  const [codSelecionado, setCodSelecionado] = useState<number | null>(null)
  const selecionada = tropas.find((t) => t.cod === codSelecionado) ?? null // sempre a versão atual da lista

  function salvar() {
    const nomeLimpo = nome.trim()
    if (!nomeLimpo) return

    if (editando) {
      setTropas(tropas.map((t) => (t.cod === editando.cod ? { ...t, nome: nomeLimpo } : t)))
    } else {
      const proximoCod = Math.max(0, ...tropas.map((t) => t.cod)) + 1
      setTropas([...tropas, { cod: proximoCod, nome: nomeLimpo }])
    }
    cancelar()
  }

  function novo() {
    setEditando(null)
    setNome('')
    setFormularioAberto(true)
  }

  function editar(tropa: Tropa) {
    setCodSelecionado(tropa.cod)
    setEditando(tropa)
    setNome(tropa.nome)
    setFormularioAberto(true)
  }

  function cancelar() {
    setEditando(null)
    setNome('')
    setFormularioAberto(false)
  }

  function excluir() {
    setConfirmandoExclusao(false)
    if (!selecionada) return
    setTropas(tropas.filter((t) => t.cod !== selecionada.cod))
    if (editando?.cod === selecionada.cod) cancelar()
    setCodSelecionado(null)
  }

  return (
    <main className="pagina">
      <header>
        <h1>Tropa</h1>
        <p className="subtitulo">Clique para selecionar, duplo clique para editar</p>
      </header>

      {formularioAberto && (
        <Formulario
          titulo={editando ? `Editando a tropa ${editando.cod}` : 'Nova tropa'}
          aoConfirmar={salvar}
          aoCancelar={cancelar}
          rotuloOk={editando ? 'Salvar alterações' : 'Adicionar'}
          desabilitarOk={!nome.trim()}
        >
          <CampoTexto rotulo="Nome da tropa" valor={nome} aoMudar={setNome} tamanhoMaximo={TAMANHO_MAXIMO_NOME} autoFocus obrigatorio />
        </Formulario>
      )}

      <Grade
        titulo="Tropas cadastradas"
        subtitulo={`${tropas.length} ${tropas.length === 1 ? 'tropa' : 'tropas'}`}
        colunas={colunas}
        linhas={tropas}
        chave={(tropa) => tropa.cod}
        textoVazio="Nenhuma tropa cadastrada ainda."
        aoClicarLinha={(tropa) => setCodSelecionado(tropa.cod)}
        aoDuploClique={editar}
        chaveSelecionada={codSelecionado}
        rodape={
          <>
            <Button variant="contained" onClick={novo}>
              Novo
            </Button>
            <Button variant="outlined" color="error" disabled={!selecionada} onClick={() => setConfirmandoExclusao(true)}>
              Excluir
            </Button>
          </>
        }
      />

      <DialogoConfirmacao
        aberto={confirmandoExclusao}
        mensagem={<>Excluir a tropa <strong>{selecionada?.nome}</strong>? Essa ação não pode ser desfeita.</>}
        aoConfirmar={excluir}
        aoCancelar={() => setConfirmandoExclusao(false)}
      />
    </main>
  )
}
