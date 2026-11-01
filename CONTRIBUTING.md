# Contribuindo com o Arke

O Arke iniciou uma fundação nativa delimitada: iOS/Android, testes e compilação de verificação conforme [ADR-009](docs/decisoes/ADR-009.md). Contribuições fora desse recorte continuam dependendo de especificação e autorização. Consulte o [guia de desenvolvimento](docs/engenharia/DEVELOPMENT.md). Backend, distribuição e aprovação das demais propostas não fazem parte desta autorização.

## Antes de abrir uma alteração

1. Leia [AGENTS.md](AGENTS.md) e [ROADMAP](docs/ROADMAP.md). A execução de qualquer agent também exige [PROMPT-OBRIGATORIO](docs/agents/PROMPT-OBRIGATORIO.md), [QUALITY-GATES](docs/agents/QUALITY-GATES.md) e [SAFEGUARDS](docs/agents/SAFEGUARDS.md); documentos técnicos/jurídicos aplicam-se conforme risco.
2. Consulte [STATUS](docs/STATUS.md), [HANDOFF](docs/agents/HANDOFF.md), [ADRs](docs/decisoes/README.md) e a especificação do problema.
3. Estruture o trabalho com [TASK-CONTRACT](docs/agents/TASK-CONTRACT.md).
4. Preserve mapa-first, dois cartões, ausência de tab bar, relógio/carrossel, natividade independente e **um azul opaco com tema do dispositivo**.
5. Registre proposta de mudança de decisão em ADR; não simule aprovação.
6. Mantenha branch/PR pequeno, reveja links e gere [HANDOFF](docs/agents/HANDOFF.md).

## Revisão

Veja [REVIEW-PROTOCOL](docs/agents/REVIEW-PROTOCOL.md) e [QUALITY-001](docs/qualidade/QUALIDADE.md). Toda tarefa avalia UX/UI, viewport, acessibilidade, segurança e impacto iOS/Android, mesmo que documente “não aplicável” com motivo. **Nunca afirme compilação ou teste E2E sem execução real.**

## Comunicação com pessoas

Quando a alteração for perceptível ao usuário, sugerir nota em português simples segundo [CHANGELOG-GUIDE](docs/agents/CHANGELOG-GUIDE.md). Enquanto não há app, descrever **avanços do projeto**, não funcionalidades já disponíveis. O modal de novidades está [somente especificado](docs/releases/NEWS-MODAL.md).

Não enviar tokens, chaves, perfis de assinatura, conteúdo privado ou obras sem licença ao repositório.
