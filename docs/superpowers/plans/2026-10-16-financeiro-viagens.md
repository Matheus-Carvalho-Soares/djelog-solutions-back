# Controle financeiro de viagens Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implementar comissão editável, recebimentos de frete vinculados à viagem, despesas parceladas com vencimento/baixa, dashboard e relatório unificados, localidades brasileiras e revisão visual.

**Architecture:** Backend Spring Boot owns calculations, ownership validation, persistence and Excel export. Angular consumes those contracts and presents schedules and summaries. Preserve the fare total on `Viagem`; represent receipts and expense installments as separately dated records, so trip revenue and cash settlement remain distinct.

**Tech Stack:** Java 21, Spring Boot, JPA, SQL migrations, Angular, TypeScript, Chart.js, IBGE API de Localidades.

**Spec:** `docs/superpowers/specs/2026-10-16-financeiro-viagens-design.md`

## Global Constraints

- Trip freight revenue belongs to the week of `dataInicio`, even when received later.
- Expense installments belong to the week of `dataVencimento`; payment records carry `dataPagamento`.
- Use BigDecimal/decimal cents for money and enforce authenticated owner scoping.
- Preserve historical trip, expense, and stay records; do not physically delete stay data.
- Weeks run Monday through Sunday.
- The IBGE API is used only for Brazilian states and municipalities.
- Do not commit or push.

## Review Focus

- Freight split rounding and edited commission values must not make installment totals differ from the contracted fare.
- Partial and late receipts must reduce only their trip balance and must not duplicate weekly revenue.
- Expense installments with unknown legacy payment state must not be silently marked paid.
- Owner checks must cover standalone vehicle expenses and trip receipts.
- Date-range queries must use trip start for revenue and due date for expenses.

## Implementation Status

- [x] Task 1: Finance tables, owner-scoped services and receipt/expense APIs implemented.
- [x] Task 2: Commission/split defaults, receipt confirmation and late receipt dates implemented.
- [x] Task 3: Independent, vehicle and trip expenses with installments implemented.
- [x] Task 4: Dashboard periods, filters, categories and profit implemented.
- [x] Task 5: Unified filtered report and date-based expense/revenue rows implemented.
- [x] Task 6: Brazil-only locality selectors and newest-first trip ordering implemented.
- [x] Task 7: Stay UI/API/calculations discontinued while historical table/data remain; contrast rules updated.
- [x] Task 8: Frontend production build and backend compile passed; diff checks are clean.
- [ ] Apply SQL migrations to the target database before deployment.
- [x] Independent final review completed; its important findings were fixed and the backend/frontend builds passed again.

Automated tests were not added or run in this pass, per the execution instructions.

---

### Task 1: Financial persistence and ownership

**Files:**
- Create migration `djelog-solutions-back/src/main/resources/db/13_FINANCEIRO_VIAGEM.sql`.
- Create JPA entities/DTOs/repositories/services/controllers for trip receipts and expense installments.
- Modify `Viagem`, `Despesa`, and their DTO/service mappings.
- Preserve existing expense data and map its legacy due date to trip start with payment state `NAO_INFORMADO`.

**Deliverable:** Owner-scoped APIs for freight receipt schedule/confirmation and expenses with optional trip/vehicle associations and installment dates/status.

### Task 2: Trip entry and settlement UI

**Files:**
- Modify `create-viagem-dialog` component/template/styles.
- Add or update trip detail/receipts dialog and Angular models/services.
- Show default 15% commission in BRL and default 70/30 payment split in BRL; persist receipt confirmations and dates against the trip.

**Deliverable:** Create/edit trip, review pending balance and record partial/late receipts.

### Task 3: Expense entry and installments

**Files:**
- Add standalone expense screen/dialog under `screens/despesas` and route/sidebar entry.
- Modify trip expense UI and expense services/models.
- Add payment-state/date controls to each installment.

**Deliverable:** Add trip-linked, truck-linked, or independent expenses and maintain their installments and payment dates.

### Task 4: Dashboard financial calculations and chart

**Files:**
- Modify `dashboard-data.service.ts`, dashboard overview component/template/styles and `performance-chart` component.
- Add date range filters and category series/totals.

**Deliverable:** Weekly/monthly revenue by trip date, expenses by due date, profit, receipt balances by trip, and category breakdown filtered consistently.

### Task 5: Report query repair and unified report

**Files:**
- Modify `ViagemRepository`, `RelatorioService`, report DTOs/controllers and `ExcelService`.
- Consolidate report routes/components and maintain filtered backend export.

**Deliverable:** One report flow with date/entity/status/category/payment filters and detailed/grouped outputs; period filtering applies to trip start or expense due date according to the spec.

### Task 6: Brazilian city selectors and trip ordering

**Files:**
- Add Angular IBGE locations service and unit-independent cache.
- Add municipality/UF code snapshot fields via migration and map DTOs.
- Modify trip entry/import compatibility and trip list sorting.

**Deliverable:** Select city/UF for origin, stop and destination; default newest trip first with inverse ordering control.

### Task 7: Discontinue stays and audit contrast

**Files:**
- Remove stay navigation, route, screen and new calculation/reporting references while retaining the DB table/data.
- Review global styles plus all filters/input component styles.

**Deliverable:** No active stay workflow or new stay totals; consistent readable fields and controls in every state.

### Task 8: Build verification and review

**Files:**
- Review all changed backend/frontend files and migration scripts.

**Deliverable:** Backend compilation and frontend production build succeed; report any unavailable runtime/database checks separately.
