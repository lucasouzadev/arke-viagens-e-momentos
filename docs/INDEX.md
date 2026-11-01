# Índice e maturidade da documentação

**Documento de navegação do Arke.** ROADMAP é [único e canônico](ROADMAP.md); andamento em [STATUS](STATUS.md), continuação em [HANDOFF](agents/HANDOFF.md).

## Estados

[M5-PREVIEW-01](engenharia/M5-PREVIEW-01.md) registra mapas por plataforma e capturas em simuladores; [ADR-010](decisoes/ADR-010.md) aprova Apple Maps/Google Maps. Google Cloud/chave Android ainda pendentes.

**Atualização — 31/10/2026:** as classificações históricas abaixo não aprovam propostas por associação. [ADR-009](decisoes/ADR-009.md) autoriza somente a fundação nativa e compilação; [contrato](engenharia/M5-BOOTSTRAP-01.md) e [guia de desenvolvimento](engenharia/DEVELOPMENT.md).

- **Aprovado:** direção explicitamente aceita.
- **Proposto:** ainda depende de decisão.
- **Em revisão:** documento enviado para PR, não necessariamente integrado.
- **Concluído:** entrega provada e integrada quando aplicável; documentação não implica app executável.

## Primeiro para agents

| Documento | Responsabilidade |
| --- | --- |
| [AGENTS.md](../AGENTS.md) | regras inegociáveis e leitura inicial |
| [ROADMAP](ROADMAP.md) | fonte única de milestones, tarefas, dependências |
| [STATUS](STATUS.md) | estado factual da execução |
| [HANDOFF](agents/HANDOFF.md) | continuidade entre agents |
| [WORKFLOW](agents/WORKFLOW.md) | processo de trabalho |
| [TASK-CONTRACT](agents/TASK-CONTRACT.md) | formato das tarefas |
| [REVIEW-PROTOCOL](agents/REVIEW-PROTOCOL.md) | gates e revisões |
| [PROMPT-OBRIGATORIO](agents/PROMPT-OBRIGATORIO.md) | instrução mestre para todos os agents |
| [CANARY](agents/CANARY.md) | nome confirmado exclusivamente nas respostas do chat; nunca em repositório |
| [QUALITY-GATES](agents/QUALITY-GATES.md) | gates G-01 a G-14 e classificação de risco |
| [SAFEGUARDS](agents/SAFEGUARDS.md) | parada, contenção, rollback e recuperação |
| [THREAT-MODEL](agents/THREAT-MODEL.md) | análise de ameaças por feature |
| [CHANGELOG](../CHANGELOG.md) | novidades do projeto em linguagem simples |
| [CHANGELOG-GUIDE](agents/CHANGELOG-GUIDE.md) | regras para changelog sem jargão |
| [NEWS-MODAL](releases/NEWS-MODAL.md) | comportamento futuro de novidades dentro do app |
| [ADR-007](decisoes/ADR-007.md) | decisão de governança e roadmap único |

## Produto e interação

| Documento | Situação |
| --- | --- |
| [Visão](produto/VISAO.md) | direção aprovada |
| [Escopo](produto/ESCOPO.md) | cinco contextos aprovados; detalhes propostos |
| [Jornadas](produto/JORNADAS.md) | fluxos propostos |
| [DESIGN-001](design/DESIGN-001.md) | dois cartões aprovados |
| [DESIGN-002](design/DESIGN-002.md) | relógio/carrossel aprovados conceitualmente |
| [DESIGN-003](design/DESIGN-003.md) | Liquid/Flat aprovados em direção |
| [DESIGN-004](design/DESIGN-004.md) | física e fallbacks propostos |
| [DESIGN-005](design/DESIGN-005.md) | sistema visual proposto |
| [M1-UX-CONTRACT](design/M1-UX-CONTRACT.md) | contrato conceitual de UI para agents, em revisão |
| [INTERACTIONS](design/INTERACTIONS.md) | máquina de estados conceitual |
| [UX-001](produto/UX-001-ESTADOS.md) | estados/rascunhos propostos |
| [GROUP-001](produto/GROUP-001.md) | papéis e fluxos propostos |
| [FINANCE-001](produto/FINANCE-001.md) | finanças informativas propostas |

## Marca e estilos

| Documento | Situação |
| --- | --- |
| [Índice da marca](brand/README.md) | orientação |
| [BRAND-001](brand/BRAND-001.md) | linguagem gráfica proposta |
| [THEME-001](brand/THEME-001.md) | uma cor azul e tema do device aprovados; HEX proposto |
| [STAMPS-001](brand/STAMPS-001.md) | estilo de memórias proposto |
| [PLATFORM-001](brand/PLATFORM-001.md) | equivalência visual nativa |
| [BRAND-QUALITY-001](brand/BRAND-QUALITY-001.md) | critérios propostos |

## Arquitetura, segurança e testes

| Documento | Situação |
| --- | --- |
| [Estratégia nativa](arquitetura/ESTRATEGIA-NATIVA.md) | direção aprovada, sem código |
| [ARCHITECTURE-CODE](standards/ARCHITECTURE-CODE.md) | arquitetura e qualidade de código |
| [ACCESSIBILITY-UI](standards/ACCESSIBILITY-UI.md) | UX acessível e responsiva |
| [SECURITY-PRIVACY](standards/SECURITY-PRIVACY.md) | OWASP MASVS e dados pessoais |
| [LEGAL-COMPLIANCE](standards/LEGAL-COMPLIANCE.md) | LGPD, LBI, lojas, mídia e proteção de menores |
| [Domínio](arquitetura/DOMINIO.md) | entidades propostas |
| [M3-DATA-CONTRACT](arquitetura/M3-DATA-CONTRACT.md) | entidades/vínculos e integridade, proposta |
| [PERMISSIONS](arquitetura/PERMISSIONS.md) | matriz de autorização por registro, proposta |
| [SYNC-OFFLINE](arquitetura/SYNC-OFFLINE.md) | erros, retries, conflitos e offline, proposta |
| [Privacidade](arquitetura/PRIVACIDADE.md) | princípios propostos |
| [QUALITY-001](qualidade/QUALIDADE.md) | gates obrigatórios futuros |
| [QUALITY-002](qualidade/MATRIZ-UX-002.md) | cenários de teste, não executados |
| [Distribuição](engenharia/DISTRIBUICAO.md) | futura, sem workflows |
| [Questões abertas](OPEN-QUESTIONS.md) | decisões pendentes |
| [ADRs](decisoes/README.md) | direções aceitas |

## Hierarquia da fonte de verdade

**ADRs aceitos e decisões explícitas** → especificações aprovadas → propostas → tarefas/roadmap → STATUS factual → HANDOFF contextual. README e CHANGELOG comunicam o projeto e **não devem impor comportamento técnico novo**.

A documentação original não implementou aplicativo. A branch inicial de desenvolvimento acrescenta bases nativas e workflow de verificação, ainda sujeitos às evidências de [STATUS](STATUS.md). Sem release ou modal implementado.
