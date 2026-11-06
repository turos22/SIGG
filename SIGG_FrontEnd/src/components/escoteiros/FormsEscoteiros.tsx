import { useState } from 'react'
import Box from '@mui/material/Box'
import CampoCpf from '@/components/formulario/CampoCpf'
import CampoData from '@/components/formulario/CampoData'
import CampoInteiro from '@/components/formulario/CampoInteiro'
import CampoTexto from '@/components/formulario/CampoTexto'
import Formulario from '@/components/formulario/Formulario'
import { cpfValido } from '@/data/utils/validadores'

export interface DadosEscoteiro {
  nome: string
  dt_nascto: string
  CPF: string
  RG: string
  CEP: string
  endereco: string
  endereco_num: number | null
  endereco_bairro: string
  cidade: string
  estado: string
  telefone: string
  email: string
  cat_mensalidade: number | null
  patrulha_cod: number | null
}

//Dicionario para poder guardar erro com Chave e Valor, podendo identificar em cada campo mais abaixo
type Erros = Map<string, string|undefined>

const VAZIO: DadosEscoteiro = {
  nome: '', dt_nascto: '', CPF: '', RG: '', CEP: '', endereco: '', endereco_num: null,
  endereco_bairro: '', cidade: '', estado: '', telefone: '', email: '',
  cat_mensalidade: null, patrulha_cod: null,
}

const HOJE = new Date().toLocaleDateString('sv-SE')

const obrigatorio = (valor: string, maximo: number) =>
  !valor.trim() ? 'Obrigatório' : valor.length > maximo ? `Até ${maximo} caracteres` : undefined

function validar(d: DadosEscoteiro): Erros {

  const erros = new Map<string, string|undefined>()
  erros.set("nome", obrigatorio(d.nome, 30))
  erros.set("RG", obrigatorio(d.RG, 10))
  erros.set("endereco", obrigatorio(d.endereco, 50))
  erros.set("endereco_bairro", obrigatorio(d.endereco_bairro, 50))
  erros.set("cidade", obrigatorio(d.cidade, 50))
  erros.set("estado", obrigatorio(d.estado, 30))
  
 
  if (!d.dt_nascto) erros.set("dt_nascto", 'Obrigatório')
  else if (d.dt_nascto > HOJE) erros.set("dt_nascto", 'Data no futuro')
  if (!/^\d{11}$/.test(d.CPF)) erros.set("CPF", 'Deve ter 11 dígitos')
  else if (!cpfValido(d.CPF)) erros.set("CPF", 'CPF inválido')
  if (!/^\d{8}$/.test(d.CEP)) erros.set("CEP", 'Deve ter 8 dígitos')
  if (!d.endereco_num || d.endereco_num <= 0) erros.set("endereco_num", 'Informe o número')
  if (d.telefone && !/^\d{10,11}$/.test(d.telefone)) erros.set("telefone", 'Deve ter 10 ou 11 dígitos')
  if (d.email && (d.email.length > 40 || !d.email.includes('@'))) erros.set("email", 'E-mail inválido (até 40 caracteres)')
  if (!d.cat_mensalidade || d.cat_mensalidade < 1 || d.cat_mensalidade > 5) erros.set("cat_mensalidade", 'De 1 a 5')
  if (!d.patrulha_cod || d.patrulha_cod <= 0) erros.set("patrulha_cod", 'Informe a patrulha')
  return erros
}

interface FormsEscoteirosProps {
  inicial?: Partial<DadosEscoteiro>
  aoConfirmar: (dados: DadosEscoteiro) => void
  aoCancelar?: () => void
  carregando?: boolean
}

