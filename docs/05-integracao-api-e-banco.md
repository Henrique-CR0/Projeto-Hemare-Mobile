# 5. Integração de componentes, APIs, serviços e Banco de Dados

## Estratégia: online-first com fallback offline

Cada repositório tenta a **API do Hemare**; se não houver rede/servidor (`IOException`/`HttpException`), usa o **banco local**. Assim o app funciona sem internet.

```
ViewModel ─► Repository ─┬─► HemareApi (Retrofit, JWT no header Authorization)
                         └─► Room (hemocentros, contas, estoque, necessidades, doadores, doações)
```

## Banco local (Room)

Tabelas: contas (senha com hash SHA-256 salgado, perfil do hospital), hemocentros (20 oficiais já incluídos), estoque, necessidades, doadores (8 de exemplo para o match offline) e doações confirmadas (histórico).

## API (backend Hemare — Node/Express + PostgreSQL)

| Recurso do app | Endpoint |
|---|---|
| Entrar | `POST auth/login` |
| Cadastrar | `POST auth/cadastro` (+ `POST hospital/perfil` para os dados da instituição) |
| Hemocentros | `GET locais` |
| Estoque | `GET/POST hospital/estoque` |
| Necessidades | `POST hospital/necessidade`, `GET hospital/necessidades` |
| Doadores compatíveis | `GET hospital/match/:id` |
| Confirmar doação | `POST hospital/confirmar-doacao` |

O token JWT fica em `Sessao`. Hospital cadastrado no servidor entra como **pendente** (`aprovado = false`) até a aprovação.

## Preferências (DataStore)
Tema claro/escuro e notificações persistem entre aberturas do app.

## Componentes ligados
- `di/AppContainer` cria banco, cliente HTTP, sessão e repositórios uma única vez e entrega as fábricas de ViewModel (`MainActivity` e `Navegacao` usam essas fábricas).
- A busca em "Onde doar" espera 300 ms após a digitação antes de consultar (evita uma chamada por letra).

## Como foi verificado
No emulador (Pixel 10): cadastro e login sem servidor (banco local), lista de hemocentros, estoque/necessidade/match/confirmação/histórico do hospital, dados mantidos após fechar e reabrir o app, e login/lista de locais contra um servidor de teste local (`adb reverse tcp:3000 tcp:3000` + `-Phemare.apiUrl=http://localhost:3000/`).

> Pendente: publicar o backend em um servidor e apontar `API_BASE_URL` para ele.
