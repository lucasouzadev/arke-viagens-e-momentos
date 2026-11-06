# M5-QA-03 — Lista priorizada de correções (QA da build 63.1)

6 de novembro de 2026. Fonte: gravação de QA completa da build beta 63.1 (`main`, `ec33dae`) + leitura do código. **Documento de planejamento: nada aqui foi corrigido ainda e nenhuma correção foi testada.** Execução só após aprovação da ordem abaixo. Sem merge, sem TestFlight sem disparo explícito.

## Como ler
- **Evidência:** vídeo (tempo) · código (arquivo) · relato do QA.
- **Causa:** *confirmada* (vista no código), *provável* (coerente com vídeo+código, não provada) ou *desconhecida*.
- **Origem:** regressão dos recortes 04–06, ou lacuna que já existia / do contrato.

## P0 — Impede criar conteúdo (bloqueia nova build)

| # | Defeito | Evidência | Causa | Correção proposta |
| --- | --- | --- | --- | --- |
| P0-1 | O "+" grande do cabeçalho existe em **todos** os contextos, mas sempre abre "Adicionar lugar" (`showingAddPlace = true`, `HomeView.swift` `cardHeader`; Android `onAdd`). Nos contextos de Gastos/Cofrinho/Avaliações/Memórias há ainda um botão de ação próprio, então a tela tem dois "+" com significados diferentes. | relato; código | **Confirmada** (semântica ambígua, introduzida no recorte 06). | Ação primária **contextual**: o "+" do cabeçalho abre o formulário do contexto atual (lugar, gasto, meta/valor, avaliação, memória) e o botão duplicado dentro do conteúdo sai (ou vira só estado vazio). Um único ponto de entrada por contexto, mesmo rótulo de acessibilidade do que ele faz. |
| P0-2 | O botão de ação do contexto (ex.: "Adicionar gasto", "Nova memória") às vezes abre "Criar lugar". | relato; vídeo 1:47, 2:30 | **Não reproduzida na leitura do código**: cada botão liga a um `showingForm` próprio. Hipóteses: toque caindo no "+" vizinho (44 pt colado ao botão de ação) ou conflito entre sheets aninhados. | Primeiro **reproduzir**: teste de UI que toca cada ação e verifica o **título do sheet** (hoje os testes só verificam que *algum* sheet abriu — por isso passaram). Depois, P0-3 elimina a classe de conflito. |
| P0-3 | Sheets espalhados: ~8 `.sheet` no `HomeView` raiz mais `.sheet` dentro de `LocalFinanceView`/`TripJournalView`/editor. Apresentar um enquanto outro está em transição é ignorado pelo SwiftUI (explica "aperto várias vezes e nada acontece"). | vídeo 1:47–1:52, 2:30–2:35, 5:22–5:26; código | **Provável.** | **Roteador único** de apresentação: um `enum ActiveSheet: Identifiable` e um só `.sheet(item:)` no nível da Home; ação ignorada só se a mesma já está aberta. Android: um único estado de sheet equivalente. |
| P0-4 | Travamento de segundos ao abrir sheets e tocar ações (telas estáticas de 4–5,5 s exatamente nos toques). | vídeo (freezedetect) | **Provável, não provada:** `JSONDecoder` de `arke.trip-journal.v1`/`local-finance.v1` (que carregam capas em base64) e `UIImage(data:)` dentro do `body`, reexecutados a cada re-render; `@AppStorage` com blobs grandes. | Medir antes (signpost + XCTest de performance abrindo o sheet com 5 lugares com capa). Depois: miniaturas pequenas persistidas, imagens em arquivo (não em AppStorage), decodificação fora do `body`/da main thread, cache. Só declarar resolvido com medição. |
| P0-5 | Mapa expandido: cartão inferior sem conteúdo, com cantos quadrados, e alça ainda visível. | vídeo; código (`Color(secondarySystemGroupedBackground)` sobreposto após o `clipShape`) | **Confirmada.** | Mapa expandido = cartão recolhido de verdade: sem alça, sem sobreposição cega; resumo/ação mínima conforme protótipo, cantos preservados. |
| P0-6 | Dados órfãos/inconsistentes: excluir lugar deixa pins/registros; contagem de Viagens ≠ lugares. | revisão anterior; código | **Confirmada.** | Exclusão em cascata (ou bloqueada com aviso) e contagem derivada da mesma fonte. |

## P1 — Funcionalidade prometida que não funciona / defeito visível constante

