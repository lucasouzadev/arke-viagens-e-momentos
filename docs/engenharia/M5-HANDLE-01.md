# M5-HANDLE-01 — Puxador entre cartões

Data: 01/11/2026. Estado: em andamento. Milestone M5, branch chore/development-kickoff, PR #2.

Autorização: responsável autorizou continuidade após o próximo item apresentado: puxador entre cartões. DESIGN-001/002 orientam o gesto exclusivo; somente este recorte de DESIGN-004 é implementado, sem aprovar suas demais propostas.

Objetivo: ajustar continuamente o espaço do mapa arrastando a faixa entre cartões e encaixar em Explorar/Equilibrado/Detalhes ao soltar. Preservar proporções atuais (65/45/25% com limites de espaço), cores semânticas, cabeçalho e contexto. Toque abre seletor existente; ações de acessibilidade equivalentes. Sem animação automática, haptics, gestos radiais, Pin, persistência ou backend.

Arquivos autorizados: HomeView.swift, ArkeHome.kt, strings Android, testes UI existentes, scripts de captura se necessário e documentos de design/engenharia/continuidade/changelog. Sem nova dependência. Gestos pertencem somente à faixa do puxador; scroll e mapa não redimensionam. Translação transitória não é salva e cancelamento retorna a estado válido. Respeitar mínimos já existentes de mapa e cabeçalho; texto ampliado/landscape/tablets continuam sujeitos a revisão própria.

Aceite: arrastar para cima reduz mapa, para baixo amplia; encaixe nos três estados; toque continua funcional; contexto preservado; scroll do corpo não altera proporção; testes nativos executados e oito imagens padrão/compacto claro/escuro inspecionadas. Testes de drag usam coordenadas relativas ao controle para representar o gesto, sem contornar hit testing de botões.

G-01/02 PASSOU na revisão do escopo aprovado. G-03/04/05/06/07/12 aguardam diff/CI/testes/PNG. G-08/09/10 NÃO APLICÁVEIS a novos dados/rede/mídia, pois não adicionados. G-11 profiling NÃO EXECUTADO. G-13 sem distribuição/merge, reversão por commit na branch. G-14 atualizar ao concluir. G-15 preferência restrita ao chat. Leitores de tela, fonte 200%, landscape/tablet e profiling NÃO EXECUTADOS; não inferir aprovação global das capturas. Risco P2: conflito entre toque/drag e overflow; interromper e corrigir sem ocultar testes. Nenhum P0/P1 identificado no recorte.
