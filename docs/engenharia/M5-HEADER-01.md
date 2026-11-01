# M5-HEADER-01 — Cabeçalho do cartão inferior

**Data:** 01/11/2026 · **Estado:** implementado e verificado no PR #2, sem merge. Branch `chore/development-kickoff`, PR #2.

## Autorização e contrato

O responsável autorizou no chat o próximo item apresentado: botão “+” à esquerda, contexto ativo central e relógio à direita, dentro do cartão inferior. Base: DESIGN-001/002 e ADR-002. Este aceite não aprova automaticamente física radial, persistência, grupos ou contratos de Pin.

Escopo: views SwiftUI/Compose, ícones vetoriais simples Android, strings, testes de UI e captura existentes; Xcode project/Gradle/workflow somente para habilitar a verificação nativa necessária. Documentos de design, STATUS/HANDOFF/ROADMAP/INDEX/CHANGELOG e este contrato atualizados.

- Cabeçalho permanece separado do scroll do corpo, com ações de 44pt/48dp e nome centralizado, inclusive ao quebrar linha.
- Relógio usa seletor nativo com os cinco contextos, seleção identificável e preservação da posição do painel: lista do sistema no iOS e DropdownMenu no Android.
- “+” abre apresentação nativa “Adicionar lugar”, informando que o salvamento ainda está em desenvolvimento. Não existe editor, Pin salvo nem sucesso fictício nesta tarefa.
- Seletor de proporção por toque fica compacto entre os dois cartões, acessível enquanto o futuro puxador por arrasto não está implementado.
- Preservar cartões sem contorno, mapa/atribuição, tema do dispositivo e texto semântico legível no escuro. Reservar espaço para o cabeçalho ao calcular a altura do mapa.

## Aceite e verificação

- [x] Cabeçalho com +/título/relógio em ambas as plataformas.
- [x] Troca dos cinco contextos por toque, mantendo o painel selecionado.
- [x] Abertura/fechamento da entrada de adicionar lugar, sem dados salvos.
- [x] Testes de UI realmente executados, builds/lint e capturas padrão/compacto claro/escuro.
- [x] Imagens inspecionadas e evidências registradas no PR/handoff.

Sem nova persistência, GPS, conta, rede, SDK ou chave. Google Maps real continua sem projeto/chave. Risco P2 de foco/overflow; reversão por correção na branch, sem mudar main. Leitores de tela, landscape/tablet e profiling exigem avaliação adicional; não serão declarados aprovados por screenshots.

G-01/02 PASSOU em revisão de escopo; G-03/04/05/06/07/12/13 aguardam implementação e prova; G-08/09/10 NÃO APLICÁVEIS a novos dados/rede/licenças (não há alterações nessas áreas; bloqueios de distribuição anteriores permanecem); G-11 profiling NÃO EXECUTADO; G-14 atualizar ao concluir; G-15 configurado no chat, valor ausente dos arquivos.

## Primeira execução e correções

[Execução inicial](https://github.com/lucasouzadev/arke-viagens-e-momentos/actions/runs/41274938064), fonte `3cc47d9`: builds e testes unitários aprovados. Android executou os 2 testes de toque sem falhas, mas a limpeza do executor removeu o APK antes de capturar; script corrigido para reinstalar o mesmo build depois dos testes. Lint: 0 erros, 8 avisos (5 anteriores e 3 de versões de dependências de teste/BOM).

iOS executou 2 testes UI: aviso do “+” passou; seletor de proporção falhou na ação de acessibilidade/visibilidade. Controle recebeu área de toque explícita de 44pt em largura disponível, estilo plain e prioridade de camada; a região interativa do mapa ficou limitada à própria forma. Teste continua usando toque nativo e agora exige `isHittable`; sem forçar toques por coordenadas, skip ou supressão. Em falha futura, script preservará um PNG diagnóstico e continuará retornando erro. Nova execução necessária.

[Segunda execução](https://github.com/lucasouzadev/arke-viagens-e-momentos/actions/runs/41275496415), fonte `31d3856`: Android completo aprovado, incluindo os 2 testes e quatro capturas inspecionadas. iOS ainda falhou em `isHittable` do Menu de proporção; PNG diagnóstico confirmou renderização correta do cabeçalho, sem resolver o toque. A apresentação iOS foi substituída por Button + confirmationDialog nativos para contexto/proporção, com opções e seleção equivalentes e identificadores na origem dos botões. Testes continuam exigindo hit testing real, unicidade do identificador e preservação do painel; sem forçar coordenadas ou ocultar falha. Compatível com o fallback em lista por toque de DESIGN-002/004; não é uma UI híbrida nem uma tab bar.

## Verificação final

[CI aprovado](https://github.com/lucasouzadev/arke-viagens-e-momentos/actions/runs/41276436997), fonte `b9d7c2864f819e70403e2a6756af83448746c0ef`. Dois testes de interface por plataforma passaram: troca dos cinco contextos preservando painel e abertura/fechamento do aviso de adicionar. Três testes unitários por núcleo aprovados; builds aprovados; lint Android com 0 erros e 8 avisos, sem supressão.

Oito PNGs originais inspecionados: iPhone 16 (1179×2556), SE 3 (750×1334), Android API 35 padrão (1080×2400) e compacto (720×1280), claro/escuro. Cabeçalho visível e cartões sem contorno; em compacto o corpo tem scroll independente. MapKit renderiza; Android permanece no fallback sem chave.

G-03/04/05/06/07/12/13: evidência restrita aos builds, testes de toque e imagens acima; não constitui aprovação global de acessibilidade. G-14 atualizado em STATUS/HANDOFF/ROADMAP e PR. G-11 profiling, leitores de tela, fonte ampliada, landscape e tablet NÃO EXECUTADOS. G-01/02/08/09/10/15 mantêm o enquadramento anterior. Próxima tarefa: puxador entre cartões; salvar lugares continua fora deste recorte.
