# DESIGN-007 — Redesenho de UI/UX aprovado

5 de novembro de 2026. **Status: direção aprovada pelo responsável no chat, com base em protótipo tela por tela (21 pranchas, privado, fora do repositório).** Este documento fixa no repositório o que o protótipo mostrou. Ele **complementa e, onde divergir, substitui** o [DESIGN-006](DESIGN-006.md) a cada recorte de [M5-UI-REDESIGN](../engenharia/M5-UI-REDESIGN-PLANO.md) que o implementar. Nada aqui está implementado além do recorte 1. Os valores de cor são os já aceitos em [ADR-006](../decisoes/ADR-006.md); nenhuma cor nova de identidade.

## Princípios

1. **Mapa primeiro:** dois cartões, sem tab bar, sem dashboard.
2. **Uma só marca:** azul opaco; o resto são neutros nativos, claro e escuro automáticos.
3. **Legível:** texto ≥ 4,5:1, mínimo 13 pt, nenhum rótulo de navegação truncado.
4. **Tocável:** alvo ≥ 48 pt (nunca < 44) e todo gesto com alternativa por toque e por leitor de tela.
5. **Um sheet só:** mesmo cabeçalho e mesma ação de rodapé em todos.
6. **Teclado primeiro:** campo ativo e ação principal sempre visíveis acima do teclado.
7. **Estados reais:** vazio, carregando, offline, sem localização, erro e confirmações desenhados.
8. **Natividade:** vidro nos controles do iOS, flat no Android; estrutura, ordem e rótulos idênticos.

## Cor

- Preenchimento (botões, pinos, seleção): azul candidato `#4C728A`, com texto branco (5,15:1).
- Tinta de texto/ação: no claro o próprio azul (5,15:1 sobre branco, 4,61:1 sobre `#F2F2F7`); no escuro **`#88AABE`**, mesma matiz com luminosidade maior (6,92:1 sobre `#1C1C1E`, 5,67:1 sobre `#2C2C2E`). Implementado no recorte 1.
- Neutros, erro e confirmação seguem cores semânticas do sistema. A estrela de avaliação precisa de 3:1 no tema claro (âmbar mais escuro) e é sempre acompanhada de nota em texto.
- Vidro só em controles sobre o mapa, com fundo que garanta contraste; o ponto de localização usa o azul do sistema, não o da marca.

## Tipografia e métricas

Fonte nativa, escalável. Título 22/28 · contexto 19/24 · cabeçalho 17/22 · corpo 16/24 · apoio 15/20 · legenda e rótulo 13/18. Margem da tela 16, entre cartões 8, entre campos 8, respiro interno 16–20. Raios: mapa 40, cartão e sheet 36–40, campo 20, botão em pílula, chip 22 (Android: cartão 28, chip 12, botão 20). Com texto muito grande o cabeçalho do cartão passa a duas linhas; nada é cortado.

## Home

- **Sem o bloco “Arke / Onde vamos?”:** o mapa sobe até a área segura. *Decisão de produto pendente, ver plano.*
- **Mapa:** campo de busca em pílula no topo; localização e expandir empilhados à direita; atribuição do mapa sempre visível; rótulo do lugar selecionado sobre o pino (omitido no detent Detalhes). Pino com a ponta no ponto real.
- **Três detents** com alça de 40×5 e alvo de 48: Explorar (mapa ~62%), Equilibrado (~44%), Detalhes (~22%). Alternativa por toque e por ação de acessibilidade.
- **Mapa expandido:** cartão recolhido vira barra de resumo (miniatura, nome, contexto e viagem) com botão **Recolher** explícito; nenhuma alça sobre texto.
- **Cabeçalho do cartão:** vizinhos como botões, título inteiro com ícone do contexto, cinco pontos de posição e “+” contextual (nome acessível muda por contexto). Primeiro e último contexto deixam o lado vazio, sem desalinhar.
- **Corpo do Planejamento:** linha com chip da viagem (abre Viagens) e contagem; carrossel finito de cartões (miniatura em selo, nome, endereço, nota resumida, **Detalhes** e **Marcar no mapa**), vizinho visível, contador “n de N” com anterior/próximo. Explorar mostra faixa compacta com tile “+”. Detalhes mostra o lugar completo com Editar, No mapa e Excluir lado a lado, fade na base.
- **Selo (stamp):** serrilhado só no iOS; moldura simples no Android.