export default function FormsEscoteiros({ inicial, aoConfirmar, aoCancelar, carregando }: FormsEscoteirosProps) {
  const [dados, setDados] = useState<DadosEscoteiro>({ ...VAZIO, ...inicial })
  const [tentouSalvar, setTentouSalvar] = useState(false)

  const erros = tentouSalvar ? validar(dados) : new Map<string, string|undefined>()

  function atualizar<C extends keyof DadosEscoteiro>(campo: C, valor: DadosEscoteiro[C]) {
    setDados((atual) => ({ ...atual, [campo]: valor }))
  }

  function confirmar() {
    setTentouSalvar(true)
    if ([...validar(dados).values()].every((mensagem) => !mensagem)) aoConfirmar(dados) 
  }


  function aoMudarCEP(valor: string) {
    const cep = valor.replace(/\D/g, '')
    atualizar('CEP', cep)
    if (/^\d{8}$/.test(cep)) procurarCEP(cep)
  }

  function procurarCEP(cep: string) {
    return fetch(`https://viacep.com.br/ws/${cep}/json/`)
      .then((res) => res.json())
      .then((json) => {
        if (json.erro) throw new Error('CEP não encontrado')
        atualizar('endereco', json.logradouro)
        atualizar('endereco_bairro', json.bairro)
        atualizar('cidade', json.localidade)
        atualizar('estado', json.uf)
      })
      .catch((erro) => {
        console.error(erro)
        alert('Não foi possível buscar o CEP')
      })
  }

  return (
    <Formulario
      titulo={inicial ? 'Editar escoteiro' : 'Novo escoteiro'}
      aoConfirmar={confirmar}
      aoCancelar={aoCancelar}
      carregando={carregando}
      largura="md"
    >
      <h3>Dados pessoais</h3>
      <Box sx={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: 2 }}>
        <CampoTexto rotulo="Nome" autoFocus valor={dados.nome} aoMudar={(v) => atualizar('nome', v)} tamanhoMaximo={30} obrigatorio erro={erros.get('nome')} />
        <CampoData rotulo="Nascimento" valor={dados.dt_nascto} aoMudar={(v) => atualizar('dt_nascto', v)} maximo={HOJE} obrigatorio erro={erros.get('dt_nascto')} />
      </Box>
      <Box sx={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: 2 }}>
        <CampoCpf rotulo="CPF" valor={dados.CPF} aoMudar={(v) => atualizar('CPF', v)} obrigatorio erro={erros.get('CPF')} />
        <CampoTexto rotulo="RG" valor={dados.RG} aoMudar={(v) => atualizar('RG', v)} tamanhoMaximo={10} obrigatorio erro={erros.get('RG')} />
      </Box>

      <h3>Endereço</h3>
      <Box sx={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: 2 }}>
        <CampoTexto rotulo="CEP" valor={dados.CEP} aoMudar={aoMudarCEP} tamanhoMaximo={8} ajuda="Só números" obrigatorio erro={erros.get('CEP')} />
        <CampoTexto rotulo="Endereço" valor={dados.endereco} aoMudar={(v) => atualizar('endereco', v)} tamanhoMaximo={50} obrigatorio erro={erros.get('endereco')} />
        <CampoInteiro rotulo="Número" valor={dados.endereco_num} aoMudar={(v) => atualizar('endereco_num', v)} obrigatorio erro={erros.get('endereco_num')} />
      </Box>
      <Box sx={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: 2 }}>
        <CampoTexto rotulo="Bairro" valor={dados.endereco_bairro} aoMudar={(v) => atualizar('endereco_bairro', v)} tamanhoMaximo={50} obrigatorio erro={erros.get('endereco_bairro')} />
        <CampoTexto rotulo="Cidade" valor={dados.cidade} aoMudar={(v) => atualizar('cidade', v)} tamanhoMaximo={50} obrigatorio erro={erros.get('cidade')} />
        <CampoTexto rotulo="Estado" valor={dados.estado} aoMudar={(v) => atualizar('estado', v)} tamanhoMaximo={30} obrigatorio erro={erros.get('estado')} />
      </Box>

      <h3>Contato</h3>
      <Box sx={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: 2 }}>
        <CampoTexto rotulo="Telefone" tipo="tel" valor={dados.telefone} aoMudar={(v) => atualizar('telefone', v.replace(/\D/g, ''))} tamanhoMaximo={11} ajuda="Opcional, com DDD" erro={erros.get('telefone')} />
        <CampoTexto rotulo="E-mail" tipo="email" valor={dados.email} aoMudar={(v) => atualizar('email', v)} tamanhoMaximo={40} ajuda="Opcional" erro={erros.get('email')} />
      </Box>

      <h3>No grupo</h3>
      <Box sx={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: 2 }}>
        <CampoInteiro rotulo="Categoria de mensalidade" valor={dados.cat_mensalidade} aoMudar={(v) => atualizar('cat_mensalidade', v)} minimo={1} maximo={5} obrigatorio erro={erros.get('cat_mensalidade')} />
        <CampoInteiro rotulo="Código da patrulha" valor={dados.patrulha_cod} aoMudar={(v) => atualizar('patrulha_cod', v)} obrigatorio erro={erros.get('patrulha_cod')} />
      </Box>
    </Formulario>
  )
}
