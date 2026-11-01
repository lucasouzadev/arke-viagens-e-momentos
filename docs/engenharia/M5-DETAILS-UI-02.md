# M5-DETAILS-UI-02 — Detalhes com menos informação expositiva

01/11/2026. M5, verificado e em revisão; branch chore/development-kickoff, PR #2. Pedido explícito: simplificar detalhes, ícones para fechar/informação, contador integrado e sutil, aviso apenas sob demanda, navegação preenchida, sheet mais claro que suas áreas internas. Refinamento de DESIGN-006/M5-DETAILS-01, sem alterar dados.

Escopo: cabeçalho com X à esquerda, título curto e contador compacto central, i à direita. Nome em destaque sem rótulo/caixa redundante; endereço e nota acompanhados por ícones, com semântica acessível; omitir nota vazia. Informação acessível por toque abre painel ancorado nativo, com linguagem simples e fechamento explícito. Anterior preenchido neutro e Próximo preenchido azul, com limites existentes. Preservar alça/fade/scroll e posição da home. Tons semânticos compartilhados para detalhes/editor: fundo do sheet mais claro que preenchimentos em claro/escuro. Labels de entrada permanecem no editor; retirar rótulos redundantes da leitura não elimina acessibilidade.

Arquivos: views/tokens iOS/Android, vetores Android nativos sem biblioteca nova, strings, testes UI e docs/guia/changelog. Nenhum dado/schema/IO/permissão/mapa/conta/rede/dependência novo; exemplos Debug podem ter nota mais natural, sem alterar dados reais. Provas: builds/testes anteriores, informação abre/fecha sem perder seleção, ausência do aviso no corpo, navegação finita/nota vazia e 24 PNGs reais padrão/compacto claro/escuro. Informação sob demanda exercitada pelos testes, não por captura estática planejada.

## Evidências finais

[Native checks aprovados](https://github.com/lucasouzadev/arke-viagens-e-momentos/actions/runs/41353801111), fonte `b9582d23b6837fc60e2549b954193a7ceae12d28`. iOS: build Simulator, 9 testes core + 5 XCUITest; Android: build debug, 6 core + 7 instrumentados (5 UI/2 repositório), todos sem falhas/skips. Lint 0 erros/9 avisos: oito anteriores e um P3 de recurso antigo place_details sem uso após trocar o título. Nenhuma supressão. Somente documentação após a fonte validada.

Testes verificam aviso ausente no corpo, i abre informação, Entendi fecha, seleção/contador preservados, nota vazia omitida e navegação finita. Fluxo salvar/reabrir uma ideia real e demais regressões de validação/descarte/exclusão/storage/gestos continuam aprovados. Popover SwiftUI ancorado ao i, com adaptação compacta preservada; DropdownMenu Compose ancorado ao mesmo controle. Painel informativo por toque, não dependente de hover. Não é novo sheet de edição nem confirmação destrutiva.

24 PNGs originais inspecionados: home/editor vazio/detalhes, padrão/compacto claro/escuro, iPhone 16/SE 3 e Android API 35. Conferidos em pares: X/i, contador no cabeçalho, nome em destaque, endereço/nota com ícones, navegação preenchida e ausência do aviso no rodapé. Tons do sheet mais claros que preenchimentos nos dois temas; iOS respeita elevação semântica das cores do sistema. Editor mantém labels persistentes para entrada e recebe os mesmos tokens tonais. Fade e espaços para rolagem preservados. Painel informativo exercitado pelos testes UI padrão, sem PNG estático específico. Capturas usam exemplos sintéticos Debug em memória, sem alterar dados reais. ZIP local com 24 PNGs, CRC verificado. Artefatos Actions continuam apenas PNG por 7 dias.

## Gates — revisão do agente implementador

| Gate | Resultado e alcance |
| --- | --- |
| G-01 | PASSOU: pedido explícito de simplificação refina DESIGN-006, preservando stack/mapas/azul. |
| G-02 | PASSOU: escopo e aceite delimitados antes de editar. |
| G-03 | PASSOU: somente apresentação/tokens/ícones/testes/docs; modelo/IO/schema inalterados. |
| G-04 | PASSOU: diff/links/XML, builds/lint sem erros; aviso P3 do título antigo registrado. |
| G-05 | PASSOU: testes de informação/navegação e comparação das capturas. |
| G-06 | PASSOU nos tamanhos/temas e testes padrão descritos. Fonte 200%, landscape/tablets/aparelho físico NÃO EXECUTADOS. |
| G-07 | PASSOU na revisão de rótulos acessíveis não visuais, contador completo, controles de 48 e informação por toque. VoiceOver/TalkBack e auditoria global de contraste NÃO EXECUTADOS. |
| G-08 | PASSOU: sem nova rede/permissão/credencial/dependência, ícones vetoriais locais. Auditoria MASVS nova NÃO APLICÁVEL à mudança visual sem fronteira de dados nova. |
| G-09 | NÃO APLICÁVEL a novos dados/finalidades/licenças externas: nenhum introduzido. |
| G-10 | PASSOU na regressão de dados/draft; modelos/armazenamento intactos. |
| G-11 | Profiling NÃO EXECUTADO, sem promessa de desempenho medido. |
| G-12 | PASSOU: CI + 24 PNGs, sem extrapolar os ambientes. |
| G-13 | PASSOU para desenvolvimento/capturas, sem merge/release; reversão preserva snapshots. |
| G-14 | PASSOU: guia/contrato/status/handoff/roadmap/índice/changelog/PR reconciliados. |
| G-15 | PASSOU: preferência privada restrita às respostas do chat, não transcrita nos artefatos. |

Risco residual P2 nas configurações não exercitadas e P3 de recurso textual sem uso; nenhum P0/P1 identificado no recorte. Aprovação visual humana final não presumida. PR #2 permanece rascunho; próximo fluxo continua edição segura de ideia salva sob contrato próprio, mantendo a UI simplificada.
