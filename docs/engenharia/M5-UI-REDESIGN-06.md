# M5-UI-REDESIGN-06 — Home map-first

5 de novembro de 2026. Recorte 06 do [plano](M5-UI-REDESIGN-PLANO.md). Segue o protótipo aprovado (`Main.dc.html`) e as decisões D2, D4 e D6.

## Entregue nesta etapa (iOS e Android)
- **D2:** sem o bloco "Arke / Onde vamos?"; o mapa sobe ao topo.
- **Controles do mapa (iOS):** busca em pílula no topo à esquerda; localização e expandir empilhados à direita (rótulos de 26 pt dentro do vidro). Android: botão de expandir com ícone vetorial no canto superior direito (busca e localização não existem no Android hoje).
- **Cabeçalho do cartão:** ícone do contexto ao lado do nome (D6: pin, receipt, target, star, photo); vizinhos mostram o ícone do contexto vizinho; botão "+" em marca no fim; pontinhos mantidos.
- **Planejamento:** chip "Viagens" com contador ("N lugares"); cartão do lugar com selo, nome, endereço, "Lugar n de N", nota (2 linhas), **Detalhes** e **Marcar no mapa**; navegação anterior / contador / próximo.
- **Editar e Excluir** saem do cartão e vão para o sheet de detalhes (Editar, No mapa, Excluir), como no protótipo.
- **D4:** câmera inicial enquadra os Pins salvos; sem Pins abre no Brasil. Nunca pede localização sozinha.

## Não feito neste recorte (explícito)
- Barra de resumo no mapa expandido, faixa compacta específica do Explorar e estados offline/sem localização desenhados.
- Mapa sem recomposição por quadro durante o arraste do painel (continua como antes).
- Busca de lugares e localização no Android (dependem de provedor e chave; ADR-010).
- Rótulo do chip com o nome da viagem ativa (hoje "Viagens"; não há conceito de viagem ativa nos dados).
- Android: câmera D4 não testável no CI (`MAPS_CONFIGURED` falso).

## Testes
Testes de UI de iOS e Android atualizados para editar/excluir via detalhes. Evidência: pendente de CI (ver STATUS).
