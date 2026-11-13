import { useEffect, useState, type ChangeEvent, type FormEvent } from 'react'
import Alert from '@mui/material/Alert'
import Box from '@mui/material/Box'
import Button from '@mui/material/Button'
import Stack from '@mui/material/Stack'
import Typography from '@mui/material/Typography'
import { BotaoCancelar, BotaoOk } from '@/components/formulario/Botoes'
import CampoTexto from '@/components/formulario/CampoTexto'
import IconeErro from '@/components/formulario/IconeErro'
import { LOGO_PADRAO, useParametrizacao } from '@/data/contexts/ParametrizacaoContexto'
import type Parametrizacao from '@/data/models/Parametrizacao'
import { alterarParametrizacao, buscarParametrizacao } from '@/data/services/parametrizacao'
import { mensagemDeErro } from '@/data/utils/erros'
import {
  errosDoBackendParametrizacao,
  validarParametrizacao,
  type DadosFormParametrizacao,
  type ErrosParametrizacao,
} from '@/data/utils/validacaoParametrizacao'

const FORM_VAZIO: DadosFormParametrizacao = {
  sigla: '',
  nomeSistema: '',
  slogan: '',
  nomeGrupo: '',
  endereco: '',
  cidade: '',
  estado: '',
  telefone: '',
  email: '',
}

const TAMANHO_MAXIMO_LOGO = 500 * 1024 // 500 KB, igual ao limite do backend
const TIPOS_DE_IMAGEM = ['image/png', 'image/jpeg', 'image/webp', 'image/gif']

function semCampo(erros: ErrosParametrizacao, campo: keyof ErrosParametrizacao): ErrosParametrizacao {
  const copia = { ...erros }
  delete copia[campo]
  return copia
}

// Texto vazio no formulário vira null no banco (campos opcionais)
function textoOuNulo(valor: string): string | null {
  const aparado = valor.trim()
  return aparado === '' ? null : aparado
}

function lerComoDataUrl(arquivo: File): Promise<string> {
  return new Promise((resolver, rejeitar) => {
    const leitor = new FileReader()
    leitor.onload = () => resolver(String(leitor.result))
    leitor.onerror = () => rejeitar(leitor.error)
    leitor.readAsDataURL(arquivo)
  })
}

