# 1. Backlog do Produto e Roadmap — Hemare Mobile

Aplicativo Android (Kotlin + Jetpack Compose) que aproxima **doadores de sangue** e **hospitais/hemocentros**.
Equipe: Henrique (SM), Rinaldo (Dev Front), Lucas (Dev Back), Thiago (UX/UI).

## Personas

| Persona | Necessidade |
|---|---|
| **Doador** | Saber se pode doar, onde doar, tirar dúvidas e ser avisado quando o seu tipo sanguíneo for necessário. |
| **Hospital / hemocentro** | Informar o estoque, publicar necessidades urgentes e encontrar doadores compatíveis. |

## Backlog do Produto

Prioridade: **A** = essencial (MVP), **M** = média, **B** = baixa. Situação: ✅ pronto · 🟡 parcial · ⬜ a fazer.

### Épico 1 — Conta e acesso
| ID | História de usuário | Prior. | Situação |
|---|---|---|---|
| US-01 | Como doador, quero me cadastrar com nome, e-mail e senha para usar o app. | A | ✅ |
| US-02 | Como hospital, quero me cadastrar com CNPJ, CNES e endereço para ter um painel próprio. | A | ✅ |
| US-03 | Como usuário, quero entrar com e-mail e senha e sair quando quiser. | A | ✅ |
| US-04 | Como usuário, quero ver a força da senha e erros de preenchimento (e-mail, CNPJ, CEP). | M | ✅ |
| US-05 | Como hospital, quero que o cadastro seja aprovado/verificado pela equipe Hemare. | M | 🟡 (campo `aprovado` existe; aprovação depende do servidor) |
| US-06 | Como usuário, quero recuperar a senha por e-mail. | B | ⬜ |

### Épico 2 — Experiência do doador
| ID | História de usuário | Prior. | Situação |
|---|---|---|---|
| US-07 | Como doador, quero uma tela inicial que explique a importância da doação. | A | ✅ |
| US-08 | Como doador, quero a ferramenta **Posso doar?** (idade, peso e perguntas de triagem) com resultado verde/amarelo/vermelho. | A | ✅ |
| US-09 | Como doador, quero ver **hemocentros** e buscar por cidade/estado, mesmo sem internet. | A | ✅ |
| US-10 | Como doador, quero um **guia** de como doar e **mitos e verdades**. | M | ✅ |
| US-11 | Como doador, quero escolher tema claro/escuro e ativar notificações; as escolhas ficam salvas. | M | ✅ |
| US-12 | Como doador, quero ver hemocentros no mapa, por proximidade (GPS). | M | ⬜ |
| US-13 | Como doador, quero receber notificação push quando o meu tipo sanguíneo for urgente. | M | ⬜ |
| US-14 | Como doador, quero um histórico das minhas doações. | B | ⬜ |

### Épico 3 — Painel do hospital
| ID | História de usuário | Prior. | Situação |
|---|---|---|---|
| US-15 | Como hospital, quero informar o nível do estoque de cada tipo sanguíneo. | A | ✅ |
| US-16 | Como hospital, quero publicar uma necessidade (tipo + urgência). | A | ✅ |
| US-17 | Como hospital, quero ver os doadores compatíveis com cada necessidade (**match**). | A | ✅ |
| US-18 | Como hospital, quero confirmar a doação de um doador e ver o **histórico**. | M | ✅ |
| US-19 | Como hospital, quero ver o perfil da instituição e o plano contratado. | M | ✅ |
| US-20 | Como hospital, quero contatar o doador compatível (ligação/WhatsApp) direto do app. | M | ⬜ |

### Épico 4 — Plataforma
| ID | Item técnico | Prior. | Situação |
|---|---|---|---|
| TEC-01 | Arquitetura MVVM + camadas `domain` / `data` / UI. | A | ✅ |
| TEC-02 | Banco local (Room) com cache e uso offline. | A | ✅ |
| TEC-03 | Integração com a API REST do Hemare (Retrofit + JWT). | A | ✅ (testada com servidor local; backend ainda não publicado) |
| TEC-04 | Preferências persistidas (DataStore). | M | ✅ |
| TEC-05 | Testes unitários das regras de negócio, ViewModels e repositórios. | A | ✅ |
| TEC-06 | Publicar o backend (Node/Express + PostgreSQL) em um servidor. | A | ⬜ |
| TEC-07 | Pipeline de CI (build + testes a cada push). | M | ⬜ |
| TEC-08 | Testes instrumentados de UI (Compose). | B | ⬜ |

## Roadmap

| Fase | Foco | Entregas | Situação |
|---|---|---|---|
| **0 — Descoberta** | Entender o problema e desenhar | Personas, backlog, wireframes e protótipo no Figma | ✅ |
| **1 — Fundação** | Ambiente e estrutura | Projeto Android, tema (claro/escuro), navegação, MVVM | ✅ |
| **2 — Telas e fluxos** | Interface completa | Login/cadastros, área do doador (Início, Lista, Posso doar, Onde doar, Guia, Mitos, Configuração), área do hospital (Painel, Histórico, Perfil, Plano) | ✅ |
| **3 — Dados e integração** | Persistência e API | Room, Retrofit, DataStore, DI manual, funcionamento offline | ✅ |
| **4 — Qualidade** | Testes | Testes unitários (domínio, ViewModels, repositórios) | ✅ |
| **5 — Backend em produção** | Servidor real | Publicar a API, apontar `API_BASE_URL`, aprovação de hospitais | ⬜ próximo |
| **6 — Recursos avançados** | Engajamento | Mapa/GPS, notificações push, contato direto com o doador | ⬜ |
| **7 — Lançamento** | Entrega | CI, testes de UI, publicação na Play Store | ⬜ |

## Critérios de pronto (Definition of Done)

- Compila sem erros (`./gradlew assembleDebug`).
- Testes unitários passando (`./gradlew testDebugUnitTest`).
- Telas conferidas no emulador nos temas claro e escuro.
- Revisão por outro integrante antes de entrar na `main`.
