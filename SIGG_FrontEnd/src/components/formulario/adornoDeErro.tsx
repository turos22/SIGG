import InputAdornment from '@mui/material/InputAdornment'
import IconeErro from './IconeErro'

// Adorno pronto para o endAdornment do TextField; undefined quando não há erro (o campo não ganha espaço extra)
export function adornoDeErro(erro: string | undefined) {
  if (!erro) return undefined
  return (
    <InputAdornment position="end">
      <IconeErro />
    </InputAdornment>
  )
}
