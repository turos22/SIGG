// Adaptador do SIGG: prepara esta máquina (Windows, Linux ou macOS) para desenvolver o projeto.
// Instala o que faltar (JDK via winget/brew/dnf/apt/pacman, sempre perguntando antes), baixa as
// dependências do backend e do frontend para este SO e confere o PostgreSQL. Não sobe a aplicação.
// adaptar.cmd e adaptar.sh garantem o Node e chamam este arquivo. Opções: --ajuda.

import { spawnSync } from 'node:child_process';
import fs from 'node:fs';
import net from 'node:net';
import os from 'node:os';
import path from 'node:path';
import readline from 'node:readline';
import { fileURLToPath } from 'node:url';

const RAIZ = path.dirname(fileURLToPath(import.meta.url));
const DIR_BACK = path.join(RAIZ, 'SIGG_BACKEND');
const DIR_FRONT = path.join(RAIZ, 'SIGG_FrontEnd');
const ARQ_PROPRIEDADES = path.join(DIR_BACK, 'src', 'main', 'resources', 'application.properties');
const DUMP = 'artefatos/Script_sql_v1.sql';

const WINDOWS = process.platform === 'win32';
const MAC = process.platform === 'darwin';
const ATALHO = WINDOWS ? 'adaptar.cmd' : './adaptar.sh';
const COM_SUDO = !WINDOWS && process.getuid?.() !== 0;

const AJUDA = `
Uso: ${ATALHO} [opções]

Prepara esta máquina para desenvolver o SIGG. Pode rodar quantas vezes quiser:
o que já está certo é pulado.

  --sim         instala o que faltar sem perguntar
  --verificar   só confere; não instala nem altera nada
  --so-front    prepara só o frontend (não precisa de Java)
  --so-back     prepara só o backend
  --ajuda       mostra esta mensagem
`;

const usarCor = process.stdout.isTTY && !process.env.NO_COLOR;
const cor = (codigo) => (texto) => (usarCor ? `\x1b[${codigo}m${texto}\x1b[0m` : texto);
const verde = cor(32);
const amarelo = cor(33);
const vermelho = cor(31);
const ciano = cor(36);
const negrito = cor(1);

const MARCAS = { ok: verde('[ok]   '), aviso: amarelo('[aviso]'), erro: vermelho('[erro] ') };
const resumo = [];

const info = (msg) => console.log(`${ciano('[sigg]')}  ${msg}`);
const etapa = (titulo) => console.log(`\n${negrito(`== ${titulo}`)}`);

function registrar(estado, item, detalhe) {
  resumo.push({ estado, item, detalhe });
  console.log(`${MARCAS[estado]} ${item}: ${detalhe}`);
}

class ErroSigg extends Error {}

// ---------------------------------------------------------------------------
// Utilidades
// ---------------------------------------------------------------------------

function lerOpcoes(argv) {
  const validas = ['--sim', '--verificar', '--so-front', '--so-back', '--ajuda', '-h'];
  for (const arg of argv) if (!validas.includes(arg)) throw new ErroSigg(`Opção desconhecida: ${arg}. Use --ajuda.`);
  const tem = (opcao) => argv.includes(opcao);
  if (tem('--so-front') && tem('--so-back')) throw new ErroSigg('Use --so-front OU --so-back, não os dois.');
  return {
    sim: tem('--sim'),
    verificar: tem('--verificar'),
    soFront: tem('--so-front'),
    soBack: tem('--so-back'),
    ajuda: tem('--ajuda') || tem('-h'),
  };
}

function perguntar(texto) {
  return new Promise((resolver) => {
    const rl = readline.createInterface({ input: process.stdin, output: process.stdout });
    rl.on('close', () => resolver('n')); // Ctrl+D / fim da entrada conta como "não"
    rl.question(texto, (resposta) => {
      resolver(resposta);
      rl.close();
    });
  });
}

async function confirmar(pergunta, opcoes) {
  if (opcoes.sim) return true;
  if (!process.stdin.isTTY) {
    info('Sem terminal interativo para perguntar; rode com --sim para aceitar.');
    return false;
  }
  const resposta = (await perguntar(`${amarelo('[?]')}     ${pergunta} [S/n] `)).trim().toLowerCase();
  return ['', 's', 'sim', 'y', 'yes'].includes(resposta);
}

