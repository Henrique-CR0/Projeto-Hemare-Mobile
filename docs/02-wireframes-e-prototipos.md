# 2. Wireframes e Protótipos

Arquivo no Figma: <https://www.figma.com/design/WKfC8AU2t05sCkCjWd9HwG>

| Página do Figma | Conteúdo |
|---|---|
| 🎨 Capa, Wireframes e Design System | Capa, **19 wireframes** em baixa fidelidade (cinza), tokens de cor/tipografia (variáveis) e componentes reutilizáveis (botões, campos, cabeçalho, barra de navegação, cartões, chips, selos). |
| 🌙 Telas — Tema escuro (padrão) | As telas em alta fidelidade, quadros de 411 × 923 dp (o tamanho real do app no emulador Pixel 10). |
| ☀️ Telas — Tema claro | As mesmas telas no tema claro. |

## Telas desenhadas

Login · Cadastro de doador · Cadastro de hospital · Início · Lista · Posso doar? (+ resultado) · Onde doar · Guia · Mitos e verdades · Configuração (doador e hospital) · Hospital — Início (hub) · Estoque e necessidades (+ necessidade publicada, + doadores compatíveis) · Histórico de doações · Perfil da instituição · Plano.

## Protótipo navegável

Cada tema tem ligações de protótipo: botões do login e cadastros, abas da barra inferior, itens da Lista, botão voltar, "Ver resultado", "Ver doadores", "Publicar" e "Sair". Para testar, abra o arquivo no Figma e clique em **Present (▶)**.

## Fluxos principais

```
Login ─┬─ Cadastro doador ──► Área do doador: Início │ Lista ─► Posso doar? / Onde doar / Guia / Mitos │ Configuração
       └─ Cadastro hospital ─► Área do hospital: Hub ─► Estoque e necessidades ─► Doadores compatíveis
                                                    ├► Histórico de doações
                                                    ├► Perfil da instituição
                                                    └► Plano            │ Configuração
```

> Os quadros foram medidos a partir do app rodando no emulador (cabeçalho, margens, altura da barra de status e da navegação), para que o design seja idêntico ao mobile.

## Plugin que gera este design

O design pode ser refeito a partir do app com o plugin em [`figma/plugin-hemare`](figma/plugin-hemare/LEIA-ME.md) (Figma Desktop → Plugins → Desenvolvimento → Importar do manifesto).
