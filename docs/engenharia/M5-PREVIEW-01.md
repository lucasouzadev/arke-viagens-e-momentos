# M5-PREVIEW-01 — Mapas por plataforma e capturas reais

**Data:** 31/10/2026 · **Estado:** em andamento.  
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

- [ ] Compilação e testes nativos mantidos aprovados.
- [ ] Capturas iOS/Android reais, sem tela de launcher/splash/crash.
- [ ] Revisão visual dos PNGs e registro de plataforma/tema/limites.
- [ ] PNGs entregues no chat; não confundir com mockup.
- [ ] STATUS/HANDOFF/ROADMAP/PR atualizados.

G-01/02: revisão de escopo e ADR-010; G-03/04: compiladores/lint pendentes; G-05/06: capturas pendentes; G-07: leitor de tela e fonte ampliada NÃO EXECUTADOS; G-08: revisão de fonte sem GPS/chaves, aguardando CI; G-09: uso de mapas/termos requer revisão antes de distribuição, sem declaração de conformidade; G-10: persistência/sync NÃO APLICÁVEIS; G-11: profiling NÃO EXECUTADO; G-12: smoke de lançamento/capturas pendentes; G-13: artifacts apenas PNGs, execução pendente; G-14: atualizar ao concluir; G-15: configurado no chat, valor ausente dos arquivos.
