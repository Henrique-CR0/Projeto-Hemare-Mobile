# 6. Execução de Testes Unitários

```bash
./gradlew testDebugUnitTest
```

**Resultado atual: 68 testes, 0 falhas.**

| Arquivo | Testes | O que cobre |
|---|---:|---|
| `domain/RegrasDeNegocioTest` | 15 | Compatibilidade ABO/Rh, triagem "Posso doar?" (limites de idade e peso, impedimentos × atenções), validadores (e-mail, senha, CNPJ, CEP) |
| `AutenticacaoViewModelTest` | 10 | Login, cadastro de doador/hospital com perfil, e-mail repetido, sair, erros |
| `PossoDoarViewModelTest` | 11 | Resultado verde/amarelo/vermelho, campos numéricos, refazer |
| `PainelHospitalViewModelTest` | 10 | Estoque, publicar necessidade, match, confirmar doação e histórico |
| `ViewModelsTest` | 3 | Busca em "Onde doar" (debounce) e preferências |
| `data/RepositoriosTest` | 19 | Login/cadastro via API e offline, hash de senha, hemocentros (API → cache → offline), estoque, necessidades, match e doações com servidor fora do ar |
| `ExampleUnitTest` | 1 | Modelo do Android Studio |

## Como os testes são feitos
- **ViewModels** rodam na JVM com `RegraDispatcherPrincipal` (troca o `Dispatchers.Main`) e **repositórios falsos** (`Fakes.kt`).
- **Repositórios** são testados com API falsa (online, fora do ar, erro HTTP) e DAOs falsos em memória.
- Não dependem de emulador nem de internet.
