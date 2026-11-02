# QA do Arke — primeira beta

2 de novembro de 2026. Resultado: problemas de interação e composição confirmados. Este relatório é uma análise; não representa correções já instaladas nem aprovação da interface atual.

## Evidências e limites

- Vídeo de aproximadamente 104 segundos fornecido no chat: análise visual por amostragem de 24 quadros da sequência e 48 quadros adicionais, separados por 250 ms, entre 86 e 98 segundos. Os tempos abaixo são aproximados.
- Código SwiftUI/MapKit e Kotlin/Compose da branch de desenvolvimento.
- Capturas originais existentes do Actions 41491276051: editor escuro do iOS padrão e Android compacto inspecionados diretamente. O pacote iOS contém 10 PNGs, sem captura compacta; não constitui uma matriz visual completa. Nenhuma imagem foi alterada para simular uma correção.
- Não executei uma nova sessão interativa em simulador nesta análise. Não há Xcode local neste Windows. As imagens do Actions são evidências anteriores; não reproduzem o toque, teclado e compositor do aparelho iOS 26 do vídeo. A piscada Android continua sem reprodução confirmada.
- Testes funcionais anteriores terem passado não valida fluidez, aparência do iOS 26 ou área real de toque dos campos. Não foi encontrado indício suficiente para afirmar perda de dados ou crash no vídeo.

## Achados por item

| Item | Evidência e causa | Correção necessária e aceite |
| --- | --- | --- |
| 1. Gesto — prioridade alta | Entre 86–98 s, mapa/cartão alternam alturas abruptamente; perto de 96 s aparecem faixas sem mapa. HomeView recalcula a altura real do Map a cada atualização do gesto; a mesma hierarquia muda de tamanho durante o arraste. O reset de GestureState e a troca de detent também precisam ser instrumentados. A ocorrência é confirmada; a causa exclusiva ainda não está demonstrada. | Estabilizar base do gesto e viewport do mapa, separar deslocamento contínuo de snap final e impedir animações concorrentes. Registrar vídeo de arraste lento, rápido, reversão e cancelamento; verificar quadros intermediários, não só início/fim. Não reinstanciar/recentrar o mapa ao mover o painel. |
| 2. Sheet/vidro — alta | A partir de 13 s, há uma superfície nativa maior atrás do cartão branco personalizado. ArkeFloatingSheet usa sheet nativo, fundo clear, raio externo zero, margens próprias e outro cartão recortado. A home ainda recebe escala manual. Há duas composições sobrepostas. | Usar um único sheet nativo com uma superfície coerente e alça interna. Remover o cartão interno duplicado, o recuo manual redundante e ajustes que competem com a apresentação do sistema. Validar no iOS 26 e no fallback iOS 17/18. Não adicionar blur/vidro por cima. |
| 3. Concluir do teclado — média | Entre 21–35 s, o botão aparece como acessório. É criado explicitamente por ToolbarItemGroup(placement: .keyboard) em PlaceEditor, não exigência do teclado. | Remover essa toolbar. Manter navegação Próximo/Retorno do teclado e dispensa por rolagem; não inserir outro botão equivalente. |
| 4. Campos e CTA — alta | Grande bloco sem divisões, com conteúdo digitável pequeno dentro de áreas altas. TextField recebe frame externo alto; isso não garante que toda a superfície visual seja um editor. CTA fica fora do ScrollView e disputa espaço com teclado. O fade existe apenas embaixo; indicadores permanecem visíveis. Android também agrupa rótulos/áreas vazias sem separação. | Um grupo com linhas reconhecíveis: rótulo discreto, superfície editável com altura real e separadores internos suaves, sem contorno. Nome em linha única; endereço e nota com editor multiline adequado. Toda a área deve focar o campo sem interceptar seleção/cursor. Colocar Revisar ao final da rolagem durante escrita; preservar salvar/carregando/erro na revisão. Ocultar indicadores e aplicar fades superiores/inferiores que não capturem toque. |
| 5. Alça — alta | No iOS a alça externa é um Button com onDismiss; o indicador nativo está oculto. O editor bloqueia swipe quando há alterações. Portanto não é uma alça de redimensionamento implementada pelo app. No Android há ModalBottomSheet, mas a alça visual também é um TextButton customizado: o comportamento não pode ser presumido igual. | Restaurar indicador/gesto do sheet nativo dentro do painel nos dois sistemas. Preservar proteção do rascunho: arrastar não pode apagar alterações; rejeitar fechamento ou mostrar confirmação conforme suporte nativo. Não bloquear salvar nem criar duas alças. |
| 6. Relógio — funcionalidade incorreta | Por volta de 52 s aparece um menu comum. iOS usa confirmationDialog e Android DropdownMenu. Não existe o mecanismo de seleção por ponteiro solicitado. | Seletor de contextos com posição de seleção fixa: vizinhos transitam, próximo contexto encaixa no mesmo ponto e permanece selecionado após soltar. Deve funcionar nos dois sentidos, anunciar a seleção ao leitor de tela e oferecer avanço/retorno por toque. Não usar rolagem livre de toda a faixa nem depender do ícone de relógio para representar o comportamento. |
| 7. Conteúdos — média | Entre 60–83 s: título geral, aviso de salvamento, nome/endereço/nota, ações textuais e rodapé técnico competem. Cada ideia é um VStack/Column seguido de divisão, sem superfície própria. O exemplo usa Teste em três campos diferentes; essa repetição específica vem dos dados inseridos, não é prova de duplicação na persistência. | Cada ideia vira um cartão, sem borda: nome uma vez, referência secundária e nota resumida opcional. Abrir pelo cartão com indicação discreta; edição e exclusão por ações acessíveis com ícones/labels sem exposição permanente repetitiva. Exclusão sempre confirmada. Remover avisos técnicos permanentes da lista e preservar a informação relevante no i. |
| 8. Responsividade/mapa — alta | Teclado comprime o corpo; margens do host nativo somam-se às próprias e ao recuo. Estado vazio ocupa espaço com três camadas de explicação. O mapa inicia em latitude/longitude zero, sem posição pessoal; Android configura isMyLocationEnabled=false. | Rever safe areas no sheet nativo e a estrutura rolável; simplificar vazio para uma frase curta e ação clara. Posição pessoal é uma entrega adicional de localização em primeiro plano, solicitada neste QA, com permissão contextual e alternativa quando negada; não é um ajuste puramente visual. Android ainda depende da chave Google Maps. |

