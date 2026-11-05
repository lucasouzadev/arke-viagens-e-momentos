# M5-UI-REDESIGN-05 — Viagens e compartilhamento

5 de novembro de 2026. Recorte 05 do [plano](M5-UI-REDESIGN-PLANO.md), conforme `Trips` e `ShareTrip`.

## Entregue (iOS e Android)
- **Segurança:** removidos do código das duas plataformas a URL e a chave publishable do Supabase embutidas e o formulário de e-mail e senha de teste. Nenhum envio de rede acontece em Compartilhar.
- **Compartilhar** passa a mostrar o estado "Compartilhar exige uma conta Arke" (sem conta, tudo continua no aparelho). Decisão de autenticação (D5, GROUP-001) continua pendente.
- **Viagens** em sheet contínuo: linhas com ícone, nome e contagem de lugares; seção "Fora de viagens" com "Ideias soltas".

## Não feito (explícito)
- **Nova viagem** como ação própria (hoje viagens nascem ao vincular um Pin) e participantes/papéis (dependem de conta e backend).
- Selo de capa e resumo de datas por viagem; "viagem ativa" (não existe nos dados).
- Revisão de RLS no backend e threat model atualizado.
- O histórico do Git ainda contém a URL e a chave publishable antigas; é chave pública, mas convém rotacionar quando D5 for decidido.

## Evidência
Pendente de CI (ver STATUS).
