# M5-PREVIEW-01 — Mapas por plataforma e capturas reais

## Ajuste M5-UI-02 — 01/11/2026

**Autorização:** responsável aprovou o layout inicial e pediu retirar o contorno dos dois cartões para uma sensação de borda infinita. Prosseguir com o refinamento visual, sem aprovar outras propostas por associação.

**Escopo:** remover os strokes SwiftUI e substituir OutlinedCard por Card sem border/elevação no Compose, preservando superfícies, espaçamentos, arredondamento, mapa e navegação. Corrigir o contraste já identificado dos labels no escuro usando texto semântico nativo, conforme THEME-001, sem escolher novo azul. Arquivos: `ios/Arke/HomeView.swift`, `android/app/src/main/kotlin/dev/arke/app/ArkeHome.kt`, DESIGN-001/STATUS/HANDOFF/ROADMAP/CHANGELOG e este contrato. Sem dados, backend, permissões, credenciais, merge ou release.

**Aceite:** ambos os cartões sem contorno/sombra; contraste dos controles escuros corrigido; builds/lint/testes existentes mantidos; oito capturas padrão/compacto claro/escuro inspecionadas. Nenhum teste unitário novo para uma alteração de apresentação.

**Gates iniciais:** G-01/02 PASSOU em revisão de escopo; G-03/04/05/06/12 aguardam compilação/capturas; G-07 contraste em correção, leitores de tela NÃO EXECUTADOS; G-08/09/10 NÃO APLICÁVEIS a novos dados/rede/licenças porque nada disso muda, restrições de distribuição anteriores permanecem; G-11 profiling NÃO EXECUTADO; G-13 CI/capturas pendentes; G-14 documentos a atualizar; G-15 confirmado no chat, valor ausente dos arquivos. Risco P3 de apresentação; reversão por correção na branch, mantendo main. Demais limites de M5-PREVIEW-01 continuam.