## Sobre Liquid Glass

O iOS 26 já usa Liquid Glass em sheets de altura parcial e adapta a superfície conforme a expansão. Não há evidência de que falte um interruptor para habilitá-lo. A hipótese principal é a combinação da apresentação nativa com o cartão customizado e suas margens. A Apple recomenda reavaliar fundos personalizados ao adotar a apresentação nova. A solução precisa ser testada no runtime do iOS 26; compilar com SDK 26 não basta.

Referência: [Apple — Build a SwiftUI app with the new design, trecho sobre sheets](https://developer.apple.com/videos/play/wwdc2025/323/).

## Outros problemas identificados

1. **Encolhimento excessivo com teclado:** o editor mantém cabeçalho, CTA, margem e alturas mínimas enquanto a área útil diminui. É preciso testar campos inferiores com texto longo e teclado aberto.
2. **Popover de informação denso:** largura fixa de 240 pt e explicação longa. Verificar texto ampliado, leitura e fechamento; preferir informação curta e painel nativo rolável quando necessário.
3. **Aviso persistente após salvar:** compete com o título e a lista. Usar confirmação transitória acessível, sem empurrar conteúdo permanentemente.
4. **Alvo de redimensionamento da home é também botão:** uma tentativa de arraste curta pode abrir o menu de proporção; distinguir toque e arraste, com tolerância e cancelamento.
5. **Mapa muda de enquadramento ao mudar altura:** além da piscada, muda a leitura geográfica. Preservar intenção da câmera durante resize e verificar atribuição/legal do mapa sempre visível.
6. **Capturas estáticas insuficientes:** o vídeo mostra defeitos que não aparecem em PNG. A regressão precisa conter gravação/checagem temporal, testes de toque fora da linha do texto, rolagem com teclado e descarte via gesto.
7. **Fades com acessibilidade:** ocultar a barra não pode retirar a capacidade de rolar; fades devem ser curtos, decorativos e não esconder a última linha ou ações. Não aplicar máscara sobre créditos obrigatórios do mapa.

## Paridade e ordem de execução

1. Corrigir base nativa dos sheets e resize da home; gravar as interações no iOS 26.
2. Corrigir foco/área editável, teclado, CTA no fluxo de escrita, separadores, safe areas e fades; aplicar a mesma organização no Android.
3. Implementar seleção por ponteiro e cartões de ideias, conservando IDs e persistência.
4. Adicionar localização pessoal em primeiro plano. Pedir somente ao acionar Minha localização; tratar concedida, negada, restrita, indisponível e localização aproximada. Sem rastreamento em segundo plano ou compartilhamento. Não inventar ponto pessoal quando não houver posição. Concluir configuração privada Google Maps antes de prometer o mapa Android.

O pedido atual substitui as diretrizes antigas de alça externa e CTA sempre fixo. A paridade continua obrigatória, com componentes nativos em ambas as plataformas.

## Aceite antes da próxima beta

- iOS 26 e versão mínima suportada; Android com e sem configuração Maps; tamanhos padrão/compacto; claro/escuro.
- Arrastes repetidos da home e do sheet, lento/rápido/reverso, sem piscar, saltar ou mudar contexto involuntariamente.
- Toque em todos os pontos de cada campo: foco abre teclado; texto, cursor e seleção permanecem utilizáveis.
- Nota/endereço longos com teclado aberto: rolar até fim, ler última linha e alcançar Revisar sem conteúdo encoberto.
- Rascunho alterado protegido em X, swipe, Voltar Android e troca de orientação. Erro de salvar mantém dados.
- Relógio encaixa uma seleção por posição, sem carrossel livre; leitor de tela anuncia contexto e oferece alternativa ao gesto.
- Fonte ampliada, VoiceOver/TalkBack, redução de movimento e permissões de localização negadas.
- Nenhuma migração/apagamento de ideias, convite automático ou publicação pública decorrente do QA.

Estado: análise concluída. Correções de produto, novas gravações em simuladores e validação em aparelho ainda não executadas neste relatório.

## Contrato e gates da análise

Task M5-QA-01, em revisão. Escopo desta entrega: análise do vídeo, código e evidências nativas existentes; arquivos autorizados: relatório/STATUS/HANDOFF. Sem alteração funcional ou nova distribuição. Referências: AGENTS, DESIGN-006, QUALITY-GATES, SAFEGUARDS. Risco P2 de atribuir causa sem reprodução; distinguir observação, hipótese e teste não executado. G-01/02/03/08/09/10/14/15 revisados para a análise documental; G-04 não aplicável (sem código novo); G-05/06 têm defeitos encontrados; G-07/11/12 não executados nesta rodada; G-13 sem novo envio. Nome pessoal isolado no chat. Aceite da análise: cobrir os oito itens, paridade e limites de evidência; atendido. Aceite das correções: pendente, matriz acima. Reversão documental preserva app e snapshots.
