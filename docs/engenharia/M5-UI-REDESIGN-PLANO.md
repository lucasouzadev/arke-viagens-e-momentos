# M5-UI-REDESIGN — Contrato de implementação

5 de novembro de 2026. **Status: aprovado em direção (protótipo aprovado no chat); implementação em recortes, só o recorte 1 aplicado.** Contrato guarda-chuva das tarefas M5-UI-REDESIGN-01 a 06. Cada recorte exige confirmação explícita do responsável antes de começar e termina em CI nativo, capturas inspecionadas e documentos atualizados. Sem merge, sem release, sem nova beta. Especificação visual: [DESIGN-007](../design/DESIGN-007.md).

## Regras comuns a todos os recortes

- **Branch:** uma por recorte, a partir da main atualizada (`ui/m5-ui-redesign-0N`). PR em rascunho só quando o responsável pedir. Nunca merge automático.
- **Paridade:** iOS e Android no mesmo recorte; diferença só de material (vidro × flat), nunca de organização. Se um lado ficar para depois, registrar no STATUS.
- **Dados:** JSON v1, persistência local e identificadores existentes preservados. Nenhuma migração, rede nova ou dependência sem contrato próprio.
- **Acessibilidade:** todo gesto novo ou alterado tem botão equivalente e ação para leitor de tela; contraste ≥ 4,5:1 (3:1 para componentes gráficos); alvos ≥ 48 pt/dp; sem rótulo de navegação truncado.
- **Identificadores de teste estáveis:** manter `home.contextTitle`, `home.panelMenu`, `home.addPlace`, `home.expandMap`, `place.*`, `detail.*`; novos seguem o padrão `tela.elemento`.
- **Teste obrigatório por recorte:** unitários existentes + testes de UI/instrumentados atualizados para os fluxos tocados, capturas padrão/compacto × claro/escuro (iOS e Android), inspeção dos PNGs em pares. **Nunca relatar como aprovado o que o CI não executou.**
- **Gates:** matriz G-01..G-15 no contrato de cada recorte; NÃO EXECUTADO não é PASSOU; G-15 sem gravar o nome em arquivo.
- **Fora de todos os recortes:** merge, assinatura, TestFlight/lojas, Google Cloud, backend novo.

## Decisões pendentes do responsável

| # | Decisão | Bloqueia | Proposta |
| --- | --- | --- | --- |
| D1 | Tinta de texto no escuro `#88AABE` (variação de luminosidade do azul candidato) | Aceite do recorte 1 | Aprovar; HEX final do azul continua no ADR-006 |
| D2 | Remover o bloco “Arke / Onde vamos?” da Home | Recorte 6 | Remover; marca no ícone e na busca |
| D3 | Alturas mínimas dos campos (68/68/104/68 no lugar de 56/96/120) | Recorte 2 | Aprovar; DESIGN-007 substitui a tabela do DESIGN-006 |
| D4 | Câmera inicial do mapa sem permissão de localização | Recorte 6 | Enquadrar os Pins salvos; sem Pins, última região; sem nada, Brasil. Nunca pedir permissão sozinho |
| D5 | Forma de autenticação para compartilhar (conta Arke, método) | Recorte 5 | Seguir GROUP-001; até decidir, manter o fluxo atual atrás de “Compartilhar”, sem login de teste na interface |
| D6 | Ícone por contexto no seletor (iOS SF Symbols; Android novos vetores) | Recorte 6 | Aprovar o conjunto: pin, receipt, target, star, photo |

## Recortes

### M5-UI-REDESIGN-01 — contraste, seletor e mapa expandido
**Aplicado** na branch `ui/m5-ui-redesign-01` (commits 8d3ef6f e 5c0040d), **sem build nem teste**. Contrato próprio em [M5-UI-REDESIGN-01](M5-UI-REDESIGN-01.md). Falta: CI nativo, capturas e decisão D1.

### M5-UI-REDESIGN-02 — editor de lugar
**Objetivo:** adicionar, editar e revisar lugar conforme DESIGN-007.
**Dentro:** campos com alturas D3, placeholders, marcação Obrigatório/Opcional, um tipo de campo (corrigir recuo diferente entre `TextField` e `TextEditor`), alinhamento do texto com o rótulo, fade superior também na meia altura (`ArkeScrollEdges` hoje só liga no detent grande), remoção da faixa retangular atrás do CTA, campo ativo visível acima do teclado (hoje `scrollTo(anchor: .top)` o leva para baixo do cabeçalho), linha de foto de capa como botão com ações separadas de 48 e “Remover” em perigo, revisão compacta, texto do “i” curto.
**Arquivos:** iOS `PlaceEditor.swift`, `ArkeUI.swift`; Android `PlaceEditor.kt`, `ArkeUI.kt`; `strings.xml`; DESIGN-006 (nota de substituição).
**Aceite:** toque em qualquer ponto do campo foca e abre o teclado; nota e endereço longos com teclado aberto rolam até a última linha e até Revisar sem encobrir; rascunho alterado protegido em X, gesto e Voltar; revisão sem blocos vazios; testes de UI que usam `place.fields`, `place.review`, `place.save` passam.
**Risco:** P2 em foco/teclado (regressão de escrita). **Depende de:** D3.

