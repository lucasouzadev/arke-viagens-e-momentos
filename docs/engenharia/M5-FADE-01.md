# M5-FADE-01 — Transição inferior suave nos sheets

01/11/2026. M5, em andamento, branch chore/development-kickoff, PR #2.

Autorização explícita no chat: adicionar gradiente inferior, eliminando a faixa sólida que corta o conteúdo junto aos botões; preservar paridade iOS/Android de DESIGN-006/ADR-012.

Escopo e arquivos: editor/revisão SwiftUI e Compose, guia DESIGN-006, continuidade/índice/changelog e este contrato. Desvanecer os últimos 32 pt/dp do corpo rolável até a superfície sem introduzir barra, borda, sombra ou cor de marca nova. Rodapé permanece legível e fixo; padding final 48 permite que todo campo/texto role para fora da região de fade. Efeito decorativo não intercepta toque/foco; sem mudanças em dados, validação, teclado, mapas, dependências ou persistência. Não gerar snapshots editados.

Aceite: transição gradual em ambos os temas/plataformas, sem corte seco no limite inferior do scroll; campos continuam editáveis/roláveis; rótulos e ações fixas preservados. Builds, regressão UI/storage existente e 16 capturas reais padrão/compacto claro/escuro; sem teste unitário que replique o desenho do gradiente. Risco P2 visual/contraste na área transitória, mitigado por espaço final maior que o fade. Reverter por commit sem apagar dados. Sem merge/distribuição.

G-01/02 PASSOU: direção explícita e escopo registrado. G-03/04/05/06/07/12 aguardam diff, CI e capturas. G-08/09: novos dados, dependências, rede, credenciais e finalidades NÃO APLICÁVEIS, pois ausentes. G-10: regressão de dados existente, sem schema/IO novo. G-11 profiling NÃO EXECUTADO. G-13: desenvolvimento/capturas sem release, reversão preserva snapshots. G-14 pendente atualização final. G-15: preferência privada somente no chat, ausente de artefatos. Fonte 200%, leitores de tela, landscape/tablet, aparelho físico e profiling NÃO EXECUTADOS. Nenhum P0/P1 identificado no recorte.
