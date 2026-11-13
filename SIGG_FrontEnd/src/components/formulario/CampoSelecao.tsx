import MenuItem from '@mui/material/MenuItem'
import TextField from '@mui/material/TextField'
import { adornoDeErro } from './adornoDeErro'
import type { CampoBaseProps } from './tipos'

export interface OpcaoSelecao<T extends string | number> {
  valor: T
  rotulo: string
}

interface CampoSelecaoProps<T extends string | number> extends CampoBaseProps {
  valor: T | null // null = nada escolhido
  opcoes: OpcaoSelecao<T>[]
  aoMudar: (valor: T | null) => void
}

// Lista de opções (combo box). O valor escolhido volta no tipo das opções, nunca como texto livre.
export default function CampoSelecao<T extends string | number>({
  rotulo, valor, opcoes, aoMudar, obrigatorio, desabilitado, erro, ajuda,
}: CampoSelecaoProps<T>) {
  return (
    <TextField
      select
      fullWidth
      size="small"
      label={rotulo}
      value={valor === null ? '' : String(valor)}
      required={obrigatorio}
      disabled={desabilitado}
      error={!!erro}
      helperText={erro ?? ajuda}
      onChange={(evento) => {
        const escolhida = opcoes.find((opcao) => String(opcao.valor) === evento.target.value)
        aoMudar(escolhida ? escolhida.valor : null)
      }}
      slotProps={{ input: { endAdornment: adornoDeErro(erro) } }}
    >
      {opcoes.map((opcao) => (
        <MenuItem key={opcao.valor} value={String(opcao.valor)}>{opcao.rotulo}</MenuItem>
      ))}
    </TextField>
  )
}
