# REVISAO-UIUX-01 — Revisão de UI/UX da beta 63.1

6 de novembro de 2026 · Autor: agente `mobile-ui-ux-designer` · **Status: proposta para decisão. Nada aqui está aprovado, implementado ou testado.** Segundo a [M5-QA-03-LISTA](../engenharia/M5-QA-03-LISTA.md), cada item só vira trabalho depois que Juice decidir.

**Material examinado:** capturas `editar-registro.png` e `home-vazia.png`; folhas de contato `s01`–`s08` (gravação completa do QA, 0:00–6:12); janelas de 1 fps `a`–`e` (Gastos, Avaliações, Memórias, Marcar no mapa/Detalhes); referência de motion `ref-expandir.png`/`ref-recolher.png`; protótipo HTML (`*.dc.html`, `arke.css`); código SwiftUI (`HomeView`, `PlaceIdeasContent`, `PlaceEditor`, `ArkeUI`) e Compose (`ArkeHome.kt`, `ArkeUI.kt`). Também consultei o DESIGN-006, o DESIGN-007, o ADR-006 e o ACCESSIBILITY-UI.

**Limites desta revisão (leia antes):**
- **Todas as capturas são do iOS, no tema escuro.** Não vi Android, tema claro nem fonte ampliada. O que digo sobre Android vem **só da leitura do código** e está marcado como *(código)*. Sem evidência de tela, paridade, tema claro e Dynamic Type continuam **NÃO VERIFICADOS**.
- *(inferência)* marca conclusões tiradas de quadros a 0,25–1 fps ou de leitura de código, sem medição.
- Os valores de contraste são **estimativas** calculadas a partir de cores do sistema e de pixels comprimidos. Precisam ser medidos no aparelho.
- Os números em pt valem igualmente em dp, salvo indicação.

---

## 1. Resumo executivo

1. **O cartão inferior desperdiça o espaço vertical, que é o recurso mais escasso de um app map-first.** Cabeçalho, chip de viagem, legenda solta ("Teste"), contagem, pager "1 de 1", "Lugar 1 de 1" e o link "Ver meus lugares" ocupam cerca de 150 pt antes do primeiro conteúdo útil (`a.png`, 0:00–0:05). A nova **barra de navegação do cartão** (§3) junta tudo em **88 pt** e libera de 60 a 90 pt para o conteúdo.
2. **Os formulários curtos abrem no detent `.large` por causa do código, não do conteúdo.** No iOS, `presentationDetents([.large])` aparece em gasto, cofrinho, avaliação e memória. No Android, `fillMaxHeight(0.9f)` *(código)*. Por isso aparecem cerca de 50% de área morta e um "Cancelar" solto no meio (`editar-registro.png`; `s04` 2:36; `b.png` 0:09). O projeto já tem a solução: o detalhe da ideia mede a altura com `onGeometryChange` e usa `.height(...)`. Basta generalizar esse padrão (§4).
3. **O segmentado parece não ter nada selecionado, mas tem.** "Prevista" já vem selecionada (`s03` 2:00; código `kind`). O problema é o contraste do `Picker(.segmented)` padrão: segmento cinza sobre trilho cinza, com o mesmo texto branco nos dois estados. É um defeito de **percepção de estado**, não de lógica (§5.1).
4. **Os campos não se identificam.** A descrição do gasto só tem placeholder: depois de preenchida, "Teste" flutua sem rótulo e sem contorno (`editar-registro.png`, y≈755). O texto de memória/avaliação tem placeholder quase invisível (`c.png` 0:07). O formulário de gasto também perdeu os campos **Data** e **Tipo** (rótulo) que o protótipo tinha (`AddExpense.dc.html`).
5. **Há três famílias de botão secundário sem regra.** Pílula cinza ("Detalhes"), texto com ícone ("Marcar no mapa") e link azul alinhado à esquerda ("Ver meus lugares"/"Ver detalhes"). Também há três chips/pílulas de ação na mesma linha, com Excluir em vermelho no mesmo peso que Editar (`s05` 3:12). Proponho uma hierarquia única de 4 níveis (§5.3).
6. **O motion de referência não serve literalmente para o Arke.** Na referência o cartão que cresce fica **embaixo**. No Arke o mapa fica **em cima**. Proponho a tradução com ancoragem no topo, mola única interrompível, pílula com morph e cantos concêntricos constantes (§6). Também traz uma vantagem própria do Android: o *predictive back* recolhe o mapa seguindo o gesto.
7. **Três problemas não estavam listados e afetam a confiança:** coordenadas cruas na interface ("Ponto marcado: -23.0099, -43.3541", `s02` 1:32), o jargão "Pin" exposto ("Nenhum Pin nesta viagem", `s05` 3:40) e o chip "Viagens/Trilha foda", que **com a mesma aparência faz duas coisas diferentes**: navega em Planejamento e filtra nos outros contextos (`s04` 3:04 × 3:08).

---

## 2. Achados priorizados

Legenda de esforço: **P** = até 1 dia por plataforma · **M** = 2–4 dias · **G** = mais de 1 semana ou exige decisão/arquitetura. Tipo: **D** = corrige defeito · **E** = evolução de design.
A referência "QA" aponta o item correspondente da M5-QA-03-LISTA que este achado aprofunda.

### P0 — estrutura (bloqueia a próxima rodada de UI)

| ID | Tela / captura · região | Problema | Princípio | Proposta iOS | Proposta Android | Esf. | Tipo |
|---|---|---|---|---|---|---|---|
| UX-01 | Home, todos os contextos · `a.png` 0:00 (topo do cartão até o pager); `home-vazia.png` y 1110–1320 | O cabeçalho ocupa cerca de 150 pt em 4 camadas (título + pontos, legenda "Teste", chip + contagem, pager). O "+" sempre abre "Adicionar lugar". Os vizinhos aparecem como ícones sem rótulo (âncora, recibo, alvo). | HIG: hierarquia e barra de navegação; Lei de Fitts; previsibilidade (QA P0-1, Q2-3) | Barra de 2 linhas, 88 pt, definida no §3. O "+" fica contextual e a ação duplicada sai do conteúdo. Os vizinhos viram chevrons, e o nome do vizinho fica na acessibilidade. | Mesma estrutura com `IconButton` 48 dp, `FilledIconButton` para o "+" e chip `AssistChip` para a viagem. | M | D |
| UX-02 | Formulários de gasto/cofrinho/avaliação/memória · `editar-registro.png` (y 1150–1750 vazio); `s04` 2:36; `b.png` 0:09 | Detent `.large` fixo: cerca de 50% vazio e "Cancelar" no meio da tela. O Android usa `fillMaxHeight(0.9f)` *(código)*. | HIG Sheets: tamanho proporcional à tarefa; M3 bottom sheet: altura pelo conteúdo (QA Q2-1) | Componente `ArkeFormSheet`: mede com `onGeometryChange` e usa `.presentationDetents([.height(h)])`, com h = conteúdo + 20 pt na base. Passa para `.large` só quando h > 90% ou com teclado (§4). | `ModalBottomSheet(skipPartiallyExpanded = true)` com conteúdo `wrapContentHeight()` e `heightIn(max = 90% da janela)`. Remover `fillMaxHeight(0.9f)`. | M | D |
| UX-03 | Chip de viagem · `s04` 3:04 (Gastos: "Trilha foda") × 3:08 (Planejamento: "Viagens") | O mesmo controle tem dois rótulos e dois comportamentos: abre a lista de Viagens (navegação) em um contexto e filtra o contexto em outro. | Consistência (Nielsen 4); um componente, um significado | Nos 5 contextos, o chip mostra **sempre a viagem ativa** ("Trilha foda ▾" ou "Todas as ideias ▾") e abre o mesmo `Menu`: viagens, divisor, "Gerenciar viagens…" (abre o sheet Viagens). | Mesmo chip, com `DropdownMenu` e item "Gerenciar viagens…". | M | D |
| UX-04 | Mapa expandido · `s01` 0:16–0:20 (faixa vazia com alça) | O cartão inferior fica vazio e de cantos quadrados. A alça aparece sem conteúdo. Há uma sobreposição cega (`overlay Color(...)` após o `clipShape`) *(código)*. | QA P0-5; DESIGN-007 (barra de resumo) | Motion e layout do §6.1: o cartão vira **pílula de resumo** de 52 pt dentro do mapa, a alça some e os cantos são concêntricos. | Igual, com `SharedTransitionLayout`. | M | D |

