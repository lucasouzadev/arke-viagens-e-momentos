# M5-FLOATING-01 — Sheets em cartões flutuantes

01/11/2026, verificado em 02/11/2026. M5 em revisão, branch chore/development-kickoff, PR #2 rascunho. Correções visuais explicitamente solicitadas após M5-DETAILS-UI-03: recuo do fundo, alça externa, título à esquerda, apenas i no cabeçalho de detalhes, ícones secundários suaves, sem contador visível e ícone junto ao nome. Modelo de cartão flutuante em ambas as plataformas.

Referências: [DESIGN-006](../design/DESIGN-006.md), [ADR-012](../decisoes/ADR-012.md), [M5-DETAILS-UI-03](M5-DETAILS-UI-03.md). A instrução atual substitui o X/contador e alça interna anteriores; paridade permanece. Recuo interpretado como tela de fundo se afastando levemente ao abrir o modal, sem mudar seleção/mapa/proporção. Sem expansão para novos fluxos ou dados.

Escopo: apresentação SwiftUI/Compose nativa de detalhes e editor/revisão; tokens compartilhados locais, estado de apresentação na home, ícone vetorial, strings, testes UI, capturas e docs. Usar sheet/ModalBottomSheet do sistema como base; superfície visual transparente envolve cartão arredondado com margem horizontal/inferior 12 e raio 28. Alça fora da superfície do cartão, acima do cabeçalho, alvo 48 e ação acessível de recolher. Sem biblioteca de sheet, framework híbrido, sombra/contorno decorativo ou mídia nova. Editor conserva Cancelar, labels e proteção de rascunho.

Aceite: cartão com quatro cantos arredondados e folga inferior segura, alça externa, cabeçalho de detalhes à esquerda com i à direita e sem X/1 de 2 visível. Nome com ícone de ideia; ícones de endereço/nota em tom semântico secundário mais fraco que texto, sem enfraquecer conteúdo. Posição continua disponível só para tecnologia assistiva. Fechar por alça/toque fora/arrasto/voltar nativo conforme plataforma; nenhum descarte de rascunho sujo sem confirmação. Recuo da home ao abrir e restauração ao fechar, respeitando redução de movimento. Conteúdo longo rolável, ações alcançáveis, alturas curta/normal e temas em paridade. Preservar dados/IO/IDs, navegação finita, seleção e retorno à home.

Provas planejadas: builds/lint/testes nativos, fechar por alça e gesto/back, geometria da alça/cartão e margens, ausência visual de X/contador, informação por toque, regressão editor/descarte/storage, 32 PNGs reais nas duas plataformas/tamanhos/temas. SDKs locais ausentes; Actions autorizado. Fonte 200%, leitores de tela, landscape/tablets/aparelho físico/profiling não executados. Risco P2 de insets, teclado, interação com gesto e recuo; mitigação por controles acessíveis, confirmação de descarte e testes/capturas. Sem nova coleta/rede/permissão/dependência/asset externo: nova avaliação de backend/mídia/jurídico não aplicável. Reversão apenas da apresentação preserva snapshots. Sem merge/release.

Texto público candidato: “Ajustamos os painéis para aparecerem como cartões flutuantes, com cabeçalho mais simples e a tela de fundo recuada.” Projeto em desenvolvimento. Próximo handoff: revisar visual e interações antes de continuar edição segura de ideias. Gates G-01..G-15 e evidências finais abaixo; canário confirmado somente no chat, sem nome em artefatos.

Primeira fonte 424681e, execução 41365594277: app Android compilou, mas compilação do teste novo falhou por referência a Espresso indisponível. Teste ajustado para enviar KEYCODE_BACK pela instrumentação Android já existente, sem dependência nova ou remoção de cenário. iOS aprovado: build, 9 testes core + 7 UI, incluindo swipe/alça/margens e posição acessível. Capturas iPhone confirmam flutuação, recuo e cabeçalho. Nenhuma alteração iOS nesta correção do teste Android.

