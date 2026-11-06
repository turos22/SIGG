import { useState, type FormEvent } from 'react'
import Alert from '@mui/material/Alert'
import Box from '@mui/material/Box'
import { BotaoOk } from '@/components/formulario/Botoes'
import CampoCpf from '@/components/formulario/CampoCpf'
import CampoSenha from '@/components/formulario/CampoSenha'
import { useSessao } from '@/data/contexts/SessaoContexto'
import { mensagemDeErro } from '@/data/utils/erros'
import { cpfValido } from '@/data/utils/validadores'

interface FormEntrarProps {
  aoEntrar: () => void
  aoCriarConta: () => void
  aoEsquecerSenha: () => void
}

export default function FormEntrar({ aoEntrar, aoCriarConta, aoEsquecerSenha }: FormEntrarProps) {
  const { entrar } = useSessao()
  const [cpf, setCpf] = useState('')
  const [senha, setSenha] = useState('')
  const [erros, setErros] = useState<Map<string, string>>(new Map())
  const [erro, setErro] = useState('')
  const [carregando, setCarregando] = useState(false)

  const enviar = async (evento: FormEvent) => {
    evento.preventDefault()
    const problemas = new Map<string, string>()
    if (!cpfValido(cpf)) problemas.set('cpf', 'Informe um CPF válido')
    if (!senha) problemas.set('senha', 'Informe a senha')
    setErros(problemas)
    if (problemas.size > 0) return
    setCarregando(true)
    setErro('')
    try {
      await entrar(cpf, senha)
      aoEntrar()
    } catch (falha) {
      setErro(mensagemDeErro(falha, 'Não foi possível entrar.'))
    } finally {
      setCarregando(false)
    }
  }

  return (
    <Box component="form" noValidate onSubmit={enviar} sx={{ display: 'grid', gap: 2 }}>
      {erro && <Alert severity="error">{erro}</Alert>}
      <CampoCpf rotulo="CPF" valor={cpf} aoMudar={setCpf} erro={erros.get('cpf')} obrigatorio />
      <CampoSenha rotulo="Senha" valor={senha} aoMudar={setSenha} autoComplete="current-password" erro={erros.get('senha')} obrigatorio />
      <BotaoOk rotulo="Entrar" carregando={carregando} />
      <div className="login-links">
        <button type="button" className="login-link" onClick={aoCriarConta}>Criar conta</button>
        <button type="button" className="login-link" onClick={aoEsquecerSenha}>Esqueci minha senha</button>
      </div>
    </Box>
  )
}
