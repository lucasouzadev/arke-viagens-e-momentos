# ROADMAP — Plano principal do Arke

**Documento canônico e único de planejamento.** Não criar ROADMAP-V2, ROADMAP-FINAL nem cópias por agente. Base documental integrada pelo PR #1 em 28/10/2026; atualizado em 31/10/2026 para a fundação nativa autorizada. Para andamento factual leia [STATUS](STATUS.md); para continuidade leia [HANDOFF](agents/HANDOFF.md).

## 1. Objetivo e fronteira atual

O Arke é um app de planejamento de viagens solo ou em grupo com uma home de **dois cartões** (mapa, navegação e conteúdo); iOS SwiftUI Liquid nos controles e Android Compose Flat; um azul opaco, restante da aparência automática do dispositivo. [Visão](produto/VISAO.md) · [ADRs](decisoes/README.md).

**Fase atual: fundação nativa delimitada em andamento.** [ADR-009](decisoes/ADR-009.md) autoriza [M5-BOOTSTRAP-01](engenharia/M5-BOOTSTRAP-01.md), ambas as bases e CI de compilação. Propostas M1–M4 continuam pendentes; workflows de publicação, assinatura e release não estão autorizados.

## 2. Convenções de trabalho

Estados permitidos:
- **Proposto**: ideia ou especificação que ainda depende de decisão.
- **Aprovado**: direção autorizada, sem significar código pronto.
- **Em andamento**: trabalho ativo com branch/issue/PR identificável.
- **Em revisão**: artefato submetido, aguardando aceite/merge.
- **Bloqueado**: pré-condição faltante ou autorização ausente.
- **Concluído**: critérios atendidos com evidência e integração confirmada quando aplicável.

Tarefas usam IDs **M{n}-ÁREA-NN**, contrato [TASK-CONTRACT](agents/TASK-CONTRACT.md), revisões [REVIEW-PROTOCOL](agents/REVIEW-PROTOCOL.md) e handoff obrigatório. Não atribuir prazo ou responsável inventado.

## 3. Marcos e dependências

| Marco | Propósito | Dependências | Estado factual |
| --- | --- | --- | --- |
| **M0 — Governança e documentação** | Orientar agents, consolidar roadmap/status/changelog, fechar decisões | docs e ADRs existentes | **Documentação integrada no PR #1** |
| **M1 — Contratos de UX/UI** | Detalhar relógio, carrossel, Pins, dois cartões, motion e acessibilidade | M0 + decisões de design pendentes | Planejado |
| **M2 — Arquitetura nativa** | Desenhar módulos, fronteiras e equivalência iOS/Android, persistência e mapa | M0 e contratos mínimos de M1 | Planejado |
| **M3 — Domínio e colaboração** | Formalizar Trip, Place, Pin, grupo, gasto, cofrinho, avaliação, stamp | M0 e decisões de privacidade/finanças | Planejado |
| **M4 — Qualidade e distribuição** | Gates verificáveis, CI seguro futuro, plano TestFlight/APK e release | M1–M3 especificados | Planejado, **sem configurar pipelines** |
| **M5 — Implementação nativa** | Fundação SwiftUI/Compose e CI de verificação | ADR-009; demais features dependem dos contratos específicos | **Fundação em andamento** |

Marcos documentais podem evoluir em paralelo **quando não dependerem de decisões contraditórias**. M5 recebeu somente a autorização delimitada em ADR-009; o merge #1 não aprova propostas por associação.

## 4. Backlog por marco

### M0 — Agent Foundation (prioridade atual)