// Mostra o comando antes de rodar: quem usa o script vê exatamente o que muda na máquina.
function executar(comando, args, opcoes = {}) {
  const linha = [comando, ...args].join(' ');
  info(`$ ${linha}`);
  // No Windows, .cmd (npm, mvnw.cmd) só roda via shell (Node 18.20+/20.12+, CVE-2024-27980).
  const viaShell = WINDOWS && /^(npm|.*\.cmd)$/i.test(comando);
  const r = viaShell
    ? spawnSync(linha, { stdio: 'inherit', shell: true, ...opcoes })
    : spawnSync(comando, args, { stdio: 'inherit', ...opcoes });
  return !r.error && r.status === 0;
}

function acharNoPath(comando) {
  if (WINDOWS) {
    // "where" entende os aliases da Microsoft Store (o winget mora num deles).
    const r = spawnSync('where', [comando], { encoding: 'utf8' });
    return r.status === 0 ? r.stdout.split(/\r?\n/)[0].trim() : null;
  }
  for (const dir of (process.env.PATH || '').split(path.delimiter)) {
    if (!dir) continue;
    const alvo = path.join(dir, comando);
    try {
      fs.accessSync(alvo, fs.constants.X_OK);
      if (fs.statSync(alvo).isFile()) return alvo;
    } catch {}
  }
  return null;
}

function subpastas(dir, sufixo = '') {
  try {
    return fs.readdirSync(dir).map((nome) => path.join(dir, nome, sufixo));
  } catch {
    return [];
  }
}

