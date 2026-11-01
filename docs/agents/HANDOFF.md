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

## Última passagem registrada — 31/10/2026

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