| Tarefa | Entrega | Critério de aceite documental | Estado |
| --- | --- | --- | --- |
| M0-AGENT-01 | [AGENTS.md](../AGENTS.md) | fonte da verdade, regras, limites e leitura inicial | Em revisão |
| M0-PLAN-01 | **Este ROADMAP**, sem sufixo | marcos, dependências, estados, legado unificado | Em revisão |
| M0-PROC-01 | [WORKFLOW](agents/WORKFLOW.md), [TASK-CONTRACT](agents/TASK-CONTRACT.md), [REVIEW](agents/REVIEW-PROTOCOL.md) | tarefa/handoff/revisão reproduzíveis | Em revisão |
| M0-CONT-01 | [STATUS](STATUS.md) e [HANDOFF](agents/HANDOFF.md) | progresso factual e retomada entre agents | Em revisão |
| M0-NEWS-01 | [CHANGELOG](../CHANGELOG.md), [guia editorial](agents/CHANGELOG-GUIDE.md) e [modal futuro](releases/NEWS-MODAL.md) | português leigo, sem alegar atualização não lançada | Em revisão |
| M0-GATE-01 | alinhar índice/README/ADR e validar links | referências coerentes, PR para avaliação, sem código | Em revisão |
| M0-STD-01 | [PROMPT OBRIGATÓRIO](agents/PROMPT-OBRIGATORIO.md), [QUALITY-GATES](agents/QUALITY-GATES.md) e [SAFEGUARDS](agents/SAFEGUARDS.md) | protocolos de execução, prova, interrupção e recuperação exigíveis em toda tarefa | Em revisão |
| M0-STD-02 | [Arquitetura](standards/ARCHITECTURE-CODE.md), [Acessibilidade](standards/ACCESSIBILITY-UI.md), [Segurança](standards/SECURITY-PRIVACY.md) e [Jurídico](standards/LEGAL-COMPLIANCE.md) | baselines por domínio + referências oficiais e revisão humana | Em revisão |
| M0-RISK-01 | [Threat model](agents/THREAT-MODEL.md) | formulário de análise de risco antes de dados pessoais, convites, backend e mídia | Em revisão |
| M0-CANARY-01 | [Canário de chat](agents/CANARY.md) | nome proveniente da conversa ou do perfil pessoal privado; apenas respostas de chat, nunca código/artefatos; evidências técnicas separadas | **regra aprovada; configuração privada por ferramenta** |

**M0 concluído somente após revisão, aprovação e integração verificadas.** O trabalho documental anterior DOCS-001/DOCS-002/BRAND-001 permanece rastreável nas especificações, não constitui versão do roadmap separada.

### M1 — UX/UI e interação

| Tarefa | Entrega | Dependência | Estado |
| --- | --- | --- | --- |
| M1-CONTRACT-01 | [Contrato M1 de UX/UI](design/M1-UX-CONTRACT.md) | proposta baseada em DESIGN-001/002/003 e padrões dos agents | Em revisão como proposta |
| M1-UX-01 | validar [DESIGN-004](design/DESIGN-004.md): relógio/carrossel/detents | decidir fallbacks e physics | Proposto |
| M1-UX-02 | validar estados vazios, Pin pendente, drafts [UX-001](produto/UX-001-ESTADOS.md) | M1-UX-01 | Proposto |
| M1-DESIGN-01 | finalizar identidade e [azul único](brand/THEME-001.md) | HEX/ícones/stamps | Proposto |
| M1-A11Y-01 | layout responsivo, fontes, safe areas e gestos alternativos | DESIGN + [QUALITY](qualidade/QUALIDADE.md) | Proposto |
| M1-UI-01 | mockups originais dos estados, em iOS/Android claro/escuro do dispositivo | direções UX/design aprovadas | Proposto |

### M2 — Arquitetura nativa (somente especificar)

| Tarefa | Entrega | Estado |
| --- | --- | --- |
| M2-IOS-01 | fronteiras de módulos SwiftUI/MapKit, navegação e persistência | Proposto |
| M2-ANDROID-01 | fronteiras Compose/mapa, navegação e persistência | Proposto |
| M2-CONTRACT-01 | estados/eventos semânticos equivalentes, lifecycle, offline e fallback | Proposto |
| M2-SECURITY-01 | modelo de dados, credenciais, separação de ambientes e privacidade | Proposto |

