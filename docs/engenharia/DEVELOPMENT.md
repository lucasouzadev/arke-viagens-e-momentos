# Desenvolvimento local e GitHub Actions

A fundação das duas bases foi autorizada em [ADR-009](../decisoes/ADR-009.md). O aplicativo ainda não está disponível para uso. [Contrato e evidências](M5-BOOTSTRAP-01.md).

## Android

Abra `android/` no Android Studio, instale JDK 17, Android SDK Platform 35 e Build Tools 35.0.0. Configure o SDK pelo Android Studio ou `ANDROID_HOME`; `local.properties` permanece ignorado pelo Git.

No PowerShell, a partir da raiz:

```powershell
Set-Location android
.\gradlew.bat :core:test :app:lintDebug :app:assembleDebug
```

No Linux/macOS use `bash gradlew` com os mesmos argumentos. O Wrapper baixa Gradle 8.11.1 e verifica SHA-256. Em Windows, confirme `JAVA_HOME` apontando para JDK 17; um Java antigo no PATH não deve ser usado diretamente para esta compilação.

O build debug é destinado a desenvolvimento. Não é APK de produção e não é publicado automaticamente.

## iOS

Em um Mac com Xcode e SDK iOS Simulator instalados, abra `ios/Arke.xcodeproj` e selecione o scheme compartilhado `Arke`. Targets iniciais: iOS 17+, iPhone/iPad. Identificador de desenvolvimento provisório, sem equipe Apple definida.

```sh
swift test --package-path ios
xcodebuild -project ios/Arke.xcodeproj -scheme Arke \
  -configuration Debug -destination 'generic/platform=iOS Simulator' \
  -derivedDataPath build/ios CODE_SIGNING_ALLOWED=NO build
```

Os testes Swift Package cobrem o estado independente de UI; não substituem XCUITest ou testes em iPhone/iPad.

## GitHub Actions

O workflow [native-checks.yml](../../.github/workflows/native-checks.yml) executa em PRs, pushes na main e acionamento manual após estar disponível na branch padrão. Android roda em Ubuntu 24.04; iOS em macOS 15 com Xcode da imagem, cuja versão aparece no log. Não usa secrets nem faz upload de binários, deploy ou release.

Uma PR aberta dispara os dois jobs. Confira separadamente testes, lint e build: workflow escrito não comprova execução. A configuração pode exigir habilitação de Actions pelo proprietário caso haja política de conta/repositório.

Runners padrão são gratuitos em repositórios públicos conforme [documentação GitHub](https://docs.github.com/en/actions/how-tos/write-workflows/choose-where-workflows-run/choose-the-runner-for-a-job). Repositórios privados seguem franquias e cobrança próprias; não usar runners maiores pagos nesta fundação.

## Ideias locais e próximos recortes

Mapas escolhidos em [ADR-010](../decisoes/ADR-010.md): Apple Maps no iOS e Google Maps no Android. No Android, configurar futuramente `ARKE_MAPS_API_KEY` como propriedade Gradle privada ou variável de ambiente; não adicionar à `gradle.properties` versionada e não enviar em chat. Restringir ao package `dev.arke.app.dev`, SHA-1 do certificado de desenvolvimento e somente Maps SDK for Android. Sem chave, o app exibe o fallback e não monta o SDK. O responsável ainda não possui projeto Google Cloud; não criar/faturar automaticamente.

O workflow agora inclui captura em iPhone Simulator e Android Emulator, com PNGs retidos por 7 dias. Capturas demonstram a tela real e não substituem testes completos de acessibilidade/fluxo. Nenhum APK/IPA é publicado.

O fluxo [M5-PLACES-01](M5-PLACES-01.md) guarda nome/endereço escrito/nota em JSON v1 no sandbox, sem geocoding ou Pin. iOS usa Application Support excluído de backup e proteção de arquivo; Android usa noBackupFilesDir e regras de backup/transferência excluídas. Falha de leitura bloqueia gravação e preserva arquivo existente; não apagar dados locais para corrigir erro sem ação explícita do usuário. Reversões de código devem preservar snapshots. Arquivos com versão desconhecida exigem uma migração própria; não há migração destrutiva ou reset automático.

Scripts capturam home e editor vazio em dois tamanhos e dois temas por plataforma (16 PNGs). iOS tem entrada de captura apenas Debug, que abre o editor sem preencher ou salvar; Android abre o + pelo nó de acessibilidade observado. Testes usam somente dados sintéticos.

Revisar contratos mínimos de Pin geográfico, recuperação/sync e comportamento do mapa antes dos próximos recortes. Aprovar o azul e testar as telas em claro/escuro, landscape, fonte ampliada e leitores de tela. Grupos, finanças, sincronização e publicação exigem tarefas próprias.
