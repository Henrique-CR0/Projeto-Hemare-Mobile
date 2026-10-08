# 3. Início do Desenvolvimento — Ambiente e Estrutura Base

## Ambiente

| Item | Versão / ferramenta |
|---|---|
| IDE | Android Studio (JDK embutido) |
| Linguagem | Kotlin 2.2.10 |
| Build | Gradle (wrapper) + Android Gradle Plugin 9.3.2, catálogo de versões em `gradle/libs.versions.toml` |
| UI | Jetpack Compose + Material 3 (BOM 2026.02.01) |
| SDK | `compileSdk`/`targetSdk` 37, `minSdk` 24 (Android 7.0+) |
| Banco local | Room 2.8.5 (KSP 2.3.12) |
| Rede | Retrofit 3.0.0 + converter Gson, OkHttp logging 4.12.0 |
| Preferências | DataStore 1.2.1 |
| Testes | JUnit 4, kotlinx-coroutines-test 1.9.0 |
| Versionamento | Git / GitHub (`main` + branches por funcionalidade) |

Como rodar: veja o README (instalar o Android Studio, abrir a pasta do projeto, escolher emulador e clicar em ▶).
Linha de comando: `./gradlew assembleDebug` (APK) e `./gradlew testDebugUnitTest` (testes).

## Estrutura base

Arquitetura **MVVM em camadas**:

```
UI (Compose) ──► ViewModel (StateFlow/UiState) ──► Repository (interface)
                                                     ├─ API remota (Retrofit)
                                                     └─ Banco local (Room) / DataStore
domain/  → regras puras, sem Android (compatibilidade ABO/Rh, triagem, validadores)
di/      → AppContainer (injeção manual) + HemareApplication
```

- Os ViewModels dependem de **interfaces** de repositório, o que permite testá-los com implementações falsas.
- `BuildConfig.API_BASE_URL` define o endereço da API (padrão `http://10.0.2.2:3000/`; altere com `-Phemare.apiUrl=...`).
- HTTP sem TLS só é liberado para `10.0.2.2` e `localhost` (`res/xml/network_security_config.xml`).
