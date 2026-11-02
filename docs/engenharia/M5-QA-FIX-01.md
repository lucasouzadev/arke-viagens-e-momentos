# M5-QA-FIX-01 — Interação da beta

Em andamento, 02/11/2026. Autorização explícita para iniciar as correções de QA-BETA-01 no chat. Plataformas nativas, branch atual/PR #2; sem merge. Arquivos autorizados: UI iOS/Android, estados/testes de interação, permissões de localização em primeiro plano e documentos de continuidade. Snapshot v1 e ideias existentes preservados.

Aceite: estabilizar gesto da home, uma única apresentação de sheet nativo/alça interna, eliminar toolbar Concluir, campos com área editável real e separadores, ação Revisar no corpo durante escrita, fades superior/inferior sem barra visível, contextos por ponteiro com snap e alternativa acessível, cartões de ideias sem texto técnico repetido e localização pessoal contextual/negável. Android Maps depende da chave privada ainda indisponível; não criar conta/faturamento. Sem localização em background, compartilhamento ou coleta nova. Pedido atual substitui as diretrizes anteriores de alça externa e CTA sempre fixo.

Validação: CI sem secrets, testes nativos de persistência/descarte/gestos e foco/teclado, screenshots padrão/compacto/claro/escuro; iOS 26 exige sessão de interação específica para reproduzir vídeo. Não publicar outra beta automaticamente. Risco P1 de regressão em rascunhos/foco; nunca dispensar com dados alterados ou durante gravação. Rollback por código preserva JSON. Gates G-01/02/03/08/09/10/15 revisados no escopo; G-04/05/06/07/12/13 aguardam execução, G-11 profiling não executado, G-14 atualização ao fim. Revisão proporcional de privacidade: posição somente local, permissão no toque, nenhuma coordenada em logs/fixtures/relatórios; negada mantém mapa utilizável. Referências: QA-BETA-01, DESIGN-006, QUALITY-GATES e SAFEGUARDS.

Evidência temporal: gravar o simulador iOS 26 durante a suíte de interação, com dados fictícios de teste, em MOV temporário de sete dias. Essa evidência específica do QA substitui o limite histórico somente PNG para a gravação solicitada pela análise de gestos. Sem vídeo de dados pessoais reais ou material privado de assinatura.

Validações intermediárias: fonte 5b7023d/run 41509583149 compilou ambas as bases, Android teve 11/12 instrumentados aprovados (teste do contexto fora da lista virtualizada corrigido para scrollToNode); iOS 26 teve 4/8 UI aprovados, com três falhas de acesso a Revisar e uma diferença geométrica subpixel. Corrigida rolagem aninhada dos editores (crescimento por conteúdo e scroll externo), tolerância geométrica de 1 pt e preservação da atribuição do mapa. Fonte 4f3446e/run 41512033197 compilou iOS, mas script escolheu visionOS 26 para SE3; corrigido filtro por identificador iOS. Nenhuma dessas execuções é aprovação completa do QA. Callbacks CoreLocation agora fazem salto explícito para MainActor, sem suprimir warnings de isolamento.

Fonte 11869a1/run 41513478869: Android aprovado integralmente (6 core/12 instrumentados, build/lint, 20 PNGs/APK). iOS 26: 5/8 UI passaram; três falhas de acesso ao CTA persistiram. Árvore/screenshot mostram ScrollView de y163 a852 enquanto teclado cobre y548 a852, sem range de rolagem suficiente. Corrigir apenas overlap não tratado pelos adjustedContentInsets nativos, via observação pública de UIScrollView/UIKeyboard; não deslocar cabeçalho nem criar barra fixa. Queda anterior no número de falhas confirma a correção geométrica, não aprovação completa.

Refino do viewport: medir o contêiner externo estável e reservar overlap fora do ScrollView, para que o fade inferior termine acima do teclado. Se o sistema já encolher o contêiner, overlap é zero. Removida dependência de UIScrollView.adjustedContentInset para não criar realimentação com tamanho do conteúdo. Testes iOS limitados a 180 s por método para diagnosticar travamentos; nenhum caso removido. Botão de localização Android movido ao topo para preservar atribuição do provedor; fallback ganhou respiro antes do fade.

