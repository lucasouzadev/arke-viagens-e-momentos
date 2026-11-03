# M5-DETAILS-UI-03 — Sheet compacto e informações agrupadas

01/11/2026. M5 verificado e em revisão, branch chore/development-kickoff, PR #2 rascunho. Direção proposta e aceita explicitamente no chat após cinco referências visuais: cabeçalho compacto com controles circulares, nome equilibrado, endereço/nota em uma superfície comum, navegação em pílulas perto do conteúdo e altura acompanhando o conteúdo.

Referências: [DESIGN-006](../design/DESIGN-006.md), [M5-DETAILS-UI-02](M5-DETAILS-UI-02.md), ADR-012. Ambas as plataformas nativas. Escopo autorizado: views de detalhes, métricas locais, testes UI, captura Debug e documentos de continuidade. Editor/home/dados/IO permanecem como estão. Sem fotos, mídia copiada, dependências, conta, rede, localização ou cores novas. Referências são inspiração de composição, não assets do produto.

Aceite: X/i circulares com alvo 48, contador no cabeçalho, nome 22 semibold; endereço e nota agrupados em superfície neutra de raio 24 sem contorno; nota vazia omitida; Anterior/Próximo preenchidos com raio cápsula e limites finitos. Sheet com altura natural até limite da área segura, conteúdo longo rolável e ações alcançáveis; alça e transição inferior suave preservadas. Mesmo conteúdo e ordem nas duas plataformas. Troca de lugar recalcula altura e reinicia scroll. Informação por toque e retorno à home preservados.

Provas planejadas: builds/lint/testes nativos de regressão, teste de sheet curto e conteúdo longo, 32 PNGs reais (incluindo detalhes sem nota), padrão/compacto e claro/escuro, inspeção em pares. Sem SDK local, executar no Actions. Fonte 200%, leitores de tela, landscape/tablets e profiling ainda não executados. Risco P2: sizing dinâmico e overflow, mitigados por limite/scroll/testes e capturas; nenhum dado novo, sem migração, rollback somente de apresentação. Sem merge/release. Canário confirmado apenas no chat, sem transcrição em arquivos.

Texto público candidato: “Reorganizamos os detalhes dos lugares em um painel mais compacto, com informações agrupadas e botões arredondados.” Em desenvolvimento, sem versão lançada.

Gates G-01..G-15 e evidências finais abaixo; nenhum teste pendente é considerado aprovado. Próximo handoff: revisar resultado visual humano antes de avançar para edição segura de ideias salvas.

Primeira execução 41359181438: Android aprovado; iOS compilou e 5/6 testes UI passaram, mas a asserção de redução de altura falhou. Troca da leitura de preferência por observação direta de geometria e asserção tipada de frame; mantido o critério de encolhimento, sem skip. Adicionadas capturas do segundo lugar sem nota e asserção equivalente Android. Limite Android passa a usar dimensões da janela nativa, resolvendo o aviso de Configuration.screenHeightDp observado no primeiro lint. Nova execução necessária.

## Evidências finais

