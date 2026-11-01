# Questões abertas — propostas ainda não são decisões

## Marca e sistema visual

- A marca gráfica será rota, selo ou tipografia? Ver [BRAND-001](brand/BRAND-001.md).
- A expressão “Onde vamos?” será nome de produto, slogan ou só pergunta editorial?
- Pesquisar disponibilidade de nome/marca e direitos de uso antes de publicação comercial.
- **Decisão encerrada:** uma cor azul opaca de marca; demais superfícies e textos seguem tema do dispositivo (ADR-006).
- **Pendente:** aprovar o HEX exato do azul proposto e o contraste sobre mapa real, controles do sistema e alto contraste.
- Selecionar tipografia e política de licença, desenho de ícones, favicon, avatar e ícone de app.
- Definir stamp original: variante padrão, exportação, direitos de fotografias, tratamento de EXIF e compartilhamento.
- Definir se estampas de memórias integram Place, Trip ou ambos na UI.

## Interação e produto

- O relógio será realmente circular, popover transformado ou híbrido com fallback acessível?
- A câmera do mapa segue apenas a seleção final em swipe rápido?
- Qual feedback tátil é útil? Como respeitar Remove Animations?
- Três detents são suficientes em tablets, landscape e fontes grandes?
- Pin pessoal salvo fica independente ao ser removido de Trip?
- Autosave e retenção de drafts; pesquisa/localização/link como primeira entrada.

## Canário de chat e segurança da governança

- **Decisão aprovada:** cada mensagem do agent no chat deve incluir o nome de tratamento explicitamente confirmado pelo responsável; o valor nunca integra código, docs, PR, logs ou artefatos (ADR-008).
- **Regra confirmada:** o tratamento foi informado em conversa; novas sessões podem ler o valor de preferências privadas persistentes de cada ferramenta. A configuração dessas ferramentas é externa ao GitHub e não pode ser atestada por este repositório.
- **Validação:** nome é sinal simples de protocolo, não prova anti-alucinação; revisar fontes, SHA, testes e evidências.

## Viagem em grupo

- Modo convidado/contas e convites; expiração e revogação.
- Papéis owner/editor/viewer; quem pode convidar?
- Visibilidade de mídia, despesas e contribuições pessoais.
- Transferência de titularidade e resolução de conflitos simultâneos.

## Gastos e cofrinho

- Moeda-base, múltiplas moedas, conversão e regras de rateio.
- Cofrinho coletivo é apenas progresso agregado com opt-in?
- Tratamento de edições financeiras e contribuições declarativas.

## Engenharia e direitos

- Fornecedor escolhido em [ADR-010](decisoes/ADR-010.md): Apple Maps no iOS e Google Maps no Android. Projeto Google Cloud, chave restrita, custos e offline seguem pendentes; nenhuma conta/faturamento ativados.
- Termos, privacidade e LGPD para dados de grupo e localização.
- Licenças de código e direitos de marca/assets.
- Identificadores Apple/Android e releases apenas após iniciar código.

Decisões devem virar ADRs antes de promover propostas a requisitos. Ver [Roadmap](ROADMAP.md).
