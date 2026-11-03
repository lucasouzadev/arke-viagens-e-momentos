# DESIGN-003 — Identidade, plataformas e movimento

**Status:** princípios aprovados; métricas de tempo/geometry são hipóteses.

## Cor e aparência — direção revisada

**Aprovado:** apenas **um azul opaco** como identidade; o restante da UI usa superfícies/textos do próprio sistema e acompanha automaticamente o tema do dispositivo. **Hex candidato:** `#4C728A` (a validar). Sem ciano auxiliar, salmão, paletas por tema, gradiente de marca ou filtro azul sobre mapa real.

O estudo anterior de vários tons foi **substituído** por [THEME-001](../brand/THEME-001.md) e [ADR-006](../decisoes/ADR-006.md). Liquid Glass continua nativo nos controles iOS, sem tornar a marca translúcida.

## iOS — Liquid

Swift/SwiftUI + MapKit. **Liquid Glass dominante nos controles flutuantes**, relógio e chrome interativo, não sobre grandes áreas de texto. Transições com continuidade de origem, snapping e haptics discretos. Respeitar Reduce Motion.

## Android — Flat

Kotlin/Jetpack Compose; superfícies opacas, formas e contrastes claros; motion do Android e predictive back quando pertinente. **Não simular Liquid com blur decorativo.**

## Três posições do painel (proporções aproximadas)

| Estado | Mapa | Painel |
| --- | ---: | ---: |
| Explorar | 70% | 30% |
| Equilibrado (padrão) | 48% | 52% |
| Detalhes | 25% | 75% |

As alturas são adaptáveis à safe area, teclado, tablets, landscape e fonte ampliada. Usar fallback quando duas áreas mínimas não couberem.

## Motion

- Rápido, físico, interrompível, sem atrasar ações simples.
- Pin → lugar sem pulos duplos de câmera.
- Carrossel central com vizinhos parcialmente expostos.
- Relógio expande da própria ação.
- Puxador encaixa com feedback contido.
- Stamps podem ter movimento opcional, nunca obrigatório.
- Acessibilidade: remover/reduzir zoom e deslocamento em Reduce Motion/Remove Animations.

Cada ajuste visual exige auditoria [QUALITY-001](../qualidade/QUALIDADE.md).
**Paridade obrigatória — 01/11/2026:** [DESIGN-006](DESIGN-006.md) orienta as duas plataformas. Alça visível em sheets, Cancelar no topo esquerdo, mesma hierarquia de ações/campos e métricas-base de texto/espaçamento. APIs e materiais continuam nativos; não usar diferenças de plataforma para mudar a organização do fluxo. Revisar pares de capturas antes de avançar.
