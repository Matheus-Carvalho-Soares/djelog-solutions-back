# Djelog financial workflow and production release — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:executing-plans` to implement this plan task by task. Steps use checkbox (`- [ ]`) syntax.

**Goal:** Integrar e publicar em produção as correções financeiras aprovadas e permitir excluir empresas preservando viagens e importações históricas.

**Architecture:** Spring Boot permanece responsável por persistência, regras de negócio, autorização por usuário e exportação XLSX. Angular apresenta lançamentos, filtros e gráficos; a API de localidades do IBGE atende somente municípios e estados brasileiros. Aplicar migrações compatíveis antes de publicar os serviços que dependem delas.

**Tech Stack:** Java 21, Spring Boot, JPA, PostgreSQL/Supabase, migrações SQL, Angular 17, TypeScript, Chart.js, API de Localidades do IBGE, Render e Netlify.

**Spec:** `docs/superpowers/specs/2026-10-16-financeiro-viagens-design.md`

**Path convention:** Paths in a task with one repository are relative to that repository root. Tasks spanning both repositories label each file group `Web:` or `Back:`.

## Global Constraints

- Toda receita de frete pertence à semana de `dataInicio` da viagem, mesmo quando recebida depois.
- Despesas entram na semana da parcela por `dataVencimento`; pagamentos guardam `dataPagamento`.
- Comissão padrão = `valorFrete * 0,15`; exibir e permitir alterar o valor em reais.
- Pagamento à vista confirma o frete integral. Adiantamento inicia com valor equivalente a 70% do frete e saldo calculado em reais, sem mostrar porcentagem na tela da viagem.
- O valor inicial de comissão e o valor inicial do adiantamento são sugestões editáveis em reais; a porcentagem é usada somente para calcular o padrão.
- Usar `BigDecimal` no backend e manter lançamentos e consultas limitados ao usuário autenticado.
- Preservar viagens, despesas, recebimentos e linhas de importação quando uma empresa for excluída; remover apenas os vínculos da empresa.
- Preservar dados históricos de estadia e de despesas; não apagar dados históricos de estadia.
- Semanas financeiras começam na segunda-feira e terminam no domingo.
- Localidades disponíveis ficam limitadas ao Brasil e à API oficial do IBGE.
- O usuário autorizou commits, push para `main`, migrações e deploy de produção; não fazer force push.
- Validar build do Angular e compilação/empacotamento do Spring; não adicionar nem executar testes automatizados neste trabalho.

## Review Focus

- Pagamento à vista deve criar recebimento integral na própria viagem; adiantamento e recebimentos tardios devem reduzir apenas o saldo daquela viagem.
- Cada recebimento guarda seu valor, data real e observação opcional; receita e lucro continuam atribuídos à semana da viagem, não à semana em que um saldo foi recebido.
- O adiantamento inicial e o total recebido não podem ultrapassar o frete; o saldo deve fechar em centavos. O valor da comissão permanece editável em reais.
- Despesas parceladas devem ser atribuídas à semana de vencimento e só aparecer pagas com uma data de pagamento confirmada.
- Exclusão de empresa deve ser limitada ao proprietário autenticado e nunca apagar viagens nem linhas de importação.
- Datas de viagem e de vencimento precisam filtrar receitas e despesas por suas datas financeiras, sem deslocamento de fuso horário.
- Migrações devem preservar dados existentes, ser executadas somente quando pendentes e deixar o schema compatível com o backend antes do deploy.

---

### Task 1: Finalize financial persistence and API contracts

**Repository:** `djelog-solutions-back`

**Files:**
- Modify `src/main/java/com/djelog/controllers/ViagemController.java`; DTOs `src/main/java/com/djelog/dtos/{ViagemDTO,DespesaDTO,ViagemRelatorioDTO,RelatorioAgrupadoDTO}.java`.
- Modify entities `src/main/java/com/djelog/entities/{Viagem,Despesa}.java`; repositories `src/main/java/com/djelog/repositories/{ViagemRepository,DespesaRepository}.java`.
- Modify services `src/main/java/com/djelog/services/{ViagemService,DespesaService,RelatorioService,ExcelService,ImportacaoViagemService}.java`.
- Add/retain `src/main/java/com/djelog/controllers/RecebimentoFreteController.java`, DTOs `src/main/java/com/djelog/dtos/{RecebimentoFreteDTO,ParcelaDespesaDTO}.java`, entities `src/main/java/com/djelog/entities/{RecebimentoFrete,ParcelaDespesa}.java`, repository `src/main/java/com/djelog/repositories/RecebimentoFreteRepository.java`, service `src/main/java/com/djelog/services/RecebimentoFreteService.java`, and migration `src/main/resources/db/13_FINANCEIRO_VIAGEM.sql`.

