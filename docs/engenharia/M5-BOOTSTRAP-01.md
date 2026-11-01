# M5-BOOTSTRAP-01 — Fundação nativa simultânea

**Milestone:** M5, recorte inicial autorizado por [ADR-009](../decisoes/ADR-009.md).
**Estado:** fundação compilada e testada em CI; revisão visual e acessibilidade pendentes.
**Branch:** `chore/development-kickoff`.
**Base verificada:** `24007b08723ecf4df833ffc5b6a4e2ecc544c08b`.

## Objetivo e escopo

Criar projetos mínimos iOS/Android que possam ser compilados separadamente no GitHub Actions. Exibir home com dois cartões, cinco contextos em menu nativo acessível por toque e três proporções selecionáveis do painel. O menu é um fallback inicial; a física do relógio, arrasto, Pins e carrossel ainda não estão implementados.

Arquivos autorizados: `ios/`, `android/`, `.github/workflows/native-checks.yml`, `.gitignore`, `.gitattributes`, README/CONTRIBUTING e documentos canônicos de decisão, tarefa, andamento, índice e changelog. Não alterar propostas anteriores para fingir aprovação.

Sem mapa integrado, dados reais, persistência, conta, backend, segredo, mídia externa, assinatura de distribuição ou release. As telas identificam os recursos como em desenvolvimento. A navegação transitória é restaurada por SceneStorage/rememberSaveable; isso não representa dados de viagem salvos.

## Dependências e ferramentas

| Plataforma | Fundação | Verificação |
| --- | --- | --- |
| Android | Kotlin 2.1.20, Compose BOM 2025.04.01, AGP 8.9.2, Gradle 8.11.1, JDK 17, SDK 35 | `:core:test`, `:app:lintDebug`, `:app:assembleDebug` |
| iOS | SwiftUI, projeto Xcode, iOS 17 mínimo, módulo Swift Package independente | `swift test --package-path ios`; `xcodebuild` para Simulator sem assinatura |

