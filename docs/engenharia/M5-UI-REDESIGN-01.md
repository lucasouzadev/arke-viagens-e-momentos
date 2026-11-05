# M5-UI-REDESIGN-01 — Redesenho de UI/UX, recorte 1

5 de novembro de 2026. **Status: em andamento, recorte 1 aplicado na branch `ui/m5-ui-redesign-01` (commit 8d3ef6f, enviada à origem) e ainda NÃO compilado nem testado.** Milestone M5. Plataformas: iOS e Android. Sem PR, sem merge, sem publicação, sem nova beta.

## Objetivo e contexto

Corrigir, em etapas pequenas, os defeitos de UI/UX encontrados na análise de um vídeo de uso da beta no iPhone (iOS 26, tema escuro, 190 s) cruzada com o código das duas plataformas. A análise e um protótipo tela por tela (privado, feito no chat) orientam o conjunto; este contrato cobre **somente o recorte 1**. Os recortes seguintes precisam de contrato próprio.

A instrução “Siga”, dada no chat depois do protótipo, foi lida como autorização para aplicar o primeiro recorte. Se não era essa a intenção, reverter o recorte descartando a branch; nenhum dado, esquema ou arquivo de persistência é tocado.

## Dentro do escopo (recorte 1)

| # | Defeito observado | Mudança |
| --- | --- | --- |
| 1 | Texto de ação em azul sobre superfície escura com ≈3,3:1 (botões de texto, links, alertas, “Cancelar”, marcador de localização) | Nova tinta de texto `ArkeUI.ink` (iOS) / `ArkeColors.inkDark` (Android): **mesma matiz do azul candidato, mais clara no escuro** (#88AABE). No claro continua #4C728A. O azul candidato segue como preenchimento de botões; o ADR-006 não é alterado |
| 2 | Rótulos vizinhos do seletor de contexto cortados (“Planeja…”, “Avaliaç…”) e não tocáveis | Vizinhos viram botões de chevron 44×48 com nome completo na acessibilidade; título centralizado com até 2 linhas; cinco pontos indicam a posição. O gesto de deslizar e as ações de acessibilidade continuam |
| 3 | Mapa expandido: alça desenhada sobre o texto do cartão, sem botão de recolher e gesto de recolher na direção oposta à da alça normal | O botão de expandir vira alternância (ícone e nome mudam para “Recolher mapa”). A borda do cartão fica opaca, sem texto sob a alça. Recolher por toque ou por arrasto **para cima** |
| 4 | Textos de informação desatualizados (“O endereço não será marcado/ainda não aparece no mapa”) | Texto aponta para “Marcar no mapa”. A frase “sem backup” permanece (há teste que a verifica) |
| 5 | Botões preenchidos no Android passariam a herdar a tinta clara do tema escuro | Botões preenchidos (Revisar, Salvar, Próximo) fixam `ArkeColors.brand` e texto branco |

## Fora do escopo (recortes seguintes, ainda sem contrato)

- Recorte 2: editor de lugar (campos com altura mínima menor que a do DESIGN-006, placeholder, fade superior em meia altura, faixa atrás do CTA, campo ativo sob o cabeçalho com teclado, linhas de foto de capa).
- Recorte 3: Marcar no mapa (ponta do pino deslocada em relação ao ponto salvo, confirmação abaixo da dobra e desabilitada sem explicação, busca sem indicador de carregamento).
- Recorte 4: Gastos, Cofrinho, Avaliações e Memórias com campos, rótulos e botões reconhecíveis, formatação de moeda.
- Recorte 5: cabeçalho comum dos sheets (X, título central, ação no rodapé), Viagens, Compartilhar com conta no lugar do login de teste, mapa inicial, ícones por contexto, seletor por ponteiro fixo.
- Fonte a 200%, VoiceOver/TalkBack, landscape, tablets.

## Decisões

- **Aprovadas por ADR:** única cor de marca azul opaca; nativo SwiftUI/Compose; dois cartões; contextos sem tab bar.
- **Proposta neste recorte, pendente de aceite do responsável:** a tinta de texto no escuro (#88AABE) como variação de luminosidade do azul candidato. Não cria segundo acento. O HEX final do azul continua em aberto no ADR-006.
- **Proposta para o recorte 2:** rever as alturas mínimas 56/96/120 de campo no DESIGN-006, que produzem blocos quase vazios. Nada foi alterado no DESIGN-006 neste recorte.

## Contraste (calculado, WCAG 2.x)

| Par | Razão |
| --- | --- |
| Azul #4C728A sobre cartão escuro #1C1C1E (hoje) | 3,31:1 |
| Tinta #88AABE sobre cartão escuro #1C1C1E | 6,92:1 |
| Tinta #88AABE sobre campo escuro #2C2C2E | 5,67:1 |
| Azul #4C728A sobre branco / sobre #F2F2F7 | 5,15:1 / 4,61:1 |
| Branco sobre azul #4C728A (botão preenchido) | 5,15:1 |

## Arquivos autorizados

iOS: `ios/Arke/ArkeUI.swift`, `ArkeApp.swift`, `HomeView.swift`, `PlaceEditor.swift` (texto), `PlaceIdeasContent.swift` (texto). Android: `ArkeUI.kt`, `MainActivity.kt`, `ArkeHome.kt`, `PlaceEditor.kt`, `PlaceIdeasContent.kt`, `res/values/strings.xml`. Documentação: este arquivo, STATUS, HANDOFF, ROADMAP, INDEX. Dados, esquema JSON v1, rede e dependências não mudam.

## Critérios de aceite

- [ ] iOS e Android compilam e os testes existentes passam no CI nativo.
- [ ] No tema escuro, links e botões de texto usam a tinta clara; no claro, o azul atual. Nenhum botão preenchido muda de cor.
- [ ] O título do contexto aparece inteiro (“Planejamento”, “Avaliações”, “Memórias”) em padrão e compacto, claro e escuro; vizinhos tocáveis trocam de contexto; deslizar no título continua trocando.
- [ ] Nos extremos (primeiro e último contexto) o lado sem vizinho fica vazio e o título permanece centralizado.
- [ ] Mapa expandido: o botão de recolher fica visível, nenhum texto aparece sob a alça, tocar na alça ou arrastar para cima recolhe, e expandir de novo funciona.
- [ ] Informação “i” do editor e do detalhe com o texto novo; testes que verificam “sem backup” continuam passando.
- [ ] Nenhuma ideia, Pin ou viagem é alterada; JSON v1 preservado.

## Auditoria e gates

| Gate | Estado |
| --- | --- |
| G-01 Direção/ADR | Revisado no diff: dois cartões, sem tab bar, um azul, stack nativa. A tinta clara no escuro é proposta pendente |
| G-02 Escopo | Este contrato; aceite acima |
| G-03 Arquitetura | Sem mudança de camadas; estado do mapa e do contexto continua local |
| G-04 Código limpo | Revisão estática do diff apenas; lint NÃO EXECUTADO |
| G-05 UX/UI | NÃO EXECUTADO (sem simulador nesta sessão) |
| G-06 Responsividade | NÃO EXECUTADO; risco: título com até 2 linhas e chevrons de 44 pt em largura compacta |
| G-07 Acessibilidade | Rótulos “Próximo/Contexto anterior: X”, pontos ocultos, ações ajustáveis mantidas; VoiceOver/TalkBack NÃO EXECUTADOS |
| G-08 Segurança | Não aplicável: sem rede, segredo ou dependência nova. Observação fora do recorte: URL e chave publishable do Supabase estão fixas nos dois apps |
| G-09 Privacidade/jurídico | Não aplicável neste recorte (sem dados novos) |
| G-10 Dados/offline | Não aplicável: nenhuma persistência alterada |
| G-11 Performance | NÃO EXECUTADO; mudança sem laços novos |
| G-12 Testes | **NÃO EXECUTADO.** Compilação e testes dependem do CI. Os testes de UI usam `home.contextTitle` (mantido) e não referenciam o botão de expandir |
| G-13 Operação/release | Não aplicável: sem assinatura, envio ou release |
| G-14 Continuidade | STATUS e HANDOFF atualizados neste recorte |
| G-15 Canário | Nome do responsável confirmado no chat; ausente de todos os arquivos |

## Riscos e recuperação

- **P2 — compilação às cegas:** o código foi escrito e relido sem Xcode nem Android SDK (ambiente Linux). Mitigação: CI nativo antes de qualquer revisão; falha de build bloqueia o recorte.
- **P2 — seletor de contexto:** o gesto agora vive no bloco central, não na faixa inteira. Verificar se o teste de deslizar ainda encontra o título.
- **P3 — tinta no escuro:** pode parecer menos “marca”; avaliar em captura real.
- **Recuperação:** descartar a branch `ui/m5-ui-redesign-01`; main intacta.

## Impacto público

Sim, perceptível: texto de ação mais legível no escuro, título de contexto completo e mapa expandido mais claro. Texto leigo candidato para o CHANGELOG, somente quando houver release: “Ficou mais fácil ler os botões no modo escuro e trocar de seção sem nomes cortados.”

## Próximo handoff

1. Revisão humana do diff e decisão sobre a tinta no escuro.
2. Com autorização, disparar o CI nativo na branch e inspecionar os PNGs em padrão/compacto, claro/escuro.
3. Abrir contrato do recorte 2 (editor de lugar) depois de aprovar o DESIGN-006 revisado.

[AGENTS](../../AGENTS.md) · [DESIGN-006](../design/DESIGN-006.md) · [QA-BETA-01](../qualidade/QA-BETA-01.md) · [QUALITY-GATES](../agents/QUALITY-GATES.md)
