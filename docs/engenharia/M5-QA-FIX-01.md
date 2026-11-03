# M5-QA-FIX-01 — Interação da beta

2 de novembro de 2026. UI e localização simulada em revisão; suíte final aprovada. Autorização explícita no chat para corrigir QA-BETA-01 nas bases nativas, branch chore/development-kickoff/PR #2, sem merge ou nova beta automática. Escopo: UI, estados/testes, CI de evidência, localização em primeiro plano e continuidade. Snapshot v1 e ideias existentes preservados. O pedido atual substitui alça externa/CTA fixo e a restrição histórica de GPS apenas para posição pessoal contextual.

## Resultado implementado

| Problema | Implementação atual |
| --- | --- |
| Gesto da home | Base absoluta estável durante o arraste; detent/reset aplicados na mesma transação. Câmera explícita, sem recorte que esconda atribuições do mapa. |
| Sheet/mesa de vidro | Uma apresentação nativa com superfície semântica; retirados cartão interno duplicado e recuo manual iOS. Alça interna com gesto do host e dispensa acessível protegida. |
| Teclado | Sem toolbar Concluir. Adaptador UIKit local limita UIHostingController ao keyboardLayoutGuide; safeAreaRegions.container evita compensação dupla. Home ignora o teclado ao fundo. Sem observers/alturas calculadas. |
| Escrita | Campos maiores agrupados, separadores suaves, área de foco ampliada e editores expansíveis. Revisar/Salvar integram a rolagem; fades decorativos superior/inferior e indicadores ocultos. |
| Relógio | Seleção central com encaixe: wheel nativo iOS e lista com snap/ponteiro Android. Alternativa por toque e Concluído preenchido. |
| Ideias | Cartões próprios sem bordas, nome único, nota resumida em três linhas e ações Abrir/excluir na mesma linha. Detalhes mantêm todo o texto; exclusão confirmada. |
| Vazio/feedback | Uma frase curta no vazio, confirmação transitória de salvamento. Informação sobre dados locais permanece no i. |
| Posição pessoal | Botão contextual de 48; pedido apenas em primeiro plano, sem armazenamento de coordenadas, background ou compartilhamento próprio. Mapas/permissionamento reais Android dependem da chave privada ausente. |

## Evidências verificadas

