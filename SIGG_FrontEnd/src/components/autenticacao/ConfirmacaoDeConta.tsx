import { useEffect, useState } from 'react'
import Alert from '@mui/material/Alert'
import Box from '@mui/material/Box'
import LinearProgress from '@mui/material/LinearProgress'
import { BotaoOk } from '@/components/formulario/Botoes'
import { confirmarConta } from '@/data/services/autenticacao'
import { mensagemDeErro } from '@/data/utils/erros'

interface ConfirmacaoDeContaProps {
  token: string
  aoVoltar: () => void
}

export default function ConfirmacaoDeConta({ token, aoVoltar }: ConfirmacaoDeContaProps) {
  const [sucesso, setSucesso] = useState('')
  const [erro, setErro] = useState('')

  useEffect(() => {
    let ativo = true
    confirmarConta(token)
      .then((resposta) => ativo && setSucesso(resposta.mensagem))
      .catch((falha: unknown) => ativo && setErro(mensagemDeErro(falha, 'Não foi possível confirmar a conta.')))
    return () => {
      ativo = false
    }
  }, [token])

  return (
    <Box sx={{ display: 'grid', gap: 2 }}>
      {!sucesso && !erro && <LinearProgress />}
      {sucesso && <Alert severity="success">{sucesso}</Alert>}
      {erro && <Alert severity="error">{erro}</Alert>}
      {(sucesso || erro) && <BotaoOk rotulo="Ir para o login" tipo="button" aoClicar={aoVoltar} />}
    </Box>
  )
}
