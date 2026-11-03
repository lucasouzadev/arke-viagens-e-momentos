# M5-PARITY-01 — Guia comum e paridade visual

Data: 01/11/2026. Estado: implementação verificada, em revisão no PR #2. M5, branch chore/development-kickoff, PR #2.

Autorização explícita: responsável exige o mesmo guia UI/UX nas duas plataformas, incluindo alça visível nos sheets, Cancelar no topo esquerdo e espaços/textos parecidos. Interromper avanço para novos fluxos até implementar e verificar esta paridade. Isso refina a adaptação nativa anterior; não muda SwiftUI/Compose, mapas, dados ou persistência.

Escopo: guia DESIGN-006, ADR-012 e referências; tokens tipográficos/espaciais equivalentes em pt/dp/sp, home e editor/revisão. Sheets grandes, alça visível, Cancelar à esquerda/título central, campos com rótulo persistente acima e superfícies preenchidas sem contorno, CTA fixo no rodapé com mesma hierarquia. Preservar temas/contraste, acessibilidade, teclado, validação, confirmação, salvamento/exclusão e posição/contexto da home. Nenhum backend, mudança de schema, conta, dependência ou funcionalidade nova.

Arquivos autorizados: views/tema/tokens SwiftUI e Compose, registro Xcode do arquivo de tokens, testes UI existentes e documentos de design/decisão/continuidade/changelog. Scripts existentes de 16 capturas mantidos, adaptáveis somente se verificação exigir. Sem apagar dados ou migrar snapshots.

Aceite: alça em ambos, Cancelar antes do título e alinhado à esquerda nas duas telas de editor/revisão, mesmos rótulos/ordem/grupos/CTA, campos com mínimos 56/96/120 e gaps 8/16/24, margens 20, fontes-base comuns. Capturas padrão/compacto claro/escuro lado a lado; testes UI/storage anteriores aprovados e asserção de posição de Cancelar. Diferenças de métricas de fonte, barras/safe areas e menus do sistema são aceitas, sem justificar mudanças de hierarquia/posição. Fonte ampliada tem prioridade sobre altura fixa.

## Evidências finais

[Native checks](https://github.com/lucasouzadev/arke-viagens-e-momentos/actions/runs/41332236559), fonte `6b17366dc3f8e165b93a12a18f4e602aec154ab0`: iOS build Simulator + 9 testes core + 4 UI; Android build debug + 6 testes core + 6 instrumentados (4 UI/2 repositório); lint 0 erros/8 avisos existentes. Testes preservam validação/revisão/correção/descarte/salvar/reler/excluir, contextos e resize/scroll; asserção nova confirma Cancelar à esquerda do título. Nenhum skip/falha final.

Dezesseis PNGs originais finais inspecionados em pares: home/editor vazio, padrão/compacto e claro/escuro; iPhone 16/SE 3 e Android API 35. Alça, cabeçalho, rótulos, dimensões mínimas dos campos, CTA e preenchimentos conferidos. Corpo rola nas telas menores, sem exigir que os três campos apareçam inteiros ao mesmo tempo. Teclado/digitação iOS e entrada/revisão pelos testes UI padrão. Revisão preenchida é coberta pelos testes, não pelas capturas estáticas. Fontes nativas, cores semânticas e safe areas explicam pequenas diferenças visuais; estrutura comum preservada. Google Maps real segue dependente de projeto/chave externo.

A execução anterior 41331398503 aprovou testes, mas a inspeção detectou sheet Android limitado na âncora externa, colado ao status bar. Corrigido limitando conteúdo e preservando janela/âncora nativas. A primeira tentativa da fonte final falhou antes do app, por timeout de boot do emulador Android; repetição apenas do job Android aprovou, sem mudar código. iOS aprovado na primeira tentativa final. Não apresentar falha de infraestrutura como teste do app.

## Gates — revisão pelo agente implementador

| Gate | Resultado e alcance |
| --- | --- |
| G-01 | PASSOU: direção explícita, ADR-012, tecnologias/mapas/semântica preservados. |
| G-02 | PASSOU: contrato e aceite delimitados, sem fluxo novo. |
| G-03 | PASSOU: somente UI/tema/testes/docs; modelos, repositórios e IO sem alteração. |
| G-04 | PASSOU: diff revisado, papéis tipográficos comuns, helpers de campo/summary, builds/lint. |
| G-05 | PASSOU no recorte: fluxo anterior e Cancelar à esquerda aprovados; pares de imagens conferidos. |
| G-06 | PASSOU para tamanhos/temas capturados e fluxos UI padrão. Fonte 200%, landscape/tablets e IME físico NÃO EXECUTADOS. |
| G-07 | PASSOU na revisão de rótulos, alvos mínimos e alternativas por toque. VoiceOver/TalkBack e auditoria global de contraste NÃO EXECUTADOS. |
| G-08 | PASSOU na revisão do diff sem permissões, dependências ou credenciais novas. Nova auditoria MASVS NÃO APLICÁVEL à mudança de layout. |
| G-09 | NÃO APLICÁVEL a novos dados/finalidades/licenças: nenhum introduzido; limites de privacidade anteriores preservados. |
| G-10 | PASSOU na regressão de storage/draft; schema/IO/snapshots não alterados. |
| G-11 | Profiling NÃO EXECUTADO; risco residual P2 de reflow/coleção grande. |
| G-12 | PASSOU conforme CI e inspeção de 16 PNGs finais, sem ampliar a evidência além dos ambientes descritos. |
| G-13 | PASSOU para builds/capturas de desenvolvimento; sem distribuição/merge. Reversão por commit preservando snapshots. |
| G-14 | PASSOU: guia, contrato, ADR, STATUS/HANDOFF/ROADMAP/INDEX/CHANGELOG e PR reconciliados. |
| G-15 | PASSOU: preferência privada restrita ao chat, não transcrita nos artefatos. |

Nenhum P0/P1 identificado no recorte. Leitores de tela, fonte ampliada, landscape/tablets e profiling permanecem pendentes antes de afirmar cobertura nessas configurações. Aprovação visual humana/merge não presumidos; PR permanece em rascunho. Próximos fluxos devem aplicar DESIGN-006 desde o início e comparar as duas plataformas.