### M3 — Domínio do produto

| Tarefa | Entrega | Estado |
| --- | --- | --- |
| M3-DOMAIN-01 | contratos de Place, Pin e Trip | Proposto |
| M3-GROUP-01 | convites, papéis e visibilidade privados/compartilhados | Proposto |
| M3-FINANCE-01 | gastos, cofrinho declarativo, moedas e rateio | Proposto |
| M3-MEMORY-01 | avaliações, fotos, stamps e direitos de mídia | Proposto |
| M3-SYNC-01 | política de conflito/autoria e retenção | Proposto |
| M3-DATA-01 | [M3-DATA-CONTRACT](arquitetura/M3-DATA-CONTRACT.md) | Em revisão como proposta |
| M3-AUTH-01 | [PERMISSIONS](arquitetura/PERMISSIONS.md), papel e visibilidade | Em revisão como proposta |
| M3-SYNC-02 | [SYNC-OFFLINE](arquitetura/SYNC-OFFLINE.md), operações e conflito | Em revisão como proposta |

### M4 — Qualidade, automação e atualização

| Tarefa | Entrega | Estado |
| --- | --- | --- |
| M4-QA-01 | matriz E2E/Maestro, XCUITest/Compose UI e viewport | Proposto |
| M4-CI-01 | arquitetura de CI separando PR sem segredo e release protegido | Proposto |
| M4-RELEASE-01 | assinatura, TestFlight/APK e verificação de disponibilização | Proposto |
| M4-NEWS-01 | contrato para publicar notas, README e modal [NEWS-MODAL](releases/NEWS-MODAL.md) | Proposto |

### M5 — Execução

**Recorte autorizado:** [M5-BOOTSTRAP-01](engenharia/M5-BOOTSTRAP-01.md), fundação das duas bases, testes e compilação GitHub Actions. Em andamento; aceite depende de execução real. Mapa/Pins, dados e demais features continuam dependentes de decisões e contratos próprios.

## 5. Relação com documentação anterior (sem duplicar roadmap)

- **DOCS-001**: visão, escopo, jornada e ADRs → base de M0.
- **DOCS-002**: DESIGN-004/005, UX/GROUP/FINANCE/QUALITY-002 → propostas distribuídas por M1–M4.
- **BRAND-001**: linguagem, stamps, paleta simplificada → M1-DESIGN-01.
- **DESIGN-001/002/003**: decisões de estrutura e direção visual → insumos de M1/M2.
- **QUALITY-001/002**: base de revisão → critérios de M4, aplicáveis desde M0.
- **PROMPT-OBRIGATORIO / QUALITY-GATES / SAFEGUARDS**: regras transversais e **CANARY (chat-only)** que começam em M0 e valem para todo marco, com execução de testes nativos apenas após autorização.
- **Standards de arquitetura, acessibilidade, segurança e jurídico**: checklists de risco e desenho; não equivalem a certificações ou parecer jurídico.
- **BUILD-001**: plano de releases futuro → M4, **não um workflow configurado**.

## 6. Atualização de status e changelog

- Mudança de marco/tarefa → atualizar **este ROADMAP** e **STATUS**, com evidências e links.
- Pausa/transferência → atualizar **HANDOFF**.
- Mudança perceptível ao público → avaliar **CHANGELOG**; antes do lançamento, falar de progresso do projeto; após release confirmada, informar novidades realmente disponíveis.
- Não criar um segundo changelog técnico concorrente para o público. Históricos de commits/PRs ficam no GitHub.

## 7. Encerramento da fundação documental

