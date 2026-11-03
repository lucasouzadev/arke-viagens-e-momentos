# DESIGN-004 — Relógio, carrossel e física dos dois cartões

**Status:** proposta de interação para revisão. Somente o puxador vertical entre cartões foi autorizado e implementado no recorte [M5-HANDLE-01](../engenharia/M5-HANDLE-01.md), com verificação nativa registrada; demais partes continuam propostas.
**Dependências:** [DESIGN-001](DESIGN-001.md), [DESIGN-002](DESIGN-002.md), [DESIGN-003](DESIGN-003.md), [INTERACTIONS](INTERACTIONS.md).

## 1. Gramática espacial

A composição permanente é um **cartão superior de mapa** e um **cartão inferior de navegação + conteúdo**. Abaixo deles não existe tab bar. Há quatro zonas de toque independentes:

| Zona | Direção/gatilho | Função | Nunca deve fazer |
| --- | --- | --- | --- |
| Mapa | pan, pinch, toque | explorar, focar Pin | alternar categoria ou puxar conteúdo |
| Faixa de nomes | swipe horizontal, botões anterior/próximo | selecionar Place | abrir página ou alterar detent |
| Corpo do cartão | scroll vertical | explorar conteúdo do contexto | deslocar cartões |
| Puxador entre cartões | drag vertical, controle expandir/recolher | escolher detent | rolar lista ou arrastar mapa |
| Botão contextual no cartão | toque / foco / teclado | abrir relógio | criar tab bar paralela |

Direções de gesto não são suficientes para resolver conflitos: a **origem do toque** determina quem recebe o gesto. Um swipe diagonal iniciado sobre carrossel pertence ao carrossel até terminar.

## 2. Relógio contextual

**Problema:** permitir trocar cinco universos sem gastar uma faixa fixa da tela.

- Origem: botão no topo direito do segundo cartão, visível em todos os detents.
- Primeiro toque: o botão se transforma em seletor de contextos; o resto do conteúdo permanece reconhecível, sem navegar.
- Cinco opções com texto+ícone e alvo de toque confortável: **Planejamento, Gastos, Cofrinho, Avaliações, Memórias**.
- O contexto ativo é sempre identificável por **nome e estado visual**, não por cor isolada.
- Seleção: toque em opção → fechar relógio → atualizar título e corpo do cartão, mantendo Trip, Place e drafts. Mudança de contexto não move o mapa sem motivo.
- Fechamento sem mudança: toque no botão, ação Voltar/Escape ou toque fora; foco acessível retorna ao botão.
- Em fontes ampliadas/telas curtas, apresentar os mesmos cinco contextos em **lista ou grade dentro do cartão**, evitando rótulos sobrepostos; não é uma tab bar.
- Giro radial e haptic feedback são **melhorias opcionais**; toque direto é a regra.
- Evitar disparar a mesma ação duas vezes após animação interrompida.

**Alternativas rejeitadas nesta proposta:** engrenagem giratória obrigatória, categorias acessíveis só por swipe, paginar cinco telas completas.

## 3. Carrossel de lugares

- O **Place** ativo ocupa centro visual, com anterior e seguinte parcialmente visíveis quando existirem.
- Limites são finitos. Primeiro e último lugar devem comunicar fim de lista; nada de loop infinito automático.
- Anterior/próximo acionáveis por VoiceOver/TalkBack/teclado e equivalentes de toque.
- Snap conclui para **um único PlaceID** após a interação; em scroll rápido, saltos não devem produzir uma sequência de mudanças de mapa, sons ou vibrações.
- Títulos extensos podem ocupar duas linhas com limite visual; nome completo é acessível a leitores de tela.
- Com **zero** lugares, substituir carrossel por mensagem contextual e ação para salvar primeiro Pin.
- Com **um** lugar, título fixo central sem falsos vizinhos e sem swipe inerte.
- Ao mudar Trip, limpar seleções impossíveis, preservar drafts privados na Trip original e escolher seleção válida ou nenhum lugar.

### Sincronização mapa ↔ carrossel

1. `SelectMapPin(P)`: atualiza PlaceID, carrossel alinha; mapa **não** recebe novo comando redundante de centralização.
2. `SelectCarouselPlace(P)`: atualiza PlaceID, destaca Pin e opcionalmente anima viewport uma única vez.
3. O sistema diferencia **gesto do usuário** de **efeito programático**. Scroll controlado por estado não reemite `SelectCarouselPlace`.
4. Cada transição possui um identificador de intenção (conceitual); uma intenção mais recente pode interromper uma anterior.
5. Após um novo filtro, se PlaceID não estiver mais presente, limpar seleção ou apresentar a razão; nunca mostrar cartão de um Pin invisível como se estivesse ativo.

## 4. Proporções e detents

- Explorar: mapa dominante (referência ≈70/30).
- Equilibrado: visão principal (≈48/52).
- Detalhes: corpo de conteúdo dominante (≈25/75), sem esconder totalmente o mapa quando couber.
- Essas são **intenções de layout, não porcentagens rígidas**. Safe areas, teclado, landscape e fontes ampliadas têm prioridade.
- O puxador possui alvo acessível, descrição do estado e botões contextuais “Expandir conteúdo” / “Mostrar mapa”.
- Arrasto é contínuo enquanto manipulado, com encaixe ao soltar. Pode ser interrompido e retomado.
- Scroll vertical do conteúdo **nunca** redimensiona o cartão: não há drag-to-dismiss acidental.
- O retorno do teclado deve preservar rascunho, foco e área visível, mesmo que detent temporário seja ajustado.

## 5. Orquestração de animações (hipóteses)

Sem impor valores de duração antes de testes em devices: usar animações nativas com percepção de continuidade, interrupção segura e possibilidade de reduzir movimento.

| Transição | Ponto de origem | Continuidade desejada |
| --- | --- | --- |
| Abrir relógio | botão do cartão | escala/forma local; sem deslocar mapa |
| Trocar contexto | relógio | corpo substitui conteúdo, cabeçalho estável |
| Mudar lugar | carrossel ou Pin | destino centraliza sem piscar |
| Mudar detent | puxador | mapa e painel redimensionam juntos |
| Criar stamp | conteúdo da seção | confirmação discreta, sem bloquear tarefa |

- **Reduce Motion/Remove Animations:** priorizar opacidade/troca instantânea; sem parallax ou rotação obrigatória.
- Modo com tela reduzida e leitores de tela usa lista direta de contextos e controles explícitos.
- iOS: Liquid Glass apenas em chrome/controles, evitando sobreposição de texto sobre o mapa.
- Android: formas e superfícies flat, APIs de animação nativa; nunca reproduzir Liquid por blur genérico.

## 6. Aceite conceitual para protótipos futuros

1. Cinco contextos atingíveis por toque sem tab bar.
2. Troca de contexto não perde editor, Trip ou Place.
3. Mudança de Pin e de carrossel gera **uma única** seleção efetiva.
4. Carrossel finito, zero/um/múltiplos lugares sem erro de layout.
5. Gestos diagonal/rápido concorrentes não escapam de suas zonas.
6. Três detents, teclado e rolagem sem colisão.
7. Leitor de tela anuncia categoria, lugar, posição e expansão.
8. Contraste, fonte ampliada e sem movimento são equivalentes funcionais.

**Todos os valores e transições abaixo de validação em dispositivos são propostas, não especificações definitivas.**