Simplificação final do teclado: restaurar NavigationStack (presente na beta do vídeo, que já posicionava conteúdo acima do teclado), sem toolbar Concluir e sem barra de navegação visível. Mantidos CTA no scroll e campos expansíveis. Removidas as duas tentativas de compensação manual/UIView probe antes de disponibilizar beta; priorizar o contêiner nativo. Versão anterior cancelada antes de aprovação de QA, sem declarar workaround validado.

## Evidência do candidato 70f623b

[Execução 41520708559](https://github.com/lucasouzadev/arke-viagens-e-momentos/actions/runs/41520708559), fonte 70f623b42c3a4dc850f023fc5219cb16ffd92eb0. Android PASSOU: build, 6 core, 12 instrumentados, lint com 0 erros/19 avisos; 20 PNGs originais inspecionados em padrão/compacto e claro/escuro. Avisos: versões/target, ícone de aplicativo ainda pendente e strings antigas sem uso; sem supressão. APK debug gerado, SHA256 DE63F1D0007846AEF0863302F0CAB68B0943C04CDFEE54AD373306F3F93F2532. Android API 35, Pixel 6, teclado/foco, revisão, persistência/reabertura, edição, exclusão confirmada, contextos e gestos exercitados. Teste instrumentado não substitui inspeção manual de seleção/cursor ou avaliação de fluidez.

iOS 26: build e núcleo aprovados; interface/capturas/gravação ainda em execução. Não usar sucessos Android como aprovação iOS. Não há Xcode local neste Windows. No editor, o contêiner NavigationStack nativo foi restaurado sem toolbar de teclado; não resta compensação manual de frame/UIScrollView.

Limites de localização: código solicita apenas ao tocar Minha localização, sem background ou persistência. Concessão/negação real e recentralização NÃO EXECUTADAS nesta rodada. Google Maps real BLOQUEADO pela chave/configuração privada ainda ausentes; o APK mantém fallback e não pede permissão nessa condição. Nova permissão de localização requer revisão das declarações de privacidade antes de distribuição pública.

Revisão visual humana, iOS mínimo suportado, VoiceOver/TalkBack, fonte 200%, redução de movimento, landscape/tablets, aparelho físico e profiling NÃO EXECUTADOS. Nenhuma nova beta/convite/merge; versão já enviada permanece 0.0.1 (8.1). Reversão somente do código preserva snapshot v1 e dados locais.

## Gates desta rodada — revisão do agente implementador

| Gate | Estado e alcance |
| --- | --- |
| G-01 | PASSOU: dois cartões, stack nativa e um azul preservados; revisão atual do DESIGN-006 prevalece. |
| G-02 | PASSOU: contrato, autorização e fronteiras registrados antes das alterações. |
| G-03 | PASSOU na revisão: UI reutilizada; persistência/store/repositórios mantidos; posição transitória em primeiro plano. |
| G-04 | PASSOU em build das duas bases e lint Android; 19 avisos registrados, sem supressão. |
| G-05 | NÃO EXECUTADO integralmente: Android automatizado/capturas aprovados; fluxo/gravação iOS atual ainda em execução. |
| G-06 | NÃO EXECUTADO integralmente: Android padrão/compacto claro/escuro inspecionado; iOS compacto, fontes ampliadas, rotação/tablets e aparelho físico pendentes. |
| G-07 | NÃO EXECUTADO integralmente: revisão de labels, alvos, seleção anunciável e alternativas por toque realizada; VoiceOver/TalkBack, contraste global e movimento reduzido pendentes. |
| G-08 | PASSOU na revisão proporcional: sem novo backend, dependência ou credencial; nenhuma coordenada ou dado pessoal nas fixtures/logs, permissão somente contextual. Auditoria MASVS completa NÃO EXECUTADA. |
| G-09 | PASSOU na delimitação de privacidade: posição local/sem background; declarações de loja e uso real ainda exigem revisão antes de distribuição pública. |
| G-10 | PASSOU nos testes Android e core iOS de validação/persistência; snapshot v1 mantido, sem migração. Fluxo completo iOS atual pendente em G-12. |
| G-11 | NÃO EXECUTADO: sem profiling; gravação temporal ainda pendente. |
| G-12 | NÃO EXECUTADO integralmente: Android 6 core/12 instrumentados aprovados, iOS 10 core/build aprovados; suíte UI iOS atual pendente. |
| G-13 | PASSOU na fronteira operacional: APK temporário, sem nova beta, merge, convite ou publicação; rollback de código conserva dados v1. |
| G-14 | NÃO EXECUTADO: consolidação final de STATUS/HANDOFF/ROADMAP/changelog e PR aguarda o resultado iOS. |
| G-15 | PASSOU: preferência pessoal usada somente nas respostas do chat, ausente dos artefatos. |

Não promover uma nova beta com regressão de revisão/salvamento (P1). A falta de configuração Google Maps é bloqueio externo explícito; localização e ambientes manuais ainda não exercitados não são convertidos em aprovação.

## Ambiente informado e alinhamento da reprodução

O aparelho informado é iPhone 14 com iOS 27. A análise inicial considerou iOS 26; validar esse runtime não reproduz integralmente o ambiente informado. Native-checks agora seleciona `xcode-27` arm64, Xcode 27.0 e runtime iOS 27.0 exato, criando iPhone 14 e SE3. Não selecionar iOS 27.1 por maior versão: a imagem o disponibiliza para outro modelo; não misturar visionOS nem runtimes entre dispositivos. Boot tem limite de 360 s por dispositivo para permitir diagnóstico explícito e upload das evidências já produzidas. Nenhum teste de interface removido. Configuração do job de assinatura/TestFlight não alterada ou executada nesta rodada; antes de novo envio, alinhar a toolchain à versão efetivamente validada.

Fontes oficiais consultadas em 02/11/2026: [runner Xcode 27 GA](https://github.blog/changelog/2026-10-15-xcode-27-runner-image-generally-available/) e [software/runtimes da imagem](https://github.com/actions/runner-images/blob/main/images/macos/xcode-27-arm64-Readme.md). Sintaxe Bash e Python embutido aprovadas localmente; compilação/interação iOS 27 ainda não executadas.

Refino de paridade no mesmo recorte: botão Concluído preenchido também no seletor Android, fade na lista de encaixe com superfície correspondente, botões Abrir/excluir na mesma linha dos cartões nas duas plataformas e notas resumidas em até três linhas (texto completo preservado em detalhes). Rodapé iOS recebe espaço antes do fade, equivalente ao respiro Android. Minha localização Android usa ícone circular de 48 dp com label acessível, mantendo atribuições do mapa livres. Sem mudança de dados/permissão ou novos cenários de teste; executar novamente a regressão existente após esse ajuste visual.

Fonte d2410e0/run 41527925569: Android repetiu aprovação completa; iPhone 14/iOS 27 compilou e passou 10 core + 5/8 UI. As três falhas continuam em Revisar: árvore mostra ScrollView y151..844, teclado começa em y516. NavigationStack sozinho não resolve. Capturas/vídeo preservados. Substituir a dependência do inset SwiftUI por um contêiner UIKit local: UIHostingController filho, conteúdo limitado ao keyboardLayoutGuide.topAnchor e safeAreaRegions.container para evitar compensação dupla. Sem observers de teclado, alturas medidas, dados novos ou deslocamento do cabeçalho. Home ignora teclado ao fundo. Regressão existente ganha assert de viewport acima do teclado, mantendo os oito casos. [API Apple](https://developer.apple.com/documentation/uikit/adjusting-your-layout-with-keyboard-layout-guide). Ainda exige aprovação dos testes antes de nova beta.
