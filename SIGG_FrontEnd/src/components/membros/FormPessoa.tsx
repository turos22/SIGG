import { useState } from 'react'
import Box from '@mui/material/Box'
import CampoCpf from '@/components/formulario/CampoCpf'
import CampoData from '@/components/formulario/CampoData'
import CampoInteiro from '@/components/formulario/CampoInteiro'
import CampoTexto from '@/components/formulario/CampoTexto'
import CampoUf from '@/components/formulario/CampoUf'
import Formulario from '@/components/formulario/Formulario'
import type { DadosPessoa } from '@/data/models/DadosPessoa'
import { filtroRg, semDigitos, somenteDigitos } from '@/data/utils/filtros'
import { cpfValido, emailValido } from '@/data/utils/validadores'

type Erros = Map<string, string | undefined>

const VAZIO: DadosPessoa = {
  nome: '', dt_nascto: '', CPF: '', RG: '', CEP: '', endereco: '', endereco_num: null,
  endereco_bairro: '', cidade: '', estado: '', telefone: '', email: '',
}

const HOJE = new Date().toLocaleDateString('sv-SE')

const obrigatorio = (valor: string, maximo: number) =>
  !valor.trim() ? 'Obrigatório' : valor.length > maximo ? `Até ${maximo} caracteres` : undefined

// Mesmas regras do backend (Membro.validar): o backend é quem manda, aqui é só para avisar antes de enviar
function validar(d: DadosPessoa): Erros {
  const erros: Erros = new Map()
  erros.set('nome', obrigatorio(d.nome, 30))
  erros.set('RG', obrigatorio(d.RG, 10))
  erros.set('endereco', obrigatorio(d.endereco, 50))
  erros.set('endereco_bairro', obrigatorio(d.endereco_bairro, 50))
  erros.set('cidade', obrigatorio(d.cidade, 50))
  if (!d.dt_nascto) erros.set('dt_nascto', 'Obrigatório')
  else if (d.dt_nascto > HOJE) erros.set('dt_nascto', 'Data no futuro')
  if (!/^\d{11}$/.test(d.CPF)) erros.set('CPF', 'Deve ter 11 dígitos')
  else if (!cpfValido(d.CPF)) erros.set('CPF', 'CPF inválido')
  if (!/^\d{8}$/.test(d.CEP)) erros.set('CEP', 'Deve ter 8 dígitos')
  if (!d.endereco_num || d.endereco_num <= 0) erros.set('endereco_num', 'Informe o número')
  if (!d.estado) erros.set('estado', 'Escolha o estado')
  if (d.telefone && !/^\d{10,11}$/.test(d.telefone)) erros.set('telefone', 'Deve ter 10 ou 11 dígitos')
  if (d.email && !emailValido(d.email)) erros.set('email', 'E-mail inválido (até 40 caracteres)')
  return erros
}

interface FormPessoaProps {
  inicial?: DadosPessoa
  aoConfirmar: (dados: DadosPessoa) => void
  aoCancelar: () => void
  carregando?: boolean
}

const GRADE = { display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: 2 }