- **iOS d138616 / [41531702064](https://github.com/lucasouzadev/arke-viagens-e-momentos/actions/runs/41531702064), job 125575720532:** Xcode 27.0/runner arm64, iPhone 14/iOS 27.0. Build, 10 core e 8 UI PASSARAM. Oito cenários mantidos: validação/descarte, contextos, detalhes finitos, foco/viewport/nota longa/revisão, swipe de sheet, detalhe longo, arraste da home e salvar/reabrir/editar/excluir. Vinte PNGs originais de iPhone 14 e SE3, claro/escuro, inspecionados; gravação de interação inspecionada na escrita e no gesto. Aviso de metadados AppIntents sem dependência é da ferramenta; sem supressão.
- **Android a6bce1d / [41536109808](https://github.com/lucasouzadev/arke-viagens-e-momentos/actions/runs/41536109808), job 125588232527:** branch temporária chore/qa-android-retry, somente workflow difere de d138616; árvores android/ios/scripts iguais. API 35/Pixel 6: build, 6 core e 12 instrumentados PASSARAM; lint 0 erros/19 avisos, sem supressão. Vinte PNGs padrão/compacto e claro/escuro inspecionados; APK debug gerado. Avisos: target/versões, ícone de aplicativo e strings antigas sem uso. SHA256 APK: 9F5FC49DC0D2A7DF56C44C29065FE8F3EE9A2F187FD006F126C41F033049EA70.
- **Complemento fc8b349 / [41541148886](https://github.com/lucasouzadev/arke-viagens-e-momentos/actions/runs/41541148886), iOS job 125605320868:** os dois testes de localização PASSARAM: ausência de prompt inicial, permissão no toque, fix por proxy nativa, recusa/retry. Captura original confirma mapa aproximado e ponto; referência pública de QA, nunca a posição de uma pessoa. Nove dos dez UI passaram; primeiro caso foi interrompido na abertura por Failed to get background assertion...Timed out, antes das interações. Demais fluxos e Android aprovados. Não é aprovação integral deste run. As verificações de validação/descarte já passaram em d138616 e 906cb03, com produção equivalente.
- **Consolidação 3ba7343 / [41544358087](https://github.com/lucasouzadev/arke-viagens-e-momentos/actions/runs/41544358087):** ambas as plataformas PASSARAM. iOS job 125616169822: 10 core, 10 UI, build e 20 PNGs/filmagem gerados. Android job 125616169523: 6 core, 12 instrumentados, build/lint e 20 PNGs/APK. Abertura inicial, nota longa/revisão e ambos os cenários de localização aprovados juntos. Código de produção equivalente às evidências visuais acima; ajuste final só no teste de abertura.

A gravação usa apenas dados fictícios e é artifact temporário de sete dias, conforme autorização de QA; não contém assinatura ou dados pessoais reais. O GIF de gesto inicialmente exibido veio de d2410e0 e não valida o teclado; frames de escrita/capturas atuais vêm de d138616. Capturas estáticas são originais; quadros extraídos de vídeo são identificados como tal.

## Diagnóstico preservado

| Fonte/run | Resultado e aprendizado |
| --- | --- |
| 11869a1 / 41513478869 | iOS 5/8 UI; Revisar inacessível com teclado. ScrollView y163..852, teclado a partir de y548. Android aprovado. |
| 70f623b / 41520708559 | Android aprovado; etapa iOS não terminou e foi cancelada, sem evidência suficiente de UI. Não usar como aprovação iOS. |
| d2410e0 / 41527925569 | Ambiente corrigido para iPhone 14/iOS 27; iOS 5/8 UI. ScrollView y151..844, teclado a partir de y516. NavigationStack sozinho insuficiente. |
| d138616 / 41531702064 | Adaptador keyboardLayoutGuide resolveu os oito cenários iOS. Android falhou antes do app ao descompactar imagem do SDK (ZipFile unknown archive); repetição isolada a6bce1d aprovada, sem mudar código do app. |

Tentativas anteriores de compensação por notificações/medidas de viewport foram retiradas. A solução atual usa Auto Layout e o guia nativo. O seletor de runtime exige iOS 27.0 exato, sem misturar visionOS ou o runtime 27.1 destinado a outro aparelho. Boot tem limite de 360 s/dispositivo. Não houve remoção de cenário para produzir aprovação.

## Limites e fronteira de dados

Aparelho informado: iPhone 14/iOS 27; reprodução realizada em simulador, sem Xcode local neste Windows. Aparelho físico, iOS mínimo suportado, fonte 200%, VoiceOver/TalkBack, contraste global, redução de movimento, landscape/tablets, seleção/cursor manual, gestos adicionais de reversão/cancelamento e profiling NÃO EXECUTADOS. Automação de foco/teclado e vídeo não equivalem a toda essa matriz. Permissão/recusa/fix simulados aprovados; GPS real e localização aproximada/restrita/desligada ainda exigem validação própria.

Google Maps real BLOQUEADO por configuração privada; fallback não pede permissão. Não criar conta/faturamento. Sem backend, geocoding, contas, grupos, sync, compartilhamento ou migração. Posição é transitória; não há coordenadas pessoais em logs/fixtures/relatórios. APIs nativas/provedores e declarações de privacidade de loja devem ser revistos antes de distribuição pública. Sem promessa de certificação MASVS/WCAG.

Nenhuma nova build enviada ao TestFlight: a beta anterior é 0.0.1 (8.1). Job de assinatura não alterado/disparado neste recorte; antes de novo envio, alinhar toolchain à efetivamente validada. Sem merge, convite ou publicação pública. Rollback somente do código preserva o snapshot v1 e as ideias salvas; nunca apagar dados para contornar teste ou instalação.

## Gates — revisão do agente implementador

| Gate | Estado e alcance |
| --- | --- |
| G-01 | PASSOU: dois cartões, stack nativa e um azul; DESIGN-006 atualizado prevalece. |
| G-02 | PASSOU: contrato/autorização/fronteiras registrados antes das alterações. |
| G-03 | PASSOU na revisão: adaptador de apresentação local, fluxo/store/repositórios mantidos. |
| G-04 | PASSOU em builds/lint, XML, sintaxe Bash/Python embutido e diff; avisos registrados. |
| G-05 | PASSOU nos fluxos UI exercitados, permissão/recusa/fix simulado e inspeção de 40 PNGs/vídeo; matriz manual ampliada pendente. |
| G-06 | NÃO EXECUTADO integralmente: padrão/compacto/claro/escuro aprovados; fonte ampliada/rotação/tablets/aparelho físico pendentes. |
| G-07 | NÃO EXECUTADO integralmente: alvos, labels, alternativa de toque e foco automatizado revisados; leitores/contraste/movimento reduzido pendentes. |
| G-08 | PASSOU na revisão proporcional: sem credenciais/dados pessoais em código ou artifacts; sem backend/dependência nova; auditoria MASVS completa NÃO EXECUTADA. |
| G-09 | PASSOU na delimitação de privacidade: pedido contextual/sem persistência/background; declarações de loja e uso físico ainda exigem revisão. |
| G-10 | PASSOU nos testes de validação, descarte, update/retry/conflito/identidade/reabertura/exclusão; JSON v1 mantido. |
| G-11 | NÃO EXECUTADO: vídeo de gesto inspecionado; profiling não realizado. |
| G-12 | PASSOU: 38 testes na suíte final 3ba7343, builds/lint, 40 capturas de produção equivalente e gravação inspecionada. |
| G-13 | PASSOU na fronteira operacional: APK temporário, sem nova beta/merge/publicação; rollback preserva dados. |
| G-14 | PASSOU: STATUS/HANDOFF/ROADMAP, changelog, contrato e PR reconciliados com resultados e limites. |
| G-15 | PASSOU: preferência pessoal restrita ao chat e ausente dos artefatos. |

Revisão humana pendente. Não promover release com regressão de escrita/salvamento. As pendências manuais e de configuração externa não são convertidas em aprovação automática.

Referências oficiais: [keyboardLayoutGuide](https://developer.apple.com/documentation/uikit/adjusting-your-layout-with-keyboard-layout-guide), [safeAreaRegions](https://developer.apple.com/documentation/swiftui/uihostingcontroller/safearearegions), [runner Xcode 27](https://github.blog/changelog/2026-10-15-xcode-27-runner-image-generally-available/), [simulação de localização](https://developer.apple.com/documentation/xcode/simulating-location-in-tests).

Complemento 906cb03/run 41538918894: Android aprovado novamente; iOS 9/10 UI, incluindo recusa/retry e ausência de prompt inicial. Falhou somente o fix simulado após Allow Once (20 s). Alterar apenas a fixture: XCUIDevice.shared.location/XCUILocation configurados no próprio método, conforme documentação Apple; retirar simctl set anterior à sessão. Usar ponto público do exemplo Apple e limpar a proxy ao terminar. Código de produção mantido. Nova execução necessária, sem declarar fix ou localização física aprovados.

Fonte fc8b349/run 41541148886: Android passou; iOS passou os dois testes de localização (proxy nativa/consentimento/recusa) e os demais fluxos, exceto a inicialização do primeiro caso, interrompida pelo XCTest com Failed to get background assertion...Timed out. Não é aprovação integral desse run. Trocar apenas a primeira abertura por activate(): na instalação nova mantém o cenário de abrir o app, e evita terminar uma instância já preparada pelo runner; falhas de ativação continuam falhando o teste. launch/relaunch de persistência nos demais cenários preservados. Produção inalterada. [Apple activate](https://developer.apple.com/documentation/xcuiautomation/xcuiapplication/activate()).
