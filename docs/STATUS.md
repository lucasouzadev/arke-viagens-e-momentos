# STATUS — Estado verificável do Arke

**Atualizado em:** 01/11/2026. O andamento não substitui decisões de produto.

## Situação verificada

**Em andamento — M5-UI-02:** layout inicial aprovado pelo responsável, com pedido de cartões sem contorno. Strokes removidos no iOS; Android usa Card sem borda/elevação. Labels escuros passam a usar texto semântico conforme THEME-001 para corrigir contraste. Novos builds/capturas ainda pendentes. Contrato no início de [M5-PREVIEW-01](engenharia/M5-PREVIEW-01.md).

**Recorte atual:** [M5-PREVIEW-01](engenharia/M5-PREVIEW-01.md), oito capturas reais geradas e inspecionadas na [execução aprovada](https://github.com/lucasouzadev/arke-viagens-e-momentos/actions/runs/41265098608), fonte `4128e2e`. Apple Maps renderizado no iPhone 16/SE, claro/escuro; Android Emulator API 35 executou o fallback sem chave em padrão/compacto, claro/escuro. Google Cloud ainda não existe, portanto mapa real Android continua bloqueado por configuração externa. Contraste dos textos azuis no modo escuro requer correção; não declarar aprovação visual final.

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
| M2 | Duas bases nativas iniciadas neste recorte; persistência/mapas e arquitetura de features pendentes |
| M3 | Contratos de domínio, grupos, permissões e sync continuam propostos |
| M4 | Workflow de verificação executado e aprovado nas duas plataformas; distribuição fora do escopo |
| M5 | **Fundação compilada/testada; revisão visual pendente**, autorizada em ADR-009; demais features dependem de tarefas próprias |

## Verificações e ambiente

Clone limpo da main, histórico e integração do PR #1 verificados. JDK 17 local disponível; Java do PATH aponta para Java 8. SDK Android não encontrado nos locais verificados. Swift/Xcode não disponíveis no Windows. Gradle Wrapper oficial baixado e seu SHA-256 conferido; distribuição possui checksum fixado.

Compilação nativa, testes unitários e lint Android aprovados. Oito screenshots e inspeção estática padrão/compacta claro/escuro executadas; baixo contraste do azul no escuro identificado. E2E de interações, leitores de tela, fonte ampliada, landscape/tablet e profiling **NÃO EXECUTADOS**. Matrizes em [fundação](engenharia/M5-BOOTSTRAP-01.md) e [capturas/mapas](engenharia/M5-PREVIEW-01.md). Guia em [DEVELOPMENT](engenharia/DEVELOPMENT.md).

Lint Android terminou com **0 erros e 5 avisos**: target/dependências anteriores aos mais recentes, regras de extração/backup e ícone de desenvolvimento. Estão detalhados no contrato; não houve supressão. Definir regras de backup/transferência antes de persistir dados e revisar SDK/dependências antes de distribuição.

## Pendências e próxima ação

1. Revisar o PR #2, as evidências de CI e os avisos de manutenção do lint; manter rascunho até a revisão visual.
2. Revisar a home em iOS/Android, claro/escuro, tamanhos distintos, fonte ampliada e VoiceOver/TalkBack.
3. Configurar Google Cloud/Maps Android somente com decisão do responsável; provedor já escolhido em ADR-010. Aprovar o primeiro recorte de Pin, persistência/privacidade e interações.
4. Resolver licença final, marca e requisitos de distribuição antes de lançamento. Nenhuma licença foi presumida.

Não há merge automático nem aprovação geral de M1–M4. Continuidade em [HANDOFF](agents/HANDOFF.md); planejamento em [ROADMAP](ROADMAP.md).