**Interfaces:**
- Replace the current JPA-entity request body with a dedicated create request DTO that contains the existing trip fields plus `formaPagamentoFrete: A_VISTA | ADIANTAMENTO`, an editable `valorAdiantamento` in BRL, and `dataRecebimentoInicial` (defaults to today in the UI and is editable). Keep `ViagemDTO` as the response contract.
- `ViagemService.create` persists the trip and initial receipt in one transaction: full freight for `A_VISTA`; the entered/default 70%-of-freight amount for `ADIANTAMENTO`. The initial receipt uses `dataRecebimentoInicial`; both initial and later receipts reduce the trip's pending balance, while reporting revenue stays assigned to trip start week.
- `POST /api/viagem/{viagemId}/recebimentos` records later confirmations with amount, date, and optional note; cumulative receipts cannot exceed the trip freight.
- `DespesaDTO` carries optional trip and vehicle references plus installments containing amount, due date, payment state, and payment date.

- [ ] Review existing dirty changes and reconcile the new create request DTO, response DTO, entity, repository, and controller mappings against the exact interfaces above.
- [ ] Confirm ownership checks cover trips, receipts, vehicle-only expenses, and independent expenses.
- [ ] Preserve historical expenses as `NAO_INFORMADO`; do not infer they were paid.
- [ ] Keep receipt totals attached to the trip and commission represented once in category `COMISSAO`.
- [ ] Review migration 13 for safe backfill and schema compatibility; retain historical totals while adding installments and receipts.

### Task 2: Complete trip entry, receipt confirmation, and expense entry

**Repository:** `djelog-solutions-web`

**Files:**
- Modify `src/app/screens/viagens/create-viagem-dialog/create-viagem-dialog.component.ts`, `.html`, `.css`, `viagens.component.ts`, `viagens.component.html`.
- Modify `src/app/models/viagem.model.ts`, `src/app/services/viagem/viagem.service.ts`, and `src/app/app.routes.ts`.
- Add/retain the three files in `src/app/screens/viagens/recebimentos-dialog/`.
- Add/retain `src/app/screens/despesas/despesas.component.ts`, `.html`, `.css`, and `despesa-dialog.component.ts`, `.html`, `.css`.

- [ ] Set commission to 15% of freight as an editable R$ value; recompute while freight changes until the user edits the commission.
- [ ] Add `À vista` and `Adiantamento` modes. `À vista` records the full freight as received; `Adiantamento` prefills an editable R$ amount equivalent to 70% and shows the remaining R$ balance without a percentage.
- [ ] Keep later receipt confirmations and their dates/notes associated with the original trip, including receipts after the trip week.
- [ ] Keep trip-linked expenses in trip entry and support fuel, toll, maintenance, and other categories with installment amount, due date, paid/pending state, and payment date.
- [ ] Keep the independent expense screen able to attach an expense to a vehicle or to neither a vehicle nor a trip.
- [ ] Ensure changes to a trip do not duplicate initial receipts or discard its receipt history.

### Task 3: Align dashboard totals, filters, weekly bars, and performance

**Repository:** `djelog-solutions-web`

**Files:**
- Modify `services/dashboard/dashboard-data.service.ts`.
- Modify `dashboard/components/dashboard-overview/dashboard-overview.component.ts`, `.html`, `dashboard.component.html`.
- Modify `dashboard/components/financial-cards/financial-cards.component.ts`, `.html`, `.css`.
- Modify `dashboard/components/performance-chart/performance-chart.component.ts`, `.html`.

- [ ] Apply dashboard date, vehicle, and professional filters consistently to financial totals and chart data.
- [ ] Draw weekly bars for revenue, expense total stacked by toll/fuel/commission/maintenance/other, and profit.
- [ ] Allocate trip revenue and commission to trip start week; allocate expense installments to due week; keep late receipts as balances and annotations only.
- [ ] Show future expenses as a separate index and keep weekly/monthly performance rows with profit = revenue minus expenses.
- [ ] Check that commission is counted once when an explicit commission expense exists.

### Task 4: Repair report date filtering and unify report flows

**Repositories:** `djelog-solutions-web` and `djelog-solutions-back`

**Files:**
- Web: add/retain `src/app/screens/relatorios/super-relatorio.component.ts`, `.html`, `.css` and update `src/app/screens/relatorios/relatorios.component.ts`, `.css`.
- Web: modify `src/app/screens/relatorios/relatorio-por-data/relatorio-por-data.component.ts`, `.html`, `src/app/screens/relatorios/relatorio-agrupado/relatorio-agrupado.component.ts`, `.html`, `src/app/models/relatorio.model.ts`, and `src/app/services/relatorio/relatorio.service.ts`.
- Back: modify `src/main/java/com/djelog/controllers/ViagemController.java`, `ExcelController.java`, `services/RelatorioService.java`, `ExcelService.java`, report DTOs, and repository query.