| # | Defeito | Causa | Correção |
| --- | --- | --- | --- |
| P1-1 | Foto de capa não se mantém depois de criar e não aparece em Detalhes, Viagens nem Memórias. | **Confirmada:** `PlaceDetails` não renderiza capa; só o cartão da Home a usa; registros guardam a capa do lugar só por consulta frágil (`HomeView.swift:936`). | Capa como dado do lugar, exibida em cartão, Detalhes, Viagens e Memórias; teste de persistência pós-criação e pós-relançamento. |
| P1-2 | Não é possível subir imagem em Memórias. | **Confirmada:** **não existe seletor de foto** no formulário de memória; só herda a capa do lugar. Lacuna de contrato, não bug de regressão. | Seletor de fotos (PhotosPicker / Photo Picker Android) por memória, com redução/recorte e permissão mínima; **exige decisão de produto** (várias fotos por memória? limite?). |
| P1-3 | Campos de texto com a mesma cor do fundo do sheet quando expandido. | **Confirmada:** preenchimento do campo ≈ `sheetSurface`. | Token de campo com contraste próprio (claro/escuro), medido (WCAG não-texto ≥ 3:1 no contorno ou fundo). |
| P1-4 | Imagem adicionada sem moldura "stamp" e sem preencher o espaço reservado. | **Confirmada:** capa usa `scaledToFill` + `clipShape(10)` sem moldura dentadeada; o protótipo define o selo. | Componente único "stamp" (moldura + proporção fixa + preenchimento) usado no editor, cartão, Detalhes e Memórias, iOS e Android. |
| P1-5 | Faixa de cor no fim dos sheets, cortando conteúdo (tipo rodapé). | **Provável:** fundo do viewport do teclado/safe area inferior diferente do fundo do sheet. | Um fundo contínuo até a borda inferior; rolagem com inset em vez de rodapé colorido; verificar com e sem teclado, iPhone com/sem home indicator. |
| P1-6 | Cartão inferior muito elevado (respeita safe area, mas sobe demais). | **Provável:** inset inferior somado duas vezes (safe area + padding fixo). | Medir em 3 aparelhos; usar só o inset do sistema + margem de 8 pt do protótipo. |

## P2 — Acabamento e motion

| # | Defeito | Observação |
| --- | --- | --- |
| P2-1 | Mapa espremido (limite do painel): botões desalinham dentro do mapa. Vídeo 4:16–4:20: busca, localização e expandir colidem e o texto do mapa fica cortado. | Layout compacto dos controles: ancoragem por canto e ocultar a busca abaixo de altura mínima. |
| P2-2 | Transição entre cartões/contextos sem motion; tudo "duro". | Spring/interpolação ligada ao gesto, matched transitions, `contentTransition`, equivalente Compose; respeitar Reduzir Movimento. Definir curvas em DESIGN antes de codar. |
| P2-3 | Sheet de Detalhes aparece translúcido/"fantasma" sobre o cartão durante a transição (vídeo 4:23–4:25) e conteúdo cortado no topo. | Tratar junto com P0-3 (roteador) e P1-5. |
| P2-4 | Memórias: topo das células do grid cortado sob o cabeçalho ao rolar (5:27–5:30). | Inset superior da rolagem. |
| P2-5 | Rótulos repetidos ("Meta/Meta"), barras de navegação nativas em Viagens/Compartilhar fora do padrão contínuo. | Auditoria de cópia e de cabeçalhos de sheet (DESIGN-006/007). |

## O que eu deixei passar (e por quê)
1. **Testes verificavam presença, não identidade:** "tocou e *algum* sheet abriu" passa mesmo quando abre o sheet errado. Nova regra: todo teste de ação confere o título do sheet.
2. **Sem teste de desempenho/travamento** nem dados realistas (várias capas); CI usa estado vazio.
3. **Capturas estáticas** não mostram transição, congelamento nem faixa inferior com teclado.
4. **Recorte 06 introduziu o "+" global** seguindo o protótipo, mas o protótipo não definia a ação por contexto: ambiguidade que eu devia ter levantado.

## Ordem de execução sugerida
1. **Slice A (P0-1..P0-4):** testes que reproduzem → roteador único → "+" contextual → medição/correção do travamento.
2. **Slice B (P0-5, P0-6, P1-5, P1-6):** mapa expandido, dados órfãos, faixa inferior, elevação.
3. **Slice C (P1-1..P1-4):** capa/stamp/campos; Memórias com foto **após decisão de produto**.
4. **Slice D (P2):** motion e acabamento, com curvas aprovadas.
Cada slice: iOS e Android em paridade, G-01..G-15, CI verde **verificado por run**, evidência em vídeo/captura, sem nova build até você pedir.

## Decisões necessárias de Juice
- D-A: "+" do cabeçalho passa a ser contextual e o botão duplicado sai do conteúdo? (recomendado)
- D-B: Memórias com foto: uma ou várias por memória; limite?
- D-C: Mapa expandido: cartão some totalmente ou fica uma faixa mínima (resumo + ação)?
