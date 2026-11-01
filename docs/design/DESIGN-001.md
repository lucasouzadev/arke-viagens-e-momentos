# DESIGN-001 — Dois cartões permanentes

**Status: conceito visual aprovado; dimensões em validação.** A referência enviada pelo usuário orienta o arranjo, não deve ser copiada literalmente nem redistribuída sem autorização.

## Cartão superior — Mapa

- Destinos, Pins, linha/rota e seleção geográfica.
- Controles leves de mapa e acesso contextual a viagem/participantes.
- O mapa é uma superfície de trabalho, não só ilustração.
- Evitar formulários grandes e informações financeiras sobre o mapa.

## Cartão inferior — Conteúdo e navegação

**Recorte autorizado em 01/11/2026:** cabeçalho fixo em relação ao scroll do corpo, com “+” à esquerda, nome do contexto central e relógio à direita. Menu nativo de cinco contextos por toque. Seletor compacto de proporção entre cartões até implementar o puxador por arrasto. O “+” abre aviso nativo de preparação; salvar Pin continua fora deste recorte. Ver [M5-HEADER-01](../engenharia/M5-HEADER-01.md).

- Botão de adição, título do contexto e botão de relógio no próprio cabeçalho.
- **Título selecionado centralizado**, destinos vizinhos parcialmente visíveis.
- Detalhes, registros, fotos e stamps dentro de um corpo de **scroll vertical**.
- Tocar um lugar atualiza o mapa, mantendo a mesma tela.

## O que não construir

- Tab bar fixa abaixo dos cartões.
- Dashboard anterior ao mapa.
- Menu lateral principal para as cinco áreas.
- Cinco pilhas de telas por categoria.
- Liquid Glass aplicado indiscriminadamente a textos e conteúdo.

## Composição

**Ajuste aprovado em 01/11/2026:** os dois cartões não têm contorno desenhado nem sombra acrescentada para simular borda. A separação vem das superfícies nativas e do espaçamento, com sensação de borda infinita. Preservar o arredondamento e a estrutura atual. Não confundir com remover separadores internos de conteúdo.

Dois cartões ocupam quase toda a viewport, separados por pequeno intervalo/puxador. O estado equilibrado é inicial. Arraste **apenas o puxador** para alternar proporção; rolar o conteúdo nunca redimensiona. Respeitar barras/áreas seguras do sistema.