- [ ] Use one report screen with date, vehicle, professional, company, trip status, expense category, and payment-state filters.
- [ ] Ensure trip rows filter by trip start and expense installment rows filter by due date.
- [ ] Keep grouped and detailed views for trip, vehicle, professional, and category with the same filter state.
- [ ] Export the same filtered result as the report view, using backend XLSX generation where that contract already exists.
- [ ] Remove the old standalone report flows from navigation without removing report APIs still used by the unified report.

### Task 5: Finish Brazilian localities, trip sorting, stay retirement, and contrast

**Repositories:** `djelog-solutions-web` and `djelog-solutions-back`

**Files:**
- Web: add/retain `src/app/models/localidade.model.ts` and `src/app/services/localidade/ibge-localidades.service.ts`.
- Back: add/retain migration `src/main/resources/db/14_LOCALIDADES_IBGE.sql`.
- Web: modify trip form files from Task 2 and `src/app/screens/viagens/viagens.component.ts`, `.html`.
- Web: remove stay navigation/routes and the existing `src/app/screens/estadias/`, `src/app/models/estadia.model.ts`, and `src/app/services/estadia/estadia.service.ts`; back: remove active stay controllers/services/DTO and stay totals from new reporting. Keep the historical entity and repository available for stored data and existing build-time references.
- Web: review `src/styles.css` and input/filter styles under `src/app/screens/**` and `src/app/dashboard/**`.

- [ ] Limit locality selectors to Brazilian states and municipalities from IBGE; retain the typed route text for import compatibility and persist selected municipality/UF codes.
- [ ] Default trips to newest-first and provide a clear ascending/descending control.
- [ ] Remove active stay workflows and calculations while leaving historical stay table data intact.
- [ ] Review text and placeholder contrast for normal, focus, selected, disabled, and error states across all filters and inputs.

### Task 6: Add safe company deletion

**Repositories:** `djelog-solutions-back` and `djelog-solutions-web`

**Files:**
- Back: add `src/main/java/com/djelog/services/EmpresaService.java` and migration `src/main/resources/db/15_EMPRESA_EXCLUSAO_SEGURA.sql`.
- Back: modify `src/main/java/com/djelog/controllers/EmpresaController.java` so delete delegates to owner-scoped service logic and returns `204` only for the authenticated owner.
- Web: modify `src/app/services/empresa/empresa.service.ts`, `src/app/screens/empresas/empresas.component.ts`, and `.html`.

- [ ] Replace the current direct repository delete with a service operation scoped by company ID and authenticated user ID.
- [ ] Set nullable company references on `viagem` and `viagem_importacao_linha` to `ON DELETE SET NULL`; preserve trip, expense, receipt, import, and `empresa_original` data.
- [ ] Add a confirmation action in the company table and refresh the list after successful deletion; surface a failure without hiding the row.
- [ ] Add migration 15 as an idempotent, named constraint update compatible with existing migration 11/12 schemas.

### Task 7: Review changes and build both applications

**Files:**
- Review all changed files in both repositories and migrations `13_FINANCEIRO_VIAGEM.sql`, `14_LOCALIDADES_IBGE.sql`, and `15_EMPRESA_EXCLUSAO_SEGURA.sql`.

- [ ] Check staged/unstaged diffs and `git diff --check` in each repository without overwriting unrelated work.
- [ ] Run `npm run build` in `djelog-solutions-web`.
- [ ] Run `./mvnw.cmd -q -DskipTests package` in `djelog-solutions-back`.
- [ ] Review expense/receipt ownership, report filters, migration ordering, and application startup schema validation.
- [ ] Record build results and any remaining production-only checks; no automated tests are added or run in this plan.

### Task 8: Apply production migrations and release in order

**Files/Systems:**
- GitHub `main` branches for `djelog-solutions-back` and `djelog-solutions-web`.
- Production PostgreSQL project used by the Render backend; Render service `djelog-solutions-back`; Netlify site attached to the frontend repository.

- [ ] Confirm the target database, effective schema state, and migration ledger; do not assume scripts 13 or 14 are unapplied.
- [ ] Verify database recovery posture and set `SPRING_JPA_HIBERNATE_DDL_AUTO=validate` for production if it is not already effective.
- [ ] Apply only pending migrations 13, 14, and 15 in order; confirm added columns, tables, and `ON DELETE SET NULL` constraints afterward.
- [ ] Commit the reviewed backend and frontend changes on their existing `main` branches; do not include unrelated files.
- [ ] Push backend `main` after migrations and verify Render deployment and backend health before pushing frontend `main`.
- [ ] Push frontend `main`, then verify the Netlify deployment and that the production site loads the new app against the updated API.
- [ ] Report exact commit IDs, applied migrations, deployment IDs/status, and any authenticated acceptance that could not be verified without the user’s session.
