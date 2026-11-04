import type { DadosEscoteiro } from '@/components/escoteiros/FormsEscoteiros'
import type Membro from '@/data/models/Membro'

export function membroParaDados(membro: Membro): Partial<DadosEscoteiro> {
  return {
    nome: membro.nome,
    dt_nascto: membro.dt_nascto,
    CPF: membro.CPF,
    RG: membro.RG,
    CEP: membro.CEP,
    endereco: membro.endereco,
    endereco_num: membro.endereco_num,
    endereco_bairro: membro.endereco_bairro,
    cidade: membro.cidade,
    estado: membro.estado,
    telefone: membro.telefone ?? '',
    email: membro.email ?? '',
  }
}
