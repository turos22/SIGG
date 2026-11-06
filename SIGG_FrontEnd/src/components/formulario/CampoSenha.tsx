import { useState } from 'react'
import IconButton from '@mui/material/IconButton'
import InputAdornment from '@mui/material/InputAdornment'
import TextField from '@mui/material/TextField'
import Visibilidade from '@mui/icons-material/Visibility'
import VisibilidadeDesligada from '@mui/icons-material/VisibilityOff'
import type { CampoBaseProps } from './tipos'

interface CampoSenhaProps extends CampoBaseProps {
  valor: string
  aoMudar: (valor: string) => void
  autoComplete: 'current-password' | 'new-password'
}

export default function CampoSenha({
  rotulo, valor, aoMudar, autoComplete, obrigatorio, desabilitado, erro, ajuda,
}: CampoSenhaProps) {
  const [visivel, setVisivel] = useState(false)

  return (
    <TextField
      fullWidth
      size="small"
      type={visivel ? 'text' : 'password'}
      label={rotulo}
      value={valor}
      required={obrigatorio}
      disabled={desabilitado}
      error={!!erro}
      helperText={erro ?? ajuda}
      onChange={(evento) => aoMudar(evento.target.value)}
      slotProps={{
        htmlInput: { maxLength: 72, autoComplete },
        input: {
          endAdornment: (
            <InputAdornment position="end">
              <IconButton
                edge="end"
                size="small"
                aria-label={visivel ? 'Ocultar senha' : 'Mostrar senha'}
                onClick={() => setVisivel(!visivel)}
              >
                {visivel ? <VisibilidadeDesligada fontSize="small" /> : <Visibilidade fontSize="small" />}
              </IconButton>
            </InputAdornment>
          ),
        },
      }}
    />
  )
}
