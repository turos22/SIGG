import { useState, type FormEvent } from 'react'
import Alert from '@mui/material/Alert'
import Box from '@mui/material/Box'
import { BotaoOk } from '@/components/formulario/Botoes'
import CampoCpf from '@/components/formulario/CampoCpf'
import CampoTexto from '@/components/formulario/CampoTexto'
import { solicitarRecuperacaoDeSenha } from '@/data/services/autenticacao'
import { mensagemDeErro } from '@/data/utils/erros'
import { cpfValido, emailValido } from '@/data/utils/validadores'

interface FormRecuperarSenhaProps {
  aoVoltar: () => void
}

export default function FormRecuperarSenha({ aoVoltar }: FormRecuperarSenhaProps) {
  const [cpf, setCpf] = useState('')
  const [email, setEmail] = useState('')
  const [erros, setErros] = useState<Map<string, string>>(new Map())
  const [erro, setErro] = useState('')
  const [sucesso, setSucesso] = useState('')
  const [carregando, setCarregando] = useState(false)

  const enviar = async (evento: FormEvent) => {
    evento.preventDefault()
    const problemas = new Map<string, string>()
    if (!cpfValido(cpf)) problemas.set('cpf', 'Informe um CPF válido')
    if (!emailValido(email)) problemas.set('email', 'E-mail inválido (até 40 caracteres)')
    setErros(problemas)
    if (problemas.size > 0) return
    setCarregando(true)
    setErro('')
    try {
      const resposta = await solicitarRecuperacaoDeSenha(cpf, email.trim())
      setSucesso(resposta.mensagem)
    } catch (falha) {
      setErro(mensagemDeErro(falha, 'Não foi possível solicitar a redefinição.'))
    } finally {
      setCarregando(false)
    }
  }

  if (sucesso) {
    return (
      <Box sx={{ display: 'grid', gap: 2 }}>
        <Alert severity="success">{sucesso}</Alert>
        <BotaoOk rotulo="Voltar ao login" tipo="button" aoClicar={aoVoltar} />
      </Box>
    )
  }

  return (
    <Box component="form" noValidate onSubmit={enviar} sx={{ display: 'grid', gap: 2 }}>
      <Alert severity="info">
        Informe o CPF e o e-mail cadastrados. Se os dados conferirem, enviaremos um link para criar uma nova senha.
        Sem acesso ao e-mail, procure um administrador do grupo.
      </Alert>
      {erro && <Alert severity="error">{erro}</Alert>}
      <CampoCpf rotulo="CPF" valor={cpf} aoMudar={setCpf} erro={erros.get('cpf')} obrigatorio />
      <CampoTexto rotulo="E-mail cadastrado" valor={email} aoMudar={setEmail} tipo="email" tamanhoMaximo={40} erro={erros.get('email')} obrigatorio />
      <BotaoOk rotulo="Enviar link" carregando={carregando} />
      <div className="login-links">
        <button type="button" className="login-link" onClick={aoVoltar}>Voltar ao login</button>
      </div>
    </Box>
  )
}
