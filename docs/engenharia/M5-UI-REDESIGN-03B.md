# M5-UI-REDESIGN-03B — Sheet contínuo em todas as telas

5 de novembro de 2026. Recorte do [plano M5-UI-REDESIGN](M5-UI-REDESIGN-PLANO.md). Decisão do responsável no chat (vídeo de referência + "Serve; Concordo; Em todos" + "Aprovado" ao protótipo): substituir alça, X/título/“i” e barra de ação fixa por um **sheet contínuo**, descrito em [DESIGN-007](../design/DESIGN-007.md#sheets).

## Escopo
- iOS e Android: editor/revisão de lugar, detalhe da ideia, e demais sheets existentes no código (Pin editor mantém o mapa, só alinha o modelo de fechamento).
- Título que rola com o conteúdo; ação principal e secundária flutuantes; sem alça e sem X.
- Fechar por arrastar para baixo; com rascunho, confirmação "Descartar rascunho?"; Back (Android) e escape (VoiceOver) equivalentes; "Cancelar" não-gestual no fim do formulário.
- Fora do escopo: dados, persistência, Pin/geocoding, rede, contas. Telas ainda inexistentes no código (Gastos, Compartilhar, Busca, Estados) seguem o modelo quando forem criadas (recortes 04–05).

## Aceite
1. Nenhum sheet exibe alça, X, "i" ou faixa fixa de ação; a última linha de conteúdo alcança o respiro acima do botão flutuante.
2. Arrastar para baixo fecha; com rascunho abre a confirmação; Back/escape idem.
3. Teclado: campo ativo visível, botão acima do teclado.
4. Testes de interface existentes ajustados e verdes nas duas plataformas; novas verificações dos identificadores `sheet.discard*`.
5. Capturas iOS/Android inspecionadas.
6. Matriz G-01..G-15 no PR; sem merge.

## Riscos
Perda acidental de rascunho (mitigada pela confirmação); gestos conflitantes com o scroll do conteúdo (arrastar só fecha com scroll no topo, comportamento nativo do sheet); botão flutuante cobrindo a última linha (respiro inferior fixo).

## Evidência
Pendente: nenhuma build deste recorte foi executada ainda.
