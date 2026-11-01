# M5-DETAILS-01 — Abrir e percorrer ideias salvas

01/11/2026. M5, verificado e em revisão; branch chore/development-kickoff, PR #2. Continuidade autorizada explicitamente no chat após aceite do fade. Referências: ADR-011/012, DESIGN-002/006, M5-PLACES-01 e M5-FADE-01.

Recorte: abrir detalhes de uma ideia da coleção local, ler nome/endereço/nota completos, navegar por Anterior/Próximo com contador e limites finitos, fechar retornando à mesma home/contexto/proporção. Seleção por ID estável, sem salvar preferência no disco ou editar os dados. Coleção de um item desabilita as duas direções; identidade ausente mostra indisponibilidade em vez de abrir outro lugar. Esta etapa entrega navegação por toque; carrossel gestual/sincronização com mapa ficam para contrato próprio.

UI nas duas plataformas: sheet grande com alça, Fechar no topo esquerdo (leitura sem rascunho, portanto não Cancelar), título central, labels persistentes, superfícies sem contorno, corpo rolável com fade inferior 32 e espaço final 48; contador e navegação fixos. Mudança de lugar reinicia o scroll do detalhe, não a home. Endereço é texto já salvo, sem pesquisa/Pin/GPS/rota/viagem. Nenhum dado/dependência/permissão/IO/schema novo, nenhuma exclusão automática. Edição posterior de ideia fica para recorte próprio.

Arquivos autorizados: views de coleção/detalhe e entrada Debug de captura, testes UI, scripts existentes de screenshot, strings e docs de continuidade/guia/changelog. Captura Debug usa duas ideias sintéticas apenas em memória; não grava, apaga ou substitui snapshots reais. Desabilitada em Release. Verificação: fluxo real salvar → abrir → fechar, navegação finita Debug e retorno à home, regressões anteriores, 24 PNGs (home/editor/detalhes × padrão/compacto × claro/escuro × duas plataformas). Sem imagens simuladas/editadas ou mídia externa.

## Evidências finais

[Native checks aprovados](https://github.com/lucasouzadev/arke-viagens-e-momentos/actions/runs/41344099050), fonte `8858f3f8792ce0c0189991644aad5adede8762b6`. iOS: build Simulator, 9 testes core e 5 XCUITest. Android: build debug, 6 testes core e 7 instrumentados (5 UI e 2 repositório); lint 0 erros/8 avisos existentes. Nenhum skip/falha final. Rodada anterior substituída para corrigir contraste/área de toque de Ver detalhes antes da validação final; não usar execução cancelada como evidência.

Testes novos: salvar ideia real pelo editor e abrir os mesmos nome/endereço nos detalhes; coleção de um item bloqueia as duas direções. Dois exemplos Debug verificam contador, anterior desabilitado no início, próximo desabilitado no fim, nota vazia, retorno ao primeiro e Fechar para home/contexto/proporção. Android recria a Activity com o segundo lugar selecionado e mantém a seleção. Testes anteriores preservam validação/revisão/cancelamento/salvar/reler/excluir, armazenamento e resize/scroll. Seleção ausente é tratada na view sem escolher silenciosamente outro ID; não é cenário UI automatizado desta rodada.

Vinte e quatro PNGs originais gerados e inspecionados: home/editor vazio/detalhes, padrão/compacto e claro/escuro, iPhone 16/SE 3 e Android API 35. Pares de detalhes conferidos quanto a alça/Fechar à esquerda/título central, rótulos, conteúdo rolável, fade e navegação fixa. Textos maiores rolam nas telas menores; detalhes começam no topo ao trocar lugar. Capturas de detalhes usam exemplos sintéticos somente em memória, por entradas Debug explícitas; não semeiam nem limpam snapshots reais. BuildConfig.DEBUG e #if DEBUG restringem o caminho de captura; nenhuma credencial/dado real. ZIP local com 24 PNGs e CRC conferido; retenção Actions continua 7 dias e somente PNG, sem APK/IPA.

## Gates — revisão do agente implementador

| Gate | Resultado e alcance |
| --- | --- |
| G-01 | PASSOU: continuidade explícita, guia comum e stack nativa preservados. Navegação por toque não é carrossel/mapa final. |
| G-02 | PASSOU: contrato, aceite e fronteiras definidos. |
| G-03 | PASSOU: views/estado de apresentação; modelos/IO/schema não alterados. |
| G-04 | PASSOU: diff, XML/links/scripts, build/lint e identidade estável revisados. |
| G-05 | PASSOU para abertura de ideia salva, navegação finita e retorno; testes e pares de imagens. |
| G-06 | PASSOU para viewports/temas e recriação Android exercitados. Fonte 200%, landscape/tablets e aparelho físico NÃO EXECUTADOS. |
| G-07 | PASSOU na revisão de rótulos, alvos mínimos e navegação por toque. VoiceOver/TalkBack e auditoria global de contraste NÃO EXECUTADOS. |
| G-08 | PASSOU no diff: sem novas permissões/rede/dependências, captura Debug isolada em memória. Nova auditoria MASVS NÃO APLICÁVEL ao recorte sem mudança de fronteira de dados. |
| G-09 | NÃO APLICÁVEL a novas finalidades/dados/licenças: nenhum introduzido. |
| G-10 | PASSOU na regressão de armazenamento/draft; seleção de consulta não escreve no disco. |
| G-11 | Profiling NÃO EXECUTADO; coleção grande permanece risco P2. |
| G-12 | PASSOU conforme CI e 24 PNGs, sem extrapolar a matriz executada. |
| G-13 | PASSOU para desenvolvimento/PNG; sem merge/distribuição. Reverter código preservando snapshots. |
| G-14 | PASSOU: contrato/guia/índice/STATUS/HANDOFF/ROADMAP/CHANGELOG/PR reconciliados. |
| G-15 | PASSOU: preferência privada somente nas respostas de chat, não transcrita nos artefatos. |

Nenhum P0/P1 identificado neste recorte. Configurações não exercitadas e avaliação humana permanecem pendentes; PR em rascunho, sem presumir aceite visual final. Próximo recorte: editar uma ideia salva com revisão/cancelamento e gravação segura sob contrato próprio. Associação ao mapa/geocoding/carrossel gestual continuam futuros, sem Google Cloud/chave/faturamento automático.
