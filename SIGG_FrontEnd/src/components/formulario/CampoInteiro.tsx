import TextField from '@mui/material/TextField'
import type { CampoBaseProps } from './tipos'

interface CampoInteiroProps extends CampoBaseProps {
  valor: number | null // null = campo vazio
  aoMudar: (valor: number | null) => void
  minimo?: number
  maximo?: number
}

// Campo de número inteiro não negativo (Int): ignora qualquer tecla que não seja dígito
export default function CampoInteiro({
  rotulo, valor, aoMudar, minimo, maximo, obrigatorio, desabilitado, erro, ajuda,
}: CampoInteiroProps) {
  const foraDoLimite =
    valor !== null && ((minimo !== undefined && valor < minimo) || (maximo !== undefined && valor > maximo))
  const mensagemLimite = foraDoLimite ? `Informe um valor entre ${minimo ?? 0} e ${maximo ?? '∞'}` : undefined

  return (
    <TextField
      fullWidth
      size="small"
      label={rotulo}
      value={valor === null ? '' : String(valor)}
      required={obrigatorio}
      disabled={desabilitado}
      error={!!erro || foraDoLimite}
      helperText={erro ?? mensagemLimite ?? ajuda}
      onChange={(evento) => {
        const texto = evento.target.value
        if (!/^\d*$/.test(texto)) return
        aoMudar(texto === '' ? null : Number(texto))
      }}
      slotProps={{ htmlInput: { inputMode: 'numeric', pattern: '[0-9]*' } }}
    />
  )
}