### M5-UI-REDESIGN-03 — Marcar no mapa
**Objetivo:** mapa como conteúdo, confirmação clara.
**Dentro (iOS):** sair do `Form` (mapa dentro de rolagem disputa gesto); alvo central com a ponta do pino no ponto salvo (hoje o símbolo `mappin` é centralizado e a ponta fica ~14 pt abaixo); painel inferior com lugar, ponto, chips de vínculo, **Confirmar ponto** fixo e dica quando falta ponto; busca de endereço com indicador de carregamento e estado “nenhum resultado”; aviso ao buscar em outra região; “Minha localização” sob toque. **Android:** `PinEditor` hoje é diálogo; migrar para o mesmo sheet em tela cheia.
**Arquivos:** iOS `HomeView.swift` (PinEditor, MapPlaceSearch) — extrair para arquivo próprio; Android `TravelMap.kt`, `ArkeHome.kt`.
**Aceite:** ponto salvo igual ao ponto sob a ponta (teste com coordenada simulada); confirmar só após persistir, sem duplicar em toque duplo; cancelar não altera Pins salvos; fluxo sem permissão de localização funciona.
**Risco:** P2 (coordenadas erradas gravadas). Google Maps real no Android continua dependendo de chave (ADR-010).

### M5-UI-REDESIGN-04 — Gastos, Cofrinho, Avaliações, Memórias
**Objetivo:** tirar os formulários crus (controles soltos, sem rótulo, sem forma de botão).
**Dentro:** chip de viagem, totais por moeda em grade, registros em linhas, “Adicionar gasto” como sheet (valor grande, moeda em chips, descrição, tipo segmentado, data), Cofrinho com barra e aviso informativo, Avaliações com estrelas + nota em texto e ação “Avaliar este lugar”, Memórias em grade de selos; “Escolha o tipo” deixa de ser opção real; moedas além das cinco fixas, com formato por moeda e localidade (sem “BRL 95”); estados vazios com ação; fade na base.
**Arquivos:** extrair `LocalFinanceView`, `TripJournalView` do `HomeView.swift` para arquivos próprios (iOS) e `LocalFinanceContent`/`TripJournalContent` do `ArkeHome.kt` (Android); `ios/Sources/ArkeCore` para formatação de moeda testável.
**Aceite:** testes unitários de formatação (BRL, USD, JPY sem casas, KWD com três); edição preserva ID e ordem; totais separados por moeda e estado; Cofrinho nunca sugere carteira ou depósito; chaves `arke.local-finance.v1` e `arke.trip-journal.v1` preservadas.
**Risco:** P2 em dinheiro (arredondamento, moeda misturada).

### M5-UI-REDESIGN-05 — Viagens e compartilhamento
**Objetivo:** Viagens e Compartilhar conforme DESIGN-007, sem login de teste.
**Dentro:** sheet Viagens (selo, resumo, participantes, ativa, “Fora de viagens”), chrome comum de sheets, Compartilhar viagem (convite, papéis, participantes), estados “entrar para compartilhar” e sem conexão. **Segurança:** retirar do código das duas plataformas a URL e a chave publishable do Supabase embutidas e o formulário e-mail/senha de teste, conforme D5; revisar RLS e auditar o que o app envia (nome e coordenadas apenas, com confirmação).
**Arquivos:** iOS `HomeView.swift` (SharePinForm, Viagens), Android `ArkeHome.kt` (SharePinDialog), configuração de ambiente; threat model.
**Aceite:** sem credencial ou chave no código-fonte; compartilhar confirma o que será enviado; falha de rede preserva o rascunho. **Depende de:** D5 e GROUP-001. **Risco:** P1 (autorização e vazamento entre viagens).

### M5-UI-REDESIGN-06 — Home map-first
**Objetivo:** layout da Home e do mapa conforme DESIGN-007.
**Dentro:** D2 (sem título fixo), controles do mapa (busca em pílula, localização e expandir à direita; Android com os mesmos controles), atribuição sempre visível em qualquer altura, rótulo do pino selecionado, câmera inicial D4, ícones por contexto D6, carrossel com contador e anterior/próximo, faixa compacta no Explorar, barra de resumo no mapa expandido, estados vazio/carregando/sem localização/offline, mapa sem recomposição por quadro durante o arraste (altura do `Map` hoje muda a cada quadro).
**Arquivos:** iOS `HomeView.swift` (extrair `HomeMap`, `ContextHeader`, `PlacesCarousel`), Android `ArkeHome.kt`, `TravelMap.kt`.
**Aceite:** arraste lento, rápido e reverso sem piscar nem saltar (gravação e quadros intermediários); mapa não muda de enquadramento ao mudar o painel; atribuição visível nos três detents; todos os fluxos anteriores intactos.
**Risco:** P2 (gesto e câmera). **Depende de:** D2, D4, D6 e dos recortes 02–05 estáveis.

## Ordem e dependências

01 (CI + D1) → 02 (D3) → 03 → 04 → 05 (D5) → 06 (D2, D4, D6). Os recortes 02, 03 e 04 não dependem entre si e podem inverter de ordem; 06 vem por último porque reorganiza o esqueleto que os demais usam.

## Verificação por recorte (modelo)

1. Contrato do recorte atualizado (objetivo, arquivos, aceite, riscos, matriz G-01..G-15).
2. Build e testes iOS e Android no CI nativo na branch; lint Android sem erros.
3. Capturas padrão/compacto × claro/escuro nas duas plataformas, inspecionadas em pares, mais gravação para gesto e teclado.
4. Itens não cobertos ficam explícitos: VoiceOver/TalkBack, fonte 200%, landscape, tablets, aparelho físico, profiling.
5. STATUS, HANDOFF, ROADMAP e CHANGELOG (texto leigo, só quando houver release) atualizados.

## Impacto público

Sim, cumulativo: leitura, navegação, formulários e fluxo de mapa. CHANGELOG só quando houver release.

[AGENTS](../../AGENTS.md) · [DESIGN-007](../design/DESIGN-007.md) · [QUALITY-GATES](../agents/QUALITY-GATES.md) · [QA-BETA-01](../qualidade/QA-BETA-01.md)
