# STATUS — Estado verificável do Arke

**Atualizado em:** 31/10/2026. O andamento não substitui decisões de produto.

## Situação verificada

- O PR documental [#1](https://github.com/lucasouzadev/arke-viagens-e-momentos/pull/1) foi integrado em **28/10/2026**, commit `24007b08723ecf4df833ffc5b6a4e2ecc544c08b`. Os registros anteriores de PR aberto estavam desatualizados.
- O responsável autorizou iniciar **ambas as bases nativas** e compilar no **GitHub Actions**: [ADR-009](decisoes/ADR-009.md).
- Branch: `chore/development-kickoff`; tarefa [M5-BOOTSTRAP-01](engenharia/M5-BOOTSTRAP-01.md), com builds/testes aprovados, revisão visual pendente e sem merge desta implementação. [PR #2](https://github.com/lucasouzadev/arke-viagens-e-momentos/pull/2), em rascunho.
- Projetos SwiftUI e Compose preparados com home inicial de dois cartões, menus dos cinco contextos e três proporções de painel. Mapas são espaços identificados como em desenvolvimento.
- Testes de estado, lint Android e compilações iOS/Android **aprovados em CI**: [execução](https://github.com/lucasouzadev/arke-viagens-e-momentos/actions/runs/41260321973), fonte `8bfdbfa853dd796b7b8fef27e5822a5c992c144f`. **Build aprovado não equivale a app validado em dispositivo.**
- Sem mapa integrado, Pins, carrossel, gestos do relógio, viagem persistida, contas, grupos, backend, publicação, TestFlight ou release.

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

Compilação nativa, testes unitários e lint Android aprovados. Kotlin local: 3 testes sem falhas; Swift no macOS: 3 testes sem falhas. Screenshots, E2E, leitores de tela, contraste, viewport e profiling **NÃO EXECUTADOS**. A matriz detalhada G-01..G-15 fica no [contrato](engenharia/M5-BOOTSTRAP-01.md). Guia em [DEVELOPMENT](engenharia/DEVELOPMENT.md).

Lint Android terminou com **0 erros e 5 avisos**: target/dependências anteriores aos mais recentes, regras de extração/backup e ícone de desenvolvimento. Estão detalhados no contrato; não houve supressão. Definir regras de backup/transferência antes de persistir dados e revisar SDK/dependências antes de distribuição.

## Pendências e próxima ação

1. Revisar o PR #2, as evidências de CI e os avisos de manutenção do lint; manter rascunho até a revisão visual.
2. Revisar a home em iOS/Android, claro/escuro, tamanhos distintos, fonte ampliada e VoiceOver/TalkBack.
3. Escolher provedor de mapa Android e aprovar o primeiro recorte de Pin, persistência/privacidade e interações.
4. Resolver licença final, marca e requisitos de distribuição antes de lançamento. Nenhuma licença foi presumida.

Não há merge automático nem aprovação geral de M1–M4. Continuidade em [HANDOFF](agents/HANDOFF.md); planejamento em [ROADMAP](ROADMAP.md).
