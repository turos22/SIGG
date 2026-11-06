import { useEffect, useState } from 'react'
import Alert from '@mui/material/Alert'
import LinearProgress from '@mui/material/LinearProgress'
import DetalheMensalidades from '@/components/mensalidades/DetalheMensalidades'
import type SaldoMensalidades from '@/data/models/SaldoMensalidades'
import type Usuario from '@/data/models/Usuario'
import { buscarSaldoDoEscoteiro, buscarSaldoDoResponsavel } from '@/data/services/mensalidade'
import { mensagemDeErro } from '@/data/utils/erros'

interface MensalidadesPessoaisProps {
  usuario: Usuario
}

export default function MensalidadesPessoais({ usuario }: MensalidadesPessoaisProps) {
  const [saldos, setSaldos] = useState<SaldoMensalidades[]>([])
  const [carregando, setCarregando] = useState(true)
  const [erro, setErro] = useState('')

  useEffect(() => {
    let ativo = true
    const buscas: Promise<SaldoMensalidades[]>[] = []
    if (usuario.perfis.includes('RESPONSAVEL')) buscas.push(buscarSaldoDoResponsavel(usuario.mem_cod))
    if (usuario.perfis.includes('ESCOTEIRO')) buscas.push(buscarSaldoDoEscoteiro(usuario.mem_cod).then((saldo) => [saldo]))
    Promise.all(buscas)
      .then((listas) => {
        if (!ativo) return
        const porEscoteiro = new Map(listas.flat().map((saldo) => [saldo.esc_cod, saldo]))
        setSaldos([...porEscoteiro.values()])
      })
      .catch((falha: unknown) => ativo && setErro(mensagemDeErro(falha, 'Não foi possível calcular as mensalidades.')))
      .finally(() => ativo && setCarregando(false))
    return () => {
      ativo = false
    }
  }, [usuario])

  return (
    <main className="pagina">
      <header>
        <h1>Mensalidades</h1>
        <p className="subtitulo">Mensalidades em aberto e histórico de pagamentos</p>
      </header>
      {carregando && <LinearProgress />}
      {erro && <Alert severity="error">{erro}</Alert>}
      {!carregando && !erro && saldos.length === 0 && (
        <Alert severity="info">Não há escoteiros vinculados ao seu cadastro.</Alert>
      )}
      {saldos.map((saldo) => (
        <DetalheMensalidades key={saldo.esc_cod} saldo={saldo} />
      ))}
    </main>
  )
}
