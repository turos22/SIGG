import TextField from '@mui/material/TextField'
import { adornoDeErro } from './adornoDeErro'
import type { CampoBaseProps } from './tipos'

interface CampoTextoProps extends CampoBaseProps {
  valor: string
  aoMudar: (valor: string) => void
  tamanhoMaximo?: number
  tipo?: 'text' | 'email' | 'tel' | 'password'
  placeholder?: string
  autoFocus?: boolean // foca o campo ao abrir (use no primeiro campo do modal)
  filtro?: (valor: string) => string // tira o que o campo não aceita, digitado ou colado (ver data/utils/filtros)
}

// Campo de uma linha (Edit)
export default function CampoTexto({
  rotulo, valor, aoMudar, tamanhoMaximo, tipo = 'text', placeholder, autoFocus, filtro, obrigatorio, desabilitado, erro, ajuda,
}: CampoTextoProps) {
  return (
    <TextField
      fullWidth
      autoFocus={autoFocus}
      size="small"
      type={tipo}
      label={rotulo}
      value={valor}
      placeholder={placeholder}
      required={obrigatorio}
      disabled={desabilitado}
      error={!!erro}
      helperText={erro ?? ajuda}
      onChange={(evento) => {
        const texto = filtro ? filtro(evento.target.value) : evento.target.value
        aoMudar(tamanhoMaximo !== undefined ? texto.slice(0, tamanhoMaximo) : texto)
      }}
      // com filtro, o limite é aplicado depois dele: o maxLength do navegador cortaria o texto colado antes
      slotProps={{ htmlInput: { maxLength: filtro ? undefined : tamanhoMaximo }, input: { endAdornment: adornoDeErro(erro) } }}
    />
  )
}
