import InputAdornment from '@mui/material/InputAdornment'
import TextField from '@mui/material/TextField'
import { adornoDeErro } from './adornoDeErro'
import type { CampoBaseProps } from './tipos'

interface CampoMoedaProps extends CampoBaseProps {
  valor: string // texto digitado, com ponto ou vírgula ("150,50")
  aoMudar: (valor: string) => void
}

// Campo de valor em reais: aceita só dígitos e uma vírgula/ponto com até 2 casas
export default function CampoMoeda({ rotulo, valor, aoMudar, obrigatorio, desabilitado, erro, ajuda }: CampoMoedaProps) {
  return (
    <TextField
      fullWidth
      size="small"
      label={rotulo}
      value={valor}
      required={obrigatorio}
      disabled={desabilitado}
      error={!!erro}
      helperText={erro ?? ajuda}
      onChange={(evento) => {
        const texto = evento.target.value
        if (/^\d*([.,]\d{0,2})?$/.test(texto)) aoMudar(texto)
      }}
      slotProps={{
        htmlInput: { inputMode: 'decimal' },
        input: {
          startAdornment: <InputAdornment position="start">R$</InputAdornment>,
          endAdornment: adornoDeErro(erro),
        },
      }}
    />
  )
}
