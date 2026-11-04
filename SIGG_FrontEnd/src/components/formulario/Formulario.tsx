import type { FormEvent, ReactNode } from 'react'
import Box from '@mui/material/Box'
import Dialog from '@mui/material/Dialog'
import DialogActions from '@mui/material/DialogActions'
import DialogContent from '@mui/material/DialogContent'
import DialogTitle from '@mui/material/DialogTitle'
import Stack from '@mui/material/Stack'
import { BotaoCancelar, BotaoOk } from './Botoes'

interface FormularioProps {
  titulo?: string
  aoConfirmar: () => void
  aoCancelar?: () => void // chamado pelo botão Cancelar, pelo Esc e pelo clique fora; sem ele, só o OK fecha
  rotuloOk?: string
  rotuloCancelar?: string
  desabilitarOk?: boolean
  carregando?: boolean
  aberto?: boolean // padrão true: abre ao ser montado, então {condicao && <Formulario />} continua valendo
  largura?: 'xs' | 'sm' | 'md' | 'lg'
  children: ReactNode // os campos
}

// Modal com os campos empilhados e os botões OK / Cancelar no rodapé.
// Enter nos campos e o clique em OK chamam aoConfirmar; a validação nativa (obrigatorio) roda antes.
// Se o conteúdo passar da altura da tela, só a área dos campos rola; título e botões ficam fixos.
export default function Formulario({
  titulo, aoConfirmar, aoCancelar, rotuloOk, rotuloCancelar, desabilitarOk, carregando,
  aberto = true, largura = 'sm', children,
}: FormularioProps) {
  return (
    <Dialog
      open={aberto}
      onClose={carregando ? undefined : aoCancelar}
      maxWidth={largura}
      fullWidth
      slotProps={{
        paper: {
          sx: { backgroundColor: 'var(--superficie)', backgroundImage: 'var(--trama), var(--fibra)', borderRadius: '14px' },
        },
      }}
    >
      <Box
        component="form"
        onSubmit={(evento: FormEvent) => {
          evento.preventDefault()
          aoConfirmar()
        }}
        sx={{ display: 'flex', flexDirection: 'column', minHeight: 0 }}
      >
        {titulo && <DialogTitle>{titulo}</DialogTitle>}
        <DialogContent>
          <Stack spacing={2} sx={{ pt: 1 }}>
            {children}
          </Stack>
        </DialogContent>
        <DialogActions sx={{ px: 3, pb: 2 }}>
          {aoCancelar && <BotaoCancelar rotulo={rotuloCancelar} aoClicar={aoCancelar} desabilitado={carregando} />}
          <BotaoOk rotulo={rotuloOk} desabilitado={desabilitarOk} carregando={carregando} />
        </DialogActions>
      </Box>
    </Dialog>
  )
}
