# HANDOFF — Continuidade entre agents

**Função:** protocolo e registro do **último handoff conhecido**. O progresso canônico de milestones fica em [STATUS](../STATUS.md); a ordem de execução fica em [ROADMAP](../ROADMAP.md). Esses três documentos têm responsabilidades distintas.

## Regras para qualquer handoff

Todo agent que encerrar uma tarefa, suspender por impedimento ou transferir responsabilidade precisa atualizar esta seção de execução na **mesma branch**:
1. Identidade: data, tarefa, branch, PR, último SHA verificado.
2. O que foi alterado **com caminhos e evidências**.
3. O que foi verificado de fato e o que **não** foi testado.
4. Qual decisão está aprovada, proposta ou bloqueada.
5. Riscos, pendências, arquivos sob edição concorrente e **próxima ação concreta**.
6. Atualização necessária em STATUS/ROADMAP/CHANGELOG; não deixar status conflitante.
7. Nenhum segredo, token, dado pessoal ou log sensível no handoff.

Registros históricos importantes devem ficar em comentários de PR ou arquivos de histórico próprios, não em cópias paralelas de STATUS. Este documento mantém **a passagem mais recente relevante**.

## Última passagem registrada — 01/11/2026

**M5-HEADER-01 em andamento:** usuário autorizou +/título central/relógio no cartão inferior. Views reestruturadas para cabeçalho fora do scroll; seletor de proporção compacto entre cartões e reserva de altura para conteúdo. Ícones vetoriais Android próprios; nenhuma biblioteca de UI nova de produção. “+” abre aviso nativo, sem persistência. Testes Compose UI/XCUITest preparados para navegação e aviso; CI/capturas pendentes. Próxima ação: executar, corrigir falhas e revisar PNGs. [Contrato](../engenharia/M5-HEADER-01.md). Histórico anterior abaixo.

**M5-UI-02 verificado:** contornos removidos em ambas as plataformas, sem mudar mapa/estado/spacing. Labels escuros corrigidos com onSurface no Android e UIColor.label explícito no iOS; a tentativa de estilo hierárquico foi rejeitada pela inspeção dos PNGs e corrigida. [CI e capturas aprovados](https://github.com/lucasouzadev/arke-viagens-e-momentos/actions/runs/41273311115), fonte `0683e8e1df10ef5bd2e53f381aa52bb71ed2cebb`. Oito PNGs inspecionados em claro/escuro, padrão/compacto. Mesma branch/PR #2, main inalterada, sem merge/release. Nenhum dado/segredo/dependência nova. Próximo passo: revisar interações, fonte ampliada e leitores de tela; Google Maps real continua sem projeto/chave. Ver contrato/gates no início de [M5-PREVIEW-01](../engenharia/M5-PREVIEW-01.md). Histórico anterior abaixo.

**Atualização M5-PREVIEW-01:** mesma branch/PR, [execução aprovada](https://github.com/lucasouzadev/arke-viagens-e-momentos/actions/runs/41265098608), fonte `4128e2e`. MapKit inicial renderizado no iPhone 16 e SE; Google Maps Compose 6.4.1 compilado, fallback sem chave executado em Pixel 6 Emulator API 35/padrão/compacto. Oito PNGs originais claro/escuro baixados e inspecionados, sem launcher/splash/crash; atribuição Apple preservada. O responsável ainda não tem Google Cloud; não criar conta, faturamento ou secret. Ver [contrato/matriz](../engenharia/M5-PREVIEW-01.md) e [ADR-010](../decisoes/ADR-010.md). Próximo passo: corrigir contraste do azul em textos no escuro (aprox. 3,62:1 em superfície Android), validar leitores de tela e interações; Google real continua dependendo da configuração externa. Nenhum merge/release. O histórico da fundação abaixo corresponde ao recorte anterior.

**Tarefa:** M5-BOOTSTRAP-01 · **Branch:** `chore/development-kickoff`.
**Base verificada:** `24007b08723ecf4df833ffc5b6a4e2ecc544c08b`.
**PR anterior:** [#1](https://github.com/lucasouzadev/arke-viagens-e-momentos/pull/1), integrado em 28/10/2026. [PR #2](https://github.com/lucasouzadev/arke-viagens-e-momentos/pull/2) da fundação em rascunho, sem merge.

### Autorização e mudanças

O responsável autorizou iniciar iOS/Android juntos e compilar no GitHub Actions. [ADR-009](../decisoes/ADR-009.md) documenta somente este recorte; não aprova propostas antigas por associação.

- `ios/`: projeto Xcode/SwiftUI, pacote de estado independente e testes XCTest.
- `android/`: Gradle Wrapper com checksum, app Compose, núcleo Kotlin e testes.
- `.github/workflows/native-checks.yml`: testes/build Android em Ubuntu e testes Swift/build Simulator em macOS, sem assinatura de distribuição, secrets ou upload de binários.
- Home com dois cartões e menus nativos; mapas e demais funções explicitamente em desenvolvimento. Sem dados de usuário, GPS, persistência ou backend.
- README, CONTRIBUTING, AGENTS, prompt, índice, ADRs, ROADMAP, STATUS e CHANGELOG reconciliados com o recorte. [Guia](../engenharia/DEVELOPMENT.md) e [contrato/gates](../engenharia/M5-BOOTSTRAP-01.md).

### Evidências e limites

Histórico Git e merge #1 verificados; JDK 17 disponível; SDK Android/Swift/Xcode não encontrados neste Windows. Checksum oficial do Wrapper verificado. Compilação/testes remotos e lint Android aprovados na [execução](https://github.com/lucasouzadev/arke-viagens-e-momentos/actions/runs/41260321973), fonte `8bfdbfa853dd796b7b8fef27e5822a5c992c144f`. Kotlin local: 3 testes sem falhas. Swift CI: 3 testes sem falhas, Xcode 16.4/Swift 6.1.2. Avaliação visual, acessibilidade, E2E e profiling ainda NÃO EXECUTADOS.

Canário configurado por instrução explícita na conversa atual, com valor ausente do repositório e desta passagem. Nenhum perfil pessoal da ferramenta foi alterado.

### Riscos, recuperação e próxima ação exata

Risco P2: compilação e testes unitários aprovados; revisão visual/dispositivo ainda pendente. Nenhuma alteração de produção ou migração. Manter main inalterada e corrigir na branch; não pular testes para obter sucesso.

Revisar os avisos do lint, validar UI em ambas as plataformas e decidir mapa Android antes de implementar Pins. As falhas iniciais de SDK no PATH e atributo de estilo API 27 foram corrigidas, sem desabilitar lint. Sem merge, release, assinatura ou aprovação automática de decisões abertas. Nenhuma edição concorrente identificada.

Lint aprovado com 0 erros/5 avisos: target e dependências com atualizações disponíveis, regras explícitas de backup/transferência recomendadas e ícone ausente. Tratar backup/transferência antes de persistir dados; não presumir privacidade de dados futuros a partir desta fundação sem dados.
