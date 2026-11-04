import './Icone.css'

const ICONES = {
  inicio: (
    <>
      <path d="M3 11l9-8 9 8" />
      <path d="M5 10v10h14V10" />
    </>
  ),
  tropa: (
    <>
      <path d="M2 20h20L12 4 2 20z" />
      <path d="M12 4v16M9 20l3-6 3 6" />
    </>
  ),
  escoteiros: (
    <>
      <circle cx="12" cy="8" r="4" />
      <path d="M4 21c0-4 4-6 8-6s8 2 8 6" />
    </>
  ),
  responsaveis: (
    <>
      <circle cx="9" cy="8" r="3.5" />
      <circle cx="17" cy="9" r="2.5" />
      <path d="M2 20c0-3.5 3-5.5 7-5.5s7 2 7 5.5" />
      <path d="M16 14.5c3 0 6 1.5 6 4.5" />
    </>
  ),
  frequencia: (
    <>
      <rect x="3" y="5" width="18" height="16" rx="2" />
      <path d="M8 3v4M16 3v4M3 10h18M9 15l2 2 4-4" />
    </>
  ),
  financeiro: (
    <>
      <circle cx="12" cy="12" r="9" />
      <path d="M14.5 9c-.5-1-1.5-1.5-2.5-1.5-1.5 0-2.5.8-2.5 2s1 1.7 2.5 2 2.5.8 2.5 2-1 2-2.5 2c-1.2 0-2.2-.6-2.7-1.6M12 6v1.5M12 16.5V18" />
    </>
  ),
  estoque: (
    <>
      <path d="M21 8l-9-5-9 5 9 5 9-5z" />
      <path d="M3 8v8l9 5 9-5V8M12 13v8" />
    </>
  ),
  relatorios: <path d="M4 20V10M10 20V4M16 20v-8M22 20H2" />,
  seta: <path d="M9 6l6 6-6 6" />,
  ok: <path d="M5 12l5 5 9-10" />,
  alerta: <path d="M12 8v5M12 17h.01" />,
}

export type NomeIcone = keyof typeof ICONES

export default function Icone({ nome, classe }: { nome: NomeIcone; classe?: string }) {
  return (
    <svg className={classe ? `icone ${classe}` : 'icone'} viewBox="0 0 24 24" aria-hidden="true">
      {ICONES[nome]}
    </svg>
  )
}
