# SIGG

[![CI](https://github.com/turos22/SIGG/actions/workflows/ci.yml/badge.svg)](https://github.com/turos22/SIGG/actions/workflows/ci.yml)

Sistema Integrado de Gestão Guayporé: backend Spring Boot (`SIGG_BACKEND`) e frontend React + Vite (`SIGG_FrontEnd`).

**Guia completo** (setup, como criar endpoints e telas): [`docs/Guia_Desenvolvedor_SIGG.docx`](docs/Guia_Desenvolvedor_SIGG.docx).

## Preparar a máquina (Windows, Linux ou macOS)

Rode uma vez depois de clonar, e de novo sempre que algo parar de funcionar:

| SO | Comando |
|---|---|
| Windows | duplo clique em `adaptar.cmd` (ou `adaptar.cmd` no terminal) |
| Linux / macOS | `./adaptar.sh` |

O adaptar confere e, **perguntando antes**, instala o que falta:

| Item | O que faz |
|---|---|
| Node.js | instala o LTS se não houver (winget, brew, dnf, apt/snap ou pacman) |
| JDK 25 | instala se o terminal não tiver um JDK 25+. No Windows, se já existir um JDK (ex.: baixado pelo IntelliJ), só define o `JAVA_HOME` |
| Frontend | roda `npm ci` quando falta `node_modules`, o lock mudou ou os binários são de outro SO |
| Backend | baixa o Maven e as dependências e compila, para provar que o build funciona neste SO |
| PostgreSQL | **só confere** se o banco `Escoteiros` responde e tem tabelas; se não, mostra como resolver no seu SO |

Opções: `--verificar` (só confere, não instala nada), `--sim` (instala sem perguntar), `--so-front`, `--so-back`.

## Rodar

- Backend: `cd SIGG_BACKEND` e `./mvnw spring-boot:run` (Windows: `mvnw.cmd spring-boot:run`), ou Run no IntelliJ
- Frontend: `cd SIGG_FrontEnd` e `npm run dev`

## CI

A cada push na `main` e a cada pull request, o GitHub Actions (`.github/workflows/ci.yml`) compila o backend e roda
os testes contra um PostgreSQL com o dump, e roda `npm ci`, `lint` e `build` no front. Se o selo acima ficar vermelho,
alguém subiu algo que quebra o projeto: veja o log na aba **Actions** do GitHub.

## Para não quebrar o projeto no SO dos colegas

- Não commite `.env`, `.idea/`, `*.iml` nem `node_modules/`. Eles já estão no `.gitignore`.
- O fim de linha é controlado pelo `.gitattributes`: `.sh` e `mvnw` sempre LF, `.cmd` e `.bat` sempre CRLF.
- Para instalar o que já está no lock, use `npm ci` (o adaptar já faz isso). O `npm install` sem argumentos pode
  reescrever o `package-lock.json` com diferenças de outro SO ou de outra versão do npm. Para adicionar um pacote,
  rode `npm install <pacote>` e commite o lock.
