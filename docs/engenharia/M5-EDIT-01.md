# M5-EDIT-01 — Navegação limpa e edição segura de ideias

02/11/2026. Implementado, verificado e em revisão. Autorizado explicitamente no chat: corrigir o sheet e seguir o próximo fluxo do plano (edição segura, indicado em HANDOFF). Branch chore/development-kickoff, PR #2 rascunho; sem merge/release.

Primeiro recorte: título do sheet centralizado com colunas laterais equivalentes; bookmark preenchido alinhado ao eixo dos ícones de endereço/nota por recuo de 20. Primeira página sem Anterior e Próximo largo; páginas seguintes com Anterior somente ícone, alvo 48 e nome acessível. Última página (também coleção unitária) com Concluído largo, fechando o painel. Listas maiores mantêm Próximo nos itens intermediários. Posição somente acessível, informação por toque, alça externa, flutuação e fade preservados nas duas plataformas.

Segundo recorte: editar uma ideia existente pela lista de Planejamento, usando ícone de lápis acessível de 48 e o editor/revisão nativo já existente, pré-preenchido. Cancelar sem mudanças fecha; alterações pedem confirmação antes de descartar. Revisar/editar novamente/Salvar alterações conservam validação, teclado e erros. Salvar só fecha após sucesso; falha mantém rascunho. Mesmo ID, ordem e demais registros preservados, sem criar novo lugar. Operação update separada de add, exigindo snapshot original; conflito ou registro ausente não sobrescreve nem recria. Retry idempotente aceita conteúdo já atualizado. Snapshot v1 e caminhos/backup/proteções permanecem; nenhuma migração ou nova rede/conta/GPS/geocoding/Pin/Trip.

Arquivos autorizados: views/editor/lista, store/viewmodel, repositórios de ideias, recursos vetoriais/strings locais, testes core/repositório/UI, capturas Debug e docs. Sem bibliotecas/serviços/segredos/mídia externa novos. Referências: DESIGN-006, ADR-011/012, M5-PLACES-01, M5-FLOATING-01 e protocolos de agents.

Risco P2: perda de rascunho, erro de identidade/conflito, altura/IME e retorno à home. Mitigação: escrita atômica serializada existente, validação e comparação da versão lida no update; nenhuma remoção de snapshot; descarte confirmado apenas quando houve alteração. Reversão de código deve preservar dados v1. Escrita local pessoal já autorizada, sem nova finalidade/coleta/compartilhamento; avaliação adicional de backend/lojas/mídia não aplicável. Profiling, leitores de tela, fontes 200%, landscape/tablets e aparelho físico não executados até haver evidência.

Aceite/provas: testes de atualizar/reabrir mantendo ID/ordem/outros registros, retry, conflito/ausente/ID inválido sem perda; UI de editar/cancelar/descarte/revisar/salvar/reabrir e navegação primeira/última/unitária; alinhamento e centro do cabeçalho, builds/lint e capturas reais padrão/compacto claro/escuro comparadas por plataforma. Exemplos de captura somente Debug/memória, sem semear dados. Ações não recebem sucesso antes de persistir. Gates e evidências finais serão registrados após execução; pendente nunca equivale a PASSOU. Próximo handoff após este recorte: revisão visual humana e avaliação do próximo contrato de planejamento, sem presumir Google Cloud/mapa disponível.

Texto público candidato: “Simplificamos a navegação dos lugares e adicionamos a edição das ideias salvas neste aparelho.” Em desenvolvimento, sem versão lançada. Preferência privada de tratamento apenas no chat, nunca em artefatos.

Primeira execução 41385270237, fonte 35fb002: Android aprovado (6 core + 11 instrumentados, lint 0 erros/9 avisos e 20 PNGs originais inspecionados). iOS compilou e passou 10 core + 6/7 UI. Teste de edição falhou porque backspaces com cursor no início não apagaram o endereço anterior: foi salvo “Nova entrada, 200Rua de teste, 100”. Teste passa a usar Selecionar Tudo nativo, conferir valor do campo antes de revisar e manter a asserção de reabertura. Nenhum comportamento de produto ou cenário removido; nova execução necessária.

