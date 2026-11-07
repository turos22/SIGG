import { useState } from 'react'
import Alert from '@mui/material/Alert'
import Box from '@mui/material/Box'
import Button from '@mui/material/Button'
import Formulario from '@/components/formulario/Formulario'
import CampoSenha from '@/components/formulario/CampoSenha'
import type ContaAcesso from '@/data/models/ContaAcesso'
import { erroDeSenha, gerarSenhaTemporaria } from '@/data/utils/validadores'

interface FormSenhaDaContaProps {
  conta: ContaAcesso
  carregando: boolean
  aoConfirmar: (senha: string) => void
  aoCancelar: () => void
}

export default function FormSenhaDaConta({ conta, carregando, aoConfirmar, aoCancelar }: FormSenhaDaContaProps) {
  const [senha, setSenha] = useState('')
  const [confirmacao, setConfirmacao] = useState('')
  const [mostrarErros, setMostrarErros] = useState(false)

  const erroSenha = erroDeSenha(senha)
  const erroConfirmacao = confirmacao !== senha ? 'As senhas não conferem' : undefined

  const confirmar = () => {
    setMostrarErros(true)
    if (erroSenha || erroConfirmacao) return
    aoConfirmar(senha)
  }

  const gerar = () => {
    const nova = gerarSenhaTemporaria()
    setSenha(nova)
    setConfirmacao(nova)
  }

  return (
    <Formulario
      titulo={`Redefinir senha de ${conta.nome}`}
      aoConfirmar={confirmar}
      aoCancelar={aoCancelar}
      rotuloOk="Redefinir senha"
      carregando={carregando}
    >
      <Box sx={{ display: 'grid', gap: 2 }}>
        <Alert severity="info">
          O usuário terá as sessões encerradas e entrará com a nova senha. Informe-a a ele por um canal seguro.
        </Alert>
        <CampoSenha
          rotulo="Nova senha"
          valor={senha}
          aoMudar={setSenha}
          autoComplete="new-password"
          ajuda="Mínimo de 8 caracteres, com letras e números"
          erro={mostrarErros ? erroSenha : undefined}
          obrigatorio
        />
        <CampoSenha
          rotulo="Confirmar nova senha"
          valor={confirmacao}
          aoMudar={setConfirmacao}
          autoComplete="new-password"
          erro={mostrarErros ? erroConfirmacao : undefined}
          obrigatorio
        />
        <Button variant="outlined" size="small" onClick={gerar} sx={{ justifySelf: 'start' }}>
          Gerar senha forte
        </Button>
      </Box>
    </Formulario>
  )
}
