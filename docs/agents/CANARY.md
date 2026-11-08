# CANARY — Nome somente nas respostas privadas do chat

> **Atualização 2026-11-08 (decisão do responsável):** a fonte do nome passa a ser o próprio responsável, **no início de cada sessão**. O nome fica só na conversa (o hook PreCompact o preserva na compactação) e nunca é gravado em arquivo. Sem nome informado: perguntar uma vez e marcar `CANARY NÃO CONFIGURADO`. Prevalece sobre os itens 2–4 abaixo. Regra genérica: `docs/agents/standard/CANARY.md`.

**Status:** diretriz aprovada. Este arquivo contém somente a regra genérica, **nunca o valor do nome**. O canário é um sinal fraco de que a instrução foi seguida, não prova de ausência de alucinações.

## Fonte da preferência e ordem de prioridade

1. O **nome explicitamente atualizado pelo responsável na conversa atual** prevalece.
2. Na ausência de atualização, usar a forma de tratamento **confirmada nas instruções pessoais e persistentes da ferramenta**, quando fornecidas ao agent.
3. Se não houver informação disponível, **não deduzir** pelo usuário GitHub, arquivos, perfis, e-mails, metadados ou nomes de terceiros. O agent sinaliza `CANARY NÃO CONFIGURADO` e solicita **uma configuração privada por ferramenta**, em vez de perguntar em toda conversa.
4. **ChatGPT, Codex, Claude e outros agents não compartilham automaticamente preferências globais.** O responsável pode configurar cada serviço no espaço pessoal e privado correspondente; isso não é parte do projeto nem do Git.
5. Nenhuma mensagem deve afirmar que o perfil pessoal de uma ferramenta foi alterado sem resposta confirmando essa alteração.

## Regra absoluta de saída

**Cada resposta visível no chat com o responsável** (inclusive progresso, esclarecimento, falha e entrega final) deve conter a forma exata de tratamento recebida pelo agent. Preferencialmente no começo ou fim da mensagem.

O valor **nunca** deve ir para código fonte, testes, fixtures, scripts, comentários, strings de UI, arquivos de configuração do projeto, documentos, ADRs, README, CHANGELOG, commits, PRs, issues, logs, screenshots ou artefatos exportados.

**Única exceção de armazenamento permitida:** preferências **pessoais privadas da própria ferramenta**, fora do repositório, salvas pelo responsável por seu mecanismo de configuração. Não propagar o nome por ambiente, Git ou arquivos públicos.

## Exemplo genérico

> **[NOME_CONFIRMADO_PRIVADAMENTE]**, atualizei o contrato M3 e conferi os links da branch. Não executei E2E porque o app ainda não foi implementado. Permanece pendente a decisão de permissões de grupo.

O placeholder não é o nome; **não** escrevê-lo literalmente em uma resposta real para fingir que o canário foi configurado.

## G-15: validar sem inventar garantias

- **Presença:** cada resposta do chat continha o nome confirmado e apropriado?
- **Fonte:** o agent realmente leu ADRs, contratos, branch e revisão atual?
- **Provas:** testes, links e resultados foram verificados, com limitações explícitas?
- **Isolamento:** diff, PR e arquivos estão **sem o valor do nome**?

A verificação do chat não deve copiar o nome para relatórios persistentes. **Um agente pode repetir o nome e ainda alucinar**: G-15 não substitui G-01..G-14, revisão, testes ou evidências. Não usar o canário como senha ou autenticação.

[AGENTS](../../AGENTS.md) · [PROMPT](PROMPT-OBRIGATORIO.md) · [QUALITY-GATES](QUALITY-GATES.md).