function lerPropriedades(arquivo) {
  const props = {};
  for (const linha of fs.readFileSync(arquivo, 'utf8').split(/\r?\n/)) {
    const m = linha.match(/^\s*([^#!\s=:]+)\s*[=:]\s*(.*?)\s*$/);
    if (m) props[m[1]] = m[2];
  }
  return props;
}

function distroLinux() {
  try {
    const texto = fs.readFileSync('/etc/os-release', 'utf8');
    return texto.match(/^PRETTY_NAME="?([^"\n]*)"?/m)?.[1] || 'Linux';
  } catch {
    return 'Linux';
  }
}

function descreverSo() {
  if (WINDOWS) return `Windows ${os.release()} (${process.arch})`;
  if (MAC) return `macOS / Darwin ${os.release()} (${process.arch})`;
  return `${distroLinux()} (${process.arch})`;
}

function gerenciador() {
  if (WINDOWS) return acharNoPath('winget') ? 'winget' : null;
  if (MAC) return acharNoPath('brew') ? 'brew' : null;
  return ['dnf', 'apt-get', 'pacman'].find(acharNoPath) ?? null;
}

// [comando, args] já com sudo quando precisa (Linux sem root).
function sistema(...args) {
  return COM_SUDO ? ['sudo', args] : [args[0], args.slice(1)];
}

const textoComandos = (comandos) => comandos.map(([c, a]) => [c, ...a].join(' ')).join(' && ');

// ---------------------------------------------------------------------------
// Node.js
// ---------------------------------------------------------------------------

function dicaAtualizarNode() {
  if (/[\\/](\.nvm|mise|\.volta|fnm|\.asdf)[\\/]/.test(process.execPath)) {
    return 'atualize pelo seu gerenciador de versões (nvm, mise, volta...) para o LTS';
  }
  if (WINDOWS) return 'winget upgrade -e --id OpenJS.NodeJS.LTS';
  if (MAC) return 'brew upgrade node';
  const g = gerenciador();
  if (g === 'apt-get') return 'sudo apt-get remove -y nodejs && sudo snap install node --classic --channel=24/stable';
  if (g === 'dnf') return 'sudo dnf upgrade -y nodejs';
  if (g === 'pacman') return 'sudo pacman -Syu nodejs npm';
  return 'https://nodejs.org';
}

function verificarNode() {
  etapa('Node.js (frontend)');
  const [maior, menor] = process.versions.node.split('.').map(Number);
  const serve = (maior === 20 && menor >= 19) || (maior === 22 && menor >= 12) || maior >= 23;
  if (serve) registrar('ok', 'Node.js', `${process.versions.node} (${process.execPath})`);
  else registrar('erro', 'Node.js', `${process.versions.node} é antigo para o Vite (precisa 20.19+ ou 22.12+). Atualize: ${dicaAtualizarNode()}`);
}

// ---------------------------------------------------------------------------
// JDK
// ---------------------------------------------------------------------------

function versaoJavaDoPom() {
  const pom = fs.readFileSync(path.join(DIR_BACK, 'pom.xml'), 'utf8');
  return Number(pom.match(/<java\.version>(\d+)<\/java\.version>/)?.[1] ?? 17);
}

// JDK de verdade tem javac (só JRE não compila). O arquivo "release" informa a versão sem executar nada.
function versaoDoJdk(home) {
  try {
    if (!fs.statSync(path.join(home, 'bin', WINDOWS ? 'javac.exe' : 'javac')).isFile()) return null;
    const m = fs.readFileSync(path.join(home, 'release'), 'utf8').match(/^JAVA_VERSION="(\d+)(?:\.(\d+))?/m);
    if (!m) return null;
    return m[1] === '1' ? Number(m[2]) : Number(m[1]); // "1.8.0_x" é Java 8
  } catch {
    return null;
  }
}

// O JDK que o terminal (e o mvnw) usa de fato: JAVA_HOME, senão o java do PATH.
function jdkAtivo() {
  if (process.env.JAVA_HOME) {
    const home = path.resolve(process.env.JAVA_HOME);
    return { home, versao: versaoDoJdk(home), origem: 'JAVA_HOME' };
  }
  let home = null;
  if (MAC) {
    // /usr/bin/java no macOS é um stub; java_home diz qual JDK ele usaria, sem abrir diálogo se não houver.
    const r = spawnSync('/usr/libexec/java_home', [], { encoding: 'utf8' });
    if (r.status === 0) home = r.stdout.trim();
  } else if (acharNoPath('java')) {
    const r = spawnSync('java', ['-XshowSettings:properties', '-version'], { encoding: 'utf8' });
    home = (r.stderr || '').match(/^\s*java\.home = (.+)$/m)?.[1]?.trim() ?? null;
  }
  return home ? { home, versao: versaoDoJdk(home), origem: 'PATH' } : null;
}

function descreverJdk(jdk) {
  if (!jdk) return 'nenhum Java';
  if (!jdk.versao) return `${jdk.home} (sem javac: é só JRE)`;
  return `JDK ${jdk.versao} em ${jdk.home}`;
}

function candidatosJdk() {
  const casa = os.homedir();
  const lista = [];
  // Instalados por ferramentas, em qualquer SO (IntelliJ baixa em ~/.jdks)
  lista.push(...subpastas(path.join(casa, '.jdks')));
  lista.push(...subpastas(path.join(casa, '.sdkman', 'candidates', 'java')));
  lista.push(...subpastas(path.join(casa, '.local', 'share', 'mise', 'installs', 'java')));
  lista.push(...subpastas(path.join(casa, '.asdf', 'installs', 'java')));

  if (WINDOWS) {
    const bases = [
      process.env.ProgramFiles,
      process.env['ProgramFiles(x86)'],
      process.env.LOCALAPPDATA && path.join(process.env.LOCALAPPDATA, 'Programs'),
    ].filter(Boolean);
    const fornecedores = ['Java', 'Eclipse Adoptium', 'Microsoft', 'Zulu', 'Amazon Corretto', 'BellSoft', 'Semeru', 'OpenJDK'];
    for (const base of bases) for (const fornecedor of fornecedores) lista.push(...subpastas(path.join(base, fornecedor)));
  } else if (MAC) {
    lista.push(...subpastas('/Library/Java/JavaVirtualMachines', 'Contents/Home'));
    lista.push(...subpastas(path.join(casa, 'Library', 'Java', 'JavaVirtualMachines'), 'Contents/Home'));
  } else {
    for (const dir of ['/usr/lib/jvm', '/usr/java', '/opt/java', '/opt']) lista.push(...subpastas(dir));
  }

  const reais = lista.map((p) => {
    try {
      return fs.realpathSync(p);
    } catch {
      return null;
    }
  });
  return [...new Set(reais.filter(Boolean))];
}

// Prefere exatamente a versão do pom; senão, a mais nova acima dela.
function melhorJdkInstalado(minima) {
  return (
    candidatosJdk()
      .map((home) => ({ home, versao: versaoDoJdk(home) }))
      .filter((jdk) => jdk.versao >= minima)
      .sort((a, b) => (a.versao !== minima) - (b.versao !== minima) || b.versao - a.versao)[0] ?? null
  );
}

function comandosJdk(g, versao) {
  switch (g) {
    case 'winget':
      return [['winget', ['install', '-e', '--id', `EclipseAdoptium.Temurin.${versao}.JDK`, '--accept-source-agreements', '--accept-package-agreements']]];
    case 'brew':
      return [['brew', ['install', '--cask', `temurin@${versao}`]]];
    case 'dnf':
      return [sistema('dnf', 'install', '-y', `java-${versao}-openjdk-devel`)];
    case 'apt-get':
      return [sistema('apt-get', 'update'), sistema('apt-get', 'install', '-y', `openjdk-${versao}-jdk`)];
    case 'pacman':
      return [sistema('pacman', '-S', '--needed', '--noconfirm', 'jdk-openjdk')]; // Arch só tem a versão atual
    default:
      return null;
  }
}

async function instalarJdk(minima, opcoes) {
  const comandos = comandosJdk(gerenciador(), minima);
  if (!comandos) {
    const falta = WINDOWS ? 'winget' : MAC ? 'Homebrew (https://brew.sh)' : 'dnf, apt ou pacman';
    registrar('erro', 'JDK', `falta JDK ${minima}+ e não achei ${falta} para instalar. Baixe em https://adoptium.net`);
    return false;
  }
  const texto = textoComandos(comandos);
  if (opcoes.verificar) {
    registrar('erro', 'JDK', `falta JDK ${minima}+. Seria instalado com: ${texto}`);
    return false;
  }
  if (!(await confirmar(`Instalar o JDK ${minima} com: ${texto} ?`, opcoes))) {
    registrar('erro', 'JDK', `instalação recusada. Para instalar depois: ${texto}`);
    return false;
  }
  for (const [comando, args] of comandos) {
    if (!executar(comando, args)) {
      registrar('erro', 'JDK', `falhou: ${[comando, ...args].join(' ')}`);
      return false;
    }
  }
  return true;
}

function dicaAtivarJdk(alvo) {
  if (process.env.JAVA_HOME) return `Corrija o JAVA_HOME (onde você o define, ex.: ~/.bashrc ou ~/.zshrc) para ${alvo.home}.`;
  if (MAC) return `Adicione ao ~/.zshrc: export JAVA_HOME=$(/usr/libexec/java_home -v ${alvo.versao})`;
  const g = gerenciador();
  if (g === 'dnf') return 'Escolha o padrão com: sudo alternatives --config java';
  if (g === 'apt-get') return 'Escolha o padrão com: sudo update-alternatives --config java';
  return `Aponte o JAVA_HOME para ${alvo.home}.`;
}

async function definirJavaHomeWindows(alvo, opcoes) {
  if (opcoes.verificar) {
    registrar('erro', 'JDK', `JDK ${alvo.versao} existe em ${alvo.home}, mas o terminal não o usa. Rode sem --verificar para definir o JAVA_HOME.`);
    return alvo.home;
  }
  if (!(await confirmar(`Definir JAVA_HOME=${alvo.home} para o seu usuário?`, opcoes))) {
    registrar('aviso', 'JDK', `JDK ${alvo.versao} em ${alvo.home}, mas sem JAVA_HOME o terminal não o usa.`);
    return alvo.home;
  }
  // setx grava no registro do usuário (HKCU\Environment): vale para terminais e IDEs abertos depois.
  if (executar('setx', ['JAVA_HOME', alvo.home])) {
    registrar('ok', 'JDK', `JDK ${alvo.versao}: JAVA_HOME=${alvo.home} (vale para terminais abertos a partir de agora)`);
  } else {
    registrar('erro', 'JDK', 'o setx falhou ao definir o JAVA_HOME');
  }
  return alvo.home;
}

// Devolve o home do JDK que o build do backend deve usar (ou null).
async function adaptarJdk(opcoes) {
  const minima = versaoJavaDoPom();
  etapa(`JDK ${minima}+ (backend)`);
  const serve = (jdk) => jdk?.versao >= minima;

  let ativo = jdkAtivo();
  if (serve(ativo)) {
    registrar('ok', 'JDK', descreverJdk(ativo));
    return ativo.home;
  }
  info(`O terminal usa: ${descreverJdk(ativo)}${ativo?.origem === 'JAVA_HOME' ? ' (via JAVA_HOME)' : ''}.`);

  // No Windows basta apontar o JAVA_HOME para um JDK que já exista (ex.: baixado pelo IntelliJ).
  // No Linux/macOS o certo é ter o JDK do sistema, que já entra no PATH.
  let alvo = WINDOWS ? melhorJdkInstalado(minima) : null;
  if (!alvo) {
    if (!(await instalarJdk(minima, opcoes))) return melhorJdkInstalado(minima)?.home ?? null;
    ativo = jdkAtivo();
    if (serve(ativo)) {
      registrar('ok', 'JDK', `instalado: ${descreverJdk(ativo)}`);
      return ativo.home;
    }
    alvo = melhorJdkInstalado(minima);
    if (!alvo) {
      registrar('erro', 'JDK', 'a instalação terminou, mas não achei o JDK. Abra um terminal novo e rode de novo.');
      return null;
    }
  }
  if (WINDOWS) return definirJavaHomeWindows(alvo, opcoes);
  registrar('aviso', 'JDK', `JDK ${alvo.versao} em ${alvo.home}, mas o terminal ainda usa ${descreverJdk(ativo)}. ${dicaAtivarJdk(alvo)}`);
  return alvo.home;
}

// ---------------------------------------------------------------------------
// Backend e frontend
// ---------------------------------------------------------------------------

function adaptarBackend(jdkHome, opcoes) {
  etapa('Backend (Maven)');
  if (!WINDOWS) {
    // Clone feito no Windows pode chegar sem o bit de execução.
    const mvnw = path.join(DIR_BACK, 'mvnw');
    if ((fs.statSync(mvnw).mode & 0o111) === 0) {
      if (opcoes.verificar) registrar('aviso', 'mvnw', 'sem permissão de execução; rode sem --verificar para corrigir');
      else {
        fs.chmodSync(mvnw, 0o755);
        info('mvnw: permissão de execução restaurada.');
      }
    }
  }
  if (!jdkHome) return registrar('erro', 'Backend', 'pulado: nenhum JDK utilizável.');
  if (opcoes.verificar) return registrar('aviso', 'Backend', 'dependências não conferidas no modo --verificar.');

  info('Baixando Maven e dependências e compilando (a primeira vez pode levar alguns minutos)...');
  const opcoesBuild = { cwd: DIR_BACK, env: { ...process.env, JAVA_HOME: jdkHome } };
  const ok = WINDOWS
    ? executar('mvnw.cmd', ['-B', '-DskipTests', 'test-compile'], opcoesBuild)
    : executar('sh', ['mvnw', '-B', '-DskipTests', 'test-compile'], opcoesBuild);
  if (ok) registrar('ok', 'Backend', 'Maven, dependências e compilação OK neste SO');
  else registrar('erro', 'Backend', 'o build falhou; veja a saída acima');
}

// Importar o Vite carrega o binário nativo do rolldown; se o node_modules veio de outro SO, falha aqui.
function viteCarrega() {
  const r = spawnSync(process.execPath, ['--input-type=module', '-e', "await import('vite')"], { cwd: DIR_FRONT, stdio: 'ignore' });
  return r.status === 0;
}

function motivoReinstalarFront() {
  const lockInstalado = path.join(DIR_FRONT, 'node_modules', '.package-lock.json');
  if (!fs.existsSync(lockInstalado)) return 'dependências ainda não instaladas';
  if (fs.statSync(path.join(DIR_FRONT, 'package-lock.json')).mtimeMs > fs.statSync(lockInstalado).mtimeMs) {
    return 'package-lock.json mudou desde a última instalação';
  }
  if (!viteCarrega()) return 'node_modules tem binários de outro SO';
  return null;
}

function adaptarFront(opcoes) {
  etapa('Frontend (npm)');
  const plataforma = `${process.platform}-${process.arch}`;
  const motivo = motivoReinstalarFront();
  if (!motivo) return registrar('ok', 'Frontend', `dependências instaladas e compatíveis com ${plataforma}`);
  if (opcoes.verificar) return registrar('erro', 'Frontend', `precisa reinstalar (${motivo}); rode sem --verificar`);

  info(`Reinstalando dependências do front (${motivo})...`);
  // npm ci instala exatamente o package-lock.json e nunca o reescreve: não gera diff entre SOs.
  if (!executar('npm', ['ci'], { cwd: DIR_FRONT })) return registrar('erro', 'Frontend', 'npm ci falhou; veja a saída acima');
  if (!viteCarrega()) return registrar('erro', 'Frontend', 'dependências instaladas, mas o binário nativo do Vite não carrega neste SO');
  registrar('ok', 'Frontend', `dependências instaladas para ${plataforma}`);
}

// ---------------------------------------------------------------------------
// PostgreSQL (só confere e orienta; não instala)
// ---------------------------------------------------------------------------

// Mesma precedência do Spring: variável de ambiente (SPRING_DATASOURCE_URL...) vence o application.properties.
function lerBanco() {
  const props = lerPropriedades(ARQ_PROPRIEDADES);
  const url = process.env.SPRING_DATASOURCE_URL || props['spring.datasource.url'] || '';
  const m = url.match(/^jdbc:postgresql:\/\/([^:/?]+)(?::(\d+))?\/([^?]+)/);
  if (!m) return null;
  return {
    host: m[1],
    porta: Number(m[2] || 5432),
    nome: m[3],
    usuario: process.env.SPRING_DATASOURCE_USERNAME ?? props['spring.datasource.username'] ?? 'postgres',
    senha: process.env.SPRING_DATASOURCE_PASSWORD ?? props['spring.datasource.password'] ?? '',
  };
}

function conecta(host, porta, limiteMs = 2000) {
  return new Promise((resolver) => {
    const socket = net.connect({ host, port: porta });
    const fim = (ok) => {
      socket.destroy();
      resolver(ok);
    };
    socket.setTimeout(limiteMs, () => fim(false));
    socket.once('connect', () => fim(true));
    socket.once('error', () => fim(false));
  });
}

function acharPsql() {
  const noPath = acharNoPath('psql');
  if (noPath) return noPath;
  // Os instaladores oficiais (Windows) e o Homebrew (keg-only) não põem o psql no PATH.
  const candidatos = WINDOWS
    ? subpastas(path.join(process.env.ProgramFiles || 'C:\\Program Files', 'PostgreSQL'), path.join('bin', 'psql.exe'))
    : MAC
      ? [
          ...subpastas('/opt/homebrew/opt').filter((p) => path.basename(p).startsWith('postgresql')).map((p) => path.join(p, 'bin', 'psql')),
          ...subpastas('/usr/local/opt').filter((p) => path.basename(p).startsWith('postgresql')).map((p) => path.join(p, 'bin', 'psql')),
          '/Applications/Postgres.app/Contents/Versions/latest/bin/psql',
        ]
      : [];
  return candidatos.filter((p) => fs.existsSync(p)).sort((a, b) => b.localeCompare(a, undefined, { numeric: true }))[0] ?? null;
}

function orientacaoPostgres(banco) {
  const g = gerenciador();
  const linhas = [];
  if (WINDOWS) {
    linhas.push('Já instalado? Inicie o serviço "postgresql-x64-NN" (Win+R > services.msc).');
    linhas.push('Não instalado? winget install -e --id PostgreSQL.PostgreSQL.17');
    linhas.push('  No instalador, use como senha do usuário postgres a mesma do application.properties.');
  } else if (MAC) {
    linhas.push('brew install postgresql@17 && brew services start postgresql@17');
    linhas.push(`Crie o usuário: psql postgres -c "CREATE ROLE ${banco.usuario} LOGIN SUPERUSER PASSWORD '<senha do application.properties>'"`);
  } else {
    if (g === 'dnf') {
      linhas.push('sudo dnf install -y postgresql-server && sudo postgresql-setup --initdb && sudo systemctl enable --now postgresql');
      linhas.push("  No Fedora o acesso TCP vem como 'ident': troque por 'scram-sha-256' em /var/lib/pgsql/data/pg_hba.conf e reinicie.");
    } else if (g === 'apt-get') {
      linhas.push('sudo apt-get install -y postgresql');
    } else {
      linhas.push('Instale o PostgreSQL pelo gerenciador da sua distro.');
    }
    linhas.push(`Senha do usuário: sudo -u postgres psql -c "ALTER USER ${banco.usuario} PASSWORD '<senha do application.properties>'"`);
  }
  linhas.push(`Crie o banco e carregue o dump: createdb -h ${banco.host} -U ${banco.usuario} ${banco.nome}`);
  linhas.push(`                                psql -h ${banco.host} -U ${banco.usuario} -d ${banco.nome} -f ${DUMP}`);
  linhas.push(`Ou com Docker: docker run -d --name sigg-postgres -e POSTGRES_PASSWORD=<senha> -e POSTGRES_DB=${banco.nome} -p 127.0.0.1:${banco.porta}:5432 postgres:17-alpine`);
  return linhas;
}

async function verificarPostgres() {
  etapa('PostgreSQL (backend)');
  const banco = lerBanco();
  if (!banco) return registrar('aviso', 'PostgreSQL', 'URL do banco no application.properties não reconhecida; nada conferido.');
  const alvo = `${banco.host}:${banco.porta}`;

  if (!(await conecta(banco.host, banco.porta))) {
    registrar('aviso', 'PostgreSQL', `não responde em ${alvo}. O backend não sobe sem ele.`);
    for (const linha of orientacaoPostgres(banco)) info(linha);
    return;
  }
  const psql = acharPsql();
  if (!psql) return registrar('aviso', 'PostgreSQL', `responde em ${alvo}, mas sem o psql não dá para conferir o banco "${banco.nome}".`);

  const r = spawnSync(
    psql,
    ['-w', '-h', banco.host, '-p', String(banco.porta), '-U', banco.usuario, '-d', banco.nome, '-tAc',
      "select count(*) from information_schema.tables where table_schema = 'public'"],
    { encoding: 'utf8', env: { ...process.env, PGPASSWORD: banco.senha, PGCONNECT_TIMEOUT: '5' } },
  );
  const erro = (r.stderr || '').trim();
  if (r.status === 0) {
    const tabelas = Number(r.stdout.trim());
    if (tabelas > 0) registrar('ok', 'PostgreSQL', `banco "${banco.nome}" em ${alvo} com ${tabelas} tabelas`);
    else registrar('aviso', 'PostgreSQL', `banco "${banco.nome}" existe mas está vazio. Carregue: psql -h ${banco.host} -U ${banco.usuario} -d ${banco.nome} -f ${DUMP}`);
  } else if (/does not exist|não existe/i.test(erro)) {
    registrar('aviso', 'PostgreSQL', `o banco "${banco.nome}" não existe em ${alvo}.`);
    info(`Crie e carregue: createdb -h ${banco.host} -U ${banco.usuario} ${banco.nome}`);
    info(`                 psql -h ${banco.host} -U ${banco.usuario} -d ${banco.nome} -f ${DUMP}`);
  } else if (/password|senha|autentica/i.test(erro)) {
    registrar('aviso', 'PostgreSQL', `senha recusada para "${banco.usuario}". Ela precisa bater com spring.datasource.password (ou defina SPRING_DATASOURCE_PASSWORD).`);
  } else {
    registrar('aviso', 'PostgreSQL', `responde em ${alvo}, mas não consegui conferir: ${erro.split('\n')[0]}`);
  }
}

// ---------------------------------------------------------------------------
// Principal
// ---------------------------------------------------------------------------

function imprimirResumo() {
  etapa('Resumo');
  for (const { estado, item, detalhe } of resumo) console.log(`${MARCAS[estado]} ${item}: ${detalhe}`);
  const erros = resumo.filter((r) => r.estado === 'erro').length;
  console.log();
  if (erros) {
    info(vermelho(`${erros} item(ns) com erro. Corrija e rode ${ATALHO} de novo.`));
  } else {
    info(verde('Máquina pronta para o SIGG.'));
    info(`Backend : cd SIGG_BACKEND && ${WINDOWS ? 'mvnw.cmd' : './mvnw'} spring-boot:run   (ou Run no IntelliJ)`);
    info('Frontend: cd SIGG_FrontEnd && npm run dev');
  }
  return erros;
}

async function principal() {
  const opcoes = lerOpcoes(process.argv.slice(2));
  if (opcoes.ajuda) {
    console.log(AJUDA);
    return 0;
  }
  info(`SO: ${descreverSo()}${opcoes.verificar ? ' | modo --verificar: nada será instalado ou alterado' : ''}`);

  const querFront = !opcoes.soBack;
  const querBack = !opcoes.soFront;
  if (querFront) verificarNode();
  const jdkHome = querBack ? await adaptarJdk(opcoes) : null;
  if (querFront) adaptarFront(opcoes);
  if (querBack) {
    adaptarBackend(jdkHome, opcoes);
    await verificarPostgres();
  }
  return imprimirResumo() ? 1 : 0;
}

principal()
  .then((codigo) => process.exit(codigo))
  .catch((erro) => {
    console.error(`${vermelho('[sigg]')}  ${erro instanceof ErroSigg ? erro.message : erro.stack}`);
    process.exit(1);
  });
