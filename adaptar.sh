#!/bin/sh
# Prepara esta máquina (Linux ou macOS) para desenvolver o SIGG. Opções: ./adaptar.sh --ajuda
# Aqui só garantimos o Node.js (instalando se faltar); o resto da lógica está em adaptar.mjs.
cd "$(dirname "$0")" || exit 1

SIM=0
VERIFICAR=0
for arg in "$@"; do
  [ "$arg" = "--sim" ] && SIM=1
  [ "$arg" = "--verificar" ] && VERIFICAR=1
done
if [ "$(id -u)" -eq 0 ]; then SUDO=""; else SUDO="sudo"; fi

# Versão do pacote (ex.: "18.19.1+dfsg-1", "1:20.19.2") atende o Vite? (20.19+ ou 22.12+)
# sh não tem "local": variáveis com prefixo vs_ para não pisar em nada fora da função.
versao_serve() {
  vs_versao=${1#*:}
  vs_maior=${vs_versao%%.*}
  vs_resto=${vs_versao#*.}
  vs_menor=${vs_resto%%.*}
  case "$vs_maior$vs_menor" in *[!0-9]*|"") return 1 ;; esac
  [ "$vs_maior" -ge 23 ] || { [ "$vs_maior" -eq 22 ] && [ "$vs_menor" -ge 12 ]; } || { [ "$vs_maior" -eq 20 ] && [ "$vs_menor" -ge 19 ]; }
}

instalar_node() {
  if [ "$(uname -s)" = "Darwin" ]; then
    if ! command -v brew >/dev/null 2>&1; then
      echo "[sigg] Instale o Homebrew (https://brew.sh) e rode de novo."
      return 1
    fi
    brew install node
  elif command -v dnf >/dev/null 2>&1; then
    $SUDO dnf install -y nodejs npm
  elif command -v apt-get >/dev/null 2>&1; then
    $SUDO apt-get update || return 1
    # O Ubuntu 24.04 só oferece Node 18 no apt, velho demais para o Vite: nesse caso, snap.
    if versao_serve "$(apt-cache policy nodejs | awk '/Candidate:/ {print $2}')"; then
      $SUDO apt-get install -y nodejs npm
    elif command -v snap >/dev/null 2>&1; then
      $SUDO snap install node --classic --channel=24/stable
    else
      echo "[sigg] O Node do apt é antigo para o Vite. Instale o LTS: https://nodejs.org"
      return 1
    fi
  elif command -v pacman >/dev/null 2>&1; then
    $SUDO pacman -S --needed --noconfirm nodejs npm
  else
    echo "[sigg] Não sei instalar o Node nesta distro. Instale o LTS: https://nodejs.org"
    return 1
  fi
}

if ! command -v node >/dev/null 2>&1; then
  if [ "$VERIFICAR" = 1 ]; then
    echo "[sigg] Node.js não encontrado. Rode sem --verificar para instalar."
    exit 1
  fi
  if [ "$SIM" != 1 ]; then
    if [ ! -t 0 ]; then
      echo "[sigg] Node.js não encontrado e não há terminal para perguntar. Rode com --sim para instalar."
      exit 1
    fi
    printf "[sigg] Node.js não encontrado. Instalar agora? [S/n] "
    read -r resposta || resposta=n
    case "$resposta" in
      "" | s | S | sim | y | Y) ;;
      *) echo "[sigg] Sem Node não dá para continuar."; exit 1 ;;
    esac
  fi
  instalar_node || exit 1
  PATH="$PATH:/snap/bin"
  if ! command -v node >/dev/null 2>&1; then
    echo "[sigg] Node.js instalado, mas não encontrado no PATH. Abra um terminal novo e rode de novo."
    exit 1
  fi
fi

exec node adaptar.mjs "$@"