Primeira verificação de M5-UI-02: [execução](https://github.com/lucasouzadev/arke-viagens-e-momentos/actions/runs/41272639484), fonte `b9b0a98`, builds/testes/capturas aprovados nas duas plataformas. Oito PNGs inspecionados: contornos removidos em ambos; labels Android escuros corrigidos. No iOS, `.primary` hierárquico ainda herdou o tint azul do menu; substituído por `Color(uiColor: .label)` explícito. Nova captura iOS necessária antes de declarar contraste corrigido.

**Resultado M5-UI-02:** implementado e verificado em [CI/capturas](https://github.com/lucasouzadev/arke-viagens-e-momentos/actions/runs/41273311115), fonte `0683e8e1df10ef5bd2e53f381aa52bb71ed2cebb`. Build/testes iOS e Android, lint Android e captura dos dois simuladores aprovados. Oito PNGs atuais inspecionados: cartões sem contorno/sombra, superfícies/spacing preservados, Apple Maps com atribuição e fallback Google sem chave preservados; labels dos dois sistemas escuros aparecem claros. Originais entregues no chat, em claro/escuro e padrão/compacto, sem retoques.

**Gates finais deste ajuste:** G-01/02/03/04 PASSOU no escopo; G-05/06 PASSOU para estado inicial estático nas quatro configurações de M5-PREVIEW-01; G-07 PASSOU para a correção visual de labels escuros, sem declarar aprovação global de a11y (leitores de tela, foco e fonte ampliada NÃO EXECUTADOS); G-08/09/10 NÃO APLICÁVEIS a novos dados/rede/licenças, pois não houve alteração nessas áreas; G-11 profiling NÃO EXECUTADO; G-12 PASSOU para build/testes existentes e smoke/capturas, sem testes novos para estilos; G-13/14/15 PASSOU no recorte. Avisos anteriores de lint e bloqueios de distribuição/Google Cloud permanecem. Sem merge/release. Próximo passo: interações e acessibilidade, sem aprovar marca final ou contratos de Pin por associação.

---

**Data:** 31/10/2026 · **Estado:** capturas reais geradas e inspecionadas; Google Maps real bloqueado por configuração externa; contraste escuro pendente.
**Branch/PR:** `chore/development-kickoff`, PR #2.
**Autorização:** pedido no chat para executar simuladores, mostrar o app e usar Apple Maps no iOS e Google Maps no Android; [ADR-010](../decisoes/ADR-010.md).

## Contrato

Entregar PNGs reais do app aberto em iPhone Simulator e Android Emulator, em claro/escuro, e registrar a matriz capturada. Integrar mapa explorável sem GPS no iOS e preparar Google Maps com fallback honesto sem chave no Android. Arquivos no escopo: `ios/Arke/`, `android/app/`, `.github/workflows/native-checks.yml`, `scripts/`, documentos canônicos de decisão/andamento/índice e changelog. Sem redesenhar os dois cartões, inventar Pins, criar contas, habilitar faturamento, credenciais ou publicar binários.

O responsável confirmou que ainda não possui o projeto Google Cloud. Logo, **mapa Android real bloqueado por configuração externa**; SDK pode ser compilado e fallback executado. Chave futura via propriedade/variável `ARKE_MAPS_API_KEY`, nunca versionada ou solicitada no chat. Nenhum secret será criado nesta tarefa. CI pública usa chave vazia e não acessa projetos pagos.

## Ameaças e privacidade do recorte

- Entradas: gestos de câmera; mapa mundial inicial público, sem posição pessoal. Nenhum GPS, Pin, viagem ou conta.
- Fronteira: dispositivo → provedor de mapas, quando SDK efetivamente ativo. MapKit usa rede do sistema; Google Maps só é montado quando configurado. Não pedir localização nem ativar camada de posição.
- Ameaças: revelar posição em screenshot; expor chave em logs; simular mapa funcionando sem chave; ocultar atribuição. Mitigações: mapa mundial, sem dados pessoais, sem impressão de chave, fallback textual, atribuição nativa preservada.
- PNGs são evidências solicitadas, com retenção de 7 dias no Actions; nenhum APK/IPA é enviado. Capturas não são assets promocionais nem offline tiles.
- Risco P2: SDK/provedor pode falhar ou demorar a carregar. Sem promessa de disponibilidade; falhas e mapa efetivamente visível serão verificados nas imagens.
- Reversão: manter main inalterada e corrigir na branch. Sem dados persistidos/migração.

## Aceite e gates

- [x] Compilação, testes nativos e lint Android aprovados.
- [x] Capturas iOS/Android reais, sem tela de launcher/splash/crash.
- [x] Oito PNGs inspecionados, modelos/dimensões abaixo; nenhuma imagem reconstruída ou retocada.
- [x] PNGs organizados para entrega no chat e ZIP; não são mockups.
- [x] STATUS/HANDOFF/ROADMAP/PR atualizados com evidências e limites.

| Gate | Resultado no recorte |
| --- | --- |
| G-01/02 | PASSOU: ADR-010, dois cartões e stacks nativas preservados |
| G-03/04 | PASSOU no recorte automatizado: build/testes das duas plataformas e lint Android, 0 erros/5 avisos anteriores |
| G-05 | PASSOU para abertura/estado inicial nas imagens; menus, arrasto e fluxos de Pin NÃO EXECUTADOS |
| G-06 | PASSOU para renderização estática nas quatro configurações/two themes abaixo; tablet, landscape, IME e fonte ampliada NÃO EXECUTADOS |
| G-07 | FALHOU na meta interna de contraste para texto azul no escuro: candidato #4C728A sobre superfície Android escura cerca de 3,62:1, abaixo de 4,5:1. Leitores de tela/foco NÃO EXECUTADOS. Não aprovar identidade visual com base nestas capturas |
| G-08 | PASSOU parcial em revisão: sem GPS/chave versionada/dados pessoais; câmera mundial e atribuição nativa visível. Não é pentest ou auditoria de SDK |
| G-09 | BLOQUEADO para distribuição: termos/custos/privacy e licença final ainda requerem revisão; Google Cloud/chave não configurados |
| G-10 | NÃO APLICÁVEL: sem dados de viagem persistidos/sync |
| G-11 | NÃO EXECUTADO: sem profiling |
| G-12 | PASSOU para testes unitários, build e smoke de lançamento/captura. E2E de interações NÃO EXECUTADO |
| G-13 | PASSOU: PNGs gerados e publicados como artifacts por 7 dias, sem APK/IPA |
| G-14 | PASSOU: documentos e PR reconciliados |
| G-15 | PASSOU: configuração privada aplicada no chat; valor ausente dos arquivos |

## Evidências e matriz capturada

[Execução aprovada](https://github.com/lucasouzadev/arke-viagens-e-momentos/actions/runs/41265098608), fonte `4128e2e02a652de5e3cf630f52062cdb0f1cb32c`. iOS: Xcode 16.4/Swift 6.1.2, 3 testes sem falhas e build Simulator aprovado. Android: testes Kotlin, lint e assembleDebug aprovados. Scripts com sintaxe Bash validada; manifest XML e links documentais conferidos.

| Dispositivo/configuração | PNG | Temas |
| --- | --- | --- |
| iPhone 16 Simulator | 1179 × 2556 | claro e escuro |
| iPhone SE (3ª geração) Simulator | 750 × 1334 | claro e escuro |
| Pixel 6 Emulator, API 35, Google APIs | 1080 × 2400 | claro e escuro |
| Mesmo emulador com viewport/densidade compactos | 720 × 1280, 320 dpi | claro e escuro |

Inspeção: Apple Maps efetivamente renderizado nos quatro PNGs iOS, com logo/legal preservados. Android executou o fallback sem chave nos quatro PNGs; **não comprova mapa Google autenticado**. Textos/botões iniciais visíveis, corpo inferior rolável em compacto, sem sobreposição aparente nesta amostra. Não houve interação automatizada com menus/pan/zoom, teste de rolagem até o fim ou avaliação manual de acessibilidade.

Artifacts: `ios-screenshots` (13066724575) e `android-screenshots` (13067176208), retenção até 07/11/2026 à noite no fuso America/Sao_Paulo. Originais baixados para entrega; não versionar PNGs de fornecedor como assets do produto.

## Próxima ação

Ajustar o uso do azul em textos/controles no modo escuro sem aprovar um HEX final por suposição; validar contraste e leitores de tela. Configurar Google Cloud/chave restrita apenas quando o responsável avançar essa etapa. Depois testar pan/zoom, fallbacks de rede, rotação, fonte ampliada e demais interações. Nenhum merge ou release nesta tarefa.