A fundação de documentos para agents, produto, design, dados, segurança e qualidade **já foi redigida e está em revisão**. O próximo trabalho útil não é criar ROADMAP-V3 nem duplicar standards: é aprovar/corrigir as propostas existentes e registrar decisões humanas abertas. [Diagnóstico de cobertura](STATUS.md#auditoria-de-cobertura-documental--27102026).

- [x] Fonte única de roadmap, protocolos obrigatórios, STATUS/HANDOFF e changelog preparados em branch.
- [x] Contratos iniciais de UX/UI, domínio, permissões, offline, segurança e jurídico preparados como propostas.
- [x] Integração do PR #1 confirmada em 28/10/2026, commit `24007b0`; propostas não são aprovadas por associação.
- [ ] Resolver design, identidade, grupos, sync, mapa e licenças, **sem transformar hipótese em decisão**.
- [ ] Aprovar a arquitetura e testes específicos antes de autorizar o primeiro código de aplicativo.

Novos documentos devem nascer **de uma lacuna demonstrada**, não de contagem de arquivos. Não declarar M1–M4 concluídos apenas pela cobertura inicial.

## 8. Gates para implementação

- [x] Pacote documental M0 integrado via PR #1; demais propostas preservadas como pendentes.
- [ ] Contratos mínimos M1/M2/M3 aprovados com pendências explícitas.
- [ ] Política de mapas, privacidade, sync e financiamento informativo definida.
- [ ] Matriz QA e segurança de build aprovada.
- [x] Autorização expressa para a fundação iOS/Android e CI de compilação (ADR-009); nenhuma distribuição autorizada.
- [ ] Nenhuma decisão aceita contradita.

[AGENTS](../AGENTS.md) · [STATUS](STATUS.md) · [HANDOFF](agents/HANDOFF.md) · [Índice](INDEX.md).

## 9. Primeira execução autorizada — 31/10/2026

| Tarefa | Entrega | Estado |
| --- | --- | --- |
| [M5-BOOTSTRAP-01](engenharia/M5-BOOTSTRAP-01.md) | Bases SwiftUI/Compose, home inicial, testes e CI | Builds/testes aprovados; revisão visual/acessibilidade pendentes no PR #2 |
| [M5-PREVIEW-01](engenharia/M5-PREVIEW-01.md) | Apple Maps iOS, Google Maps Android e capturas reais de simuladores | 8 PNGs originais inspecionados e CI aprovada; mapa Google real bloqueado por chave/projeto; contraste tratado em M5-UI-02 |
| M5-UI-02, no contrato M5-PREVIEW-01 | Cartões sem contorno e correção de labels no escuro | Implementado/verificado em CI e 8 PNGs; disponível no PR #2, sem merge |
| [M5-HEADER-01](engenharia/M5-HEADER-01.md) | +/contexto central/relógio, cabeçalho fora do scroll | Implementado no PR #2; testes de toque e oito capturas verificados, sem merge |
| [M5-TONES-01](engenharia/M5-TONES-01.md) | Fundo/cartão em tons semânticos distintos | CI e oito PNGs verificados no PR #2, sem merge |
| [M5-HANDLE-01](engenharia/M5-HANDLE-01.md) | Puxador entre cartões | Implementado/verificado: 3 testes UI por plataforma e oito capturas no PR #2, sem merge |
| [M5-PLACES-01](engenharia/M5-PLACES-01.md) | Adicionar/revisar ideias locais e excluir com confirmação | Implementado/verificado: 8 testes UI, persistência local e 16 capturas no PR #2, sem merge |
| [M5-PARITY-01](engenharia/M5-PARITY-01.md) | Guia comum, paridade de home/sheets | Em revisão; CI e 16 PNGs verificados; obrigatório nos próximos fluxos |
| [M5-FADE-01](engenharia/M5-FADE-01.md) | Transição inferior suave dos sheets | Em revisão; CI e 16 PNGs verificados no PR #2 |
| [M5-DETAILS-01](engenharia/M5-DETAILS-01.md) | Abrir e percorrer ideias salvas | Em revisão; CI e 24 PNGs verificados no PR #2 |

[Guia de desenvolvimento](engenharia/DEVELOPMENT.md). Os itens M0 historicamente em revisão descrevem a entrega original; integração está confirmada, sem transformar especificações propostas em decisões aceitas.
