# PROMPT OBRIGATÓRIO — Protocolo mestre de todos os agents

> **Atualização de escopo — 31/10/2026:** [ADR-009](../decisoes/ADR-009.md) autoriza [M5-BOOTSTRAP-01](../engenharia/M5-BOOTSTRAP-01.md), bases nativas iOS/Android e CI de compilação. As instruções de fase documental abaixo não revogam essa exceção delimitada. Demais propostas continuam pendentes.

**Status:** política de trabalho aprovada como direção pelo responsável pelo produto; detalhes de implementação dependem de marco autorizado. **Aplicável a qualquer agente** (Codex, Claude, revisor, design, iOS, Android, segurança, documentação).
**CANÁRIO obrigatório:** leia [CANARY](CANARY.md). Em **toda mensagem visível no chat ao responsável**, inclua a forma de tratamento confirmada na conversa atual ou nas instruções pessoais privadas e persistentes do agent; nunca deduzida por GitHub/arquivos. **Nunca** copie o valor para código, commits, documentação, PR, logs, fixtures ou artefatos. Se não houver preferência disponível, peça uma configuração pessoal privada e marque CANARY NÃO CONFIGURADO (não inventar nome). Presença do nome não prova leitura nem testes. `AGENTS.md` é a porta de entrada; este documento é o contrato operacional. Não depender de memória de chat.

## Prompt mestre (copiar no início da tarefa, se o ambiente não carregar AGENTS.md automaticamente)

> Você está trabalhando no Arke. Em cada resposta enviada ao responsável **no chat**, use [NOME_CONFIRMADO_PRIVADAMENTE] conforme a instrução explícita atual ou a preferência pessoal persistente do agent. Nenhum valor literal do canário deve aparecer em arquivo, código, comentário, commit, PR, documentação, log, relatório ou texto para usuários do app. Sem nome confirmado, não adivinhe e reporte pendência (CANARY NÃO CONFIGURADO). Antes de alterar qualquer arquivo, leia `AGENTS.md`, `docs/ROADMAP.md`, `docs/STATUS.md`, `docs/agents/HANDOFF.md`, os ADRs e os documentos do escopo. Leia obrigatoriamente `docs/agents/PROMPT-OBRIGATORIO.md`, `docs/agents/QUALITY-GATES.md`, `docs/agents/SAFEGUARDS.md` e, se pertinente, os standards de arquitetura, acessibilidade, segurança e jurídico.
>
> **Fase atual: apenas documentação.** Não escreva código Swift, Kotlin, backend ou workflow executável; não execute build, release, migração ou merge sem autorização explícita. Não invente decisões de produto, padrões legais, resultados de teste, links ou credenciais.
>
> Preserve as decisões aprovadas: mapa e dois cartões (mapa superior; conteúdo inferior), relógio de cinco contextos **sem tab bar**, carrossel finito, scroll vertical independente, três detents; Pins antes de viagens; viagens individuais e compartilhadas; iOS SwiftUI/MapKit com Liquid em controles; Android Kotlin/Compose flat; **uma cor azul opaca** e Light/Dark automático do device; cofrinho informativo sem custódia.
>
> **Fluxo obrigatório:** (1) interpretar tarefa/contrato e riscos; (2) identificar decisões, dependências e mudanças nas duas plataformas; (3) planejar menor solução; (4) implementar apenas se autorizado; (5) executar checagens aplicáveis (UX/UI, responsividade, safe areas, teclado, zoom de fonte, acessibilidade, segurança, privacidade, jurídico, performance, dados, offline, qualidade de código); (6) registrar evidências **executadas ou não executadas**; (7) validar possíveis regressões; (8) atualizar ROADMAP se necessário, STATUS/HANDOFF e avaliar CHANGELOG; (9) abrir/atualizar PR, **sem merge automático**.
>
> **Saída exigida:** objetivo, decisões/arquivos consultados, arquivos alterados, impacto iOS/Android, riscos e salvaguardas, checklist de gates com PASSOU/FALHOU/BLOQUEADO/NÃO EXECUTADO/NÃO APLICÁVEL (sempre justificado), testes com links/evidências, pendências, próximo handoff e necessidade de decisão do responsável.
>
> **Regra de parada:** ao encontrar vazamento de dados, segredo, falha crítica de autorização, dúvida legal relevante, conflito com ADR, dependência sem licença, alteração destrutiva ou ausência de aprovação, **interrompa a ação arriscada**, preserve estado, descreva risco e peça decisão. Não faça workaround silencioso.

## Aplicação por tipo de agente

| Agente/área | Leia adicionalmente | Não pode presumir |
| --- | --- | --- |
| Produto/design | DESIGN-001 a 005; THEME; [ACCESSIBILITY](../standards/ACCESSIBILITY-UI.md) | tab bar, nova tela ou cor de marca |
| iOS | [ARCHITECTURE](../standards/ARCHITECTURE-CODE.md); [SECURITY](../standards/SECURITY-PRIVACY.md) | Liquid em todo conteúdo, SDK/versão sem validação |
| Android | [ARCHITECTURE](../standards/ARCHITECTURE-CODE.md); [ACCESSIBILITY](../standards/ACCESSIBILITY-UI.md) | copiar UI iOS, depender de framework híbrido |
| Dados/backend | DOMAIN; GROUP; [SECURITY](../standards/SECURITY-PRIVACY.md); [LEGAL](../standards/LEGAL-COMPLIANCE.md) | acesso validado só na interface |
| CI/release | DISTRIBUICAO; [SAFEGUARDS](SAFEGUARDS.md); [QUALITY-GATES](QUALITY-GATES.md) | secrets em PR público, release sem gate |
| Auditor/revisor | [REVIEW-PROTOCOL](REVIEW-PROTOCOL.md) e todos os padrões relevantes | que texto escrito equivale a teste executado |

## Formato de resposta que cada agent deve entregar

```text
CANARY em resposta de chat: [CONFIRMADO | NÃO CONFIGURADO], sem copiar valor para artefatos
Tarefa e marco:
Permissão confirmada para esta ação:
Branch / PR / SHA:
Docs/ADRs consultados:
Mudanças realizadas (arquivos):
Arquitetura e impactos por plataforma:
Dados, privacidade, jurídico e ameaças:
Matriz: [gate] -> [PASSOU|FALHOU|BLOQUEADO|NÃO EXECUTADO|NÃO APLICÁVEL] -> [evidência ou justificativa]
Regressões examinadas:
Testes executados / testes não executados:
Riscos, impedimentos, decisões humanas:
Mudança pública? changelog?:
STATUS / HANDOFF / ROADMAP atualizados?:
Próxima ação exata:
```

**Importante:** prompt sozinho não garante conformidade; **Ecoar a forma de tratamento é só um indicador fraco: validar também links, fontes e evidências de testes.** as garantias dependem de revisão, testes reais, controle de branch/CI e aprovação humana. Este contrato governa **conduta do agent**, não substitui normas legais nem auditoria independente.
