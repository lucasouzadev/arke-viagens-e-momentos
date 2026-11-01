# STATUS — Estado verificável do Arke

**M5-DETAILS-UI-03 em andamento:** refinamento visual autorizado após referências: altura natural limitada, controles circulares, informações agrupadas e navegação em cápsulas. [Contrato](engenharia/M5-DETAILS-UI-03.md). Testes/capturas novos pendentes; registros abaixo são históricos. Mesmo PR #2 rascunho, sem merge/release.

**M5-DETAILS-UI-02 verificado:** detalhes simplificados, X/i, contador integrado ao cabeçalho, nome em destaque, endereço/nota com ícones, informação sob demanda e navegação preenchida. Sheet mais claro que preenchimentos em ambos os temas, inclusive editor. [CI aprovado](https://github.com/lucasouzadev/arke-viagens-e-momentos/actions/runs/41353801111), fonte `b9582d23b6837fc60e2549b954193a7ceae12d28`: iOS 9 core + 5 UI, Android 6 core + 7 instrumentados; lint 0/9 (um P3 de título antigo sem uso); 24 PNGs originais inspecionados. Painel informativo abre/fecha e preserva seleção nos testes; sem captura estática do painel. Dados/IO intactos. Fonte 200%, leitores de tela, landscape/tablets e profiling não executados. PR #2 em rascunho, sem merge/release. Próximo: contrato de edição segura.

**M5-DETAILS-01 verificado:** abrir ideia salva, ler nome/endereço/nota, percorrer por Anterior/Próximo com contador/limites finitos e Fechar retornando à home. [CI aprovado](https://github.com/lucasouzadev/arke-viagens-e-momentos/actions/runs/41344099050), fonte `8858f3f8792ce0c0189991644aad5adede8762b6`: iOS 9 core + 5 UI, Android 6 core + 7 instrumentados; lint 0/8; 24 PNGs originais inspecionados. Exemplos de captura somente em memória/Debug; teste abre também ideia realmente salva. Dados/schema/IO inalterados. Fonte 200%, leitores de tela, landscape/tablets e profiling não executados. PR #2 segue rascunho, sem merge/release. Próximo: contrato de edição de ideia salva; mapa/geocoding permanecem separados.

**M5-FADE-01 verificado:** fade inferior de 32 pt/dp e espaço final de 48 no editor/revisão, sem corte seco junto às ações, em paridade iOS/Android. [CI aprovado](https://github.com/lucasouzadev/arke-viagens-e-momentos/actions/runs/41337689076), fonte `fd9d9d50da562592f136b82dd4dced224c5c337d`: iOS 9 core + 4 UI, Android 6 core + 6 instrumentados; lint 0/8; 16 PNGs originais inspecionados em pares. Android repetido por timeout de boot antes do app, sem alterar código. Dados/IO/fluxos preservados. Fonte 200%, leitores de tela, landscape/tablet e profiling não executados. Mesmo PR #2 em rascunho, sem merge/release; próximos sheets seguem DESIGN-006.

**M5-PARITY-01 verificado:** guia comum DESIGN-006/ADR-012 obrigatório para próximas telas. Home/editor/revisão com métricas equivalentes, alça visível, Cancelar à esquerda, título central, labels persistentes e CTA fixo. [CI aprovado](https://github.com/lucasouzadev/arke-viagens-e-momentos/actions/runs/41332236559), fonte `6b17366dc3f8e165b93a12a18f4e602aec154ab0`: iOS 9 core + 4 UI; Android 6 core + 6 instrumentados; lint 0/8. Dezesseis PNGs finais inspecionados em pares, padrão/compacto claro/escuro. Altura Android corrigida após inspeção; primeira tentativa final falhou no boot do emulador e foi repetida sem alteração de fonte. Sem dados/schema/IO novos. Limites: fonte 200%, leitores de tela, landscape/tablets e profiling não executados. PR #2 permanece rascunho, sem merge/release. Próximo: seleção/detalhes sob o guia comum.

**M5-PLACES-01 verificado:** adicionar/revisar/corrigir/cancelar, ideias pessoais gravadas no aparelho e exclusão confirmada. [CI aprovado](https://github.com/lucasouzadev/arke-viagens-e-momentos/actions/runs/41326580725), fonte `e54cc6fde18f0851d4f3bc70a6546c81baf43914`. iOS: 9 core + 4 UI; Android: 6 core + 4 UI + 2 repositório; lint 0/8; 16 PNGs inspecionados. Sem geocoding/Pin/conta/rede nova; sem backup nesta etapa. Regras/limites e testes não executados no [contrato](engenharia/M5-PLACES-01.md). Próximo: contrato de seleção/detalhes e futura associação ao mapa. Mesma branch/PR #2 em rascunho, sem merge/release. Registros anteriores são históricos.

**M5-HANDLE-01 verificado:** puxador vertical contínuo e encaixe nos três estados, com seletor por toque e ações acessíveis. [CI aprovado](https://github.com/lucasouzadev/arke-viagens-e-momentos/actions/runs/41310026574), fonte `f2e8f67496c6b6c21657f6295209f71371f76800`. Três testes UI e três unitários por plataforma aprovados; lint 0/8; oito PNGs padrão/compacto claro/escuro inspecionados. iOS corrigido após falha de retorno; repetição Android superou download inválido do SDK. Detalhes e limites em [contrato](engenharia/M5-HANDLE-01.md). Gestos testados nos aparelhos padrão; leitores de tela, fonte ampliada e landscape/tablet pendentes. Próximo: contrato do fluxo de adicionar lugar. Sem merge/release. Registros anteriores abaixo são históricos.


**M5-TONES-01 verificado:** fundo/cartão com tons semânticos distintos nas duas plataformas, sem contorno/sombra ou alteração de layout. [CI aprovado](https://github.com/lucasouzadev/arke-viagens-e-momentos/actions/runs/41305472522), fonte `a2e4e0b354ef4d79851779cc7f8040a16812af35`. Builds, testes UI/unitários e lint aprovados; oito PNGs inspecionados. Contrato: [M5-TONES-01](engenharia/M5-TONES-01.md). Sem merge/release. Próximo: puxador entre cartões.

**Atualizado em:** 01/11/2026. O andamento não substitui decisões de produto.

## Situação verificada

**M5-HEADER-01 verificado:** cabeçalho +/contexto central/relógio nas duas plataformas; seletor nativo por toque, preservando painel. “+” abre aviso, sem salvar dados. [CI aprovado](https://github.com/lucasouzadev/arke-viagens-e-momentos/actions/runs/41276436997), fonte `b9d7c2864f819e70403e2a6756af83448746c0ef`. Dois testes UI e três unitários por plataforma aprovados, lint 0 erros/8 avisos, oito capturas padrão/compacto claro/escuro inspecionadas. Próxima tarefa: puxador entre cartões. Leitores de tela, fonte ampliada, landscape/tablet e profiling pendentes. Google Maps real sem projeto/chave. Mesmo PR #2 em rascunho, sem merge/release. Contrato em docs/engenharia/M5-HEADER-01.md.

**M5-UI-02 verificado:** cartões sem contorno no iOS/Android, sem sombras acrescentadas e com superfícies/spacing preservados. Labels escuros usam cores semânticas legíveis nas duas plataformas. [Builds/testes/lint/capturas aprovados](https://github.com/lucasouzadev/arke-viagens-e-momentos/actions/runs/41273311115), fonte `0683e8e1df10ef5bd2e53f381aa52bb71ed2cebb`; oito PNGs inspecionados. Contrato/gates no início de [M5-PREVIEW-01](engenharia/M5-PREVIEW-01.md). Layout inicial aprovado pelo responsável; esta alteração específica segue disponível no PR #2, sem merge.

**Recorte atual:** [M5-PREVIEW-01](engenharia/M5-PREVIEW-01.md), oito capturas reais geradas e inspecionadas na [execução aprovada](https://github.com/lucasouzadev/arke-viagens-e-momentos/actions/runs/41265098608), fonte `4128e2e`. Apple Maps renderizado no iPhone 16/SE, claro/escuro; Android Emulator API 35 executou o fallback sem chave em padrão/compacto, claro/escuro. Google Cloud ainda não existe, portanto mapa real Android continua bloqueado por configuração externa. O baixo contraste encontrado neste recorte anterior foi tratado em M5-UI-02; isso não equivale a aprovação global de acessibilidade.

- O PR documental [#1](https://github.com/lucasouzadev/arke-viagens-e-momentos/pull/1) foi integrado em **28/10/2026**, commit `24007b08723ecf4df833ffc5b6a4e2ecc544c08b`. Os registros anteriores de PR aberto estavam desatualizados.
- O responsável autorizou iniciar **ambas as bases nativas** e compilar no **GitHub Actions**: [ADR-009](decisoes/ADR-009.md).
- Branch: `chore/development-kickoff`; tarefa [M5-BOOTSTRAP-01](engenharia/M5-BOOTSTRAP-01.md), com builds/testes aprovados, revisão visual pendente e sem merge desta implementação. [PR #2](https://github.com/lucasouzadev/arke-viagens-e-momentos/pull/2), em rascunho.
- Projetos SwiftUI e Compose com home inicial de dois cartões, menus dos cinco contextos e três proporções de painel. MapKit iOS renderiza o mapa inicial; Google Maps Android tem integração compilada e fallback sem chave.
- Testes de estado, lint Android e compilações iOS/Android **aprovados em CI**: [execução](https://github.com/lucasouzadev/arke-viagens-e-momentos/actions/runs/41260321973), fonte `8bfdbfa853dd796b7b8fef27e5822a5c992c144f`. **Build aprovado não equivale a app validado em dispositivo.**
- Sem Google Maps autenticado, Pins, carrossel, gestos do relógio, viagem persistida, contas, grupos, backend, publicação, TestFlight ou release.

## Marcos

| Marco | Estado verificável |
| --- | --- |
| M0 | Pacote documental integrado via PR #1; isso não aprova todas as propostas anexas |
| M1 | Estrutura aprovada preservada; detalhes de interação, azul final e validação visual pendentes |
| M2 | Bases nativas e coleção de ideias escritas locais verificadas; Pin geográfico, persistência de viagens/grupos e sync pendentes |
| M3 | Contratos de domínio, grupos, permissões e sync continuam propostos |
| M4 | Workflow de verificação executado e aprovado nas duas plataformas; distribuição fora do escopo |
| M5 | **Fundação e cabeçalho compilados/testados; capturas inspecionadas**, autorizada em ADR-009; demais features dependem de tarefas próprias |

## Verificações e ambiente

Clone limpo da main, histórico e integração do PR #1 verificados. JDK 17 local disponível; Java do PATH aponta para Java 8. SDK Android não encontrado nos locais verificados. Swift/Xcode não disponíveis no Windows. Gradle Wrapper oficial baixado e seu SHA-256 conferido; distribuição possui checksum fixado.

Compilação nativa, testes unitários e lint Android aprovados. Oito screenshots atuais e inspeção estática padrão/compacta claro/escuro executadas; labels escuros corrigidos por cores semânticas em M5-UI-02. Interações do cabeçalho verificadas por testes UI; leitores de tela, fonte ampliada, landscape/tablet e profiling **NÃO EXECUTADOS**. Matrizes em [fundação](engenharia/M5-BOOTSTRAP-01.md) e [capturas/mapas](engenharia/M5-PREVIEW-01.md). Guia em [DEVELOPMENT](engenharia/DEVELOPMENT.md).

Lint Android terminou com **0 erros e 8 avisos**: target/dependências anteriores aos mais recentes, regras de extração/backup e ícone de desenvolvimento, incluindo três avisos de versões de dependências de teste/BOM. Estão detalhados no contrato; não houve supressão. Definir regras de backup/transferência antes de persistir dados e revisar SDK/dependências antes de distribuição.

## Pendências e próxima ação

1. Revisar o PR #2, as evidências de CI e os avisos de manutenção do lint; manter rascunho até a revisão visual.
2. Revisar a home em iOS/Android, claro/escuro, tamanhos distintos, fonte ampliada e VoiceOver/TalkBack.
3. Configurar Google Cloud/Maps Android somente com decisão do responsável; provedor já escolhido em ADR-010. Aprovar o primeiro recorte de Pin, persistência/privacidade e interações.
4. Resolver licença final, marca e requisitos de distribuição antes de lançamento. Nenhuma licença foi presumida.

Não há merge automático nem aprovação geral de M1–M4. Continuidade em [HANDOFF](agents/HANDOFF.md); planejamento em [ROADMAP](ROADMAP.md).
