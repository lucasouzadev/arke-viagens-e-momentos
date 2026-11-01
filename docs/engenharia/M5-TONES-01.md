# M5-TONES-01 — Composição tonal

Data: 01/11/2026. Estado: implementação pronta, validação CI/visual pendente. Branch chore/development-kickoff, PR #2.

Autorização: responsável confirmou tons distintos para compor elementos, após referência visual e diagnóstico de superfícies iguais. Escopo: apenas preenchimento de fundo Android e cartão iOS, DESIGN-001, changelog e continuidade. Sem mudar layout, contornos, sombras, estado, mapas, dados ou dependências.

iOS usa secondarySystemGroupedBackground no cartão sobre systemGroupedBackground. Android usa surfaceContainerLow no fundo e mantém surface no cartão, com elevação zero. A inversão tonal nativa no escuro é aceitável; ambos precisam distinguir cartão e fundo.

Aceite: cartões com preenchimento distinguível, sem contorno, modos claro/escuro e padrão/compacto, textos legíveis. Executar workflow existente (builds, lint, testes e oito capturas) e inspecionar PNGs. Sem novos testes para troca de duas cores semânticas.

G-01/02 PASSOU: escopo aprovado e nativo. G-03/04 PASSOU em revisão do diff restrito a cores. G-05/06/07/12 aguardam CI/PNG; leitores de tela, fonte ampliada e landscape/tablet NÃO EXECUTADOS neste recorte. G-08/09/10 NÃO APLICÁVEIS a novos dados/rede/mídia: não adicionados; referência privada não copiada ao repositório. G-11 profiling NÃO EXECUTADO. G-13 sem distribuição/merge; reversão por commit na branch. G-14 continuidade será atualizada com evidências. G-15 preferência restrita ao chat, ausente dos arquivos. Risco P2 visual, nenhum P0/P1 identificado neste diff.