Inspeção iOS inicial: alça externa por vezes se confundia com conteúdo do mapa/alça da home. Adicionada base neutra opaca de 64 × 16 ao indicador 36 × 4, dentro do mesmo alvo 48, nas duas plataformas. Sem sombra/contorno; nenhuma alteração de gesto ou altura. Capturas finais devem usar essa fonte atualizada.

Execução 41367198748, fonte 12993e8: ambos compilaram; lint Android 0 erros/9 avisos (o título antigo sem uso saiu; vetor ic_close sem uso após remover X é P3). iOS 6/7 UI aprovados, falha síncrona de hittable do relógio logo após trocar detent; adicionada espera condicionada de até 5 s mantendo a asserção. Android 8/9 instrumentados aprovados, falha no cenário combinado swipe/Voltar; separados para diagnóstico, mantendo os dois aceites. Alça Android movida ao slot dragHandle nativo, visualmente ainda fora do cartão; gesto de teste agora vai da alça à margem inferior, como no iOS. Regressão de descarte passa a acionar a alça do editor antes de continuar editando. Nenhum cenário ignorado, sem nova dependência.

Execução 41368315255, fonte 96c5e60: Android compilou/lint 0 erros e 9/10 instrumentados passaram, incluindo swipe nativo e alça do editor com descarte protegido. Falha isolada no teste de Voltar com tecla sintética; trocado pelo GLOBAL_ACTION_BACK da UiAutomation, com asserção de aceitação pelo sistema e manutenção da verificação de fechamento. API de teste do SDK, sem dependência nova ou alteração de comportamento do app. iOS aprovado: 9 core + 7 UI e capturas finais com cápsula de contraste. Nenhuma alteração iOS na correção de teste seguinte.

