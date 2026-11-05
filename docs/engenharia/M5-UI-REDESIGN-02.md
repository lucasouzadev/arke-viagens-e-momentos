# M5-UI-REDESIGN-02 — Editor de lugar

5 de novembro de 2026. **Status: aplicado na branch `ui/m5-ui-redesign-02` (empilhada sobre a 01); CI nativo aprovado nas duas plataformas na segunda execução (run [42008889411](https://github.com/lucasouzadev/arke-viagens-e-momentos/actions/runs/42008889411)). Falta revisão humana das capturas.** Contrato guarda-chuva: [M5-UI-REDESIGN-PLANO](M5-UI-REDESIGN-PLANO.md); especificação: [DESIGN-007](../design/DESIGN-007.md). Decisão D3 aprovada. Plataformas: iOS e Android. Sem PR, merge ou release.

## Objetivo

Adicionar, editar e revisar lugar sem os blocos enormes e quase vazios, com campo ativo visível acima do teclado e revisão compacta.

## Dentro do escopo

| Defeito observado no vídeo | Mudança |
| --- | --- |
| Um grupo único com campos de 96/120 pt para uma linha de texto | Um cartão por campo, altura mínima 68 (nome, endereço, link) e 104 (nota) |
| Campo vazio sem pista (`prompt: Text("")`) | Placeholder em todos os campos e marca “Obrigatório/Opcional” no rótulo |
| Texto do `TextEditor` ~5 pt à direita do `TextField` | Compensação do recuo; no Android um só tipo de campo e sem recuo extra |
| Campo ativo jogado para baixo do cabeçalho (`scrollTo(anchor: .top)`) | `anchor: .center` e contorno no campo ativo |
| Corte seco do texto sob o cabeçalho em meia altura (fades só no detent grande) | `ArkeScrollEdges` sempre ativo |
| Faixa retangular de cor diferente atrás do CTA | Fundo só em iOS < 26; no 26 o desvanecimento vem da própria borda de rolagem |
| “Trocar/Remover foto” como linhas de texto de ~26 pt, “Remover” sem cor de perigo | Linha-botão “Adicionar foto de capa”; com foto, ações separadas de 48 e “Remover” em perigo |
| Revisão com blocos de 56/96/120 | Resumo compacto em grupo único com divisores |
| Texto do “i” longo | Texto curto, mantendo “sem backup” |

Preservados: identificadores `place.*` (inclui `place.name.region`), `TextEditor`/`textViews` em endereço e nota no iOS, proteção de rascunho, validações, limites da capa, dados e esquema.

## Fora do escopo

Crescimento automático do `TextEditor` no iOS, fonte a 200%, landscape/tablet, mudança de cor de marca, qualquer fluxo fora do editor.

## Arquivos

iOS `ios/Arke/PlaceEditor.swift`. Android `android/app/src/main/kotlin/dev/arke/app/PlaceEditor.kt`. Documentos: DESIGN-007, PLANO, este contrato, STATUS, HANDOFF, ROADMAP, INDEX.

## Aceite (todos NÃO EXECUTADOS)

- [ ] Toque em qualquer ponto do cartão foca o campo e abre o teclado (teste `testEditorFullFieldTapAndScrollableReview` e equivalentes Android).
- [ ] Nota longa com teclado aberto: rola até a última linha e até “Revisar lugar” sem encobrir.
- [ ] Campo ativo nunca fica sob o cabeçalho; sem corte seco no topo em meia altura.
- [ ] Sem faixa de cor diferente atrás do CTA (iOS 26 e anterior).
- [ ] Revisão compacta; Editar informações acima de Salvar; rascunho alterado protegido (X, gesto, Voltar).
- [ ] Capturas padrão/compacto × claro/escuro nas duas plataformas, inspecionadas em pares.

## Gates

G-01 revisado no diff (dois cartões, um azul, nativo). G-02 este contrato. G-04 revisão estática apenas. G-05 a G-07, G-11 e G-12 NÃO EXECUTADOS. G-08, G-09, G-10 e G-13 não aplicáveis (sem rede, dados novos ou release). G-14 documentos atualizados. G-15 nome ausente dos arquivos.

## Riscos

- **P2 — compilação e foco às cegas:** escrito sem compilador. Mitigação: CI nativo antes de qualquer revisão.
- **P2 — `TextEditor` com padding negativo e placeholder em sobreposição:** alinhamento só verificável em captura.
- **P3 — `if #unavailable(iOS 26)` em `background`:** conferir no CI.
- Recuperação: descartar a branch; a 01 e a main ficam intactas.

## Evidência de CI

- **Run 42000055639 (primeira):** Android aprovado; iOS compilou e passou nos unitários, mas `testSaveReviewReopenAndConfirmedDelete` passou de 3 min (buscas de `place.name` levando ~30 s) e derrubou a etapa de captura. O mesmo teste passa na branch 01 (run 42003942009).
- **Run 42008889411 (repetição, mesmo código):** Android e iOS aprovados, incluindo todos os testes de UI; 45 PNGs. A falha anterior foi **intermitente**: a captura levou 32 min contra 27 min na 01, então o simulador pode estar lento, mas a causa não está provada. Se voltar a ocorrer, investigar o editor (elemento de acessibilidade por campo, `TextEditor` com padding negativo, bordas de rolagem sempre ativas).
- **Inspecionado (iOS, escuro e claro, padrão e compacto; Android, escuro e claro):** campos em cartões com placeholder e Obrigatório/Opcional, edição com valores preenchidos, botão “Revisar lugar” sem faixa atrás. Defeitos vistos: no iOS o texto “Adicionar foto de capa” quebrava em duas linhas porque o “Opcional” ao lado tomava a largura (corrigido depois do run: “Opcional” passou para baixo do título; **a correção não foi verificada por CI**); em largura compacta o último campo aparece parcialmente por trás do botão antes de rolar (comportamento de rolagem, sem fade forte).
- **Não coberto:** capturas da revisão compacta e do teclado aberto, VoiceOver/TalkBack, fonte 200%, landscape, tablets, aparelho físico.

## Próximo handoff

Rodar o CI na branch, inspecionar capturas do editor e abrir o recorte 03 (Marcar no mapa).