Versões fixadas como baseline inicial compatível, sem alegar que são as mais recentes. [Compatibilidade AGP](https://developer.android.com/build/releases/agp-8-9-0-release-notes), [Compose BOM](https://android-developers.googleblog.com/2025/04/whats-new-in-jetpack-compose-april-25.html). Checkout/setup-java oficiais e setup-android da comunidade fixados por SHA; workflow usa `contents: read`, sem credenciais persistidas, limite de tempo e cancelamento de execução obsoleta. A [action de SDK](https://github.com/android-actions/setup-android/tree/be39fa834029ff78f1a44aa3bb0819b8fc2bd8fd) usa licença MIT e Node 24; action.yml/licença revisados. Command-line tools 12266719 fixadas para JDK 17; SDK/build tools 35.

Windows local: JDK 17 disponível em `JAVA_HOME`; o `java` do PATH aponta para Java 8. Gradle Wrapper usa `JAVA_HOME`. SDK Android e Swift/Xcode não encontrados nos locais verificados. A matriz GitHub Actions usa Ubuntu 24.04 e macOS 15. Nenhum binário é publicado pelo workflow.

## Aceite

- [x] Bases SwiftUI/Compose independentes, sem UI híbrida ou tab bar.
- [x] Home de dois cartões com estados vazios honestos e seleção por toque.
- [x] Testes equivalentes de preservação de contexto e posição do painel escritos.
- [x] Workflow de verificação sem assinatura de distribuição/segredos preparado.
- [x] Android: testes, lint e build debug aprovados em execução real.
- [x] iOS: testes Swift e build Simulator aprovados em execução real.
- [ ] Revisão visual e acessibilidade em dispositivos/simuladores.
- [x] [PR #2](https://github.com/lucasouzadev/arke-viagens-e-momentos/pull/2) em rascunho, disponível para revisão; sem merge.

## Gates e riscos

Autor da implementação faz a revisão inicial; revisão humana de produto/engenharia permanece pendente.

| Gate | Estado atual | Evidência ou limite |
| --- | --- | --- |
| G-01 | PASSOU em revisão de fonte | SwiftUI/Compose, dois cartões, sem tab bar; candidato azul identificado |
| G-02 | PASSOU | Contrato e ADR-009 delimitam a autorização |
| G-03 | PASSOU em revisão de fonte | Estado transitório independente da UI; sem camadas de dados fictícias |
| G-04 | PASSOU no recorte automatizado | Lint Android e compiladores aprovados; avisos de manutenção permanecem no relatório |
| G-05 | NÃO EXECUTADO | Menus nativos iniciais; sem validação visual, mapa/carrossel/gestos pendentes |
| G-06 | NÃO EXECUTADO | Insets e scroll previstos; faltam capturas em compacto, tablet, landscape e fonte 200% |
| G-07 | NÃO EXECUTADO | Controles por toque e labels preparados; faltam TalkBack/VoiceOver, contraste e foco |
| G-08 | PASSOU parcial em revisão de fonte | Sem permissões GPS/rede, dados, mídia ou segredos; CI somente leitura. Não é auditoria MASVS |
| G-09 | BLOQUEADO para distribuição | Licença final, marca e requisitos de loja pendentes; nenhum dado de usuário tratado nesta fundação |
| G-10 | NÃO APLICÁVEL a persistência/sync | Nenhum registro de viagem é salvo; somente seleção transitória da home |
| G-11 | NÃO EXECUTADO | Sem profiling de dispositivo |
| G-12 | PASSOU no recorte unitário/build | 3 testes Kotlin locais e CI Kotlin aprovados; 3 testes Swift aprovados no macOS. E2E e screenshots NÃO EXECUTADOS |
| G-13 | PASSOU para CI | Jobs Android/iOS aprovados. Release/assinatura de distribuição NÃO APLICÁVEIS: fora do escopo |
| G-14 | PASSOU em revisão documental | STATUS/HANDOFF/ROADMAP e changelog reconciliados nesta branch |
| G-15 | PASSOU após configuração | Preferência confirmada e aplicada no chat a partir da resposta; valor ausente dos arquivos gerados |

Risco P2: UI inicial ainda sem prova visual ou execução interativa em dispositivo; compilação/testes unitários comprovados. Nenhum processamento de dados, backend, mapa externo ou distribuição nesta tarefa. Risco de dependências: bibliotecas oficiais AndroidX/Kotlin e ferramentas oficiais; sem assets externos. Licença do código Arke continua sem decisão.

**Recuperação:** manter a main no commit base; revisar a branch isolada. Se CI falhar, corrigir na mesma branch e registrar a falha; não ignorar testes, forçar merge ou habilitar assinatura para contornar erro.

## Próximo handoff

Revisar visualmente as duas plataformas e escolher provedor de mapa Android antes da primeira feature Pin. Não tratar os builds aprovados como aprovação de UX ou de propostas de domínio. Nenhuma funcionalidade de viagem está entregue nesta fundação.

## Evidências executadas — 31/10/2026

- [Execução aprovada](https://github.com/lucasouzadev/arke-viagens-e-momentos/actions/runs/41260321973), fonte `8bfdbfa853dd796b7b8fef27e5822a5c992c144f`: Android testes/lint/assembleDebug; iOS testes Swift e build Simulator sem assinatura. Xcode 16.4, Swift 6.1.2 no runner macOS 15.
- Testes Kotlin locais: 3 executados, zero falhas/erros/skips, JDK 17 e Gradle Wrapper 8.11.1; relatório local ignorado pelo Git. No CI, tarefa `:core:test` executada com sucesso.
- Testes Swift: 3 executados, zero falhas, via `swift test` no macOS. Build Xcode: `BUILD SUCCEEDED`.
- Histórico transparente: [execução inicial](https://github.com/lucasouzadev/arke-viagens-e-momentos/actions/runs/41259947482) falhou por SDK ausente no PATH; [segunda execução](https://github.com/lucasouzadev/arke-viagens-e-momentos/actions/runs/41260058502) encontrou atributo de estilo API 27 com minSdk 26. SDK preparado explicitamente e atributo removido, mantendo lint ativo e minSdk 26.
- XMLs Android/scheme parseados; links documentais locais sem destinos ausentes; `git diff --check` sem erros. Wrapper comparado ao checksum oficial.
- Esta atualização de evidências altera somente documentação. Nenhum screenshot, E2E, VoiceOver/TalkBack, profiling, mapa ou dado persistido foi validado.
- Lint Android: **0 erros, 5 avisos** — target API 35 anterior ao mais recente, Compose BOM/activity com versões novas disponíveis, regra explícita de backup/transferência recomendada e ícone de desenvolvimento ausente. Atualização de target/dependências exige nova validação; antes de persistir dados, definir/testar regras de extração/backup. Ícone e identidade final seguem pendentes. Nenhum aviso foi suprimido por baseline.
