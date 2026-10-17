# Safe Finance Column Migration Plan

> **For agentic workers:** execute the scoped schema and entity changes without modifying user data.

**Goal:** Let the backend start safely when older `viagem` rows predate `percentual_adiantamento`.

**Architecture:** Allow Hibernate schema update to add the field as nullable; keep the service default for new trips and DTO fallback for historical rows. Make the manual SQL migration backfill nulls before applying the default and `NOT NULL` constraint.

**Tech Stack:** Spring Boot, Hibernate, PostgreSQL SQL migration.

## Tasks

- [ ] Mark the entity column nullable for Hibernate's automatic first-add behavior.
- [ ] Make migration 13 idempotently add the nullable column, backfill historical nulls, then apply default and `NOT NULL`.
- [ ] Package the backend with tests skipped and inspect the generated diff.

## Constraints

- Do not apply migrations to production or change database contents in this task.
- Do not add or run tests.
- Do not commit, push, or deploy as part of this plan.
