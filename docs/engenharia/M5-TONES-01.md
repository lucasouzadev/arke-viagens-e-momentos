# M5-TONES-01 — Composição tonal

Data: 01/11/2026. Estado: implementado e verificado no PR #2, sem merge. Branch chore/development-kickoff, PR #2.

Autorização: responsável confirmou tons distintos para compor elementos, após referência visual e diagnóstico de superfícies iguais. Escopo: apenas preenchimento de fundo Android e cartão iOS, DESIGN-001, changelog e continuidade. Sem mudar layout, contornos, sombras, estado, mapas, dados ou dependências.

iOS usa secondarySystemGroupedBackground no cartão sobre systemGroupedBackground. Android usa surfaceContainerLow no fundo e mantém surface no cartão, com elevação zero. A inversão tonal nativa no escuro é aceitável; ambos precisam distinguir cartão e fundo.

Aceite: cartões com preenchimento distinguível, sem contorno, modos claro/escuro e padrão/compacto, textos legíveis. Executar workflow existente (builds, lint, testes e oito capturas) e inspecionar PNGs. Sem novos testes para troca de duas cores semânticas.

G-01/02 PASSOU: escopo aprovado e nativo. G-03/04 PASSOU em revisão do diff restrito a cores. G-05/06/07/12 aguardam CI/PNG; leitores de tela, fonte ampliada e landscape/tablet NÃO EXECUTADOS neste recorte. G-08/09/10 NÃO APLICÁVEIS a novos dados/rede/mídia: não adicionados; referência privada não copiada ao repositório. G-11 profiling NÃO EXECUTADO. G-13 sem distribuição/merge; reversão por commit na branch. G-14 continuidade será atualizada com evidências. G-15 preferência restrita ao chat, ausente dos arquivos. Risco P2 visual, nenhum P0/P1 identificado neste diff.

Validação inclui correção de sincronização no XCUITest existente: execução documental 41301783738 falhou ao ler o título antigo imediatamente após tocar. A asserção agora aguarda até 5 segundos pelo título esperado, sem retry de toque, skip ou mudança do resultado esperado.

## Resultado

[CI aprovado](https://github.com/lucasouzadev/arke-viagens-e-momentos/actions/runs/41305472522), fonte `a2e4e0b354ef4d79851779cc7f8040a16812af35`. Builds, três testes unitários e dois testes UI por plataforma aprovados; lint Android 0 erros/8 avisos. Oito PNGs originais inspecionados: iPhone 16/SE 3 e Android API 35 padrão/compacto, claro/escuro. Fundo/cartão distintos, sem contorno; no escuro Android o cartão é mais escuro que o fundo, conforme os papéis semânticos escolhidos. G-05/06/07 PASSOU somente na inspeção visual dessas viewports e regressão de toque; limites manuais acima permanecem NÃO EXECUTADOS. G-12 PASSOU no workflow; G-13 PASSOU para verificação sem distribuição; G-14 PASSOU com docs/PR atualizados. Próxima tarefa: puxador entre cartões.
