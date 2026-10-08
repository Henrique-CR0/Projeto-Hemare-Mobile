# 🩸 Hemare Mobile

Versão Android nativa do [Hemare](https://github.com/Henrique-CR0/Hemare), plataforma que conecta doadores de sangue a hemocentros e hospitais, incentivando a doação voluntária e regular.

O app tem telas em Jetpack Compose, regras de negócio na camada **domain**, banco local (**Room**), preferências (**DataStore**) e integração com a API do backend Hemare (**Retrofit**). Funciona **online com a API** e **offline com o banco do aparelho**.

📚 Documentação das etapas do projeto: [`docs/`](docs/) (backlog e roadmap, wireframes, ambiente, telas, integração e testes).

## Tecnologias

- **Kotlin** 2.2.10
- **Jetpack Compose** (Material 3) + Compose BOM 2026.02.01
- **Navigation Compose** — bottom navigation com 3 abas + navegação aninhada dentro da aba Lista
- **MVVM** — cada tela com seu `ViewModel` expondo `UiState` (via `StateFlow`)
- **Room** 2.8.5 (+ KSP) — banco local; **DataStore** — preferências; **Retrofit** + OkHttp — API REST; **Coroutines/Flow**
- Injeção de dependências manual (`di/AppContainer`)
- Testes: JUnit 4 + kotlinx-coroutines-test
- `compileSdk` / `targetSdk` 37, `minSdk` 24 (Android 7.0+)

## Como rodar (em qualquer máquina)

### 1. Instalar o Android Studio
Baixe e instale o [Android Studio](https://developer.android.com/studio) (Windows, macOS ou Linux). Ele já vem com o JDK e o Android SDK necessários — não precisa instalar nada disso separadamente. Na primeira abertura, siga o assistente de configuração padrão (ele baixa as ferramentas do SDK sozinho).

### 2. Clonar o repositório
Com o [Git](https://git-scm.com/downloads) instalado, rode no terminal:
```bash
git clone https://github.com/Henrique-CR0/Projeto-Hemare-Mobile.git
```
(Ou baixe o ZIP pelo botão **Code → Download ZIP** na página do repositório, se preferir não usar Git.)

### 3. Abrir o projeto
No Android Studio, vá em **File → Open** e selecione a pasta `Projeto-Hemare-Mobile` que você acabou de clonar/extrair (o `build.gradle.kts` fica direto na raiz dela). Espere o Gradle sincronizar as dependências — aparece uma barra de progresso na parte inferior da janela, e pode demorar alguns minutos na primeira vez (baixando bibliotecas).

### 4. Preparar onde rodar o app
Você precisa de um emulador Android **ou** um celular físico:

**Opção A — Emulador (não precisa de celular):**
1. No Android Studio, abra o **Device Manager** (ícone de celular na barra lateral direita, ou **Tools → Device Manager**).
2. Clique em **Create Device**, escolha um modelo (ex.: Pixel 8) e uma versão do Android (qualquer uma a partir da 7.0 / API 24), e finalize.

**Opção B — Celular físico:**
1. No celular, ative as **Opções do desenvolvedor** (Configurações → Sobre o telefone → toque 7x em "Número da versão").
2. Dentro de Opções do desenvolvedor, ative a **Depuração USB**.
3. Conecte o celular ao computador via USB e aceite a autorização que aparece na tela do celular.

### 5. Rodar
Escolha o emulador ou dispositivo no seletor ao lado do botão de play (topo do Android Studio) e clique no botão verde **▶**. O app compila, instala e abre sozinho.

Não é necessário configurar nada para rodar: sem servidor, o app usa o banco do aparelho (hemocentros oficiais já vêm incluídos). Para usar a API do backend Hemare, suba o servidor local e, se for outro endereço, informe-o com `-Phemare.apiUrl=http://SEU_HOST:3000/` (o padrão `http://10.0.2.2:3000/` aponta para o computador a partir do emulador).

## Funcionalidades

### 🔐 Login e cadastro
Antes de entrar no app, o usuário faz login ou se cadastra — como **doador** ou como **hospital/hemocentro** (igual ao site). As contas ficam salvas no banco do aparelho (senha com hash) e, quando há servidor, também na API do Hemare. **Não há contas de teste**: para entrar, cadastre-se primeiro.

O cadastro de doador tem as mesmas validações do site (nome sem números, email válido, indicador de força da senha). O cadastro de hospital pede CNPJ (com máscara e validação do dígito verificador), CNES, e endereço completo com seletor de UF.

De acordo com o tipo de conta, o app direciona para uma experiência diferente:
- **Doador** → as 3 abas normais do app (Início, Lista, Configuração).
- **Hospital** → 2 abas: **Hospital** (painel de gestão — estoque, necessidades e doadores compatíveis) e **Configuração** (tema, notificações, conta e sair).

O botão "Sair" está na aba Configuração (doador) ou no topo do painel (hospital).

O app tem 3 abas principais para o doador, acessíveis pela barra inferior:

### 🏠 Início
Tela de boas-vindas com a mensagem principal do Hemare ("Sua doação pode salvar até 4 vidas"), cards com estatísticas rápidas (vidas salvas por doação, tempo médio de doação, percentual da população que doa) e uma ilustração decorativa.

### 📋 Lista
Hub de conteúdo educativo sobre doação de sangue, com 4 seções:

| Item | Conteúdo |
|---|---|
| **Posso doar?** | Formulário de triagem igual ao do site: campos de idade e peso (com aviso em tempo real se fora da faixa ideal) e perguntas de sim/não organizadas em 3 grupos (Situações recentes, Saúde, Condições a confirmar). O resultado tem 3 níveis — verde (tudo indica que pode doar), amarelo (confirmar no hemocentro) e vermelho (ponto importante a verificar) — com os motivos de cada um. |
| **Onde doar** | Lista dos hemocentros oficiais (API ou banco local, funciona offline) com nome, cidade/estado, endereço e telefone, com campo de busca por cidade/estado. |
| **Guia de doação** | Orientações organizadas em 3 fases — antes, durante e depois de doar — adaptadas do guia do site Hemare. |
| **Mitos e verdades** | 16 afirmações comuns sobre doação de sangue, cada uma marcada como Mito / Verdade / Depende, com explicação baseada em fontes oficiais (Ministério da Saúde, Hemominas, Hemoce, Pró-Sangue). |

### ⚙️ Configuração
- **Conta**: nome/email da conta logada e botão para sair.
- **Preferências**: alternar entre tema claro e escuro (funcional — muda as cores do app inteiro) e ativar/desativar notificações (as duas escolhas ficam salvas no aparelho com DataStore).
- **Sobre o app**: versão do app e créditos dos desenvolvedores.

### 🏥 Painel do Hospital
Aba exclusiva para contas do tipo hospital (substitui as abas Início e Lista do doador — a aba Configuração continua igual):

- **Termômetro de estoque**: define o nível (Estável / Alerta / Crítico / Emergência) de cada um dos 8 tipos sanguíneos.
- **Publicar necessidade**: escolhe o tipo sanguíneo e a urgência e publica um pedido.
- **Minhas necessidades**: lista o que foi publicado, com botão para ver doadores compatíveis.
- **Doadores compatíveis**: aplica a regra real de compatibilidade ABO/Rh (API ou diretório local) e permite "confirmar doação" para os doadores identificados.

## Estrutura do projeto

```
app/src/main/java/com/example/hemaremobile/
├── MainActivity.kt / HemareApplication (di/)   # ponto de entrada e container de dependências
├── *Screen.kt                                 # telas Compose (Login, Cadastros, Início, Lista, Posso doar?,
│                                              #   Onde doar, Guia, Mitos, Configuração, Painel/Hub/Histórico/Perfil/Plano do hospital)
├── *ViewModel.kt                              # estado das telas (UiState via StateFlow)
├── domain/                                    # regras puras: modelos, compatibilidade ABO/Rh, triagem, validadores
├── data/
│   ├── local/                                 # Room: entidades, DAOs, banco (com hemocentros e doadores de exemplo), hash de senha
│   ├── remote/                                # Retrofit: HemareApi, ClienteApi, Sessao (token JWT)
│   └── repository/                            # contratos (interfaces) e implementações (API → banco local)
├── di/AppContainer.kt                         # DI manual: repositórios e fábricas de ViewModel
├── navigation/Navegacao.kt                    # raiz (auth → doador ou hospital), NavHost e rotas
└── ui/theme/                                  # cores, tema claro/escuro e tipografia
app/src/test/                                  # testes unitários (domain, ViewModels, repositórios)
```

## Testes

```bash
./gradlew testDebugUnitTest
```

68 testes unitários cobrindo regras de negócio (triagem, compatibilidade, validadores), ViewModels (com repositórios falsos) e repositórios (API falsa + DAOs falsos, incluindo o modo offline).

## Roadmap

Veja o backlog completo em [`docs/01-backlog-e-roadmap.md`](docs/01-backlog-e-roadmap.md).

- [x] Presentation Layer (telas + navegação)
- [x] Login e cadastro (doador e hospital), com direcionamento por tipo de conta
- [x] Painel do hospital (estoque, necessidades, match, histórico, perfil e plano)
- [x] Domain Layer (compatibilidade sanguínea, triagem, validadores)
- [x] Repository/Data Layer (Room + Retrofit + DataStore) com funcionamento offline
- [x] Persistência de contas e preferências
- [x] Testes unitários
- [ ] Publicar o backend e apontar `API_BASE_URL` para ele
- [ ] Localização real e mapa em "Onde doar"
- [ ] Notificações push

## Autores

- Henrique Carneiro Ribeiro
- Lucas Pereira Vietiez
- Rinaldo Pereira de Andrade Júnior
- Thiago Louback Bonifácio