### P1 — defeito visível constante ou funcionalidade que confunde

| ID | Tela / captura · região | Problema | Princípio | Proposta iOS | Proposta Android | Esf. | Tipo |
|---|---|---|---|---|---|---|---|
| UX-05 | Segmentado Prevista/Realizada e Meta/Valor reservado · `editar-registro.png` y 850–925; `s03` 2:12 | O selecionado (#636366 aprox.) e o trilho (#2C2C2E) têm o mesmo texto branco. O estado selecionado fica imperceptível, embora exista. | WCAG 1.4.11 (≥ 3:1 em componentes); 1.4.1 (cor não é o único sinal) (QA Q2-2) | Segmentado próprio (§5.1): selecionado com preenchimento da marca, texto branco semibold e ✓. | `SingleChoiceSegmentedButtonRow` + `SegmentedButton` com `activeContainerColor = brand` e ícone ✓ padrão. | P | D |
| UX-06 | Descrição do gasto · `editar-registro.png` y 755 ("Teste" solto); `s03` 2:00 ("Descrição" placeholder) | O campo não tem rótulo persistente. Depois de preenchido, o valor perde contexto. O fundo do campo é igual ao do sheet. | WCAG 3.3.2 (rótulos); DESIGN-007 (rótulo persistente, Obrigatório/Opcional) (QA P1-3) | Usar o mesmo `ArkeField` do editor de lugar: rótulo 13/18 semibold + ícone e "Opcional"; cartão de raio 20 em `tertiarySystemFill` sobre a superfície do sheet; foco com 2 pt `ink`. | `OutlinedTextField` com `label` persistente (como DESIGN-007 Android). | P | D |
| UX-07 | Formulário de gasto · `editar-registro.png`, `s03` 2:00 vs. `AddExpense.dc.html` | Faltam **Data** e o rótulo **Tipo**. A linha "Rio em família · registro informativo" do protótipo também sumiu: não se sabe a que viagem o gasto vai. | Fidelidade ao protótipo aprovado; visibilidade do estado do sistema | Adicionar linha **Data** (`DatePicker(.compact)`, padrão hoje), rótulo "Tipo" acima do segmentado e linha de contexto "Trilha foda · registro informativo". | `DatePickerDialog` M3 a partir de um campo somente leitura. | M | D |
| UX-08 | Moedas e chips de viagem · `editar-registro.png` x 730–880 (JPY cortado); `s02` 1:32 (chip "Teste" cortado) | A rolagem horizontal não tem indicação. O corte duro parece erro, não rolagem. | Affordance; HIG "show partial content" | Fade de 24 pt na borda final (máscara linear) ou mostrar meio chip de propósito. **Alternativa recomendada para moeda:** `Menu` no símbolo ("R$ ▾") ao lado do valor, que economiza uma linha de 44 pt. | `LazyRow` com `contentPadding(end = 24.dp)` + fade; ou `ExposedDropdownMenuBox`. | P | D/E |
| UX-09 | Valor do gasto · `editar-registro.png` y 400–470 | O valor tem 36 pt fixos (`.system(size: 36)`) e **não escala** com Dynamic Type. O "R$" em cinza tem baixo contraste estimado e o "0,00" é placeholder. | Dynamic Type; legibilidade de números | `.font(.largeTitle.weight(.bold))` + `.monospacedDigit()`; formatação ao vivo com `FormatStyle.currency(code:)`; "R$" em `.secondary` com no mínimo 4,5:1. | `MaterialTheme.typography.displaySmall` com `fontFeatureSettings="tnum"`. | P | D |
| UX-10 | Sheets de formulário · `s04` 2:48, `s08` 5:40/5:56/6:12 (Cancelar espremido sob "Revisar lugar") | O "Cancelar" textual fica entre a ação flutuante e a borda, cortado. Há conflito entre documentos: o DESIGN-007 pede "sem botões no cabeçalho" e o DESIGN-006/AGENTS pede "Cancelar no topo esquerdo". | DESIGN-006 paridade; HIG (cancelar em `cancellationAction`) (QA P1-5, Q2-1) | Fechar com **X de vidro 44 pt no topo esquerdo** (nome acessível "Cancelar"), como no M5-EDITOR-UI-02. Remover o "Cancelar" textual do corpo. **Decisão D-1.** | `IconButton` X 48 dp no topo esquerdo, com mesmo rótulo. | P | D |
| UX-11 | Detalhe do lugar · `s05` 3:12–3:20; `d.png` 0:06 | Editar, No mapa e Excluir aparecem como três chips iguais, com Excluir em vermelho no mesmo peso. "Concluído" é uma pílula primária grande em um sheet só de leitura. | Hierarquia de ações; prevenção de erro (destrutivo separado) | Ações: **Editar** (secundária de vidro) e **No mapa** (secundária). **Excluir** vai para um menu "…" no topo direito, com confirmação em alerta. Fechar com X ou arrastando; trocar "Concluído" pelo pager ‹ › quando houver vários lugares. | `TopAppBar` local com `IconButton` "Mais opções" → `DropdownMenu` "Excluir"; `AlertDialog`. | M | E |
| UX-12 | Cartão do Planejamento · `a.png` 0:00 (cartão "Teste") | O cartão tem "Lugar 1 de 1" interno + pager "1 de 1" com chevrons desabilitados + legenda "Teste" acima. A nota repete o nome. O placeholder da foto é um ícone cinza. | Redundância; densidade | Esconder o pager quando N = 1. Remover "Lugar n de N" do cartão (fica só no pager). Remover a legenda sob o cabeçalho. Sem capa, usar **selo** com `mappin` em `ink` sobre `tertiarySystemFill` (QA P1-4). | Igual; selo com moldura simples (DESIGN-007). | P | D |
| UX-13 | Botões de rodapé do cartão · `a.png` 0:06 ("Ver meus lugares"); código `Button("Ver detalhes")` | O link azul fica solto e alinhado à esquerda, sem altura de toque visível. "Ver meus lugares" repete o ‹ da nova barra. | Hierarquia; alvo ≥ 44 (QA Q2-5) | Componente **Rodapé do cartão** (§5.3 nível 3). "Ver meus lugares" sai porque a barra já navega. "Ver detalhes" vira "Mostrar tudo", que leva ao detent Detalhes. | `FilledTonalButton` 48 dp, mesmo texto. | P | D |
| UX-14 | Marcar no mapa · `s02` 1:32; `s06` 4:24–4:44 | Mostra "Ponto marcado: -23.0099, -43.3541" (coordenadas cruas). A câmera inicial abre no Brasil inteiro (`s06` 4:12). O botão "Confirmar ponto" desabilitado aparece só como cinza, sem explicar o motivo junto dele. | Linguagem do usuário (Nielsen 2); visibilidade | Texto "Ponto marcado no mapa ✓" (coordenadas só na acessibilidade/Detalhes). Câmera inicial no endereço digitado (busca local já existente), senão no último lugar, senão na região do usuário. *Endereço aproximado por geocodificação reversa = **decisão D-6** (o recorte M5-PLACES-01 restringia geocoding).* | Igual. | M | D |
| UX-15 | Jargão "Pin" · `s05` 3:40 "Nenhum Pin nesta viagem."; `s01` 0:24–0:32 "Compartilhar Pin" | O termo técnico vaza para a interface, enquanto o resto do app diz "lugar". | Consistência terminológica | "Nenhum lugar nesta viagem ainda." / "Compartilhar lugar". | Igual (`strings.xml`). | P | D |
| UX-16 | Nova memória · `c.png` 0:07–0:11; `s04` 2:36 | O seletor "Sem capa ⌃" na verdade escolhe **de qual lugar** vem a capa. O placeholder é quase invisível. Não há foto própria (QA P1-2). | Clareza de controle; WCAG 1.4.3 (placeholder ≥ 4,5:1 pelo DESIGN-007) | Duas linhas: **Lugar** (`Menu`: "Nenhum", lugares da viagem) e **Foto** (linha-botão `PhotosPicker`, depende de D-B). Placeholder em `.secondary`, não `.tertiary`. | `ExposedDropdownMenuBox` para lugar + Photo Picker. | M | D |
| UX-17 | Avaliar este lugar · `b.png` 0:09 | As 5 estrelas aparecem **cheias em laranja** antes de qualquer escolha (o código parece iniciar `rating` em 5 *(inferência)*). Não se diz **qual lugar** está sendo avaliado. `systemOrange` é laranja, não o âmbar previsto. | Sem valor padrão enviesado; visibilidade; DESIGN-007 (âmbar ≥ 3:1 + nota em texto) | Começar com 0 e estrelas vazias em `.secondary`. Mostrar "Toque para dar uma nota" e, ao escolher, "4 de 5". Linha **Lugar** obrigatória no topo. Usar âmbar do protótipo (#E0A030 escuro / #B7791F claro). | Igual, com `IconToggleButton` 48 dp. | P | D |
| UX-18 | Aviso "Lugar salvo neste aparelho." · `s06` 4:04 | O texto entra no fluxo do cartão, empurra o conteúdo para baixo e some depois de 4 s, com salto de layout. | Estabilidade de layout; feedback não intrusivo | Toast de vidro flutuante 8 pt acima da base do cartão, sem afetar o layout; `AccessibilityNotification.Announcement`; háptico `.success`. | `SnackbarHost` ancorado no cartão; `announceForAccessibility`. | P | D |
| UX-19 | Materiais de sheet incoerentes · `s04` 2:44/2:48 (Adicionar lugar translúcido, com o verde do mapa vazando) × `editar-registro.png` (opaco) | No iOS 26, um sheet em detent parcial vira vidro flutuante e em `.large` fica opaco. O editor (0.78) e os formulários (large) parecem de famílias diferentes. Campos sobre vidro perdem o contraste previsível. | Consistência; contraste de campos (QA P1-3) | Formulários: `.presentationBackground(ArkeUI.sheetSurface)` opaco em todos os detents. Vidro fica para os sheets de leitura (Detalhes) e controles. **Decisão D-2.** | Sem vidro: `containerColor = surfaceContainerLow` em todos. | P | D |
| UX-20 | Busca · `s01` 0:08; `s07` 5:16 | O sheet abre grande e vazio, com campo sem foco visível e sem sugestões. Aparece fantasma/translúcido durante a transição. | Estado vazio útil; continuidade espacial | A pílula de busca do mapa faz **morph** até o sheet (`matchedTransitionSource` + `.navigationTransition(.zoom)`). O campo abre focado e o teclado sobe junto. O vazio mostra "Busque por nome ou endereço" + "Seus lugares" (lista local). | Busca em `SearchBar` M3 expandindo para `SearchView` em tela cheia (padrão nativo de busca). | M | E |
| UX-21 | Contorno "leitoso" do mapa · todas as Home; código `strokeBorder(.white.opacity(0.25), lineWidth: 9).blur(7)` | Um halo branco desfocado contorna o mapa. Ele diminui o contraste da atribuição e dos controles de vidro nas bordas e não é material do sistema. | ADR-006 (sem efeitos decorativos); legibilidade sobre o mapa | Remover o halo. Separar mapa e fundo pelo próprio raio e pela cor de fundo do sistema. | Já não tem halo *(código)*; manter. | P | D |
| UX-22 | Raios desencontrados · código: mapa 30, cartão 20, sheets 36; DESIGN-007: mapa 40, cartão 36–40 | Os cantos não são concêntricos com a tela, o que dá aspecto de peças avulsas. | HIG iOS 26 (concentricidade); DESIGN-007 | Mapa e cartão com `ConcentricRectangle()` / `.containerShape` (iOS 26). Na prática fica raio ≈ raio da tela − margem 12 (≈ 43 pt num aparelho de 55 pt). Campos 20 e botões em cápsula. | Mapa e cartão 28 dp, campo 16 dp (Android), botão 20 dp (DESIGN-007). | P | D |
| UX-23 | Alça entre os cartões · `home-vazia.png` y 1000 | A alça flutua no vão preto entre os cartões e não fica claro a qual cartão pertence. Ocupa 44 pt de altura de vão. | Gestalt (proximidade) | Mover a alça para **dentro do topo do cartão** (36×5, 6 pt da borda, alvo 48 sobreposto à barra). O vão entre os cartões cai para 8 pt. Ganho: cerca de 36 pt. | Igual, com `BottomSheetDefaults.DragHandle` no topo do cartão. | P | E |

### P2 — acabamento e evolução

| ID | Tela / captura · região | Problema | Proposta (iOS · Android) | Esf. | Tipo |
|---|---|---|---|---|---|
| UX-24 | Estados vazios · `a.png` 0:06 (Gastos), `home-vazia.png`, `c.png` 0:00 (Memórias) | Só texto, ou texto + botão largo que repete o "+". Isso deixa de 300 a 450 pt vazios. | Componente **Estado vazio** (§7). | M | E |
| UX-25 | Carregando · código `ProgressView("Abrindo seus lugares…")` | O spinner com texto troca de altura quando o conteúdo chega. | Esqueleto com a forma do cartão (`.redacted(reason: .placeholder)` · placeholder estático Compose, sem shimmer). | P | E |
| UX-26 | Erro de carga · código `Text(error)` vermelho + `Button("Tentar novamente")` | O erro aparece solto, sem ícone e sem superfície. | Faixa de erro (§7) com ícone, texto e ação tonal. | P | E |
| UX-27 | "Revisar lugar" · `s02` 1:12, `s06` 4:00 | O passo de revisão de um formulário de 4 campos acrescenta 1 tela e 2 toques. Sem foto, a revisão tem 50% de vazio. | Salvar direto quando o formulário é válido. A revisão fica só em edição com mudanças, ou é removida. **Decisão D-5.** | M | E |
| UX-28 | Cofrinho · `s03` 2:08 | Tem "Meta / Meta" (rótulo repetido) e a barra a 0% aparece como uma linha cinza. O cartão "Reservado" não mostra percentual (o protótipo mostra 42%). | Linha "Meta da viagem · R$ 95,00". Barra com trilho visível (`tertiarySystemFill`), percentual em texto e "Falta R$ 95,00". | P | D |
| UX-29 | "Registrar valor" × "Definir meta" · `s03` 2:12; código | O mesmo sheet com segmentado Meta/Valor reservado mistura duas tarefas. As moedas aparecem bloqueadas (opacidade 0.4) sem explicação. | Duas ações explícitas no menu do "+": "Adicionar reserva" (padrão) e "Alterar meta". Moeda bloqueada com legenda "Mesma moeda da meta (BRL)". | M | E |
| UX-30 | Rótulo do lugar no mapa · `s01` 0:00 (chip "Teste" sobre a atribuição) | O chip do lugar selecionado repete o `Marker` e fica a 34 pt da atribuição; em mapa espremido, colide com ela. | Omitir o chip quando o `Marker` já mostra o título. Se ficar, ancorar ≥ 8 pt acima da atribuição. | P | D |
| UX-31 | Iconografia de contexto · `a.png` (âncora, recibo, alvo, estrela, foto) | Âncora para Planejamento e alvo para Cofrinho são metáforas fracas e, sem rótulo, quase ilegíveis. | Com a barra nova os ícones saem do cabeçalho e o problema some. Se forem mantidos em outro lugar: `map`/`list.bullet` e um símbolo de cofre. **Decisão D-8.** | P | E |
| UX-32 | Push dentro do sheet Viagens · `s01` 0:24 (título "Trilha foc" cortado na transição) | A barra de navegação nativa fora do padrão contínuo; o título é truncado durante o push. | `NavigationStack` com `.navigationTitle` inline e `toolbarTitleDisplayMode(.inline)`; nunca título manual com `lineLimit(1)` em largura fixa. | P | D |

---

## 3. Barra de navegação do cartão (aprofunda Q2-3, P0-1, P2-4)

### 3.1 Objetivo
Um único elemento fixo no topo do cartão diz **onde estou** (contexto), **em que viagem** (escopo) e **o que posso criar** (ação). Ele navega entre contextos e não tem ícone do contexto atual. Todo o resto do cartão é conteúdo rolável, que passa **por baixo** da barra sem ser cortado.

### 3.2 Layout compacto (padrão do iPhone; 2 linhas, 88 pt)

```
┌──────────────── cartão (padding horizontal 16) ────────────────┐
│ [‹]          Planejamento            [›]                 [+]   │  linha 1 · 44
│              • ━ • • •                                          │  (pontos dentro da linha 1)
│ [🧳 Trilha foda ▾]                         3 lugares · Privado  │  linha 2 · 36
└────────────────────────────────────────────────────────────────┘
   gap 8 entre linhas · 8 entre barra e conteúdo
```

| Elemento | Medida | Detalhe |
|---|---|---|
| Cartão | padding 16 lateral, 8 no topo (alça interna, UX-23) | Raio concêntrico (UX-22) |
| Linha 1 | altura 44 (mínimo; cresce com fonte) | `‹` 44×44, título flexível, `›` 44×44, 8 de espaço, `+` 44×44 |
| `‹` / `›` | glifo `chevron.left/right` 17 semibold, cor `.secondary`; oculto (espaço preservado) no primeiro/último | Nome acessível "Contexto anterior: Gastos" |
| Título | 17/22 semibold (`.headline`), 1 linha, centralizado **entre os chevrons**, sem ícone | Pontos 6 pt (atual 16×6) a 4 pt abaixo, dentro dos 44 |
| `+` | círculo 44, marca `#4C728A`, glifo branco 20 semibold | iOS: vidro proeminente com tinta da marca, **se D-3 aprovar**; senão, preenchimento opaco atual |
| Linha 2 | altura 36 (chip com 36 visíveis, **alvo de toque 44** via `contentShape` que invade 4 pt acima e abaixo) | Chip: ícone 15 + nome 15 semibold + chevron 12; largura máxima 60% da linha; trunca no fim |
| Metadado à direita | 15/20 regular `.secondary`: "3 lugares", "2 moedas", "Só você vê", "Privado" | Um só texto por contexto; o cadeado entra como glifo |
| Fundo | iOS: sem fundo próprio; `scrollEdgeEffectStyle(.soft, for: .top)` (iOS 26) faz o conteúdo esmaecer sob a barra. Android: superfície do cartão + divisor `outlineVariant` 1 dp que aparece só com rolagem > 0 | Resolve o corte de Memórias (P2-4) |

**Ganho estimado:** passa de cerca de 150 pt para 88 pt (+ 8 de respiro), o que libera cerca de 54 pt. Somando a alça interna (UX-23), o ganho fica em cerca de 90 pt de conteúdo no detent Equilibrado. *(inferência por medida nas capturas de 1170 px a 3×)*

### 3.3 Layout largo (largura interna ≥ 400 pt: iPhone Pro Max em paisagem, iPad, dobráveis)
Tudo em uma linha de 44: `[🧳 Trilha foda ▾]  [‹] Planejamento [›]  3 lugares  [+]`. O título fica centrado no cartão e os lados equilibrados (colunas laterais de largura igual).

### 3.4 Comportamento em 320 pt (iPhone SE com Zoom de tela; Android 320 dp)
- Largura interna: 320 − 2×12 (margem da home reduzida de 16 para 12 em `horizontalSizeClass == .compact && width < 360`) − 2×16 = **264 pt**.
- Linha 1: 44 + 44 + 8 + 44 = 140, sobram **124 pt** para o título. "Planejamento" a 17 semibold ocupa cerca de 105 pt e cabe. "Avaliações", "Memórias", "Cofrinho" e "Gastos" também cabem.
- Linha 2: o chip vai até 60% (158 pt), o que dá cerca de 8 caracteres de nome de viagem antes da reticência. O metadado sai da linha e vira `accessibilityValue` do chip quando faltar espaço (`ViewThatFits`). Android: `Layout`/`SubcomposeLayout` ou `FlowRow` com prioridade.
- Nenhum texto de **navegação** é truncado (DESIGN-007, princípio 3). Só o nome da viagem, que é dado do usuário, pode truncar, e ele continua completo na acessibilidade e no menu.

### 3.5 Dynamic Type grande (AX1–AX5) e font scale 1.5–2.0
- Até `.xxxLarge`: mesmo layout, linhas crescem em altura.
- `AX1–AX2`: o título pode quebrar em 2 linhas (`lineLimit(2)`), com chevrons e "+" centralizados verticalmente. Os pontos somem (são decorativos) e a posição é anunciada.
- `AX3+` (ou `fontScale ≥ 1.5`): o **"+" desce para a linha 2**, à direita do chip, e o metadado vai para a acessibilidade. A linha 1 fica só com `‹ Título ›`.
- A barra nunca passa de **25% da altura do cartão**. Se passar, o cartão sobe automaticamente para o detent Detalhes. *(Regra proposta, a validar com captura.)*
- Ícones acompanham com `@ScaledMetric(relativeTo: .headline)`. No Android, `dimensionResource` + `LocalDensity.fontScale`.

### 3.6 Acessibilidade da barra
- Ordem de foco: título (cabeçalho, ajustável) → `‹` → `›` → `+` → chip → metadado. O título mantém `accessibilityAdjustableAction`, como hoje (bom), e anuncia "Planejamento, 1 de 5".
- O `+` tem rótulo **contextual** ("Adicionar lugar", "Adicionar gasto", "Adicionar reserva", "Avaliar um lugar", "Nova memória"). No Android isso vale para `contentDescription` e `testTag` por contexto, para que o teste confira **o título do sheet** (QA "o que deixei passar" 1).
- O swipe horizontal no título continua como atalho, com alternativa por toque (chevrons) e por leitor.
- Voice Control: os rótulos visíveis "‹"/"›" não podem ser ditos. Adicionar `accessibilityInputLabels(["Anterior", "Gastos"])`.
- Reduzir Transparência: o `+` e os chevrons de vidro passam a preenchimento opaco. Aumentar Contraste: o chip ganha contorno 1 pt `separator`.

### 3.7 Implementação sugerida
- **iOS:** `GlassEffectContainer(spacing: 8)` envolvendo `‹`, `›` e `+`, que permite morph entre eles e com o sheet (§6.3). Barra em `.safeAreaInset(edge: .top)` do `ScrollView` (como hoje), **sem** fundo sólido (o comentário no código sobre "faixa estranha" é resolvido pelo `scrollEdgeEffect`).
- **Android:** `Column` fixa acima de `LazyColumn`, com `rememberLazyListState().canScrollBackward` controlando o divisor. Sem vidro (ADR-001).

---

## 4. Sheets: regras de altura e detents

### 4.1 Classes de sheet

| Classe | Exemplos | iOS | Android | Ações |
|---|---|---|---|---|
| **C — compacto** (≤ 5 controles, sem texto longo) | Gasto, Reserva/Meta, Avaliação, Nova viagem, Descartar/Excluir (alerta) | `.presentationDetents([.height(h)])`, h medido por `onGeometryChange`, limite `min(h, 0.9 × tela)`. Com teclado, o sistema empurra o sheet e, se não couber, usar `.large` | `ModalBottomSheet(skipPartiallyExpanded = true)`, `wrapContentHeight()`, `heightIn(max = 0.9f × janela)`, `imePadding()` | X topo esquerdo + primária **colada ao conteúdo** (16 abaixo do último controle), não flutuando no fundo |
| **M — médio** (formulário com foto/nota) | Adicionar/Editar lugar, Nova memória | `[.height(h), .large]`, começando em `h`. Com foco em campo multilinha, ir para `.large` | `skipPartiallyExpanded = true`, conteúdo com rolagem e altura natural | X + primária flutuante (DESIGN-007) somente se o conteúdo rolar; senão, colada |
| **L — lista/busca** | Buscar, Viagens, Compartilhar | `[.medium, .large]`; busca abre em `.large` com foco | Busca: `SearchView` tela cheia; Viagens: sheet com `partialExpanded` | X; ações em linhas |
| **R — leitura** | Detalhe do lugar | `[.height(h), .large]` (padrão atual, manter) | Altura natural ≤ 90% | Pager ‹ › + menu "…"; sem primária |
| **Mapa** | Marcar no mapa | Tela cheia (já contínuo) | Igual | Confirmar ponto fixo |

### 4.2 Regras comuns
1. **A altura segue o conteúdo; o detent nunca é fixado em `.large` por padrão.** Critério de aceite: nenhum sheet com mais de 64 pt vazios entre a última linha e a ação primária, sem teclado.
2. **Respiros:** topo 20 (título), entre grupos 16, campo a campo 8, última linha até a primária 16, primária até a borda inferior 20 + safe area. Hoje, `padding(.bottom, 100)` + `padding(.bottom, 14)` geram sobra.
3. **Cabeçalho:** linha de 52: `[X 44]  Título 17/22 semibold centralizado  [espaço 44 ou "…"]`. **Alternativa B (DESIGN-007):** título grande 28/34 rolando com o conteúdo e X flutuante. Para sheets compactos recomendo o título inline: economiza 40 pt e é o padrão nativo do iOS 26 (`.toolbarTitleDisplayMode(.inline)`). **Decisão D-1.**
4. **Teclado:** campo focado sempre visível. A primária sobe junto com o teclado (`safeAreaInset(edge: .bottom)` no iOS; `imePadding()` no Android). No campo de valor (teclado decimal sem Return), adicionar `ToolbarItemGroup(placement: .keyboard)` com "Concluído".
5. **Foco inicial:** em "Adicionar gasto", focar o valor ao abrir (o teclado decimal é a tarefa). Em "Editar", não focar.
6. **Fundo opaco** para formulários (UX-19), sem faixa colorida na base (QA P1-5): a mesma cor vai até a borda, inclusive sob o home indicator.
7. **Um roteador único de apresentação** (QA P0-3) com `enum ActiveSheet`. As classes acima viram propriedades do enum (`.compact`, `.medium`...), o que garante consistência por construção.

### 4.3 "Editar registro" redesenhado (exemplo de classe C, 390 pt)
```
[X]            Editar gasto              [ ]     52
Valor                                             18
R$ ▾  99,00                                        48   (Menu de moeda no símbolo)
Descrição · Opcional                              18+8
[ Teste                                     ]      52
Tipo                                              18+8
[ ✓ Prevista  |    Realizada   ]                   44
Data                         [ 6 de nov. de 2026 ] 44
ⓘ Registro informativo: o Arke não processa pagamentos.  40
[        Salvar alterações         ]               56
                                                  20 + safe area
```
A altura total fica em cerca de 520 pt, contra os 844 do sheet atual. Excluir o registro (que hoje não aparece na captura) fica como linha de texto vermelha **abaixo** de Salvar, com confirmação. **Decisão D-7** sobre onde excluir registros.

---

## 5. Controles

### 5.1 Segmentado (Prevista/Realizada; Meta/Reserva se mantido)
- **Trilho:** altura 44, raio cápsula, preenchimento `tertiarySystemFill` (iOS) / `surfaceContainerHighest` (Android), padding interno 3.
- **Selecionado:** cápsula com preenchimento **marca `#4C728A`**, texto **branco** 15 semibold e glifo `checkmark` 13 à esquerda (sinal que não depende de cor).
- **Não selecionado:** texto `label` (primário) 15 regular, sem preenchimento.
- **Contraste (estimativa):** marca × trilho escuro (#2C2C2E sobre #1C1C1E) ≈ **2,7:1**, abaixo de 3:1. Por isso o ✓ e o peso do texto são obrigatórios, ou a cápsula selecionada ganha contorno 1 pt `#88AABE` (tinta escura), chegando a cerca de 4:1. No claro, marca × #E5E5EA ≈ 4:1. **Medir no aparelho.**
- **Pré-seleção:** gasto novo → "Prevista"; reserva → "Valor reservado" se já houver meta, senão o sheet é "Definir meta" sem segmentado (UX-29). Na edição, vem o valor salvo.
- **iOS:** o `Picker(.segmented)` não aceita cor de seleção em SwiftUI puro. Opções: (a) `UISegmentedControl.appearance()` com `selectedSegmentTintColor`, que tem efeito global e é frágil; (b) **recomendado:** componente próprio `ArkeSegmented` com `matchedGeometryEffect` na cápsula selecionada, `accessibilityAddTraits(.isSelected)` e semântica de `Picker` via `accessibilityRepresentation { Picker(...) }`.
- **Android:** `SingleChoiceSegmentedButtonRow` + `SegmentedButton(colors = SegmentedButtonDefaults.colors(activeContainerColor = brand, activeContentColor = Color.White))`. O ícone ✓ já vem por padrão.

### 5.2 Campos
- Um componente só, `ArkeField`, em **todos** os formulários: hoje o editor de lugar tem cartões com rótulo, e gasto, memória e avaliação usam `TextField` cru.
- Rótulo 13/18 semibold `.secondary` + ícone 15 + "Obrigatório/Opcional" à direita; cartão raio 20 (Android 16 contornado), padding 12×16, valor 17/24.
- Preenchimento: escuro `tertiarySystemFill` sobre `secondarySystemBackground`; claro `secondarySystemBackground` sobre `systemBackground`. Foco: contorno interno 2 pt `ink`. Erro: contorno `systemRed` + mensagem 13 abaixo com ícone.
- Placeholder em `.secondary` (≥ 4,5:1), nunca `.tertiary` (`c.png`: "Escreva sua memória" quase invisível).
- Multilinha: altura mínima 104, cresce até 6 linhas e depois rola (DESIGN-007).
- Valor monetário: §UX-09.

### 5.3 Botões: hierarquia em 4 níveis

| Nível | Uso | iOS | Android | Medidas |
|---|---|---|---|---|
| 1 · Primário | 1 por tela: Salvar, Confirmar ponto, Revisar | Cápsula opaca marca, texto branco 17 semibold. Vidro proeminente com tinta só se D-3 | `Button` (filled) marca, raio 20 | Altura 56, largura total menos 20+20 |
| 2 · Secundário | Editar, No mapa, Editar informações | `.buttonStyle(.glass)` neutro | `FilledTonalButton` | Altura 48, largura pelo conteúdo ou par 50/50 |
| 3 · Rodapé do cartão | "Mostrar tudo", "Ver detalhes" | `.glass` cápsula, ícone + texto 15 semibold, **centralizado**, 16 abaixo do conteúdo | `FilledTonalButton` 48 dp centralizado | Altura 44, padding 16 horizontal; máx. 2 lado a lado, larguras iguais |
| 4 · Texto | Ações terciárias inline | Texto `ink` 15 semibold, alvo 44 | `TextButton` | Alvo 44/48 |
| Destrutivo | Excluir | Só em menu ou alerta, `role: .destructive` | `DropdownMenuItem` com cor `error` + `AlertDialog` | — |

**Estados (todos os níveis):** pressionado (escala 0.97 + opacidade 0.85, 100 ms; o vidro já tem `.interactive()`); desabilitado (opacidade 0.4 **e** legenda explicando o motivo quando não for óbvio: "Dê um nome à nova viagem para continuar" já existe em `s06` 4:40, aplicar o mesmo padrão em Salvar avaliação/memória); carregando (rótulo trocado por `ProgressView` do mesmo tamanho, botão travado para evitar duplicata); sucesso (háptico + toast, UX-18).

### 5.4 Chips
- Altura visível 36, alvo 44, raio cápsula (Android 12), texto 15 semibold. Selecionado: marca + branco + ✓ (igual ao segmentado). As moedas seguem a mesma regra: hoje BRL selecionado não tem ✓.

---

## 6. Motion

### 6.1 Expandir/recolher o mapa (aprofunda Q2-6, P0-5)
**Tradução da referência *(inferência de adaptação)*:** na referência, o cartão de mídia está **embaixo** e cresce para cima. No Arke, o mapa está **em cima**. A tradução fiel ao *princípio* é:
- O mapa fica **ancorado no topo** (safe area) e cresce **para baixo** até 12 pt da base.
- O cartão de conteúdo **esmaece e afunda**: opacidade 1→0 nos primeiros 60% da curva, `offset y +24` e escala 0.97. É o equivalente ao "conteúdo acima esmaece" da referência.
- A barra do cartão (título + chip) **vira pílula de resumo** dentro do mapa, centralizada na base, 12 acima da borda: `[selo 28] Nome do lugar · Planejamento  [⤡]`, altura 52, vidro (Android: superfície sólida com elevação 3).
- Botão expandir no **mesmo lugar**, com `contentTransition(.symbolEffect(.replace))` entre `arrow.up.left.and.arrow.down.right` e `arrow.down.right.and.arrow.up.left`.
- Cantos: **raio constante** durante todo o movimento (concêntrico, UX-22). Nenhum quadro com canto reto.
- *Alternativa (decisão D-4):* inverter a âncora (o mapa empurra o cartão para baixo, que sai pela base). É mais parecida com a referência quadro a quadro, mas perde o resumo visível.

**Curvas medidas na referência** (`ref-expandir`: movimento 0,15→0,45 s, assenta até 0,55 s; `ref-recolher`: 0,20→0,55 s):

| Parâmetro | iOS | Android |
|---|---|---|
| Expandir | `.spring(response: 0.38, dampingFraction: 0.88)` (≈ 320 ms visíveis) | `spring(dampingRatio = 0.88f, stiffness = 420f)` |
| Recolher | `.spring(response: 0.44, dampingFraction: 0.90)` (≈ 380 ms, assentamento lento) | `spring(dampingRatio = 0.90f, stiffness = 320f)` |
| Fade do cartão | opacidade ligada ao mesmo progresso (0→0,6) | `graphicsLayer { alpha = 1 - (p / 0.6f).coerceIn(0f,1f) }` |
| Morph barra→pílula | `matchedGeometryEffect(id: "mapSummary")` ou `glassEffectID` em `GlassEffectContainer` | `SharedTransitionLayout` + `Modifier.sharedBounds(rememberSharedContentState("mapSummary"))` |
| Gesto | arrastar a alça/pílula interpola `progress` 0…1 com `.interactiveSpring`; soltar decide pelo lado mais próximo e pela velocidade (> 500 pt/s) | `AnchoredDraggableState` com as mesmas âncoras; **Predictive Back** (`PredictiveBackHandler`) recolhe o mapa seguindo o gesto de voltar |

*Os valores de stiffness do Android são aproximações para casar com o `response` do iOS. Validar com a gravação lado a lado de 60 fps exigida na lista.*

### 6.2 Troca de contexto
- Título: `.contentTransition(.interpolate)` com transição `push(from: .trailing/.leading)` conforme a direção, 220 ms `.snappy`.
- Conteúdo: deslocamento horizontal de 24 pt + fade, 240 ms. Altura do cartão com `animation(.smooth(duration: 0.25))` para não "pular".
- Pontos: a cápsula ativa desliza (já existe, 200 ms).
- Háptico `.selection` (já existe; manter).
- Android: `AnimatedContent(transitionSpec = { (slideInHorizontally { dir * it / 16 } + fadeIn(tween(240, easing = EmphasizedDecelerateEasing))) togetherWith fadeOut(tween(90)) })`.

### 6.3 Abertura de sheet e morph do "+" (aprofunda Q2-4)
- **iOS 26:** o "+" (e a pílula de busca) recebe `.matchedTransitionSource(id: "add.\(context)", in: ns)`, e o sheet usa `.navigationTransition(.zoom(sourceID: "add.\(context)", in: ns))`. O botão "se abre" no sheet e o sheet volta para o botão ao fechar. Ações dentro do sheet (X, "…", primária quando for vidro) ficam em `GlassEffectContainer` para fundir e separar ao trocar de estado (por exemplo, Revisar → Editar informações + Salvar: `glassEffectID` mantém a identidade e o morph é feito pelo sistema).
- **Android:** o `ModalBottomSheet` vive em outra janela, então **shared element não atravessa** *(limitação conhecida do Compose, verificar na versão em uso)*. Opções: (a) manter o slide-up padrão M3 e animar o "+" com escala 1→0.9→1 no toque (recomendado, nativo e barato); (b) sheet próprio dentro da hierarquia com `sharedBounds` (container transform M3), que custa mais e perde comportamentos do sistema. **Recomendo (a)**: paridade de hierarquia, não de material (ADR-001).
- O recuo da Home (escala 0,96) ao abrir sheet continua. Duração alinhada ao sheet (≈ 350 ms).

### 6.4 Reduzir Movimento / Remover animações
- iOS `accessibilityReduceMotion`: zoom e matched geometry → **crossfade 150 ms** sem escala nem deslocamento. O mapa muda de altura com fade do conteúdo, sem mola. O `zoom` de navegação precisa ser **desligado explicitamente** (não confiar no fallback do sistema *(inferência; verificar)*).
- Android `ANIMATOR_DURATION_SCALE == 0` / "Remover animações": todas as specs viram `snap()`, e a semântica de estado (expandido/recolhido) é anunciada.
- Reduzir Transparência: vidro → superfícies opacas `secondarySystemBackground`.

### 6.5 Tabela de tokens de motion (proposta para o DESIGN-007)
| Token | Valor iOS | Valor Android | Uso |
|---|---|---|---|
| `motion.press` | `.spring(response: 0.2, dampingFraction: 0.9)` | `spring(1f, 1400f)` | toque em botões |
| `motion.small` | `.snappy(duration: 0.22)` | `tween(220, EmphasizedDecelerate)` | título, pontos, chips |
| `motion.content` | `.smooth(duration: 0.25)` | `tween(250, Emphasized)` | troca de contexto, altura do cartão |
| `motion.container.expand` | `.spring(0.38, 0.88)` | `spring(0.88f, 420f)` | mapa |
| `motion.container.collapse` | `.spring(0.44, 0.90)` | `spring(0.90f, 320f)` | mapa |
| `motion.reduced` | `.easeInOut(duration: 0.15)` opacidade | `snap()` | Reduzir Movimento |

---

## 7. Estados vazio, erro e carregando

**Componente Estado vazio (cartão):** ícone 32 em `ink` dentro de círculo 56 `tertiarySystemFill` → título 17/22 semibold → texto 15/20 `.secondary` (máx. 2 linhas) → **uma** ação secundária (nível 2) que é a mesma ação do "+" (com o mesmo rótulo e o mesmo sheet, então não cria um segundo significado). Centralizado no espaço disponível; nunca deixar mais de 40% do cartão vazio sem o componente.

| Contexto | Título | Texto | Ação |
|---|---|---|---|
| Planejamento (Home vazia) | Seu primeiro lugar | Guarde ideias antes mesmo de ter uma viagem. | Adicionar lugar · e, secundária de texto, "Buscar no mapa" (foca a busca) |
| Gastos | Nenhum gasto ainda | Anote o que você prevê gastar em Trilha foda. | Adicionar gasto |
| Cofrinho | Defina uma meta | Registre quanto quer guardar. O Arke não movimenta dinheiro. | Definir meta |
| Avaliações | Nenhuma avaliação | Conte o que achou dos lugares desta viagem. | Avaliar um lugar |
| Memórias | Nenhuma memória | Guarde um momento desta viagem. | Nova memória (tile "Nova memória" sai da grade enquanto vazia) |

- **Carregando:** esqueleto com a forma real (cartão de lugar 300×206 com selo, 2 linhas de texto) em `.redacted(reason: .placeholder)`. Sem spinner textual. No Android, placeholder estático `surfaceContainerHigh`, sem shimmer (Material não define shimmer). Se demorar mais de 400 ms, o esqueleto aparece; abaixo disso, não aparece nada (evita piscar).
- **Erro de carga:** faixa no topo do conteúdo com raio 16, `systemRed` a 12% de fundo, ícone `exclamationmark.triangle`, texto "Não deu para abrir seus lugares." e botão nível 2 "Tentar de novo". O layout não pula: a faixa entra com `motion.content`.
- **Falha ao salvar:** dentro do sheet, acima da primária: "Não foi salvo. Seu rascunho continua aqui." + "Tentar de novo" (já previsto no protótipo `States`). Dois toques nunca duplicam.
- **Mapa carregando** (`s06` 4:16, tiles cinza): manter. Não sobrepor spinner ao mapa.
- **Sem localização:** alerta atual "Minha localização"/"Entendi" → usar o texto do protótipo ("O mapa continua funcionando…") com "Abrir Ajustes" / "Agora não".

---

## 8. Tipografia, cor e modo escuro

- **Escala única** (DESIGN-007): título 22/28 · contexto **17/22 na barra nova** (proposta: reduzir de 19 para caber em 320 pt; *diverge do DESIGN-007, decisão D-9*) · cabeçalho 17/22 · corpo 16/24 · apoio 15/20 · rótulo/legenda 13/18. Hoje coexistem `.title.bold()` (sistema) e `arkeText(.screenTitle)` (28 escalado a partir de `.body`). Unificar com estilos de texto do sistema (`.title2`, `.headline`, `.body`, `.subheadline`, `.footnote`) para herdar a curva nativa de Dynamic Type. O `ScaledMetric(relativeTo: .body)` em títulos escala títulos como corpo, o que não segue a curva da HIG.
- **Texto terciário:** "Lugar 1 de 1" (`a.png`) e placeholders usam `tertiaryLabel` (~30% no escuro). A estimativa fica abaixo de 4,5:1, o que viola o DESIGN-007 (princípio 3). Regra: `tertiaryLabel` só para texto decorativo/desabilitado; informação usa `secondaryLabel`.
- **Cor da marca:** o código usa `#4C728A` (ADR-006 candidato) e o protótipo usa `#3372A3`. **As duas fontes divergem**, então é preciso registrar qual é a referência (D-10). Tinta escura `#88AABE` (implementada) correta para texto/ícones.
- **Laranja do sistema** nas estrelas (`systemOrange`) é uma segunda cor quente de fato. Usar o âmbar do protótipo como "cor semântica de avaliação", sempre com nota em texto (DESIGN-007).
- **Modo claro: NÃO VERIFICADO** (nenhuma captura). Riscos previstos: vidro claro sobre mapa claro (busca e controles), chips `tertiarySystemFill` sobre branco, segmentado. Exigir pares claro/escuro em todas as capturas da próxima rodada.
- **Escuro:** o fundo da Home é preto (`systemGroupedBackground` = #000) e o cartão #1C1C1E. Funciona, mas o vão de 44 pt com a alça (UX-23) aumenta a sensação de "dois apps empilhados".

---

## 9. Acessibilidade (além do já citado)

| Item | Situação | Proposta |
|---|---|---|
| Alvos | "+" 44 pt (iOS) / 44 dp (Android) *(código)*; expandir mapa Android **40 dp** *(código)* | "+" 48 (DESIGN-007 pede ≥ 48); expandir 48 dp (`minimumInteractiveComponentSize`) |
| Vizinhos de contexto | só ícones com rótulo de acessibilidade (bom); visualmente ambíguos | Chevrons + `accessibilityInputLabels` (§3.6) |
| Estrelas | rótulo "n de 5" por estrela (bom); valor inicial 5 enviesa | Grupo ajustável único "Nota, 0 de 5", +/− por swipe vertical |
| Segmentado/Chips | estado só por cor de fundo | ✓ + `.isSelected` |
| Anúncios | salvar/excluir silenciosos | `AccessibilityNotification.Announcement("Gasto salvo")` / `announceForAccessibility` |
| Escape | detalhe tem `.accessibilityAction(.escape)` (bom) | Aplicar em todos os sheets via roteador (`ActiveSheet`), com proteção de rascunho |
| Imagens | capas sem descrição | `accessibilityLabel("Foto de capa de \(nome)")`; selos decorativos ocultos |
| Dynamic Type | valor monetário fixo 36; nenhuma captura em fonte grande | UX-09; capturas AX3 obrigatórias para barra, sheets C e cartão de lugar |
| Reduzir Transparência / Aumentar Contraste | não verificado | Fallbacks do §3.6 e §6.4 |
| Ordem de foco no cartão | barra (overlay) + conteúdo; não verificado | Testar com VoiceOver: a barra deve ser lida antes do conteúdo e permanecer alcançável pelo rotor "Cabeçalhos" |

---

## 10. Paridade iOS/Android

- **Nenhuma captura Android foi fornecida:** paridade **NÃO VERIFICADA**. O código Android repete os defeitos estruturais do iOS: sheets `fillMaxHeight(0.9f)`, cartão raio 20 × mapa 30, "+" 44 dp, botão de expandir 40 dp. Ou seja, **as correções P0/P1 valem para as duas plataformas**.
- **Mesma estrutura, material diferente:** barra do cartão, classes de sheet, hierarquia de botões, estados vazios e microcopy devem ser idênticos. Ficam diferentes só os materiais (vidro × tonal), as transições de container (zoom × slide-up, §6.3), a busca (sheet × `SearchView`) e o Predictive Back (exclusivo do Android, desejável).
- **Critério de aceite proposto:** para cada tela, um par iOS/Android lado a lado em 390/393 pt × 844/851, escuro e claro, fonte padrão e AX3/2.0. A ordem dos elementos e os rótulos devem bater 1:1, conferidos por um checklist gerado da árvore de acessibilidade (rótulos idênticos).

---

## 11. Microcopy (pt-BR)

| Onde | Hoje | Proposta | Por quê |
|---|---|---|---|
| Sheet gasto (edição) | Editar registro | Editar gasto | Diz o objeto |
| Botão gasto | Salvar registro | Salvar gasto | Idem |
| Cofrinho | Registrar valor / Meta / Valor reservado | Adicionar reserva · Alterar meta | Uma tarefa por sheet (UX-29) |
| Cofrinho lista | Meta / Meta | Meta da viagem | Sem repetição |
| Marcar no mapa | Ponto marcado: -23.0099, -43.3541 | Ponto marcado no mapa | Sem coordenadas cruas |
| Viagem vazia | Nenhum Pin nesta viagem. | Nenhum lugar nesta viagem ainda. | Sem jargão |
| Compartilhar | Compartilhar Pin | Compartilhar lugar | Idem |
| Nova memória | Sem capa ⌃ | Lugar: Nenhum ▾ | Diz o que escolhe |
| Avaliações | Avaliar este lugar (sem lugar definido) | Avaliar um lugar | Não pressupõe seleção |
| Home vazia | Guarde seu próximo lugar com + | Seu primeiro lugar · Guarde ideias antes mesmo de ter uma viagem. | Instrução por símbolo não é acessível |
| Rodapé | Ver meus lugares | (remover) | Redundante com a barra |
| Rodapé | Ver detalhes | Mostrar tudo | Diferencia de "Detalhes" do lugar |
| Detalhe | Fica só neste aparelho, sem backup. Para ver no mapa, use Marcar no mapa. | Salvo só neste aparelho. | Curto; a ação "No mapa" já está ao lado |
| Chip de viagem (Planejamento) | Viagens | Trilha foda ▾ / Todas as ideias ▾ | Mostra o escopo (UX-03) |
| Detalhe (última página) | Concluído | (remover; fechar por X/gesto) | Sheet de leitura |
| Estrelas | 2 de 5 | Nota 2 de 5 | Lido isoladamente, "2 de 5" parece paginação |

Também não deve haver "Teste" fixo de interface: todos os "Teste" nas capturas são dados de QA. Para as próximas capturas, recomendo usar o conjunto de exemplo do protótipo (Pão de Açúcar etc.) para avaliar truncamento real.

---

## 12. Decisões necessárias do dono

| # | Decisão | Opções | Recomendação |
|---|---|---|---|
| D-1 | Cancelar e título nos sheets | (A) X de vidro no topo esquerdo + título inline 17 (DESIGN-006/AGENTS, M5-EDITOR-UI-02); (B) sem cabeçalho, título grande que rola + "Cancelar" de vidro no fim (DESIGN-007) | **A**: hoje o B produz o "Cancelar" solto/cortado |
| D-2 | Material dos sheets de formulário | Opaco sempre × vidro nos detents parciais (padrão iOS 26) | **Opaco** para formulários; vidro só em leitura |
| D-3 | "+" e primárias em vidro proeminente com tinta azul | O ADR-006 diz "sem transformar azul da marca em material translúcido" | Manter azul **opaco** nas primárias; vidro neutro nas secundárias e chevrons. Testar `glassProminent` só se o ADR for ajustado |
| D-4 | Âncora da expansão do mapa | Mapa cresce para baixo com pílula de resumo × cartão sai pela base | **Para baixo + pílula** (§6.1) |
| D-5 | Passo "Revisar lugar" | Manter × salvar direto | Salvar direto na criação; revisar só alterações |
| D-6 | Endereço aproximado em "Marcar no mapa" | Geocodificação reversa (rede, privacidade) × texto genérico | Texto genérico agora; geocodificação só com ADR |
| D-7 | Onde excluir gasto/reserva/memória | Deslizar na lista × botão no sheet de edição × ambos | Deslizar + menu de contexto; no sheet, texto vermelho abaixo de Salvar |
| D-8 | Ícones de contexto fora da barra | Remover totalmente × manter em outros lugares | Remover da barra (pedido); rever se aparecerem em outro lugar |
| D-9 | Tamanho do título de contexto | 19/24 (DESIGN-007) × 17/22 | **17/22** para caber em 320 pt sem truncar |
| D-10 | Azul de referência | `#4C728A` (ADR-006/código) × `#3372A3` (protótipo) | Registrar um só no ADR-006 antes das próximas capturas |
| D-11 | Moeda | Chips roláveis × menu no símbolo | **Menu** no símbolo ("R$ ▾"): menos uma linha, escala para mais moedas |
| D-12 | "Ver meus lugares" nos contextos | Manter × remover | Remover (a barra cobre) |

---

## 13. Ordem sugerida (encaixe na M5-QA-03)
1. **Slice A+:** roteador único (P0-3) **com classes de sheet** (§4) → UX-02, UX-10, UX-19 saem juntos.
2. **Barra do cartão** (UX-01, UX-03, UX-12, UX-13, UX-23) + "+" contextual (P0-1), com captura em 320 pt, 390 pt e AX3.
3. **Controles** (UX-05 a UX-09, UX-16, UX-17): componentes `ArkeField`, `ArkeSegmented` e chip com ✓, compartilhados.
4. **Mapa expandido + motion** (UX-04, §6) com gravação 60 fps lado a lado.
5. **Estados e microcopy** (UX-14, UX-15, UX-18, §7, §11).

Cada etapa: iOS e Android em paridade, pares claro/escuro, G-01..G-15, testes que conferem **o título do sheet aberto**.

[AGENTS](../../AGENTS.md) · [DESIGN-006](DESIGN-006.md) · [DESIGN-007](DESIGN-007.md) · [M5-QA-03-LISTA](../engenharia/M5-QA-03-LISTA.md) · [ACCESSIBILITY-UI](../standards/ACCESSIBILITY-UI.md) · [ADR-006](../decisoes/ADR-006.md)
