# M5-HANDLE-01 — Puxador entre cartões

Data: 01/11/2026. Estado: implementado e verificado no PR #2, sem merge. Milestone M5, branch chore/development-kickoff, PR #2.

Autorização: responsável autorizou continuidade após o próximo item apresentado: puxador entre cartões. DESIGN-001/002 orientam o gesto exclusivo; somente este recorte de DESIGN-004 é implementado, sem aprovar suas demais propostas.

Objetivo: ajustar continuamente o espaço do mapa arrastando a faixa entre cartões e encaixar em Explorar/Equilibrado/Detalhes ao soltar. Preservar proporções atuais (65/45/25% com limites de espaço), cores semânticas, cabeçalho e contexto. Toque abre seletor existente; ações de acessibilidade equivalentes. Sem animação automática, haptics, gestos radiais, Pin, persistência ou backend.

Arquivos autorizados: HomeView.swift, ArkeHome.kt, strings Android, testes UI existentes, scripts de captura e upload de PNGs no workflow quando necessário e documentos de design/engenharia/continuidade/changelog. Sem nova dependência. Gestos pertencem somente à faixa do puxador; scroll e mapa não redimensionam. Translação transitória não é salva e cancelamento retorna a estado válido. Respeitar mínimos já existentes de mapa e cabeçalho; texto ampliado/landscape/tablets continuam sujeitos a revisão própria.

Aceite: arrastar para cima reduz mapa, para baixo amplia; encaixe nos três estados; toque continua funcional; contexto preservado; scroll do corpo não altera proporção; testes nativos executados e oito imagens padrão/compacto claro/escuro inspecionadas. Testes de drag usam coordenadas relativas ao controle para representar o gesto, sem contornar hit testing de botões.

G-01/02 PASSOU na revisão do escopo aprovado. G-03/04/05/06/07/12 aguardam diff/CI/testes/PNG. G-08/09/10 NÃO APLICÁVEIS a novos dados/rede/mídia, pois não adicionados. G-11 profiling NÃO EXECUTADO. G-13 sem distribuição/merge, reversão por commit na branch. G-14 atualizar ao concluir. G-15 preferência restrita ao chat. Leitores de tela, fonte 200%, landscape/tablet e profiling NÃO EXECUTADOS; não inferir aprovação global das capturas. Risco P2: conflito entre toque/drag e overflow; interromper e corrigir sem ocultar testes. Nenhum P0/P1 identificado no recorte.

Primeira execução: 41307389121, fonte 2ce0166. Android aprovou testes, mas as quatro capturas foram REJEITADAS na inspeção por diálogo de ANR do Pixel Launcher sobre o app. Captura agora valida o XML da janela e falha se houver diálogo do sistema ou ausência do Arke; PNG bruto fica disponível para diagnóstico mesmo em falha. Nenhum aviso é fechado automaticamente e falhas do app não são suprimidas. Nova execução necessária para evidência limpa.

Segunda execução 41308104680, fonte cae021d: Android passou os três testes UI e a validação das capturas sem diálogo; iOS passou os testes de cabeçalho/aviso, mas o novo teste falhou antes de arrastar por buscar a opção ativa sem seu sufixo ✓. Consulta agora aceita explicitamente rótulo selecionado ou não selecionado, sem alterar expectativas de gesto. Lint Android: 0 erros/9 avisos; investigar aviso adicional na revisão final.

Aviso adicional do lint identificado: nome do parâmetro Modifier em DetentMenu. Renomeado conforme a convenção Compose, sem supressão ou alteração funcional.

Execução 41308974373, fonte 03937c6: Android completo aprovado (lint 0/8), quatro PNGs limpos inspecionados. iOS aprovou cabeçalho, aviso, arrasto para Detalhes e scroll sem redimensionar, mas falhou ao retornar a Explorar. Hipóteses: disputa toque/drag no botão e coordenada do alvo móvel. Puxador agora prioriza DragGesture quando exceder distância mínima; teste mantém origem no centro hittable do controle e congela o destino no espaço da janela. Evidência do estado após drag anexada ao XCTest; script de falha preserva a tela atual sem relançar o app. Expectativas originais mantidas, nova execução necessária.

Referências técnicas primárias: [XCUICoordinate](https://developer.apple.com/documentation/xcuiautomation/xcuicoordinate) documenta coordenadas dinâmicas relativas ao elemento; a janela estável evita que o destino acompanhe o puxador. [highPriorityGesture](https://developer.apple.com/documentation/swiftui/view/highprioritygesture(_:including:)) documenta precedência sobre os gestos existentes. A causa exata da falha anterior não foi isolada; correção conjunta sujeita ao teste original.

Execução 41310026574, fonte f2e8f67: primeiro job Android compilou e passou lint 0/8, mas não iniciou emulador por imagem Google APIs com arquivo ZIP inválido no download do SDK. Falha de infraestrutura, nenhum teste UI executado nessa tentativa. Código Android idêntico à fonte 03937c6 já verificada; repetir job somente após encerrar o run (API não permite enquanto iOS roda).

## Resultado final

[CI aprovado](https://github.com/lucasouzadev/arke-viagens-e-momentos/actions/runs/41310026574), fonte `f2e8f67496c6b6c21657f6295209f71371f76800`. iOS aprovado na tentativa 1 e Android na tentativa 2 após download íntegro da imagem do sistema. Três testes unitários por núcleo e três testes UI por plataforma passaram, sem skip; lint Android 0 erros/8 avisos anteriores. XCUITest confirmou Explorar após o arrasto de retorno, mantendo o mesmo critério que falhou antes. Toque continua funcional com prioridade de drag após a distância mínima.

Oito PNGs originais inspecionados: iPhone 16 (1179×2556) e SE 3 (750×1334), Android API 35 padrão (1080×2400) e compacto (720×1280), claro/escuro. Puxador/cabeçalho visíveis e superfícies sem contorno preservadas. Testes de gesto executados nos aparelhos padrão; capturas compactas não equivalem a testes de gesto nesses tamanhos. MapKit real preservado; Google Maps Android continua fallback sem chave/projeto.

G-01/02/03/04 PASSOU: contrato, diff nativo, estado transitório e lint revisados. G-05 PASSOU nos fluxos testados: arrasto nos dois sentidos, encaixe, preservação de contexto, toque e scroll independente. G-06 PASSOU somente na inspeção das quatro viewports/temas; fonte 200%, landscape/tablet e teclado NÃO EXECUTADOS. G-07 PASSOU na inspeção visual e presença de equivalentes por toque/ações; VoiceOver/TalkBack e movimento reduzido reais NÃO EXECUTADOS. G-08/09/10 mantêm NÃO APLICÁVEL para novas áreas de dados/rede/mídia, sem alterações; restrições anteriores de distribuição permanecem. G-11 profiling NÃO EXECUTADO. G-12 PASSOU conforme CI; G-13 PASSOU para build/captura sem distribuição, com falhas diagnosticadas e sem supressão; G-14 PASSOU com documentos e PR reconciliados; G-15 PASSOU na revisão do diff sem valor privado em arquivos.

Nenhum P0/P1 identificado neste recorte, risco P2 de comportamento em configurações ainda não exercitadas. Próximo recorte sugerido: fluxo de adicionar lugar, com contrato próprio de editor/persistência antes de salvar dados. Não criar Google Cloud/faturamento/segredos. Mesma branch/PR #2 em rascunho; sem merge/release.