export default function PaginaParametrizacao() {
  const { definir } = useParametrizacao()
  const [carregando, setCarregando] = useState(true)
  const [salvando, setSalvando] = useState(false)
  const [erro, setErro] = useState('')
  const [sucesso, setSucesso] = useState('')

  const [dados, setDados] = useState<DadosFormParametrizacao>(FORM_VAZIO)
  const [logo, setLogo] = useState<string | null>(null)
  // Padrão de validação do Bootstrap: os erros aparecem ao tentar salvar e, a partir daí, acompanham a digitação
  const [validado, setValidado] = useState(false)
  const [errosExtras, setErrosExtras] = useState<ErrosParametrizacao>({}) // logo e mensagens do backend

  const erros: ErrosParametrizacao = { ...(validado ? validarParametrizacao(dados) : {}), ...errosExtras }

  function mudar(campo: keyof DadosFormParametrizacao) {
    return (valor: string) => {
      setDados((atual) => ({ ...atual, [campo]: valor }))
      if (errosExtras[campo]) setErrosExtras((atual) => semCampo(atual, campo))
    }
  }

  function preencher(p: Parametrizacao) {
    setDados({
      sigla: p.sigla,
      nomeSistema: p.nomeSistema,
      slogan: p.slogan ?? '',
      nomeGrupo: p.nomeGrupo,
      endereco: p.endereco ?? '',
      cidade: p.cidade ?? '',
      estado: p.estado ?? '',
      telefone: p.telefone ?? '',
      email: p.email ?? '',
    })
    setLogo(p.logo)
  }

  useEffect(() => {
    let ativo = true
    buscarParametrizacao()
      .then((dados) => ativo && preencher(dados))
      .catch((falha: unknown) => ativo && setErro(mensagemDeErro(falha, 'Não foi possível carregar a parametrização.')))
      .finally(() => ativo && setCarregando(false))
    return () => {
      ativo = false
    }
  }, [])

  async function escolherLogo(evento: ChangeEvent<HTMLInputElement>) {
    const arquivo = evento.target.files?.[0]
    evento.target.value = '' // permite escolher o mesmo arquivo de novo
    if (!arquivo) return
    const definirErroLogo = (mensagem?: string) =>
      setErrosExtras((atual) => (mensagem ? { ...atual, logo: mensagem } : semCampo(atual, 'logo')))
    if (!TIPOS_DE_IMAGEM.includes(arquivo.type)) {
      definirErroLogo('O logo deve ser uma imagem PNG, JPG, WEBP ou GIF.')
      return
    }
    if (arquivo.size > TAMANHO_MAXIMO_LOGO) {
      definirErroLogo(`A imagem tem ${Math.ceil(arquivo.size / 1024)} KB; o máximo é 500 KB.`)
      return
    }
    try {
      setLogo(await lerComoDataUrl(arquivo))
      definirErroLogo()
    } catch {
      definirErroLogo('Não foi possível ler a imagem escolhida.')
    }
  }

  async function salvar(evento: FormEvent) {
    evento.preventDefault()
    setErro('')
    setSucesso('')
    setValidado(true)
    if (Object.keys(validarParametrizacao(dados)).length > 0 || errosExtras.logo) return
    setSalvando(true)
    try {
      const salva = await alterarParametrizacao({
        sigla: dados.sigla.trim(),
        nomeSistema: dados.nomeSistema.trim(),
        slogan: textoOuNulo(dados.slogan),
        nomeGrupo: dados.nomeGrupo.trim(),
        endereco: textoOuNulo(dados.endereco),
        cidade: textoOuNulo(dados.cidade),
        estado: textoOuNulo(dados.estado.toUpperCase()),
        telefone: textoOuNulo(dados.telefone.replace(/\D/g, '')),
        email: textoOuNulo(dados.email),
        logo,
      })
      preencher(salva)
      setValidado(false)
      definir(salva) // menu, login, recibos e título da aba já passam a usar os dados novos
      setSucesso('Parametrização salva com sucesso!')
    } catch (falha) {
      const mensagem = mensagemDeErro(falha, 'Não foi possível salvar a parametrização.')
      const porCampo = errosDoBackendParametrizacao(mensagem)
      if (porCampo) setErrosExtras((atual) => ({ ...atual, ...porCampo }))
      else setErro(mensagem)
    } finally {
      setSalvando(false)
    }
  }

  return (
    <main className="pagina">
      <header>
        <h1>Parametrização do sistema</h1>
        <p className="subtitulo">Nome, logo e informações do grupo exibidos no sistema, nos recibos e na tela de login</p>
      </header>

      {erro && <Alert severity="error" sx={{ mb: 2 }}>{erro}</Alert>}
      {sucesso && <Alert severity="success" sx={{ mb: 2 }}>{sucesso}</Alert>}

      <Box component="form" onSubmit={salvar} noValidate className="cartao" sx={{ display: 'grid', gap: 3 }}>
        <Stack direction={{ xs: 'column', sm: 'row' }} spacing={3} sx={{ alignItems: { sm: 'center' } }}>
          <Box
            sx={{
              width: 120, height: 120, borderRadius: 2, bgcolor: '#fff',
              border: erros.logo ? '2px solid' : '1px solid var(--border)', borderColor: erros.logo ? 'error.main' : undefined,
              display: 'flex', alignItems: 'center', justifyContent: 'center', overflow: 'hidden', flexShrink: 0,
            }}
          >
            <img src={logo || LOGO_PADRAO} alt="Pré-visualização do logo" style={{ maxWidth: '100%', maxHeight: '100%' }} />
          </Box>
          <Stack spacing={1}>
            <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>Logo</Typography>
            <Typography variant="body2" color="text.secondary">
              PNG, JPG, WEBP ou GIF de até 500 KB. {logo ? '' : 'No momento é usado o logo padrão do projeto.'}
            </Typography>
            <Stack direction="row" spacing={1}>
              <Button variant="outlined" component="label" disabled={carregando}>
                Escolher imagem
                <input hidden type="file" accept={TIPOS_DE_IMAGEM.join(',')} onChange={escolherLogo} />
              </Button>
              {logo && (
                <BotaoCancelar
                  rotulo="Usar logo padrão"
                  aoClicar={() => {
                    setLogo(null)
                    setErrosExtras((atual) => semCampo(atual, 'logo'))
                  }}
                />
              )}
            </Stack>
            {erros.logo && (
              <Typography variant="caption" color="error" role="alert" sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                <IconeErro /> {erros.logo}
              </Typography>
            )}
          </Stack>
        </Stack>

        <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>Sistema</Typography>
        <Box sx={{ display: 'grid', gap: 2, gridTemplateColumns: { xs: '1fr', sm: '1fr 2fr' } }}>
          <CampoTexto rotulo="Sigla" valor={dados.sigla} aoMudar={mudar('sigla')} erro={erros.sigla} tamanhoMaximo={20} obrigatorio desabilitado={carregando} />
          <CampoTexto rotulo="Nome do sistema" valor={dados.nomeSistema} aoMudar={mudar('nomeSistema')} erro={erros.nomeSistema} tamanhoMaximo={80} obrigatorio desabilitado={carregando} />
        </Box>
        <CampoTexto rotulo="Slogan" valor={dados.slogan} aoMudar={mudar('slogan')} erro={erros.slogan} tamanhoMaximo={50} desabilitado={carregando} ajuda="Aparece abaixo da sigla, no menu e no login" />

        <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>Grupo</Typography>
        <CampoTexto rotulo="Nome do grupo" valor={dados.nomeGrupo} aoMudar={mudar('nomeGrupo')} erro={erros.nomeGrupo} tamanhoMaximo={80} obrigatorio desabilitado={carregando} ajuda="Usado no cabeçalho dos recibos" />
        <CampoTexto rotulo="Endereço" valor={dados.endereco} aoMudar={mudar('endereco')} erro={erros.endereco} tamanhoMaximo={150} desabilitado={carregando} />
        <Box sx={{ display: 'grid', gap: 2, gridTemplateColumns: { xs: '1fr', sm: '2fr 1fr' } }}>
          <CampoTexto rotulo="Cidade" valor={dados.cidade} aoMudar={mudar('cidade')} erro={erros.cidade} tamanhoMaximo={50} desabilitado={carregando} />
          <CampoTexto rotulo="UF" valor={dados.estado} aoMudar={(v) => mudar('estado')(v.toUpperCase())} erro={erros.estado} tamanhoMaximo={2} desabilitado={carregando} />
        </Box>
        <Box sx={{ display: 'grid', gap: 2, gridTemplateColumns: { xs: '1fr', sm: '1fr 2fr' } }}>
          <CampoTexto rotulo="Telefone (DDD + número)" tipo="tel" valor={dados.telefone} aoMudar={mudar('telefone')} erro={erros.telefone} tamanhoMaximo={15} desabilitado={carregando} />
          <CampoTexto rotulo="E-mail" tipo="email" valor={dados.email} aoMudar={mudar('email')} erro={erros.email} tamanhoMaximo={60} desabilitado={carregando} />
        </Box>

        <Box sx={{ display: 'flex', justifyContent: 'flex-end' }}>
          <BotaoOk rotulo="Salvar parametrização" carregando={salvando} desabilitado={carregando} />
        </Box>
      </Box>
    </main>
  )
}
