import ErroIcone from '@mui/icons-material/ErrorOutlineOutlined'

// Ícone de erro dentro do campo, no padrão de validação do Bootstrap (.is-invalid):
// borda vermelha + ícone à direita + mensagem vermelha abaixo (helperText).
export default function IconeErro() {
  return <ErroIcone color="error" fontSize="small" aria-hidden="true" />
}
