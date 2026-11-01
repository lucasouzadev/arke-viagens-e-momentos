# M5-DETAILS-UI-03 — Sheet compacto e informações agrupadas

01/11/2026. M5 em andamento, branch chore/development-kickoff, PR #2 rascunho. Direção proposta e aceita explicitamente no chat após cinco referências visuais: cabeçalho compacto com controles circulares, nome equilibrado, endereço/nota em uma superfície comum, navegação em pílulas perto do conteúdo e altura acompanhando o conteúdo.

Referências: [DESIGN-006](../design/DESIGN-006.md), [M5-DETAILS-UI-02](M5-DETAILS-UI-02.md), ADR-012. Ambas as plataformas nativas. Escopo autorizado: views de detalhes, métricas locais, testes UI, captura Debug e documentos de continuidade. Editor/home/dados/IO permanecem como estão. Sem fotos, mídia copiada, dependências, conta, rede, localização ou cores novas. Referências são inspiração de composição, não assets do produto.

Aceite: X/i circulares com alvo 48, contador no cabeçalho, nome 22 semibold; endereço e nota agrupados em superfície neutra de raio 24 sem contorno; nota vazia omitida; Anterior/Próximo preenchidos com raio cápsula e limites finitos. Sheet com altura natural até limite da área segura, conteúdo longo rolável e ações alcançáveis; alça e transição inferior suave preservadas. Mesmo conteúdo e ordem nas duas plataformas. Troca de lugar recalcula altura e reinicia scroll. Informação por toque e retorno à home preservados.

Provas planejadas: builds/lint/testes nativos de regressão, teste de sheet curto e conteúdo longo, 32 PNGs reais (incluindo detalhes sem nota), padrão/compacto e claro/escuro, inspeção em pares. Sem SDK local, executar no Actions. Fonte 200%, leitores de tela, landscape/tablets e profiling ainda não executados. Risco P2: sizing dinâmico e overflow, mitigados por limite/scroll/testes e capturas; nenhum dado novo, sem migração, rollback somente de apresentação. Sem merge/release. Canário confirmado apenas no chat, sem transcrição em arquivos.

Texto público candidato: “Reorganizamos os detalhes dos lugares em um painel mais compacto, com informações agrupadas e botões arredondados.” Em desenvolvimento, sem versão lançada.

Gates G-01..G-15 e evidências finais serão registrados após execução; nenhum teste pendente é considerado aprovado. Próximo handoff: revisar resultado visual humano antes de avançar para edição segura de ideias salvas.

Primeira execução 41359181438: Android aprovado; iOS compilou e 5/6 testes UI passaram, mas a asserção de redução de altura falhou. Troca da leitura de preferência por observação direta de geometria e asserção tipada de frame; mantido o critério de encolhimento, sem skip. Adicionadas capturas do segundo lugar sem nota e asserção equivalente Android. Limite Android passa a usar dimensões da janela nativa, resolvendo o aviso de Configuration.screenHeightDp observado no primeiro lint. Nova execução necessária.