export default function FormPessoa({ inicial, aoConfirmar, aoCancelar, carregando }: FormPessoaProps) {
  const [dados, setDados] = useState<DadosPessoa>(inicial ?? VAZIO)
  const [tentouSalvar, setTentouSalvar] = useState(false)
  const erros = tentouSalvar ? validar(dados) : new Map<string, string | undefined>()

  function atualizar<C extends keyof DadosPessoa>(campo: C, valor: DadosPessoa[C]) {
    setDados((atual) => ({ ...atual, [campo]: valor }))
  }

  function confirmar() {
    setTentouSalvar(true)
    if ([...validar(dados).values()].every((mensagem) => !mensagem)) aoConfirmar(dados)
  }

  function aoMudarCep(valor: string) {
    const cep = somenteDigitos(valor).slice(0, 8)
    atualizar('CEP', cep)
    if (cep.length === 8) procurarCep(cep)
  }

  function procurarCep(cep: string) {
    return fetch(`https://viacep.com.br/ws/${cep}/json/`)
      .then((res) => res.json())
      .then((json) => {
        if (json.erro) throw new Error('CEP não encontrado')
        setDados((atual) => ({
          ...atual,
          endereco: json.logradouro ?? atual.endereco,
          endereco_bairro: semDigitos(json.bairro ?? atual.endereco_bairro),
          cidade: semDigitos(json.localidade ?? atual.cidade),
          estado: json.uf ?? atual.estado,
        }))
      })
      .catch((erro) => {
        console.error(erro)
        alert('Não foi possível buscar o CEP')
      })
  }

  return (
    <Formulario titulo={inicial ? 'Editar pessoa' : 'Nova pessoa'} aoConfirmar={confirmar} aoCancelar={aoCancelar} carregando={carregando} largura="md">
      <h3>Dados pessoais</h3>
      <Box sx={GRADE}>
        <CampoTexto rotulo="Nome" autoFocus valor={dados.nome} aoMudar={(v) => atualizar('nome', v)} filtro={semDigitos} tamanhoMaximo={30} obrigatorio erro={erros.get('nome')} />
        <CampoData rotulo="Nascimento" valor={dados.dt_nascto} aoMudar={(v) => atualizar('dt_nascto', v)} maximo={HOJE} obrigatorio erro={erros.get('dt_nascto')} />
      </Box>
      <Box sx={GRADE}>
        <CampoCpf rotulo="CPF" valor={dados.CPF} aoMudar={(v) => atualizar('CPF', v)} obrigatorio erro={erros.get('CPF')} />
        <CampoTexto rotulo="RG" valor={dados.RG} aoMudar={(v) => atualizar('RG', v)} filtro={filtroRg} tamanhoMaximo={10} ajuda="Números e X" obrigatorio erro={erros.get('RG')} />
      </Box>

      <h3>Endereço</h3>
      <Box sx={GRADE}>
        <CampoTexto rotulo="CEP" valor={dados.CEP} aoMudar={aoMudarCep} filtro={somenteDigitos} tamanhoMaximo={8} ajuda="Só números" obrigatorio erro={erros.get('CEP')} />
        <CampoTexto rotulo="Endereço" valor={dados.endereco} aoMudar={(v) => atualizar('endereco', v)} tamanhoMaximo={50} obrigatorio erro={erros.get('endereco')} />
        <CampoInteiro rotulo="Número" valor={dados.endereco_num} aoMudar={(v) => atualizar('endereco_num', v)} obrigatorio erro={erros.get('endereco_num')} />
      </Box>
      <Box sx={GRADE}>
        <CampoTexto rotulo="Bairro" valor={dados.endereco_bairro} aoMudar={(v) => atualizar('endereco_bairro', v)} filtro={semDigitos} tamanhoMaximo={50} obrigatorio erro={erros.get('endereco_bairro')} />
        <CampoTexto rotulo="Cidade" valor={dados.cidade} aoMudar={(v) => atualizar('cidade', v)} filtro={semDigitos} tamanhoMaximo={50} obrigatorio erro={erros.get('cidade')} />
        <CampoUf rotulo="Estado" valor={dados.estado} aoMudar={(v) => atualizar('estado', v)} obrigatorio erro={erros.get('estado')} />
      </Box>

      <h3>Contato</h3>
      <Box sx={GRADE}>
        <CampoTexto rotulo="Telefone" tipo="tel" valor={dados.telefone} aoMudar={(v) => atualizar('telefone', v)} filtro={somenteDigitos} tamanhoMaximo={11} ajuda="Opcional, com DDD" erro={erros.get('telefone')} />
        <CampoTexto rotulo="E-mail" tipo="email" valor={dados.email} aoMudar={(v) => atualizar('email', v)} tamanhoMaximo={40} ajuda="Opcional" erro={erros.get('email')} />
      </Box>
    </Formulario>
  )
}
