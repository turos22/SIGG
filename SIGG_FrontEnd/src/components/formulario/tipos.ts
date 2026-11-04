// Propriedades comuns a todos os campos do formulário
export interface CampoBaseProps {
  rotulo: string
  obrigatorio?: boolean
  desabilitado?: boolean
  erro?: string 
  ajuda?: string 
}