Referência do teste de Voltar: [UiAutomation](https://developer.android.com/reference/android/app/UiAutomation), ação global do sistema após janela ficar ociosa; sem simular toque em coordenada fixa de navegação do dispositivo.

Execução 41369313164, fonte ddd61b6: Android manteve 9/10 instrumentados aprovados; Global Back aceito pelo sistema, mas painel não sumiu no teste. Adicionado diagnóstico da janela ativa antes/depois e PNG exato da falha, preservado pelo script sem transformar falha em sucesso. Nenhum novo comportamento de produto foi alterado com base em hipótese.

Execução 41380227823, fonte 95987b8: diagnóstico identificou janela android/android.widget.FrameLayout antes/depois do Voltar; os PNGs originais mostram “Pixel Launcher isn't responding” cobrindo o Arke nos dois instantes. Causa de infraestrutura comprovada para esta falha. O teste agora fecha exclusivamente esse aviso identificado por pacote e texto, exige janela ativa do Arke e só então envia Global Back. Avisos do Arke ou outros diálogos continuam falhando; nenhuma asserção de fechamento foi removida e nenhum comportamento de produto foi alterado. Nova execução necessária para validar o retorno real.

## Evidências verificadas — 02/11/2026

[Android aprovado](https://github.com/lucasouzadev/arke-viagens-e-momentos/actions/runs/41381270981), fonte `0fa908b0c9f0f59b9ec00d72e2d4e67949b8ce08`: build debug, 6 testes core + 10 instrumentados (8 UI/2 repositório), sem falhas/skips; lint 0 erros/9 avisos. Voltar global com janela do Arke ativa, swipe pela alça e fechamento por toque aprovados. A recuperação do Launcher é exclusiva do ambiente de teste, sem nova interceptação de Voltar no produto. [iOS aprovado](https://github.com/lucasouzadev/arke-viagens-e-momentos/actions/runs/41380227823), fonte `95987b8e22a992ffcec710ea96b618fe6e03d26f`: build Simulator, 9 core + 7 UI sem falhas. Fonte iOS idêntica à de `0fa908b`; mudanças posteriores são somente teste/diagnóstico Android e documentação. Total: 32 testes aprovados nas duas plataformas.

32 PNGs originais inspecionados: home/editor vazio/detalhe com nota/detalhe sem nota × padrão/compacto × claro/escuro × iOS/Android. Android final do artefato 13112539874; iOS inspecionado do artefato 13108193217, execução 41368315255, fonte 96c5e60, com o mesmo código iOS validado depois. iPhone 16/SE 3 e Android API 35 Pixel 6/viewport compacta. Nenhuma imagem editada; exemplos de detalhe em memória/Debug, sem gravar snapshots. Paridade de hierarquia/margens/alça externa e diferença semântica entre cartão e campos confirmada. Sistema/fonte/ícones/insets nativos mantêm diferenças próprias.

Geometria da alça acima do cartão, margens e ausência de X/contador verificadas por UI; posição permanece em accessibilityValue/stateDescription. Testes cobrem informação por toque, limites finitos, seleção/recriação Android, redução de altura sem nota, texto longo rolável, contexto/detent e editor com validação/descarte/gravação/reabertura/exclusão. Recuo da home e restauração exercitados pela abertura/fechamento; sem profiling ou teste manual de redução de movimento. Fonte 200%, leitores de tela, landscape/tablets, aparelho físico e profiling NÃO EXECUTADOS. Nenhum dado/schema/IO modificado. Falhas anteriores preservadas no histórico acima, sem skip ou supressão.

## Gates — revisão do agente implementador

| Gate | Resultado e alcance |
| --- | --- |
| G-01 | PASSOU: direção solicitada explicitamente, stack nativa, dois cartões e azul preservados. |
| G-02 | PASSOU: contrato e fronteiras registrados antes da implementação. |
| G-03 | PASSOU: shell de apresentação compartilhado localmente em cada plataforma; nenhuma mudança de modelo/schema/IO. |
| G-04 | PASSOU na revisão do diff e builds/lint executados; lint Android sem erros, nove avisos registrados, incluindo ic_close sem uso (P3). |
| G-05 | PASSOU: fluxos nativos e 32 capturas inspecionadas em pares; retorno por alça/swipe e Voltar Android aprovados. |
| G-06 | PASSOU em padrão/compacto, claro/escuro e cenários curto/longo das duas plataformas; fonte 200%, landscape/tablets e aparelho físico NÃO EXECUTADOS. |
| G-07 | PASSOU na revisão de alvos 48, rótulos e posição acessível, alternativas à alça e descarte protegido; VoiceOver/TalkBack e auditoria global de contraste NÃO EXECUTADOS. SwiftUI respeita Reduce Motion; Compose utiliza a escala nativa das animações. Alternância manual de redução de movimento NÃO EXECUTADA. |
| G-08 | PASSOU: sem nova rede/permissão/credencial/dependência/mídia. Diagnóstico limitado ao app Debug sintético e janela de sistema do emulador; não coleta dados reais. Nova auditoria MASVS NÃO APLICÁVEL à apresentação sem nova fronteira de dados. |
| G-09 | NÃO APLICÁVEL a nova coleta/mídia/licença: nenhuma introduzida. |
| G-10 | PASSOU nos testes de draft/persistência/retry/corrupção executados; modelo e snapshots preservados. |
| G-11 | Profiling NÃO EXECUTADO; geometria iOS só atualiza ao variar mais de 1 pt. Não há promessa de desempenho medido. |
| G-12 | PASSOU: 32 testes aprovados, builds/lint e 32 PNGs; histórico de falhas/diagnóstico/correção preservado. |
| G-13 | PASSOU: PR rascunho, sem merge/release/assinatura; rollback de apresentação preserva snapshots. |
| G-14 | PASSOU: guia/contrato/STATUS/HANDOFF/ROADMAP/INDEX/CHANGELOG e PR reconciliados com evidências e limites; links locais verificados. |
| G-15 | PASSOU: preferência privada restrita ao chat e ausente dos artefatos. |

Revisão visual humana final pendente. Risco residual P2 nos ambientes não exercitados e P3 dos avisos de lint; nenhum P0/P1 identificado neste recorte. Próximo: colher revisão deste resultado antes de avançar para edição segura de ideias salvas sob contrato próprio. Reversão por commit de apresentação preservando snapshots. PR #2 rascunho, sem merge/release.