## Sheets

- Cabeçalho único: **X à esquerda (48), título central, ação ou “i” à direita ou coluna vazia**. Alça nativa; fade de 24–28 no topo e na base; indicadores de rolagem ocultos.
- Ação principal **sempre no rodapé**, acima do teclado e da área segura; secundária acima dela na revisão.
- Campos: rótulo persistente com ícone e marcação **Obrigatório/Opcional**, placeholder legível (≥ 4,5:1), foco com contorno. Nome 68, endereço 68 e cresce, nota 104, link 68 (alturas mínimas, **substituem 56/96/120 do DESIGN-006**). O texto do campo alinha com o do rótulo; um tipo de campo só (sem mistura de `TextField` e `TextEditor` com recuos diferentes).
- Foto de capa: linha-botão com ícone, texto, “Opcional” e seta; trocar/remover são ações separadas de 48, remover em cor de perigo.
- Revisão: resumo compacto em grupo único (rótulo + valor), sem blocos vazios; Editar informações acima de Salvar neste aparelho.
- Teclado: campo ativo visível acima do teclado, nunca sob o cabeçalho; ação principal sobe junto.

## Marcar no mapa

Sheet em que o **mapa é o conteúdo**: busca de endereço e localização sobre o mapa, alvo central com a ponta do pino exatamente no ponto salvo (sombra e ponto no chão), dica “Mova o mapa para ajustar”. Painel inferior com o lugar, o ponto (endereço aproximado), vínculo opcional a viagem em chips (Sem viagem, viagens, Nova viagem) e **Confirmar ponto** visível e habilitado quando há ponto. Sem ponto, a dica explica o que falta. Salvar só confirma após persistir.

## Contextos

- **Gastos:** chip da viagem; totais por moeda em grade (Previsto × Realizado); registros com ícone, descrição, estado em texto (ícone de confirmação para Realizada) e valor com moeda. Adicionar gasto: valor grande com moeda em chips, descrição, tipo segmentado (Prevista/Realizada), data. Valores formatados por moeda e localidade.
- **Cofrinho:** meta, valor reservado, barra e “Falta”; selo “Só você vê”; aviso de registro informativo (sem dinheiro real).
- **Avaliações:** por lugar, estrelas + nota em texto, data e visita; ação “Avaliar este lugar”.
- **Memórias:** grade de selos com legenda e data, tile “Nova memória”, selo “Privado”.

## Viagens e grupo

Lista de viagens com selo, resumo e participantes; viagem ativa marcada; seção “Fora de viagens” para ideias soltas; “Nova viagem”. Compartilhar viagem: convite por e-mail ou link, papéis (editar, só ver), lista de participantes, aviso de que o Cofrinho continua pessoal. Compartilhar exige conta; sem conta, tudo continua funcionando no aparelho. **O login por e-mail e senha de teste sai da interface principal.**

## Estados obrigatórios

Vazio (primeira abertura), carregando (esqueleto), localização desativada, sem conexão, falha ao salvar (rascunho preservado, “Tentar novamente”, sem duplicar), descartar rascunho, excluir ideia e entrar para compartilhar. Mensagens curtas e sem culpar a pessoa.

## Android

Mesma estrutura, material flat: busca em barra sólida, botões tonais, “+” em quadrado arredondado, campos com contorno e rótulo flutuante. Controles de busca e localização no mesmo lugar da versão iOS.

## Fora deste documento

Mapa real e chave do Google Maps (ADR-010), autenticação (GROUP-001), HEX final do azul (ADR-006), fonte 200%, landscape e tablets. Ver [plano](../engenharia/M5-UI-REDESIGN-PLANO.md).

[AGENTS](../../AGENTS.md) · [DESIGN-006](DESIGN-006.md) · [QA-BETA-01](../qualidade/QA-BETA-01.md)
