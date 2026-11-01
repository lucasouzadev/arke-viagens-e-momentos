# Arke — Onde vamos?

**Um mapa, uma viagem, tudo no mesmo lugar.**

O Arke é um projeto aberto de planejamento de viagens solo e em grupo. Pins, roteiros, gastos, metas de economia, avaliações e memórias compõem uma única experiência centrada no mapa.

> **Estado atual nesta branch:** fundação iOS/Android em desenvolvimento, com SwiftUI, Compose e verificação no GitHub Actions. Apple Maps inicial integrado; Google Maps preparado, aguardando configuração privada. Oito capturas reais foram geradas em simuladores. Recursos de viagem e revisão de acessibilidade continuam pendentes. Nenhuma versão está disponível para instalar. [Fundação](docs/decisoes/ADR-009.md) · [Mapas e capturas](docs/decisoes/ADR-010.md).

## Princípios aprovados

- **Uma tela principal e dois cartões**: mapa/Pins acima, navegação e conteúdo abaixo.
- **Sem tab bar tradicional:** relógio de cinco contextos dentro do segundo cartão.
- **Carrossel horizontal** finito de lugares; **scroll vertical** para atividades, detalhes e stamps.
- **Três posições do painel:** Explorar, Equilibrado e Detalhes.
- **Pins independentes de viagens**; uma Trip pode ser solo ou compartilhada.
- **iOS nativo em SwiftUI, Android nativo em Compose**, sem framework cross-platform para UI.
- **Uma única cor de marca:** azul opaco; fundos, superfícies, textos e aparência Light/Dark seguem o dispositivo. HEX candidato em [THEME-001](docs/brand/THEME-001.md).
- **Documentação antes do código**, com início delimitado das bases nativas autorizado em [ADR-009](docs/decisoes/ADR-009.md).

## Novidades do projeto

Começamos a preparar as bases iOS e Android e a tela inicial. Os recursos de viagem continuam em desenvolvimento. **Ainda não existe uma versão para instalar.**

Para desenvolver ou compilar, veja o [guia de desenvolvimento](docs/engenharia/DEVELOPMENT.md) e o [contrato da primeira tarefa](docs/engenharia/M5-BOOTSTRAP-01.md).

Veja o [**CHANGELOG — Novidades do Arke**](CHANGELOG.md), escrito sem linguagem técnica. Quando houver aplicativo disponível, as novidades de cada versão também poderão aparecer em um modal dentro dele (ainda não implementado).

## Como o projeto é organizado

| O que você procura? | Documento |
| --- | --- |
| Plano principal | [ROADMAP](docs/ROADMAP.md) |
| Situação atual | [STATUS](docs/STATUS.md) |
| Instruções dos agents | [AGENTS.md](AGENTS.md) |
| Prompt universal dos agents | [PROMPT-OBRIGATORIO](docs/agents/PROMPT-OBRIGATORIO.md) |
| Canário privado de chat | [CANARY](docs/agents/CANARY.md) — nenhuma identificação pessoal registrada no repo |
| Contratos de dados propostos | [M3 Data](docs/arquitetura/M3-DATA-CONTRACT.md) · [Permissões](docs/arquitetura/PERMISSIONS.md) · [Sync](docs/arquitetura/SYNC-OFFLINE.md) |
| Critérios de segurança e qualidade | [QUALITY-GATES](docs/agents/QUALITY-GATES.md) · [SAFEGUARDS](docs/agents/SAFEGUARDS.md) |
| Padrões arquiteturais e legais | [Arquitetura](docs/standards/ARCHITECTURE-CODE.md) · [Acessibilidade](docs/standards/ACCESSIBILITY-UI.md) · [Segurança](docs/standards/SECURITY-PRIVACY.md) · [Jurídico](docs/standards/LEGAL-COMPLIANCE.md) |
| Continuidade entre agents | [HANDOFF](docs/agents/HANDOFF.md) |
| Guia de alterações públicas | [CHANGELOG-GUIDE](docs/agents/CHANGELOG-GUIDE.md) |
| Futuro modal de novidades | [NEWS-MODAL](docs/releases/NEWS-MODAL.md) |
| Índice de todas as especificações | [Documentação](docs/INDEX.md) |
| Contratos de design | [DESIGN-001](docs/design/DESIGN-001.md) e [DESIGN-004](docs/design/DESIGN-004.md) |
| Marca e cor única | [BRAND-001](docs/brand/BRAND-001.md) e [THEME-001](docs/brand/THEME-001.md) |
| Segurança/qualidade | [QUALITY-001](docs/qualidade/QUALIDADE.md) |
| Decisões aceitas | [ADRs](docs/decisoes/README.md) |

Leia [CONTRIBUTING.md](CONTRIBUTING.md) e [OPEN_SOURCE.md](OPEN_SOURCE.md). Código e marca ainda não possuem licença final aprovada. Imagens e fontes externas não devem ser distribuídas sem autorização.

**Propostas novas não são decisões aceitas automaticamente.**
