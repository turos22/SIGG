import { useState, type FormEvent } from 'react'
import Alert from '@mui/material/Alert'
import Box from '@mui/material/Box'
import { BotaoOk } from '@/components/formulario/Botoes'
import CampoCpf from '@/components/formulario/CampoCpf'
import CampoData from '@/components/formulario/CampoData'
import CampoSenha from '@/components/formulario/CampoSenha'
import CampoTexto from '@/components/formulario/CampoTexto'
import { criarConta } from '@/data/services/autenticacao'
import { mensagemDeErro } from '@/data/utils/erros'
import { cpfValido, dataDeNascimentoValida, emailValido, erroDeSenha } from '@/data/utils/validadores'

interface FormCriarContaProps {
  aoVoltar: () => void
}

const HOJE = new Date().toISOString().slice(0, 10)

export default function FormCriarConta({ aoVoltar }: FormCriarContaProps) {
  const [cpf, setCpf] = useState('')
  const [nascimento, setNascimento] = useState('')
  const [email, setEmail] = useState('')
  const [senha, setSenha] = useState('')
  const [confirmacao, setConfirmacao] = useState('')
  const [erros, setErros] = useState<Map<string, string>>(new Map())
  const [erro, setErro] = useState('')
  const [sucesso, setSucesso] = useState('')
  const [carregando, setCarregando] = useState(false)

  const enviar = async (evento: FormEvent) => {
    evento.preventDefault()
    const problemas = new Map<string, string>()
    if (!cpfValido(cpf)) problemas.set('cpf', 'Informe um CPF válido')
    if (!dataDeNascimentoValida(nascimento, HOJE)) problemas.set('nascimento', 'Informe uma data de nascimento válida')
    if (!emailValido(email)) problemas.set('email', 'E-mail inválido (até 40 caracteres)')
    const problemaDeSenha = erroDeSenha(senha)
    if (problemaDeSenha) problemas.set('senha', problemaDeSenha)
    if (confirmacao !== senha) problemas.set('confirmacao', 'As senhas não conferem')
    setErros(problemas)
    if (problemas.size > 0) return
    setCarregando(true)
    setErro('')
    try {
      const resposta = await criarConta({ cpf, dt_nascto: nascimento, email: email.trim(), senha })
      setSucesso(resposta.mensagem)
    } catch (falha) {
      setErro(mensagemDeErro(falha, 'Não foi possível criar a conta.'))
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
      {erro && <Alert severity="error">{erro}</Alert>}
      <CampoCpf rotulo="CPF" valor={cpf} aoMudar={setCpf} erro={erros.get('cpf')} obrigatorio />
      <CampoData rotulo="Data de nascimento" valor={nascimento} aoMudar={setNascimento} maximo={HOJE} erro={erros.get('nascimento')} obrigatorio />
      <CampoTexto rotulo="E-mail" valor={email} aoMudar={setEmail} tipo="email" tamanhoMaximo={40} erro={erros.get('email')} obrigatorio />
      <CampoSenha
        rotulo="Senha"
        valor={senha}
        aoMudar={setSenha}
        autoComplete="new-password"
        ajuda="Mínimo de 8 caracteres, com letras e números"
        erro={erros.get('senha')}
        obrigatorio
      />
      <CampoSenha rotulo="Confirmar senha" valor={confirmacao} aoMudar={setConfirmacao} autoComplete="new-password" erro={erros.get('confirmacao')} obrigatorio />
      <BotaoOk rotulo="Criar conta" carregando={carregando} />
      <div className="login-links">
        <button type="button" className="login-link" onClick={aoVoltar}>Já tenho conta</button>
      </div>
    </Box>
  )
}
