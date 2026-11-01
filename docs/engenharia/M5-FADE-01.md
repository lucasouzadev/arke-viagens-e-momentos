# M5-FADE-01 — Transição inferior suave nos sheets

01/11/2026. M5, verificado e em revisão, branch chore/development-kickoff, PR #2.

Autorização explícita no chat: adicionar gradiente inferior, eliminando a faixa sólida que corta o conteúdo junto aos botões; preservar paridade iOS/Android de DESIGN-006/ADR-012.

Escopo e arquivos: editor/revisão SwiftUI e Compose, guia DESIGN-006, continuidade/índice/changelog e este contrato. Desvanecer os últimos 32 pt/dp do corpo rolável até a superfície sem introduzir barra, borda, sombra ou cor de marca nova. Rodapé permanece legível e fixo; padding final 48 permite que todo campo/texto role para fora da região de fade. Efeito decorativo não intercepta toque/foco; sem mudanças em dados, validação, teclado, mapas, dependências ou persistência. Não gerar snapshots editados.

Aceite: transição gradual em ambos os temas/plataformas, sem corte seco no limite inferior do scroll; campos continuam editáveis/roláveis; rótulos e ações fixas preservados. Builds, regressão UI/storage existente e 16 capturas reais padrão/compacto claro/escuro; sem teste unitário que replique o desenho do gradiente. Risco P2 visual/contraste na área transitória, mitigado por espaço final maior que o fade. Reverter por commit sem apagar dados. Sem merge/distribuição.

## Evidências finais

[CI aprovado](https://github.com/lucasouzadev/arke-viagens-e-momentos/actions/runs/41337689076), fonte `fd9d9d50da562592f136b82dd4dced224c5c337d`. iOS build Simulator + 9 testes core + 4 UI, Android build debug + 6 core + 6 instrumentados (4 UI/2 repositório), todos sem falhas. Lint Android 0 erros/8 avisos existentes. Primeira tentativa Android expirou no boot do emulador, antes do app; repetição apenas do job aprovou sem mudança de código. Sem skips ou alteração de testes para contornar falha.

Inspecionados 16 PNGs originais: home/editor vazio, iPhone 16/SE 3 e Android API 35 padrão/compacto, claro/escuro. Transição suave visível nas telas compactas onde o campo de nota encontra o rodapé; botões/avisos intactos e mesmo tratamento nos dois sistemas. Em telas grandes o conteúdo termina acima da transição. Revisão/salvar/corrigir/cancelar e regressões de dados/gestos exercitados pelos testes UI padrão; revisão preenchida não é uma captura estática. Padding final de 48 supera o fade de 32, permitindo revelar o conteúdo ao rolar; efeito não adiciona hit target nem semântica. Nenhuma alteração de dados/IO/schema/dependências. Arquivo ZIP local reúne 16 PNGs, CRC verificado.

## Gates — revisão do agente implementador

| Gate | Resultado e evidência |
| --- | --- |
| G-01 | PASSOU: pedido explícito, DESIGN-006/ADR-012, duas plataformas nativas. |
| G-02 | PASSOU: contrato visual delimitado e aceite registrado. |
| G-03 | PASSOU: apenas views/docs; domínio e IO intactos. |
| G-04 | PASSOU: diff revisado, builds/lint aprovados, efeito simples sem dependências. |
| G-05 | PASSOU: transição inferior em pares de capturas e regressão dos fluxos. |
| G-06 | PASSOU somente nos tamanhos/temas capturados e UI padrão; fonte 200%, landscape/tablet e IME físico NÃO EXECUTADOS. |
| G-07 | PASSOU na revisão de semântica/toque preservados e ausência de interceptação pelo efeito. VoiceOver/TalkBack e auditoria completa de contraste NÃO EXECUTADOS. |
| G-08 | PASSOU na revisão de ausência de rede/permissões/credenciais/dependências novas. Auditoria MASVS nova NÃO APLICÁVEL a este ajuste visual. |
| G-09 | NÃO APLICÁVEL a novos dados/finalidades/licenças: nenhum introduzido. |
| G-10 | PASSOU nos testes de regressão de dados/draft; sem mudança de armazenamento. |
| G-11 | Profiling NÃO EXECUTADO; sem promessa de desempenho medido. |
| G-12 | PASSOU: CI e 16 PNGs, com limite dos ambientes descritos. |
| G-13 | PASSOU para desenvolvimento/capturas; sem merge/release. Reversão por commit preserva snapshots. |
| G-14 | PASSOU: guia/contrato/índice/status/handoff/roadmap/changelog/PR reconciliados. |
| G-15 | PASSOU: preferência pessoal somente nas respostas do chat, ausente dos artefatos. |

Risco residual P2 nas configurações de acessibilidade/dispositivos não exercitados; nenhum P0/P1 identificado no recorte. Sem distribuição ou aprovação visual humana presumida. Próximo: manter o fade no guia comum ao desenvolver os próximos sheets.
