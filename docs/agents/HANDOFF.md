# HANDOFF — Continuidade entre agents

**M5-EDITOR-UI-01 em validação:** adicionar/editar/revisar alinhados ao padrão visual dos detalhes, com i, menos texto permanente, ícones discretos e cápsulas. Builds/testes/capturas novos pendentes; sem merge/release.

**M5-EDIT-01 verificado e em revisão:** cabeçalho central, bookmark preenchido/alinhado, Próximo largo na primeira página, retorno por ícone e Concluído na última/unitária. Edição pela lista com campos preenchidos, descarte só quando alterado e update mantendo ID/ordem/outros registros; conflito/ausente recusados e retry idempotente. [Contrato](../engenharia/M5-EDIT-01.md). [CI aprovado](https://github.com/lucasouzadev/arke-viagens-e-momentos/actions/runs/41388017517), fonte `4ee4af0`: iOS 10 core + 7 UI; Android 6 core + 11 instrumentados; lint 0/9. 40 PNGs originais inspecionados; falhas anteriores de interação do teste iOS corrigidas sem mudar produto ou remover cenário. Snapshot v1 preservado, sem migração/rede nova. Fonte 200%, leitores de tela, IME Android manual, landscape/tablets, aparelho físico e profiling não executados. PR #2 rascunho, sem merge/release. Próximo: revisão visual humana e próximo contrato de planejamento.

**M5-FLOATING-01 verificado e em revisão:** cartões flutuantes com margem inferior/quatro cantos, alça externa acessível e recuo da home; detalhes com título à esquerda, apenas i, ícone junto ao nome e ícones secundários suaves, sem contador visível. Editor conserva Cancelar/descarte. [Contrato](../engenharia/M5-FLOATING-01.md). Android [aprovado](https://github.com/lucasouzadev/arke-viagens-e-momentos/actions/runs/41381270981), fonte `0fa908b`: 6 core + 10 instrumentados, incluindo Voltar com Arke em primeiro plano. iOS [aprovado](https://github.com/lucasouzadev/arke-viagens-e-momentos/actions/runs/41380227823), fonte `95987b8`, mesmo código iOS: 9 core + 7 UI. Lint 0 erros/9 avisos; 32 PNGs originais inspecionados. Aviso de travamento do Pixel Launcher diagnosticado e tratado exclusivamente no teste; sem alterar Voltar do app ou remover cenários. Dados/schema/IO intactos. Fonte 200%, leitores de tela, landscape/tablets, aparelho físico e profiling não executados. PR #2 rascunho, sem merge/release. Próximo: revisão visual humana e futuro contrato de edição segura.

**M5-DETAILS-UI-03 verificado:** detalhes com altura acompanhando conteúdo, cabeçalho circular, nome equilibrado, endereço/nota agrupados e navegação em cápsulas. [CI aprovado](https://github.com/lucasouzadev/arke-viagens-e-momentos/actions/runs/41360045740), fonte `493bb7063d22cc1dc26537dd284045eae3f9897c`: iOS 9 core + 6 UI; Android 6 core + 8 instrumentados; lint 0/9 anteriores. 32 PNGs originais inspecionados em pares, incluindo lugar sem nota. Testes verificam encolhimento e nota longa rolável. Android repetido após travamento do Pixel Launcher sobre a captura, sem alterar fonte; repetição aprovada. Limites e gates no contrato. Dados/IO/editor preservados. Fonte 200%, leitores de tela, landscape/tablets e profiling não executados. PR #2 rascunho, sem merge/release. Próximo: revisão visual humana e futuro contrato de edição segura.

**M5-DETAILS-UI-02 verificado:** detalhes simplificados, X/i, contador integrado ao cabeçalho, nome em destaque, endereço/nota com ícones, informação sob demanda e navegação preenchida. Sheet mais claro que preenchimentos em ambos os temas, inclusive editor. [CI aprovado](https://github.com/lucasouzadev/arke-viagens-e-momentos/actions/runs/41353801111), fonte `b9582d23b6837fc60e2549b954193a7ceae12d28`: iOS 9 core + 5 UI, Android 6 core + 7 instrumentados; lint 0/9 (um P3 de título antigo sem uso); 24 PNGs originais inspecionados. Painel informativo abre/fecha e preserva seleção nos testes; sem captura estática do painel. Dados/IO intactos. Fonte 200%, leitores de tela, landscape/tablets e profiling não executados. PR #2 em rascunho, sem merge/release. Próximo: contrato de edição segura.

**M5-DETAILS-01 verificado:** abrir ideia salva, ler nome/endereço/nota, percorrer por Anterior/Próximo com contador/limites finitos e Fechar retornando à home. [CI aprovado](https://github.com/lucasouzadev/arke-viagens-e-momentos/actions/runs/41344099050), fonte `8858f3f8792ce0c0189991644aad5adede8762b6`: iOS 9 core + 5 UI, Android 6 core + 7 instrumentados; lint 0/8; 24 PNGs originais inspecionados. Exemplos de captura somente em memória/Debug; teste abre também ideia realmente salva. Dados/schema/IO inalterados. Fonte 200%, leitores de tela, landscape/tablets e profiling não executados. PR #2 segue rascunho, sem merge/release. Próximo: contrato de edição de ideia salva; mapa/geocoding permanecem separados.

**M5-FADE-01 verificado:** fade inferior de 32 pt/dp e espaço final de 48 no editor/revisão, sem corte seco junto às ações, em paridade iOS/Android. [CI aprovado](https://github.com/lucasouzadev/arke-viagens-e-momentos/actions/runs/41337689076), fonte `fd9d9d50da562592f136b82dd4dced224c5c337d`: iOS 9 core + 4 UI, Android 6 core + 6 instrumentados; lint 0/8; 16 PNGs originais inspecionados em pares. Android repetido por timeout de boot antes do app, sem alterar código. Dados/IO/fluxos preservados. Fonte 200%, leitores de tela, landscape/tablet e profiling não executados. Mesmo PR #2 em rascunho, sem merge/release; próximos sheets seguem DESIGN-006.

**M5-PARITY-01 verificado:** guia comum DESIGN-006/ADR-012 obrigatório para próximas telas. Home/editor/revisão com métricas equivalentes, alça visível, Cancelar à esquerda, título central, labels persistentes e CTA fixo. [CI aprovado](https://github.com/lucasouzadev/arke-viagens-e-momentos/actions/runs/41332236559), fonte `6b17366dc3f8e165b93a12a18f4e602aec154ab0`: iOS 9 core + 4 UI; Android 6 core + 6 instrumentados; lint 0/8. Dezesseis PNGs finais inspecionados em pares, padrão/compacto claro/escuro. Altura Android corrigida após inspeção; primeira tentativa final falhou no boot do emulador e foi repetida sem alteração de fonte. Sem dados/schema/IO novos. Limites: fonte 200%, leitores de tela, landscape/tablets e profiling não executados. PR #2 permanece rascunho, sem merge/release. Próximo: seleção/detalhes sob o guia comum.

**M5-PLACES-01 verificado:** adicionar/revisar/corrigir/cancelar, ideias pessoais gravadas no aparelho e exclusão confirmada. [CI aprovado](https://github.com/lucasouzadev/arke-viagens-e-momentos/actions/runs/41326580725), fonte `e54cc6fde18f0851d4f3bc70a6546c81baf43914`. iOS: 9 core + 4 UI; Android: 6 core + 4 UI + 2 repositório; lint 0/8; 16 PNGs inspecionados. Sem geocoding/Pin/conta/rede nova; sem backup nesta etapa. Regras/limites e testes não executados no [contrato](../engenharia/M5-PLACES-01.md). Próximo: contrato de seleção/detalhes e futura associação ao mapa. Mesma branch/PR #2 em rascunho, sem merge/release. Registros anteriores são históricos.

**M5-HANDLE-01 verificado:** puxador vertical contínuo e encaixe nos três estados, com seletor por toque e ações acessíveis. [CI aprovado](https://github.com/lucasouzadev/arke-viagens-e-momentos/actions/runs/41310026574), fonte `f2e8f67496c6b6c21657f6295209f71371f76800`. Três testes UI e três unitários por plataforma aprovados; lint 0/8; oito PNGs padrão/compacto claro/escuro inspecionados. iOS corrigido após falha de retorno; repetição Android superou download inválido do SDK. Detalhes e limites em [contrato](../engenharia/M5-HANDLE-01.md). Gestos testados nos aparelhos padrão; leitores de tela, fonte ampliada e landscape/tablet pendentes. Próximo: contrato do fluxo de adicionar lugar. Sem merge/release. Registros anteriores abaixo são históricos.


**M5-TONES-01 verificado:** fundo/cartão com tons semânticos distintos nas duas plataformas, sem contorno/sombra ou alteração de layout. [CI aprovado](https://github.com/lucasouzadev/arke-viagens-e-momentos/actions/runs/41305472522), fonte `a2e4e0b354ef4d79851779cc7f8040a16812af35`. Builds, testes UI/unitários e lint aprovados; oito PNGs inspecionados. Contrato: [M5-TONES-01](../engenharia/M5-TONES-01.md). Sem merge/release. Próximo: puxador entre cartões.

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

**M5-HEADER-01 verificado:** cabeçalho +/contexto central/relógio nas duas plataformas; seletor nativo por toque, preservando painel. “+” abre aviso, sem salvar dados. [CI aprovado](https://github.com/lucasouzadev/arke-viagens-e-momentos/actions/runs/41276436997), fonte `b9d7c2864f819e70403e2a6756af83448746c0ef`. Dois testes UI e três unitários por plataforma aprovados, lint 0 erros/8 avisos, oito capturas padrão/compacto claro/escuro inspecionadas. Próxima tarefa: puxador entre cartões. Leitores de tela, fonte ampliada, landscape/tablet e profiling pendentes. Google Maps real sem projeto/chave. Mesmo PR #2 em rascunho, sem merge/release. Contrato em docs/engenharia/M5-HEADER-01.md.

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
