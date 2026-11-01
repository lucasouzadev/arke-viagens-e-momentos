# ACCESSIBILITY-UI — Acessibilidade, responsividade e UX nativa

**Status:** baseline obrigatório de UX; testes nativos só após código/autorização. Referência técnica de qualidade: **WCAG 2.2 nível AA como meta interna aplicável**, adaptada a aplicativos nativos; não declarar certificação WCAG ou equivalência legal automática.

## Critérios essenciais por interação do Arke

| Recurso | Requisito |
| --- | --- |
| Relógio | abrir/fechar e escolher qualquer categoria por **toque e leitor de tela**, não exigir gesto circular |
| Carrossel | anterior/próximo acessíveis, nome/posição anunciados; jamais depender só do swipe |
| Detents | controle de expandir/recolher sem arrastar; informar estado atual |
| Mapa | criar/selecionar lugar por lista/pesquisa; GPS opcional e alternativa manual |
| Pins | distinguir ativo/pendente/salvo sem usar cor como único significado |
| Conteúdo/stamps | foco lógico, título legível, imagens com descrição/contexto, scroll independente |
| Modal novidades | fechar sem perder Trip/Pin/draft, foco restituído, não obrigar leitura |
| Finanças/grupo | formato de moeda local, estados e permissões anunciados sem confundir pago/declarado |

## Tela e dispositivos

- iOS: respeitar safe areas, Dynamic Type, VoiceOver, Voice Control, Reduce Motion, Reduce Transparency e configurações de contraste.
- Android: respeitar system insets, IME, font scaling, TalkBack, navegação por teclado/switch, Remove Animations, screen sizes/foldables.
- Verificar aparelho compacto e padrão, tablet/foldable, landscape, split screen/window resizing, modo claro/escuro e fonte ampliada (incluindo 200% se suportado).
- **Nenhum clipping, offscreen, sobreposição, conteúdo sob teclado ou botão fora da área clicável.** Prioridade a texto legível e ação segura, não à fidelidade de pixel.
- Testar nomes de destino longos, acentos, moeda, datas, idiomas futuros e fotos ausentes.
- O mapa real possui informação própria: colocar labels/ações em superfícies que mantenham contraste contra regiões claras/escuras do mapa.
- Alvos de toque confortáveis. No Android, **48dp × 48dp** como objetivo de touch target. No iOS, usar dimensões confortáveis conforme HIG; o WCAG 2.2 para Web tem alvo 24px com exceções, **não interpretar px CSS como dp nativo**.

## Qualidade perceptiva e não funcional

- Movimento curto, interrupção sem travar gesto e alternativa sem rotação/parallax.
- Preservar contexto em rotação, teclado, suspensão, offline, perda de foco e troca de tema.
- Nenhuma opção importante deve depender só de haptics, cor, transparência, áudio ou pressão longa.
- Navegação previsível: quando relógio fecha, foco retorna ao disparador; arrasto no corpo não altera detent; back não descarta rascunho inadvertidamente.
- Leitores de tela devem anunciar estado selecionado e cargo/permissão quando relevante; não anunciar ícones decorativos.
- Não obrigar localização precisa para salvar um Pin manualmente.

## Matriz de prova futura

1. Captura/gravação por plataforma em tamanhos compactos, padrão e maiores.
2. Testes manuais VoiceOver/TalkBack e navegação alternativa sem gestos.
3. Teste de fonte, contraste e tema do dispositivo, movimentos reduzidos, teclado.
4. Testes automatizados de UI/E2E e screenshots; **não substituir avaliação manual**.
5. Registrar defeitos P0/P1 e plano de correção antes de afirmar conclusão.

## Base normativa x meta de projeto

- **LBI brasileira** promove acessibilidade e desenho universal; relevância e obrigação específica exigem análise jurídica contextual.
- **WCAG 2.2 AA** é aqui uma meta verificável de design adaptada a UI mobile, não um selo nem prova legal automática.
- Recomendações nativas Apple e Android complementam critérios WCAG quando a interação não é Web.

Referências: [WCAG 2.2](https://www.w3.org/TR/WCAG22/), [Apple SwiftUI Accessibility](https://developer.apple.com/documentation/swiftui/accessibility-fundamentals), [Android Accessibility](https://developer.android.com/guide/topics/ui/accessibility/apps), [Lei 13.146/2015](https://www.planalto.gov.br/ccivil_03/_ato2015-2018/2015/lei/l13146.htm).

**Paridade obrigatória — 01/11/2026:** [DESIGN-006](../design/DESIGN-006.md) orienta as duas plataformas. Alça visível em sheets, Cancelar no topo esquerdo, mesma hierarquia de ações/campos e métricas-base de texto/espaçamento. APIs e materiais continuam nativos; não usar diferenças de plataforma para mudar a organização do fluxo. Revisar pares de capturas antes de avançar.
