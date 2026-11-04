# QA da janela e dos modos do Arke

Direção aprovada no Estúdio de Janelas. 01B, 02A, 03A, 04A, 05B, 06D, 07A, 08A, 09C + 09E, 10A + 10E, 11A e 12B. O código de referência desta rodada é `e6498e2`. [CI nativo](https://github.com/lucasouzadev/arke-viagens-e-momentos/actions/runs/41860016093).

| Item | Estado | Aceite |
| --- | --- | --- |
| 1 · Janela do mapa | Busca compacta no iOS e ajuste de altura nas duas plataformas. QA pendente. | Arrastar até os extremos em tela pequena. Busca, localização, bússola, expansão e atribuição continuam dentro do recorte e em zonas distintas. |
| 2 · Contraste, bússola e halo | Controles iOS com cor branca no escuro, expansão movida e halo implementado. QA pendente. | Conferir texto dos controles e legibilidade da cartografia em diferentes fundos, além de zoom e rotação. O halo fica dentro da borda e não parece uma linha. |
| 3 · Mapa expandido | Fundo escurecido, botão de expandir oculto e alça inferior implementados. QA pendente. | Arrastar a alça externa para fechar. Confirmar que pan e zoom no mapa não fecham a tela e que a alça tem ação acessível. |
| 4 · Divisória | Traço fino sem texto e sem menu. QA pendente. | Arrastar entre os modos em ambas as plataformas. As opções acessíveis ficam na própria alça. |
| 5 e 5.1 · Janela e cabeçalho | Recorte e tratamento de borda iniciados. Faixa editorial completa e desfoque progressivo ainda pendentes. | Conteúdo passa sob o cabeçalho inferior sem competir com seus controles. Sheets curtos mostram o conteúdo direto. O desfoque de sheets entra quando expandidos. |
| 6 · Indicadores | Indicadores suprimidos nas superfícies revisadas do iOS. Auditoria global pendente. | Conferir todas as rolagens verticais e horizontais, inclusive listas nativas, edição, busca e sheets em ambas as plataformas. |
| 7 · Quatro modos e hierarquia | Planejamento tem miniaturas em Explorar e capa maior em Detalhes. Composições de Gastos, Cofrinho, Avaliações e Memórias pendentes. | Cada ferramenta muda de composição em Explorar, Equilibrado e Detalhes. Mapa expandido escurece e desativa a interação atrás dele. Capas, títulos e metadados têm hierarquia, sem cartão redundante ou lista como composição padrão. |
| 8 · Seus lugares | iOS ajusta o efeito de rolagem à altura. Folha curta e paridade ainda pendentes. | Com um lugar e texto curto, a folha mede o conteúdo e não rola. Com texto longo ou fonte ampliada, rola apenas o excedente. |
| 9 · Link | Normalização HTTPS e texto de erro atualizados nas duas plataformas. QA pendente. | Salvar e abrir `exemplo.com`, preservar URLs HTTP e HTTPS existentes e recusar esquemas inválidos. |
| 10 · Marcar no mapa | Busca de endereço e abertura de viagem por Pin implementadas. Ação de localizar no editor Android e teste com Google Maps configurado pendentes. | Sugerir um resultado inequívoco, listar opções em caso ambíguo, permitir busca, localização e ajuste manual. Tocar um Pin vinculado abre a viagem. Sem vínculo, verificar se o destino abre os detalhes do lugar. |
| 11 · Gastos e Cofrinho | Área do teclado, ação de fechar e bloqueio do redimensionamento ajustados. Redesenho e QA de foco pendentes. | Com teclado aberto, o campo focado, Salvar e Fechar teclado continuam alcançáveis. Verificar resumo por moeda e progresso pessoal da meta. |

## Achados adicionais das gravações

- **V1, 00:58–01:06.** A ação inferior do editor fica parcialmente cortada na expansão. Rodapé reservado no iOS; validação pendente. Testar a transição em ambas as direções com e sem teclado.
- **V2, 01:33–01:40.** A capa pode ficar no topo e o título do lugar sair da janela. O cabeçalho agora mostra o lugar selecionado; validação pendente. Manter o nome identificável ao rolar e verificar a posição ao trocar de contexto e voltar.
- **V2, 02:00–02:07.** A altura do mapa muda e ele chega a expandir com o teclado do Cofrinho aberto. Bloqueio implementado na home. Validar que editar, salvar e dispensar o teclado não alteram a proporção escolhida.

Os vídeos cobrem o iOS em modo escuro. Validar também modo claro, fonte ampliada, telas compactas, VoiceOver, TalkBack e mapa Android configurado.

A logo fornecida foi aplicada aos ícones de inicialização iOS e Android. Conferir o enquadramento no aparelho na próxima rodada.