[Native checks aprovados](https://github.com/lucasouzadev/arke-viagens-e-momentos/actions/runs/41360045740), fonte `493bb7063d22cc1dc26537dd284045eae3f9897c`. iOS: 9 testes core + 6 XCUITest; Android: 6 core + 8 instrumentados (6 UI/2 repositório), todos aprovados sem skips. Build Simulator/debug e lint Android aprovados: 0 erros/9 avisos anteriores, incluindo o título place_details sem uso (P3); nenhum aviso novo ou supressão. A recomendação sobre dimensões Android foi resolvida usando LocalWindowInfo/LocalDensity.

Após aprovação dos oito testes Android, a primeira tentativa de captura foi rejeitada pelo script: PNG original revelou o diálogo “Pixel Launcher isn't responding”, cobrindo o Arke. Job Android repetido isoladamente na mesma fonte, sem alteração/skip/ocultação de diálogo; oito testes e todas as capturas passaram na repetição. iOS aprovado na primeira execução da fonte corrigida. A falha da fonte anterior permanece documentada acima.

32 PNGs originais inspecionados individualmente e comparados por plataforma: home/editor vazio/detalhe com nota/detalhe sem nota × padrão/compacto × claro/escuro. iPhone 16/SE 3, Android API 35 em Pixel 6 e viewport compacta. Cabeçalho circular, contador, nome 22, superfície agrupada sem contorno, cápsulas e tons equivalentes confirmados. Sheet sem nota menor nos dois sistemas; safe areas/altura e scroll preservados. Diferenças nativas de fonte/ícone/insets/estado desabilitado permanecem. Nenhuma mídia das referências foi incorporada. Capturas são exemplos sintéticos somente Debug/memória, sem escrever dados. ZIP local com 32 PNGs, CRC verificado; Actions conserva somente PNG por 7 dias.

Testes verificam mudança de altura ao navegar para o lugar sem nota nas duas plataformas, nota longa com scroll real e fechar/navegar alcançáveis. Regressões de informação por toque, navegação finita, seleção/restauração Android, salvar/reabrir/excluir e contexto/painel continuam aprovadas. Longo exercitado nos testes padrão, sem PNG estático específico; informação também exercitada sem captura estática. Fonte 200%, VoiceOver/TalkBack, landscape/tablets, aparelho físico e profiling não executados. Dados/modelos/IO/schema e editor inalterados; os PNGs de editor/home verificam regressão de apresentação.

## Gates — revisão do agente implementador

| Gate | Resultado e alcance |
| --- | --- |
| G-01 | PASSOU: direção visual explicitamente aceita, dois cartões/stack/azul preservados. |
| G-02 | PASSOU: contrato delimitado antes das mudanças. |
| G-03 | PASSOU: somente apresentação, observação de geometria, testes/capturas e docs; dados/IO intactos. |
| G-04 | PASSOU: diff, links, sintaxe bash e builds/lint sem erros; nove avisos anteriores registrados. |
| G-05 | PASSOU: fluxos e comparação dos 32 PNGs; painel compacto/agrupamento/ações em paridade. |
| G-06 | PASSOU nos tamanhos/temas e cenários de texto curto/longo descritos; fonte 200%, landscape/tablets/aparelho físico NÃO EXECUTADOS. |
| G-07 | PASSOU na revisão de alvos 48, labels acessíveis/contador e alternativas por toque; leitores de tela e auditoria global de contraste NÃO EXECUTADOS. |
| G-08 | PASSOU: nenhuma rede/permissão/credencial/dependência/asset novo; captura Debug sintética. Auditoria MASVS nova NÃO APLICÁVEL à apresentação sem nova fronteira de dados. |
| G-09 | NÃO APLICÁVEL a nova coleta/mídia/licença: nenhuma introduzida. |
| G-10 | PASSOU: regressões de persistência/draft, sem alterar dados nem snapshots. |
| G-11 | Profiling NÃO EXECUTADO; observação de geometria atualiza apenas quando a altura muda mais de 1 pt, sem promessa de desempenho medido. |
| G-12 | PASSOU: 29 testes, builds e 32 PNGs; falhas anteriores/correção e repetição de infraestrutura registradas. |
| G-13 | PASSOU: branch/PR rascunho, sem merge/release; rollback de apresentação preserva snapshots. |
| G-14 | PASSOU: guia/contrato/STATUS/HANDOFF/ROADMAP/INDEX/CHANGELOG/PR reconciliados. |
| G-15 | PASSOU: preferência privada restrita ao chat e ausente dos artefatos. |

Risco residual P2 nos ambientes não exercitados e P3 dos avisos anteriores; nenhum P0/P1 identificado neste recorte. Revisão visual humana final pendente; PR #2 continua rascunho. Próximo: colher revisão deste resultado; edição segura de ideia salva exige contrato próprio. Sem mudança de dados em eventual reversão.
