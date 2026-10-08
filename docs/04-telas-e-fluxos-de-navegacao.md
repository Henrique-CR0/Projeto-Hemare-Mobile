# 4. Primeiras Telas e Fluxos de Navegação

Navegação com **Navigation Compose** (`navigation/Navegacao.kt`). A raiz escolhe o fluxo conforme a conta logada.

| Fluxo | Telas |
|---|---|
| **Autenticação** (sem conta logada) | Login → Cadastro de doador / Cadastro de hospital (validação de e-mail, força da senha, CNPJ com dígito verificador, CEP, UF) |
| **Doador** (3 abas na barra inferior) | **Início** · **Lista** (hub → Posso doar?, Onde doar, Guia de doação, Mitos e verdades) · **Configuração** (conta, tema, notificações, equipe, sair) |
| **Hospital** (2 abas) | **Hospital** (hub com selo "Verificado" e contadores → Estoque e necessidades, Histórico de doações, Perfil da instituição, Plano) · **Configuração** |

Detalhes:
- Telas internas têm cabeçalho vermelho com **seta de voltar**; a aba selecionada mantém o estado ao trocar de aba.
- Todas as telas do painel do hospital compartilham o mesmo `PainelHospitalViewModel` (escopo do grafo da aba), por isso o estoque, as necessidades e o histórico ficam coerentes entre elas.
- Tema **escuro** (padrão) e **claro**, trocados em Configuração e salvos no aparelho.
- Não há contas de exemplo: o primeiro acesso é pelo cadastro.
