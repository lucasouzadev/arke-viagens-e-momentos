# GROUP-001 — Viagens solo e em grupo

**Status:** proposta de contrato; nenhum backend nem autenticação foi implementado.

## Primeiro recorte de colaboração aprovado

A ação **Participantes** abre a lista de membros e convites de uma viagem. O proprietário convida uma conta identificada e escolhe entre **Leitor** e **Editor**. O destinatário autenticado vê a viagem e o papel antes de aceitar. Um Leitor consulta o roteiro compartilhado. Um Editor também pode adicionar itens e editar os que criou. O proprietário administra participantes e convites e pode editar o roteiro compartilhado.

- Convites são vinculados à viagem e ao destinatário, expiram em sete dias, têm uso único e podem ser revogados antes do aceite. Aceite e revogação são operações atômicas no servidor.
- A viagem local só passa a ter uma versão compartilhada por ação explícita do proprietário. Os Pins pessoais continuam locais até que a pessoa selecione individualmente o que deseja compartilhar.
- Remover um participante bloqueia novas leituras e escritas. O aplicativo descarta o cache compartilhado ao confirmar a revogação. O primeiro recorte exige conexão para ler ou editar conteúdo compartilhado, aceitar convites e administrar membros.
- Cofrinho, despesas, memórias, transferência de propriedade e exclusão de viagem compartilhada ficam para recortes próprios.

**Aceite técnico.** Testar proprietário, editor, leitor, conta externa e membro removido. Cobrir acesso direto por ID, tentativa de editar conteúdo de outro autor, convite expirado ou revogado, dois aceites concorrentes e preservação dos Pins pessoais ao ativar o grupo.

**Decisão registrada.** Os papéis e o prazo de sete dias foram aprovados para este recorte. A infraestrutura de autenticação e dados continua em aberto. Consulte [ADR-013](../decisoes/ADR-013.md).

## Princípio

Uma viagem (`Trip`) tem um ou mais membros. **Não existem duas espécies de Trip** (“solo” e “grupo”); mudar composição não recria a viagem nem seus Pins.

## Jornada de colaboração proposta

1. Pessoa cria Trip pessoal; Places, Pins e rascunhos são privados.
2. Acessa participantes por uma ação contextual do cartão do mapa ou conteúdo.
3. Escolhe convidar, vê o alcance do convite e o papel proposto.
4. Convidado aceita explicitamente. Aceitar acesso não equivale a compartilhar localização ao vivo.
5. Participantes enxergam apenas registros permitidos; autoria de edições é identificável.
6. Remover participante revoga permissões futuras e esclarece o que ocorre com conteúdo que a pessoa adicionou.

## Papéis candidatos (ainda não aprovados)

| Capacidade | Owner | Editor | Viewer |
| --- | --- | --- | --- |
| Visualizar roteiro compartilhado | sim | sim | sim |
| Criar Pins/atividades compartilhadas | sim | sim | não |
| Editar qualquer atividade compartilhada | sim | sim | não |
| Convidar e alterar papéis | sim | por decidir | não |
| Excluir Trip | sim | não | não |
| Ver Cofrinho pessoal de outro membro | **não por padrão** | **não por padrão** | **não** |
| Ver gastos marcados como compartilhados | sim | sim | sim, conforme escopo |

**A tabela não configura permissão final.** Proprietário não recebe acesso automático a conteúdo pessoal de outros participantes.

## Visibilidade explícita

- Personal: apenas titular.
- SharedTrip: membros autorizados da viagem.
- SelectedParticipants: subconjunto explicitamente escolhido, se adotado futuramente.

Fotografias e stamps pessoais não passam automaticamente a compartilhados ao vincular a Trip.

## Concorrência e remoção

- Exibir autor e último horário de alteração em conflitos relevantes.
- Conflito entre duas edições no mesmo item requer resolução clara; não usar last-write-wins silencioso para valores financeiros.
- Remoção de membro: revogar leitura futura, impedir escrita e informar política de retenção dos itens que já foram compartilhados.
- Excluir Trip: confirmação forte, impactos aos outros membros, eventual exportação e procedimento de recuperação a definir.
- Convites: expiração e revogação precisam ser definidas antes da implementação.

## Segurança e privacidade

Cada consulta/escrita ao backend futuro deve filtrar por vínculo e visibilidade **no servidor**, não apenas ocultar botões. Participar de grupo nunca habilita GPS ou upload de contatos.

## Questões abertas
## Detalhamento técnico de autorização proposto

Veja [PERMISSIONS](../arquitetura/PERMISSIONS.md) para escopos e testes de acesso entre Trips e [SYNC-OFFLINE](../arquitetura/SYNC-OFFLINE.md) para revogação de membro durante edição offline. Esses documentos **não aprovam** owner/editor/viewer como papéis finais.

## Questões abertas

Autenticação local/guest, convite por link, validade, papel inicial, transferência de titularidade, exclusão de mídia e tratamento de cópias em outros dispositivos.
