import Button from '@mui/material/Button'
import CircularProgress from '@mui/material/CircularProgress'

interface BotaoOkProps {
  rotulo?: string
  aoClicar?: () => void
  tipo?: 'submit' | 'button'
  desabilitado?: boolean
  carregando?: boolean
}

export function BotaoOk({ rotulo = 'OK', aoClicar, tipo = 'submit', desabilitado, carregando }: BotaoOkProps) {
  return (
    <Button
      variant="contained"
      type={tipo}
      onClick={aoClicar}
      disabled={desabilitado || carregando}
      startIcon={carregando ? <CircularProgress size={16} color="inherit" /> : undefined}
    >
      {rotulo}
    </Button>
  )
}

interface BotaoCancelarProps {
  rotulo?: string
  aoClicar: () => void
  desabilitado?: boolean
}

export function BotaoCancelar({ rotulo = 'Cancelar', aoClicar, desabilitado }: BotaoCancelarProps) {
  return (
    <Button variant="outlined" type="button" onClick={aoClicar} disabled={desabilitado}>
      {rotulo}
    </Button>
  )
}
