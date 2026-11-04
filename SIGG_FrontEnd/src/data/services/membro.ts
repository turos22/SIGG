import type Chefe from '@/data/models/Chefe'
import type Escoteiro from '@/data/models/Escoteiro'
import type Financeiro from '@/data/models/Financeiro'
import type Presidente from '@/data/models/Presidente'
import type Responsavel from '@/data/models/Responsavel'
import type SaudeApi from '@/data/models/SaudeApi'
import { requisitar } from '@/data/services/api'
import type { DadosEscoteiro } from '@/components/escoteiros/FormsEscoteiros';

export function buscarSaudeDoBanco(): Promise<SaudeApi> {
  return requisitar<SaudeApi>('/health/db')
}

export function buscarEscoteiros(): Promise<Escoteiro[]> {
  return requisitar<Escoteiro[]>('/escoteiros')
}

export function buscarChefes(): Promise<Chefe[]> {
  return requisitar<Chefe[]>('/chefes')
}

export function buscarPresidentes(): Promise<Presidente[]> {
  return requisitar<Presidente[]>('/presidentes')
}

export function buscarResponsaveis(): Promise<Responsavel[]> {
  return requisitar<Responsavel[]>('/responsaveis')
}

export function buscarFinanceiros(): Promise<Financeiro[]> {
  return requisitar<Financeiro[]>('/financeiros')
}

export async function PostMembro(dados: DadosEscoteiro): Promise<Escoteiro> {
  const corpo = {
    membro: {
      nome: dados.nome,
      dt_nascto: dados.dt_nascto,
      CPF: dados.CPF,
      RG: dados.RG,
      CEP: dados.CEP,
      endereco: dados.endereco,
      endereco_num: dados.endereco_num,
      endereco_bairro: dados.endereco_bairro,
      cidade: dados.cidade,
      estado: dados.estado,
      telefone: dados.telefone || null, 
      email: dados.email || null,
    },
    cat_mensalidade: dados.cat_mensalidade,
    patrulha: { cod: dados.patrulha_cod },
  }
  return await requisitar<Escoteiro>('/escoteiros', {
    method: 'POST',
    body: JSON.stringify(corpo),
  })
}

export async function ExcluirMembro(cod: number): Promise<void> {
  return await requisitar<void>(`/membros/${cod}`, {
    method: 'DELETE',
  })
}
