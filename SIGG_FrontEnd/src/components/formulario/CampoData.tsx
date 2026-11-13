import TextField from '@mui/material/TextField'
import { adornoDeErro } from './adornoDeErro'
import type { CampoBaseProps } from './tipos'

interface CampoDataProps extends CampoBaseProps {
  valor: string 
  aoMudar: (valor: string) => void
  minimo?: string
  maximo?: string
}

export default function CampoData({
  rotulo, valor, aoMudar, minimo, maximo, obrigatorio, desabilitado, erro, ajuda,
}: CampoDataProps) {
  return (
    <TextField
      fullWidth
      size="small"
      type="date"
      label={rotulo}
      value={valor}
      required={obrigatorio}
      disabled={desabilitado}
      error={!!erro}
      helperText={erro ?? ajuda}
      onChange={(evento) => aoMudar(evento.target.value)}
      slotProps={{ inputLabel: { shrink: true }, htmlInput: { min: minimo, max: maximo }, input: { endAdornment: adornoDeErro(erro) } }}
    />
  )
}
