# FINANCE-001 — Gastos ≠ Cofrinho

**Status:** proposta de UX/domínio. O Arke inicial **não** movimenta, recebe, custodia ou transfere dinheiro.

## Distinção necessária

| | Gastos | Cofrinho |
| --- | --- | --- |
| Pergunta | Quanto a viagem custa e como dividir? | Quanto queremos reservar para viajar? |
| Registros | orçamento, despesas previstas/reais, rateio declarativo | meta e contribuições declaradas |
| Resultado | total previsto, realizado, responsabilidade | progresso da meta e restante |
| Pagamento | marcar manualmente como pago, sem processar dinheiro | registrar economia já feita fora do app |
| Visibilidade | viagem ou pessoal, explícita | pessoal por padrão; compartilhado só opt-in |
| UI | recibo/atividades de custo | progresso/objetivo sem aparência de carteira |

## Fluxos dentro do cartão inferior

**Gastos:** seletor relógio → contexto Gastos → resumo da Trip → lista vertical de despesas → adicionar/editar em área expandida → salvar sem sair do mapa.

**Cofrinho:** seletor relógio → contexto Cofrinho → metas disponíveis → progresso → registrar valor declarativo. Meta pode pertencer a pessoa ou grupo, mas mostrar progresso agregado nunca deve revelar contribuição individual sem consentimento.

## Regras de dados

- Registrar `currencyCode` por item; não somar BRL e EUR sem taxa e referência temporal.
- Distinguir `planned`, `incurred` e `settled` conceitualmente; status reais ainda exigem definição.
- Componente “quem deve a quem” é cálculo informativo, não cobrança.
- Preservar histórico e autoria ao corrigir valor; em grupo, conflitos em montantes precisam de solução auditável.
- Percentual de progresso precisa explicitar denominador, arredondamento e meta zerada.
- Não usar rótulos “saldo disponível para saque”, “depositar no Arke” ou “pagamento confirmado pelo Arke”.

## Tratamento de falhas

Sem moeda, sem taxa, meta vazia, despesa duplicada, participantes que saíram, falta de rede e edições concorrentes exigem estados explícitos. Dados não sincronizados não devem parecer compartilhados.

## Primeiro recorte local

Implementação em SwiftUI e Compose. Validação nativa pendente.

Em **Gastos**, a pessoa seleciona uma viagem local e registra descrição, valor, moeda e se a despesa é prevista ou realizada. A lista permite corrigir registros e apresenta totais separados por moeda e estado. Uma despesa realizada substitui seu valor previsto no total, sem contar o mesmo item duas vezes.

Em **Cofrinho**, a pessoa define uma meta pessoal por viagem e registra quanto já reservou fora do app. Meta e contribuições usam a mesma moeda. O progresso mostra valor reservado, valor da meta e quanto falta, com restante mínimo de zero quando a meta é superada.

Os valores usam unidades mínimas inteiras da moeda e entrada conforme a região do aparelho. Rateio, conversão cambial e compartilhamento ficam para recortes posteriores. A validação deve cobrir moedas com casas decimais diferentes, edição de valores, isolamento entre viagens e reabertura sem conexão.

## Decisões pendentes

Moeda-base por Trip, cotação manual/automática, regras de rateio, exportação e permissões exatas. Cofrinho coletivo e visibilidade de contribuições requerem aceite específico.
