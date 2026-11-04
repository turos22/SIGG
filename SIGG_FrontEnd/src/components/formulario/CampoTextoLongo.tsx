import TextField from '@mui/material/TextField'
import type { CampoBaseProps } from './tipos'

interface CampoTextoLongoProps extends CampoBaseProps {
  valor: string
  aoMudar: (valor: string) => void
  tamanhoMaximo?: number
  linhas?: number
}

export default function CampoTextoLongo({
  rotulo, valor, aoMudar, tamanhoMaximo, linhas = 3, obrigatorio, desabilitado, erro, ajuda,
}: CampoTextoLongoProps) {
  const contador = tamanhoMaximo ? `${valor.length}/${tamanhoMaximo}` : undefined
  return (
    <TextField
      fullWidth
      multiline
      minRows={linhas}
      size="small"
      label={rotulo}
      value={valor}
      required={obrigatorio}
      disabled={desabilitado}
      error={!!erro}
      helperText={erro ?? ajuda ?? contador}
      onChange={(evento) => aoMudar(evento.target.value)}
      slotProps={{ htmlInput: { maxLength: tamanhoMaximo } }}
    />
  )
}
