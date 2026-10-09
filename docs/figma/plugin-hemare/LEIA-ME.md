# Plugin "Hemare Mobile — Atualizar design"

Refaz o arquivo do Figma do Hemare Mobile igual ao app atual (versão da `main`):
paleta escura quente, cabeçalho com seta de voltar, hub do hospital com Estoque,
Histórico, Perfil e Plano, 4 integrantes na Configuração e login sem contas de teste.

Roda pelo **Figma Desktop**, então não depende do limite de chamadas da integração com o Claude.

## Como rodar (uns 3 minutos)

1. Instale o **Figma Desktop**: <https://www.figma.com/downloads/> (plugins em desenvolvimento só rodam no app desktop).
2. Abra o arquivo do projeto: <https://www.figma.com/design/WKfC8AU2t05sCkCjWd9HwG>
3. Menu **Plugins → Development → Import plugin from manifest…** e escolha
   `figma-plugin-hemare/manifest.json` (esta pasta).
4. **Plugins → Development → Hemare Mobile — Atualizar design**. Leva cerca de 1 minuto.
   No fim aparece "✅ Hemare Mobile atualizado — telas: 19 por tema · protótipo: 59/59 ligações · wireframes: 19".

## O que o plugin faz

Usa as 3 páginas do arquivo e **refaz tudo nelas** (o conteúdo antigo é apagado e criado de novo):

| Página | Conteúdo |
|---|---|
| 🎨 Capa, Wireframes e Design System | Capa, variáveis de cor, estilos de texto, ícones, componentes e os 19 wireframes em cinza |
| 🌙 Telas — Tema escuro (padrão) | 19 telas + protótipo navegável |
| ☀️ Telas — Tema claro | As mesmas 19 telas no tema claro + protótipo |

Telas: Login, Cadastro Doador, Cadastro Hospital, Início, Lista, Posso doar? (+ resultado), Onde doar, Guia,
Mitos e verdades, Configuração (doador), Hospital — Início (hub), Estoque e necessidades (+ necessidade publicada,
+ doadores compatíveis), Histórico de doações, Perfil da instituição, Plano, Configuração (hospital).

Protótipo: abra uma página de telas e clique em **▶ Present** (começa no Login). A seta de voltar, as abas,
os itens da Lista e do hub, "Publicar", "Ver doadores", "Confirmar doação" e "Sair" navegam entre as telas.

## Arquivos

- `atualizar.js` — o plugin (gerado: `code-v3.js` + a entrada que monta as páginas).
- `code-v3.js` — gerador das telas (medidas tiradas do app rodando no emulador).
- `code-v2.js`, `code.js`, `code-mcp.js` — versões anteriores, guardadas só como histórico.
- Se algo der errado, use **Ctrl+Z** no Figma para desfazer, ou o histórico de versões do arquivo
  (**File → Show version history**).
