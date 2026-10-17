# Controle financeiro de viagens e cadastro de empresas — Especificação

> Revisão de escopo em 17/10/2026: inclusão da exclusão segura de empresas e da liberação coordenada para produção.

## Objetivo

Controlar receita e despesas por viagem e por semana, com valores recebidos/pagos, saldos pendentes, parcelas, filtros coerentes e relatórios que usem as mesmas regras financeiras.

## Regras confirmadas

- Ao informar o frete, preencher a comissão em reais com 15% do valor do frete. O valor calculado pode ser alterado.
- O modo de pagamento do frete é à vista ou adiantamento. À vista confirma o frete integral; no adiantamento, o valor em reais vem preenchido com o padrão 70/30, pode ser alterado e o saldo em reais é calculado sem exibir percentuais na tela da viagem.
- Toda receita de frete pertence à semana da viagem (`dataInicio`) pelo valor total do frete, independentemente das datas dos recebimentos.
- Cada recebimento fica associado à viagem, registra valor e data de recebimento, e reduz o saldo pendente dessa viagem. Um recebimento tardio não cria receita na semana em que entrou.
- Despesas podem estar associadas a uma viagem, a um veículo sem viagem, ou ser independentes. Cada despesa pode ter uma ou mais parcelas com valor, vencimento, estado pago/pendente e data de pagamento.
- Despesas previstas entram na semana de vencimento. A data de pagamento serve para confirmar e registrar a baixa.
- O módulo de estadias será descontinuado nos fluxos e cálculos. Registros antigos permanecem armazenados para preservar histórico, mas deixam de compor novos totais.
- A seleção de localidades cobre municípios brasileiros e seus estados, baseada na API oficial de localidades do IBGE.
- A lista de viagens abre da mais recente para a mais antiga, com controle para inverter a ordem.
- O dashboard terá período selecionável, receita, despesa, lucro e despesas discriminadas por categoria.
- Os relatórios existentes serão reunidos numa tela com filtros e agrupamentos/detalhes por viagem, veículo, profissional e categoria, com exportação seguindo os mesmos filtros.
- Inputs e filtros serão revisados para legibilidade em estados normal, foco, seleção e desabilitado.
- Empresas poderão ser excluídas com confirmação. A exclusão remove somente o cadastro da empresa: viagens e linhas de importação permanecem, com o vínculo da empresa removido. Não haverá exclusão em cascata de viagens, recebimentos, despesas ou importações.

## Convenções de cálculo

- Comissão padrão: `valorFrete * 0,15`; guardar a taxa usada e qualquer valor manualmente substituído para que a edição da viagem não perca a decisão do operador.
- Comissão integra despesas uma única vez, na categoria `COMISSAO`.
- A exclusão de empresa remove os vínculos opcionais de `viagem` e `viagem_importacao_linha`, mantendo os demais dados históricos. O texto original da empresa importada continua preservado na linha de importação.
- Receita semanal usa a data da viagem e o total do frete; recebimentos são estado de liquidação, não nova receita.
- Despesa semanal prevista usa data de vencimento da parcela. Dados históricos sem vencimento usam a data da viagem como referência histórica e ficam com situação de pagamento `NAO_INFORMADO`.
- Lucro semanal é receita das viagens iniciadas naquela semana menos parcelas de despesas vencendo naquela semana.
- Semanas seguem segunda a domingo, conforme a implementação atual do dashboard.

## Contratos e compatibilidade

- Manter a propriedade de valor total do frete como total contratado.
- Preservar dados antigos de estadia e de despesas; não apagar informação histórica.
- Manter textos existentes de origem, parada e destino para exibição/importação, adicionando códigos de município/UF para novos registros selecionados na API.
- Novas despesas e recebimentos devem ser escopados ao usuário autenticado no backend.
- A exclusão de empresa deve buscar a empresa pelo ID e pelo usuário autenticado; não deve aceitar exclusão por ID global.
- Exportação de relatórios segue no backend e usa a mesma consulta e os mesmos filtros da tela.

## Assunções de implementação

- O filtro de faturamento por data usa a data de início da viagem. Despesas são filtradas por vencimento.
- O pagamento de cada parcela do frete pode ser confirmado parcialmente; saldo por viagem é o total contratado menos recebimentos registrados.
- O adiantamento inicial equivale a 70% do frete, mas a operação da tela é feita por valores em reais editáveis; o saldo corresponde ao frete menos o adiantamento e os valores não podem exceder o frete.
- Despesas antigas sem informação de pagamento não serão marcadas como pagas automaticamente.

## Fora de escopo

- Exclusão física das linhas históricas da tabela de estadias.
- Integração com localidades fora do Brasil.
- Contabilidade por competência fiscal ou amortização de ativos; o lucro semanal aqui é operacional.

## Liberação em produção

- Integrar as alterações aprovadas nos repositórios Angular e Spring já posicionados na branch `main`.
- Confirmar o estado real das migrações no banco antes de executar os scripts 13, 14 e a nova migração de exclusão de empresa, aplicando somente as que ainda estiverem pendentes e na ordem correta.
- Aplicar as migrações antes de publicar os serviços que dependem delas; validar o schema na inicialização e conferir a saúde do backend e a publicação do frontend após o deploy.
- Não publicar enquanto build, revisão dos contratos financeiros e checagem das migrações pendentes não estiverem concluídos.