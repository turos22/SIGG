import { useState, type FormEvent } from 'react'
import Alert from '@mui/material/Alert'
import Box from '@mui/material/Box'
import { BotaoOk } from '@/components/formulario/Botoes'
import CampoSenha from '@/components/formulario/CampoSenha'
import { redefinirSenha } from '@/data/services/autenticacao'
import { mensagemDeErro } from '@/data/utils/erros'
import { erroDeSenha } from '@/data/utils/validadores'

interface FormRedefinirSenhaProps {
  token: string
  aoVoltar: () => void
}

export default function FormRedefinirSenha({ token, aoVoltar }: FormRedefinirSenhaProps) {
  const [senha, setSenha] = useState('')
  const [confirmacao, setConfirmacao] = useState('')
  const [erros, setErros] = useState<Map<string, string>>(new Map())
  const [erro, setErro] = useState('')
  const [sucesso, setSucesso] = useState('')
  const [carregando, setCarregando] = useState(false)

  const enviar = async (evento: FormEvent) => {
    evento.preventDefault()
    const problemas = new Map<string, string>()
    const problemaDeSenha = erroDeSenha(senha)
    if (problemaDeSenha) problemas.set('senha', problemaDeSenha)
    if (confirmacao !== senha) problemas.set('confirmacao', 'As senhas não conferem')
    setErros(problemas)
    if (problemas.size > 0) return
    setCarregando(true)
    setErro('')
    try {
      const resposta = await redefinirSenha(token, senha)
      setSucesso(resposta.mensagem)
    } catch (falha) {
      setErro(mensagemDeErro(falha, 'Não foi possível redefinir a senha.'))
    } finally {
      setCarregando(false)
    }
  }

  if (sucesso) {
    return (
      <Box sx={{ display: 'grid', gap: 2 }}>
        <Alert severity="success">{sucesso}</Alert>
        <BotaoOk rotulo="Ir para o login" tipo="button" aoClicar={aoVoltar} />
      </Box>
    )
  }

  return (
    <Box component="form" noValidate onSubmit={enviar} sx={{ display: 'grid', gap: 2 }}>
      {erro && <Alert severity="error">{erro}</Alert>}
      <CampoSenha
        rotulo="Nova senha"
        valor={senha}
        aoMudar={setSenha}
        autoComplete="new-password"
        ajuda="Mínimo de 8 caracteres, com letras e números"
        erro={erros.get('senha')}
        obrigatorio
      />
      <CampoSenha rotulo="Confirmar nova senha" valor={confirmacao} aoMudar={setConfirmacao} autoComplete="new-password" erro={erros.get('confirmacao')} obrigatorio />
      <BotaoOk rotulo="Redefinir senha" carregando={carregando} />
    </Box>
  )
}
