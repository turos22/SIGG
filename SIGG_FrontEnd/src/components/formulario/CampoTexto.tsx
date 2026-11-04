import TextField from '@mui/material/TextField'
import type { CampoBaseProps } from './tipos'

interface CampoTextoProps extends CampoBaseProps {
  valor: string
  aoMudar: (valor: string) => void
  tamanhoMaximo?: number
  tipo?: 'text' | 'email' | 'tel' | 'password'
  placeholder?: string
  autoFocus?: boolean // foca o campo ao abrir (use no primeiro campo do modal)
}

// Campo de uma linha (Edit)
export default function CampoTexto({
  rotulo, valor, aoMudar, tamanhoMaximo, tipo = 'text', placeholder, autoFocus, obrigatorio, desabilitado, erro, ajuda,
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
      onChange={(evento) => aoMudar(evento.target.value)}
      slotProps={{ htmlInput: { maxLength: tamanhoMaximo } }}
    />
  )
}
