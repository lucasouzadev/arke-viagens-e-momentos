# M5-HEADER-01 — Cabeçalho do cartão inferior

**Data:** 01/11/2026 · **Estado:** em andamento. Branch `chore/development-kickoff`, PR #2.

## Autorização e contrato

O responsável autorizou no chat o próximo item apresentado: botão “+” à esquerda, contexto ativo central e relógio à direita, dentro do cartão inferior. Base: DESIGN-001/002 e ADR-002. Este aceite não aprova automaticamente física radial, persistência, grupos ou contratos de Pin.

Escopo: views SwiftUI/Compose, ícones vetoriais simples Android, strings, testes de UI e captura existentes; Xcode project/Gradle/workflow somente para habilitar a verificação nativa necessária. Documentos de design, STATUS/HANDOFF/ROADMAP/INDEX/CHANGELOG e este contrato atualizados.

- Cabeçalho permanece separado do scroll do corpo, com ações de 44pt/48dp e nome centralizado, inclusive ao quebrar linha.
- Relógio usa menu nativo com os cinco contextos, seleção identificável e preservação da posição do painel.
- “+” abre apresentação nativa “Adicionar lugar”, informando que o salvamento ainda está em desenvolvimento. Não existe editor, Pin salvo nem sucesso fictício nesta tarefa.
- Seletor de proporção por toque fica compacto entre os dois cartões, acessível enquanto o futuro puxador por arrasto não está implementado.
- Preservar cartões sem contorno, mapa/atribuição, tema do dispositivo e texto semântico legível no escuro. Reservar espaço para o cabeçalho ao calcular a altura do mapa.

## Aceite e verificação

- [ ] Cabeçalho com +/título/relógio em ambas as plataformas.
- [ ] Troca dos cinco contextos por toque, mantendo o painel selecionado.
- [ ] Abertura/fechamento da entrada de adicionar lugar, sem dados salvos.
- [ ] Testes de UI realmente executados, builds/lint e capturas padrão/compacto claro/escuro.
- [ ] Imagens inspecionadas e evidências registradas no PR/handoff.

Sem nova persistência, GPS, conta, rede, SDK ou chave. Google Maps real continua sem projeto/chave. Risco P2 de foco/overflow; reversão por correção na branch, sem mudar main. Leitores de tela, landscape/tablet e profiling exigem avaliação adicional; não serão declarados aprovados por screenshots.

G-01/02 PASSOU em revisão de escopo; G-03/04/05/06/07/12/13 aguardam implementação e prova; G-08/09/10 NÃO APLICÁVEIS a novos dados/rede/licenças (não há alterações nessas áreas; bloqueios de distribuição anteriores permanecem); G-11 profiling NÃO EXECUTADO; G-14 atualizar ao concluir; G-15 configurado no chat, valor ausente dos arquivos.
