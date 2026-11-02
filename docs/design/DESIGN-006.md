# DESIGN-006 — Guia comum de UI/UX iOS e Android

Status: direção aprovada explicitamente no chat em 01/11/2026; implementação verificada em M5-PARITY-01, disponível para revisão no PR #2. Aplica-se a todas as próximas telas e componentes. Natividade preserva tecnologia/semântica, mas não é motivo para mudar posição, hierarquia ou espaçamento de ações equivalentes.

## Regras comuns

- Mesma ordem, rótulos, grupos de conteúdo, posição de ações e estados por fluxo.
- Editor/revisão: apresentação grande, próxima de 94% da área disponível, alça externa visível central; cabeçalho fixo com Cancelar à esquerda, título central e coluna direita reservada para equilíbrio. Corpo rolável independente; ações no rodapé fixo acima do teclado/safe area.
- Limite inferior do corpo rolável: fade decorativo de 32 pt/dp até a superfície do sheet, sem barra sólida, contorno ou sombra atrás das ações. Padding final de 48 permite revelar integralmente o último campo/texto acima da transição; efeito não bloqueia toque/foco. Mesmo tratamento no editor/revisão dos dois sistemas e nos dois temas. [M5-FADE-01](../engenharia/M5-FADE-01.md).
- Detalhes de ideia: título centralizado entre colunas de 48, apenas i à direita, sem X nem contador visível. Nome 22 semibold com bookmark preenchido, recuado 20 para alinhar seu eixo aos ícones de endereço/nota; posição completa somente na semântica acessível do nome. Endereço/nota agrupados em superfície de raio 24, ícones decorativos secundários mais fracos que o texto; nota vazia omitida. Informação local por toque no i. Primeira página só Próximo largo; demais páginas com Anterior somente ícone em alvo 48 e ação principal larga. Última página ou coleção unitária: Concluído fecha o painel. Navegação finita por ID. [M5-EDIT-01](../engenharia/M5-EDIT-01.md) refina M5-FLOATING-01.
- Edição de ideia pela lista: lápis acessível de 48 junto ao nome, editor pré-preenchido com Cancelar à esquerda. Mesma revisão/validação do cadastro; Salvar alterações atualiza o mesmo ID. Sem alterações, Cancelar fecha direto; rascunho alterado exige confirmação. Falha ou conflito preserva editor, sem sobrescrita silenciosa.
- Detalhes e editor/revisão apresentados como cartões flutuantes: quatro cantos de raio 28, margem horizontal e inferior 12 dentro das safe areas, fundo externo transparente, alça central fora do cartão com alvo 48, indicador 36 × 4 e cápsula neutra 64 × 16 para contraste sobre o mapa. Alça pode recolher por toque, além dos gestos nativos; no editor equivale a Cancelar e protege rascunho/salvamento. Apenas o cabeçalho de detalhes perde X/contador; editor mantém Cancelar. A home recua para escala 0,96 ao abrir, restaurando ao fechar; redução de movimento respeitada pelas APIs nativas. Sem sombra/contorno decorativo.
- Confirmação de descarte usa alerta central nos dois sistemas, com opção de continuar editando e confirmação destrutiva explícita.
- Editor e revisão usam a mesma estrutura. Rótulos persistentes acima dos campos; Nome, Endereço/referência e Nota nessa ordem. Revisar/Salvar é ação primária preenchida; Editar informações é secundária acima de Salvar. Avisos/erros nas mesmas zonas, sem deslocar Cancelar.
- Sheets usam superfície semanticamente mais clara que suas áreas internas em ambos os temas (iOS: systemBackground/secondarySystemBackground no claro, secondarySystemGroupedBackground/systemBackground no escuro; Android: surfaceContainerLowest/surfaceContainerLow no claro, surfaceContainerHigh/surfaceContainerLowest no escuro). Campos e cartões têm preenchimento neutro distinto do fundo, sem contorno desenhado ou sombra decorativa. Só um azul de marca. Texto de ações no escuro usa semântica legível.
- Sistemas operacionais controlam teclado, barras, fontes nativas, menus/alertas e áreas seguras. Essas diferenças não alteram a gramática do app. Não usar framework híbrido, blur simulado ou cores novas.

## Métricas-base

Valores de distância equivalentes em pt no iOS e dp no Android; texto em pontos escaláveis/sp. Não converter pixels de screenshot diretamente em dp.

| Papel | Valor |
| --- | --- |
| Margem da home | 16 |
| Margem interna de cartões/sheet | 20 |
| Gaps pequenos / label-campo / seção | 8 / 8 / 16 |
| Separação cabeçalho-corpo / corpo-rodapé | 24 / 24 |
| Alvo mínimo de botão | 48 |
| Colunas laterais do cabeçalho do editor flutuante | 76 |
| Raio cartão / campo e botão / sheet | 20 / 12 / 28 |
| Altura mínima campo nome / endereço / nota | 56 / 96 / 120 |
| Padding interno de campo | 16 |
| Título de marca / chamada principal | 22 semibold / 28 bold |
| Título de conteúdo / contexto ou sheet | 22 semibold / 18 semibold |
| Corpo / rótulo / nota auxiliar / botão | 16 regular / 14 semibold / 14 regular / 16 semibold |
| Line-height de corpo / auxiliar | aproximadamente 24 / 20 |

Alturas são mínimos, não limites. Textos longos e fontes ampliadas podem crescer; corpo rola e CTA permanece alcançável. Não truncar dados ou reduzir fonte para caber. Alça externa SwiftUI/Compose compartilha posição/área reservada; a apresentação, gestos, foco modal e insets continuam usando o host nativo.

Com o envelope flutuante, reservar também a faixa externa da alça (48) e margem inferior (12). No detalhe, SwiftUI mede o conteúdo para um detent de altura limitado pelo sistema; Compose usa altura natural com limite de 90% da tela no conteúdo. Ambos mantêm ações fora do scroll e respeitam insets.

A fração do sheet do editor é uma intenção visual sobre a área disponível, não uma tradução literal entre APIs. No Compose a altura é aplicada ao conteúdo (90% mais alça/insets), preservando a âncora inferior do modal; no SwiftUI usa-se detent de 94%. Não limitar a janela/âncora externa para obter essa altura. Conferir a margem superior e as barras do sistema nas capturas.

## Critério de revisão

Comparar sempre iOS/Android por fluxo, em claro/escuro e tamanhos próximos. Verificar alça, posição de Cancelar, centralização do título, rótulos, largura/altura dos campos, rodapé, safe areas e teclado. Uma tela que passa isoladamente, mas muda a organização em outra plataforma, precisa de ajuste. Evidências e limites: [M5-PARITY-01](../engenharia/M5-PARITY-01.md).

Editor/revisão também seguem M5-EDITOR-UI-01: i no topo direito, Cancelar à esquerda, título central, labels persistentes com ícones discretos e ações preenchidas em cápsula. Orientações permanentes ficam no i; erros continuam visíveis no corpo.
