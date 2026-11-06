import { useEffect, useState, type ReactNode } from 'react'
import { SessaoContexto } from '@/data/contexts/SessaoContexto'
import type Usuario from '@/data/models/Usuario'
import { buscarUsuarioAtual, encerrarSessao, entrarNoSistema } from '@/data/services/autenticacao'
import { AUTENTICACAO_ATIVA, registrarAoSessaoExpirar } from '@/data/services/sessao'

interface ProvedorSessaoProps {
  children: ReactNode
}

export default function ProvedorSessao({ children }: ProvedorSessaoProps) {
  const [usuario, setUsuario] = useState<Usuario | null>(null)
  const [carregando, setCarregando] = useState(AUTENTICACAO_ATIVA)

  const entrar = async (cpf: string, senha: string) => {
    setUsuario(await entrarNoSistema(cpf, senha))
  }

  const sair = async () => {
    try {
      await encerrarSessao()
    } finally {
      setUsuario(null)
    }
  }

  useEffect(() => {
    if (!AUTENTICACAO_ATIVA) return
    let ativo = true
    buscarUsuarioAtual()
      .then((atual) => ativo && setUsuario(atual))
      .catch(() => undefined)
      .finally(() => ativo && setCarregando(false))
    registrarAoSessaoExpirar(() => setUsuario(null))
    return () => {
      ativo = false
      registrarAoSessaoExpirar(null)
    }
  }, [])

  return (
    <SessaoContexto.Provider value={{ usuario, carregando, entrar, sair }}>
      {children}
    </SessaoContexto.Provider>
  )
}
