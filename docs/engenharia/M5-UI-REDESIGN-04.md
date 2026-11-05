# M5-UI-REDESIGN-04 — Gastos, Cofrinho, Avaliações, Memórias

5 de novembro de 2026. Recorte 04 do [plano](M5-UI-REDESIGN-PLANO.md), conforme os protótipos `Expenses`, `AddExpense`, `Savings`, `Reviews` e `Memories`.

## Entregue (iOS e Android)
- Chip de viagem (menu) em todos os contextos.
- **Gastos:** totais por moeda em cartões Previsto × Realizado; registros em linhas com ícone, descrição, estado em texto e valor formatado por moeda e localidade (sem "BRL 95"); **Adicionar gasto** em sheet contínuo: valor grande, moedas em chips (BRL, USD, EUR, GBP, JPY, KWD), descrição, tipo segmentado (Prevista/Realizada; "Escolha o tipo" deixou de ser opção), botão de salvar flutuante.
- **Cofrinho:** selo "Só você vê", cartão com reservado, barra e "Falta", sheet para definir meta e registrar valor, aviso de registro informativo (sem carteira, depósito ou transferência).
- **Avaliações:** estrelas + nota em texto, cartão com o comentário, ação **Avaliar este lugar** (sheet com estrelas tocáveis).
- **Memórias:** grade de selos com legenda, tile **Nova memória**, selo "Privado".
- Estados vazios com orientação; dados e chaves de armazenamento preservados (`arke.local-finance.v1`, `arke.trip-journal.v1`).

## Não feito (explícito)
- **Campo Data** do gasto: o modelo persistido não tem data; incluí-la exige contrato de dados próprio.
- Extração dos arquivos `LocalFinanceView`/`TripJournalView` e testes unitários de formatação de moeda no `ArkeCore`.
- "Avaliar este lugar" avalia a viagem como antes (sem vínculo a um lugar específico): o modelo atual guarda avaliação por viagem.
- Capturas e testes de UI específicos destes formulários.

## Evidência
Pendente de CI (ver STATUS).
