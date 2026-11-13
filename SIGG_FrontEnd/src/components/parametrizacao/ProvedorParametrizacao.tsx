import { useEffect, useState, type ReactNode } from 'react'
import {
  LOGO_PADRAO,
  PARAMETRIZACAO_PADRAO,
  ParametrizacaoContexto,
} from '@/data/contexts/ParametrizacaoContexto'
import type Parametrizacao from '@/data/models/Parametrizacao'
import { buscarParametrizacao } from '@/data/services/parametrizacao'

interface ProvedorParametrizacaoProps {
  children: ReactNode
}

// Busca a parametrização uma vez ao abrir o sistema e a entrega a todas as telas (nome, logo, dados do grupo).
export default function ProvedorParametrizacao({ children }: ProvedorParametrizacaoProps) {
  const [parametrizacao, setParametrizacao] = useState<Parametrizacao>(PARAMETRIZACAO_PADRAO)

  useEffect(() => {
    let ativo = true
    buscarParametrizacao()
      .then((dados) => ativo && setParametrizacao(dados))
      .catch(() => undefined) // backend fora do ar: segue com os valores padrão
    return () => {
      ativo = false
    }
  }, [])

  const logo = parametrizacao.logo || LOGO_PADRAO

  // Título da aba e ícone acompanham a parametrização
  useEffect(() => {
    document.title = `${parametrizacao.sigla} · ${parametrizacao.nomeSistema}`
  }, [parametrizacao.sigla, parametrizacao.nomeSistema])

  useEffect(() => {
    if (!parametrizacao.logo) return
    const icone = document.querySelector<HTMLLinkElement>('link[rel="icon"]')
    if (!icone) return
    const anterior = { href: icone.href, type: icone.type }
    icone.href = parametrizacao.logo
    icone.type = parametrizacao.logo.slice(5, parametrizacao.logo.indexOf(';'))
    return () => {
      icone.href = anterior.href
      icone.type = anterior.type
    }
  }, [parametrizacao.logo])

  return (
    <ParametrizacaoContexto.Provider value={{ parametrizacao, logo, definir: setParametrizacao }}>
      {children}
    </ParametrizacaoContexto.Provider>
  )
}
