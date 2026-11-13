import { useState } from 'react'
import TextField from '@mui/material/TextField'
import { formatarCpfParcial } from '@/data/utils/formatadores'
import { cpfValido, somenteDigitos } from '@/data/utils/validadores'
import { adornoDeErro } from './adornoDeErro'
import type { CampoBaseProps } from './tipos'

interface CampoCpfProps extends CampoBaseProps {
  valor: string
  aoMudar: (digitos: string) => void
}

export default function CampoCpf({ rotulo, valor, aoMudar, obrigatorio, desabilitado, erro, ajuda }: CampoCpfProps) {
  const [tocado, setTocado] = useState(false)
  let erroDigitacao: string | undefined
  if (tocado && valor.length > 0) {
    if (valor.length < 11) erroDigitacao = 'CPF deve ter 11 dígitos'
    else if (!cpfValido(valor)) erroDigitacao = 'CPF inválido'
  }
  const mensagem = erro ?? erroDigitacao

  return (
    <TextField
      fullWidth
      size="small"
      label={rotulo}
      value={formatarCpfParcial(valor)}
      required={obrigatorio}
      disabled={desabilitado}
      error={!!mensagem}
      helperText={mensagem ?? ajuda}
      onChange={(evento) => aoMudar(somenteDigitos(evento.target.value).slice(0, 11))}
      onBlur={() => setTocado(true)}
      slotProps={{ htmlInput: { inputMode: 'numeric', autoComplete: 'off' }, input: { endAdornment: adornoDeErro(mensagem) } }}
    />
  )
}
