# M5-UI-REDESIGN-03 — Marcar no mapa

5 de novembro de 2026. **Status: aplicado na branch `ui/m5-ui-redesign-03` (empilhada sobre a 02), CI ainda NÃO executado.** Contrato guarda-chuva: [M5-UI-REDESIGN-PLANO](M5-UI-REDESIGN-PLANO.md); especificação: [DESIGN-007](../design/DESIGN-007.md), seção “Marcar no mapa”. Plataformas: iOS e Android. Sem PR, merge ou release.

## Objetivo

Fazer do mapa o conteúdo da tela de marcar ponto, com a ponta do pino sobre o ponto realmente salvo e uma confirmação que nunca fica apagada sem explicação.

## Dentro do escopo

| Defeito observado | Mudança |
| --- | --- |
| Mapa de 230 pt dentro de um `Form` rolável, disputando o gesto com a rolagem | iOS: mapa em tela cheia, sem formulário em volta. Android: diálogo em tela cheia com o mapa no fundo |
| Pino `mappin` centralizado: a ponta ficava ~14 pt abaixo do ponto salvo | iOS: pontinho no centro exato do mapa (o ponto salvo) com o pino acima dele. Android: o marcador já é colocado no ponto tocado |
| “Confirmar ponto e salvar Pin” como última linha, fora da dobra, com cara de desabilitado e desabilitado sem explicação | Painel inferior fixo com o lugar, o ponto, a viagem opcional e **Confirmar ponto**. Quando não pode confirmar, uma dica diz o que falta (mover o mapa, abrir Pins, nomear a viagem) |
| Nome da ideia repetido; “Buscar endereço” quebrando em duas linhas; título grande colidindo com Cancelar | Cabeçalho único: X, título central, localização; busca em pílula sobre o mapa |
| Vínculo com viagem em `Picker` e `Toggle` | Chips: Sem viagem, viagens existentes, Nova viagem (com campo de nome) |
| Alerta de localização inexistente neste editor (iOS) | Alerta “Minha localização” igual ao da Home |
| Busca de endereço sem indicador de carregamento | iOS: “Buscando…” e mensagem de nenhum resultado mais útil. Android: botão mostra “Buscando…” |

Preservados: persistência (`savePin`, `LocalPinTripChoice`), recarga de Pin existente, busca automática por endereço quando há um único resultado, `interactiveDismissDisabled(busy)`, dados e esquema JSON v1.

## Fora do escopo

Endereço aproximado do ponto por geocodificação reversa (hoje mostra coordenadas), arrastar o pino, Google Maps real no Android (depende da chave, ADR-010), teste automatizado do fluxo.

## Arquivos

iOS `ios/Arke/HomeView.swift` (`PinEditor`, `MapPlaceSearch`); Android `android/app/src/main/kotlin/dev/arke/app/TravelMap.kt` (`PinEditor`, novo `PinChip`). Documentos: este contrato, STATUS, HANDOFF, ROADMAP, INDEX.

## Aceite (todos NÃO EXECUTADOS)

- [ ] Compila e os testes existentes passam nas duas plataformas.
- [ ] O ponto salvo é o ponto sob a marca: iOS, pontinho central; Android, marcador no toque. **Sem teste automatizado**; verificar manualmente com coordenada simulada.
- [ ] Sem ponto, “Confirmar ponto” aparece apagado **com a dica visível**; com ponto, habilita. Com “Nova viagem” e nome vazio, a dica pede o nome.
- [ ] Confirmar só chama `onSaved` após persistir; toque duplo não duplica (guarda `busy`).
- [ ] Cancelar não altera Pins salvos.
- [ ] Fluxo sem permissão de localização funciona (alerta e busca por endereço).
- [ ] Capturas padrão/compacto × claro/escuro: **não há captura automática desta tela**; inspeção manual necessária.

## Gates

G-01 revisado no diff. G-02 este contrato. G-04 revisão estática. G-05 a G-07, G-11 e G-12 NÃO EXECUTADOS (o CI só compila; nenhum teste de UI passa por esta tela). G-08 e G-09: sem rede nova; localização continua sob toque e em primeiro plano. G-10: grava o mesmo Pin local de antes. G-13 não aplicável. G-14 documentos atualizados. G-15 nome ausente dos arquivos.

## Riscos

- **P2 — coordenadas erradas gravadas:** no iOS o pontinho assume que o centro do `Map` é o centro da tela inteira (mapa com `ignoresSafeArea`, painel como sobreposição). Se o painel ou a área segura mudarem o enquadramento, o ponto desloca. Mitigação: conferir em captura com coordenada simulada antes de aprovar.
- **P2 — compilação e layout às cegas:** `MapLocationButtonStyle` (vidro) usado em botões novos; `Dialog` em tela cheia no Android com `decorFitsSystemWindows = false` (insets aplicados à mão).
- **P3 — Android sem mapa no CI:** `MAPS_CONFIGURED` é falso; o ramo do mapa só compila.
- Recuperação: descartar a branch; 01 e 02 ficam intactas.

## Próximo handoff

CI nativo na branch; escrever um teste de UI que marque um ponto com localização simulada e confira o Pin salvo; só então abrir o recorte 04 (Gastos, Cofrinho, Avaliações e Memórias).
