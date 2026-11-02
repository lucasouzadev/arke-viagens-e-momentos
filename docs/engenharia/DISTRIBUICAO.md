# BUILD-001 — Estratégia futura de GitHub Actions

**Envio beta confirmado em 02/11/2026:** [41491266745](https://github.com/lucasouzadev/arke-viagens-e-momentos/actions/runs/41491266745), versão 0.0.1 (8.1), fonte 90c1e5d, validação/upload Apple sem erros. A execução manual via `gh workflow run ios-testflight.yml --ref chore/development-kickoff` funciona antes do merge e exige aprovação do environment. Não é necessário marcador no commit para disparo manual. Nunca interpretar exit zero do altool sozinho como sucesso: o script exige mensagem explícita sem erros. Disponibilidade após processamento e seleção do grupo ainda não verificadas.

**Estado atual — 02/11/2026:** native-checks compila/testa ambas as plataformas e fornece PNGs e APK debug temporário. M5-TESTFLIGHT-01 autoriza pipeline separado de assinatura/upload beta; configuração privada confirmada pelo responsável, execução em validação. Demais fluxos da tabela abaixo permanecem propostas. Sem release App Store/Play Store.

Workflow ios-testflight: environment `ios-testflight`, seis secrets descritos em M5-TESTFLIGHT-01, Xcode 26.3 no macos-15. Disparo manual quando disponível no branch padrão. Enquanto PR #2 permanece aberto, push do proprietário na branch chore/development-kickoff só executa assinatura quando a mensagem do último commit inclui `[testflight]`; push comum/PR/fork não assinam. O marcador torna o primeiro envio possível sem merge. Nunca adicionar esse marcador por manutenção rotineira. Build `0.0.1 (run_number.run_attempt)`; sem artifact de IPA ou material privado. Sucesso do upload não significa processamento concluído nem convite enviado. Após processamento, selecionar build/grupo interno no App Store Connect e resolver eventual pergunta sobre criptografia de acordo com o app.

O app beta usa ícone provisório original (A branco no azul já usado); não define marca final. Manifesto declara metadados de arquivos locais (C617.1), sem tracking/coleta pelo app. MapKit mantém acesso ao provedor Apple; sem GPS. Checklist de loja completo permanece pendente para lançamento público.

## Plano

| Fluxo futuro | Disparo | Resultado |
| --- | --- | --- |
| iOS CI | PR/push | build sem assinatura e teste simulador |
| Android CI | PR/push | Gradle debug/testes sem secrets |
| TestFlight | manual, ambiente protegido | IPA assinado e enviado |
| Android APK | tag/manual autorizada | APK instalável como artefato |
| Release | tag revisada | notas e arquivos verificados |

## Segurança

- PR externo sem secrets, evitar pull_request_target para executar código não confiável.
- Releases com aprovação manual, ambiente protegido e permissões mínimas.
- Chaves App Store Connect, certificados, provisioning profiles e keystore fora do repo e logs.
- APK de debug e distribuição de produção são diferentes; assinatura de produção protegida.
- Nunca subir artifacts que contenham credenciais, perfis ou logs sensíveis.

## Antes de configurar

1. Projetos nativos existentes e compilados.
2. Identificadores oficiais/bundle IDs definidos.
3. Contas e permissões de publicação apropriadas.
4. Estrutura de assinatura Apple e estratégia Android aprovadas.
5. Testes visuais e E2E mínimos concluídos.

**Não habilitar credenciais ou deploy antes do produto precisar deles.**