Execução 41386548922, fonte 3e74ec0: Android aprovado novamente; iOS 10 core + 6/7 UI, falha porque Selecionar Tudo não apareceu após pressão longa no campo. Substituição passa a usar o gesto nativo de três toques para selecionar o parágrafo único das fixtures, conforme [guia Apple](https://support.apple.com/guide/iphone/select-edit-and-move-text-iph1a9cae52c/27/ios/27), mantendo conferência do campo e reabertura. Falha de valor agora registra hierarquia e attachment PNG dentro do teste; script exporta esses attachments, pois captura simctl após encerramento do XCTest mostrou SpringBoard. Produto permanece idêntico à fonte Android aprovada; nenhum cenário removido ou convertido em sucesso.

## Evidências finais

[Native checks aprovados](https://github.com/lucasouzadev/arke-viagens-e-momentos/actions/runs/41388017517), fonte `4ee4af04e1027f6f15141690fa591e02ebb609f6`: iOS build Simulator, 10 core + 7 XCUITest; Android build debug, 6 core + 11 instrumentados (8 UI/3 repositório), sem falhas/skips. Total 34 testes. Lint Android 0 erros/9 avisos anteriores, incluindo ic_close sem uso (P3), sem supressão. Teste iOS de edição passou com seleção nativa do parágrafo e valores conferidos antes de revisar e após reabrir. Produto idêntico ao aprovado na fonte 35fb002; somente teste/diagnóstico/docs foram corrigidos depois.

40 PNGs originais inspecionados individualmente e em pares: home/editor vazio/editor preenchido/detalhe com nota/sem nota × padrão/compacto × claro/escuro × ambas plataformas. iOS artefato 13116349601 da fonte final; Android artefato 13115292774/run 41385270237/fonte 35fb002, mesmo código Android validado novamente na execução final. iPhone 16/SE 3, Android API 35 Pixel 6/viewport compacta. Cabeçalho central, ícones/textos alinhados e bookmark preenchido confirmados; primeira página sem Anterior, última com seta e Concluído largo. Nenhuma imagem editada ou mídia da referência incorporada. Prévia preenchida somente Debug/memória, Save sem escrita; edição real validada separadamente pelo teste de persistência UI. Artefatos Actions somente PNG, sete dias, sem APK/IPA.

UI verifica geometria, primeira/última página e coleção unitária, informação por toque, altura curta/nota longa, retorno por alça/swipe/Voltar e preservação da home. Editor exercita cancelar sem alteração, continuar editando/descartar após mudança, revisão/correção/salvar/reabrir/excluir. Android recria Activity mantendo rascunho alterado e depois confirma conteúdo atualizado. Casos intermediários de coleções maiores que dois foram revisados no código, sem teste UI/PNG específico. Falha real de disco/conflito visível em UI, processo morto durante gravação, fonte 200%, leitores de tela, alternância manual de redução de movimento, IME Android manual, landscape/tablets/aparelho físico e profiling NÃO EXECUTADOS. Falhas anteriores preservadas acima; nenhum cenário removido.

## Revisão de arquitetura e dados

Fluxo pessoal: lista → cópia do original + rascunho → validação/revisão → store/viewmodel → repositório serializado → snapshot atômico v1 → confirmação de sucesso → fechar editor. Android confirma a solicitação específica por identificador transitório, sem persistir esse identificador no JSON; comparação do conteúdo sozinha não fecha antecipadamente um salvamento sem mudanças. Original/rascunho pendentes e seleção são restaurados na recriação Android exercitada no teste. iOS aguarda o resultado async do store. O callback de veto de fechamento Android é estável e lê estado atualizado, preservando o sheet enquanto o rascunho muda.

Delta de risco STORAGE/CODE: sobrescrita desatualizada/ID trocado/registro ausente são recusados no repositório; retry do mesmo conteúdo não duplica; validação de tamanho/versão e escrita atômica existentes são reutilizadas. Teste de update verifica bytes preservados nas recusas, ordem e demais registros, retry e releitura. Não há conta, autorização remota, compartilhamento, localização nova, coleta ou log de conteúdo pessoal. Novo threat model de backend NÃO APLICÁVEL a esta fronteira local; revisão proporcional de dados registrada aqui, sem alegar certificação MASVS. Falha real de disco, processo morto durante escrita, aparelho físico e auditoria MASVS completa NÃO EXECUTADOS. Reversão do código preserva o arquivo v1 e as alterações de conteúdo já salvas; não restaura nem apaga snapshots automaticamente.

## Gates — revisão do agente implementador

| Gate | Resultado e alcance |
| --- | --- |
| G-01 | PASSOU: instrução visual atual registrada, stack nativa/dois cartões/azul preservados. |
| G-02 | PASSOU: contrato de correção e próximo fluxo antes do código; escopo pessoal delimitado. |
| G-03 | PASSOU: apresentação reaproveitada, operação update separada de add, ID estável e confirmação explícita de sucesso. |
| G-04 | PASSOU na revisão de diff/XML/links/sintaxe bash e builds/lint já executados; lint Android 0 erros/9 avisos anteriores, sem supressão. |
| G-05 | PASSOU: fluxos/gestos e 40 PNGs inspecionados; nova navegação e edição real em paridade. |
| G-06 | PASSOU em padrão/compacto e claro/escuro das duas plataformas; teclado iOS exercitado no teste. Fonte 200%, IME Android manual, landscape/tablets e aparelho físico NÃO EXECUTADOS. |
| G-07 | PASSOU na revisão de alvos 48, nomes acessíveis para lápis/voltar e posição sem contador visual; VoiceOver/TalkBack, auditoria global de contraste e alternância manual de redução de movimento NÃO EXECUTADOS. |
| G-08 | PASSOU na revisão proporcional STORAGE/CODE descrita acima; sem nova rede/permissão/segredo/dependência. Auditoria MASVS completa NÃO EXECUTADA. |
| G-09 | NÃO APLICÁVEL a nova coleta/mídia/licença: mesmos dados pessoais locais e ícones vetoriais originais/APIs nativas, sem asset das referências. |
| G-10 | PASSOU nos testes de update/retry/conflito/ausente/identidade/bytes/ordem/releitura; snapshot v1 e salvaguardas existentes preservados. |
| G-11 | Profiling NÃO EXECUTADO; IO continua fora da UI e serializado, sem promessa de desempenho medido. |
| G-12 | PASSOU: 34 testes, builds/lint e 40 PNGs; falhas e correções de interação do teste registradas. |
| G-13 | PASSOU: PR rascunho, sem merge/release/assinatura; reversão de código preserva dados v1. |
| G-14 | PASSOU: guia/contrato/STATUS/HANDOFF/ROADMAP/INDEX/CHANGELOG e PR reconciliados com evidências e limites; links verificados. |
| G-15 | PASSOU: preferência privada restrita ao chat, ausente dos artefatos. |

Risco residual P2 nos ambientes não exercitados e P3 dos avisos de lint; nenhum P0/P1 identificado no recorte. Revisão visual humana pendente. Nenhum teste pendente é tratado como aprovado. Próximo: colher revisão deste resultado e definir o próximo contrato de planejamento; Google Maps real continua dependente do projeto/chave externos já pendentes. PR rascunho, sem merge/release.
