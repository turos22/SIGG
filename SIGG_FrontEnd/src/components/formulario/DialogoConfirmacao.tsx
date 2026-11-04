import type { ReactNode } from 'react'
import Button from '@mui/material/Button'
import CircularProgress from '@mui/material/CircularProgress'
import Dialog from '@mui/material/Dialog'
import DialogActions from '@mui/material/DialogActions'
import DialogContent from '@mui/material/DialogContent'
import DialogContentText from '@mui/material/DialogContentText'
import DialogTitle from '@mui/material/DialogTitle'

interface DialogoConfirmacaoProps {
  aberto: boolean
  mensagem: ReactNode
  aoConfirmar: () => void
  aoCancelar: () => void
  titulo?: string
  rotuloConfirmar?: string
  rotuloCancelar?: string
  carregando?: boolean 
}


export default function DialogoConfirmacao({
  aberto,
  mensagem,
  aoConfirmar,
  aoCancelar,
  titulo = 'Confirmar exclusão',
  rotuloConfirmar = 'Excluir',
  rotuloCancelar = 'Cancelar',
  carregando = false,
}: DialogoConfirmacaoProps) {
  return (
    <Dialog open={aberto} onClose={carregando ? undefined : aoCancelar} maxWidth="xs" fullWidth>
      <DialogTitle>{titulo}</DialogTitle>
      <DialogContent>
        <DialogContentText>{mensagem}</DialogContentText>
      </DialogContent>
      <DialogActions sx={{ px: 3, pb: 2 }}>
        <Button autoFocus variant="outlined" onClick={aoCancelar} disabled={carregando}>
          {rotuloCancelar}
        </Button>
        <Button
          variant="contained"
          color="error"
          onClick={aoConfirmar}
          disabled={carregando}
          startIcon={carregando ? <CircularProgress size={16} color="inherit" /> : undefined}
        >
          {rotuloConfirmar}
        </Button>
      </DialogActions>
    </Dialog>
  )
}
