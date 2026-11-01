# M5-PARITY-01 — Guia comum e paridade visual

Data: 01/11/2026. Estado: em andamento. M5, branch chore/development-kickoff, PR #2.

Autorização explícita: responsável exige o mesmo guia UI/UX nas duas plataformas, incluindo alça visível nos sheets, Cancelar no topo esquerdo e espaços/textos parecidos. Interromper avanço para novos fluxos até implementar e verificar esta paridade. Isso refina a adaptação nativa anterior; não muda SwiftUI/Compose, mapas, dados ou persistência.

Escopo: guia DESIGN-006, ADR-012 e referências; tokens tipográficos/espaciais equivalentes em pt/dp/sp, home e editor/revisão. Sheets grandes, alça visível, Cancelar à esquerda/título central, campos com rótulo persistente acima e superfícies preenchidas sem contorno, CTA fixo no rodapé com mesma hierarquia. Preservar temas/contraste, acessibilidade, teclado, validação, confirmação, salvamento/exclusão e posição/contexto da home. Nenhum backend, mudança de schema, conta, dependência ou funcionalidade nova.

Arquivos autorizados: views/tema/tokens SwiftUI e Compose, registro Xcode do arquivo de tokens, testes UI existentes e documentos de design/decisão/continuidade/changelog. Scripts existentes de 16 capturas mantidos, adaptáveis somente se verificação exigir. Sem apagar dados ou migrar snapshots.

Aceite: alça em ambos, Cancelar antes do título e alinhado à esquerda nas duas telas de editor/revisão, mesmos rótulos/ordem/grupos/CTA, campos com mínimos 56/96/120 e gaps 8/16/24, margens 20, fontes-base comuns. Capturas padrão/compacto claro/escuro lado a lado; testes UI/storage anteriores aprovados e asserção de posição de Cancelar. Diferenças de métricas de fonte, barras/safe areas e menus do sistema são aceitas, sem justificar mudanças de hierarquia/posição. Fonte ampliada tem prioridade sobre altura fixa.

G-01/02 PASSOU por direção explícita e contrato; G-03/04/05/06/07/12 aguardam diff/CI/PNG. G-08/09/10 NÃO APLICÁVEIS a novos dados/rede/armazenamento: nada alterado, testes anteriores continuam como regressão. G-11 profiling NÃO EXECUTADO. G-13 build/captura sem distribuição; reversão por commit preservando snapshots. G-14 atualizar ao concluir; G-15 preferência restrita ao chat, ausente de artefatos. Leitores de tela, fonte 200%, landscape/tablets e profiling NÃO EXECUTADOS. Risco P2 de reflow/teclado, sem P0/P1 identificado no recorte.
