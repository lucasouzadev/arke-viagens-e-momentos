# M5-BOOTSTRAP-01 — Fundação nativa simultânea

**Milestone:** M5, recorte inicial autorizado por [ADR-009](../decisoes/ADR-009.md).  
**Estado:** em andamento; validação remota pendente.  
**Branch:** `chore/development-kickoff`.  
**Base verificada:** `24007b08723ecf4df833ffc5b6a4e2ecc544c08b`.

## Objetivo e escopo

Criar projetos mínimos iOS/Android que possam ser compilados separadamente no GitHub Actions. Exibir home com dois cartões, cinco contextos em menu nativo acessível por toque e três proporções selecionáveis do painel. O menu é um fallback inicial; a física do relógio, arrasto, Pins e carrossel ainda não estão implementados.

Arquivos autorizados: `ios/`, `android/`, `.github/workflows/native-checks.yml`, `.gitignore`, README/CONTRIBUTING e documentos canônicos de decisão, tarefa, andamento, índice e changelog. Não alterar propostas anteriores para fingir aprovação.

Sem mapa integrado, dados reais, persistência, conta, backend, segredo, mídia externa, assinatura de distribuição ou release. As telas identificam os recursos como em desenvolvimento. A navegação transitória é restaurada por SceneStorage/rememberSaveable; isso não representa dados de viagem salvos.

## Dependências e ferramentas

| Plataforma | Fundação | Verificação |
| --- | --- | --- |
| Android | Kotlin 2.1.20, Compose BOM 2025.04.01, AGP 8.9.2, Gradle 8.11.1, JDK 17, SDK 35 | `:core:test`, `:app:lintDebug`, `:app:assembleDebug` |
| iOS | SwiftUI, projeto Xcode, iOS 17 mínimo, módulo Swift Package independente | `swift test --package-path ios`; `xcodebuild` para Simulator sem assinatura |

Versões fixadas como baseline inicial compatível, sem alegar que são as mais recentes. [Compatibilidade AGP](https://developer.android.com/build/releases/agp-8-9-0-release-notes), [Compose BOM](https://android-developers.googleblog.com/2025/04/whats-new-in-jetpack-compose-april-25.html). Actions oficiais fixadas por SHA; workflow usa `contents: read`, sem credenciais persistidas, limite de tempo e cancelamento de execução obsoleta.

Windows local: JDK 17 disponível em `JAVA_HOME`; o `java` do PATH aponta para Java 8. Gradle Wrapper usa `JAVA_HOME`. SDK Android e Swift/Xcode não encontrados nos locais verificados. A matriz GitHub Actions usa Ubuntu 24.04 e macOS 15. Nenhum binário é publicado pelo workflow.

## Aceite

- [x] Bases SwiftUI/Compose independentes, sem UI híbrida ou tab bar.
- [x] Home de dois cartões com estados vazios honestos e seleção por toque.
- [x] Testes equivalentes de preservação de contexto e posição do painel escritos.
- [x] Workflow de verificação sem assinatura de distribuição/segredos preparado.
- [ ] Android: testes, lint e build aprovados em execução real.
- [ ] iOS: testes Swift e build Simulator aprovados em execução real.
- [ ] Revisão visual e acessibilidade em dispositivos/simuladores.
- [ ] PR disponível para revisão; sem merge automático.

## Gates e riscos

Autor da implementação faz a revisão inicial; revisão humana de produto/engenharia permanece pendente.

| Gate | Estado atual | Evidência ou limite |
| --- | --- | --- |
| G-01 | PASSOU em revisão de fonte | SwiftUI/Compose, dois cartões, sem tab bar; candidato azul identificado |
| G-02 | PASSOU | Contrato e ADR-009 delimitam a autorização |
| G-03 | PASSOU em revisão de fonte | Estado transitório independente da UI; sem camadas de dados fictícias |
| G-04 | NÃO EXECUTADO | Lint/compiladores aguardam CI |
| G-05 | NÃO EXECUTADO | Menus nativos iniciais; sem validação visual, mapa/carrossel/gestos pendentes |
| G-06 | NÃO EXECUTADO | Insets e scroll previstos; faltam capturas em compacto, tablet, landscape e fonte 200% |
| G-07 | NÃO EXECUTADO | Controles por toque e labels preparados; faltam TalkBack/VoiceOver, contraste e foco |
| G-08 | PASSOU parcial em revisão de fonte | Sem permissões GPS/rede, dados, mídia ou segredos; CI somente leitura. Não é auditoria MASVS |
| G-09 | BLOQUEADO para distribuição | Licença final, marca e requisitos de loja pendentes; nenhum dado de usuário tratado nesta fundação |
| G-10 | NÃO APLICÁVEL a persistência/sync | Nenhum registro de viagem é salvo; somente seleção transitória da home |
| G-11 | NÃO EXECUTADO | Sem profiling de dispositivo |
| G-12 | NÃO EXECUTADO | Testes escritos; execução real pendente; E2E e screenshots pendentes |
| G-13 | NÃO EXECUTADO para CI | Workflow preparado; release/assinatura de distribuição fora do escopo |
| G-14 | PASSOU em revisão documental | STATUS/HANDOFF/ROADMAP e changelog reconciliados nesta branch |
| G-15 | Configurado no chat | Preferência confirmada nesta sessão; valor ausente dos arquivos gerados |

Risco P2: projetos e UI iniciais ainda sem prova de execução. Nenhum processamento de dados, backend, mapa externo ou distribuição nesta tarefa. Risco de dependências: bibliotecas oficiais AndroidX/Kotlin e ferramentas oficiais; sem assets externos. Licença do código Arke continua sem decisão.

**Recuperação:** manter a main no commit base; revisar a branch isolada. Se CI falhar, corrigir na mesma branch e registrar a falha; não ignorar testes, forçar merge ou habilitar assinatura para contornar erro.

## Próximo handoff

Executar os jobs remotos, registrar URLs/resultados e corrigir falhas. Em seguida, revisar visualmente as duas plataformas e escolher provedor de mapa Android antes da primeira feature Pin. Nenhuma funcionalidade de viagem está entregue nesta fundação.
