# 05 - Strategia Migrazioni

> Strumento: **Flyway Community** su MariaDB 10.11 (D1 risolta). Schema mantenuto portabile (D4) per non precludere alternative future. Convenzione versioning: `V<n>__<descrizione_snake_case>.sql`. Repeatable migrations `R__*.sql` riservate a viste e seed di catalogo.

## 1. Layout repository

```
infra/
└── db/
    └── migrations/
        ├── V1__init_iam.sql
        ├── V2__bc1_practice.sql
        ├── V3__bc2_task.sql
        ├── V4__bc3_document_checklist.sql
        ├── V5__bc4_bpm_integration_outbox.sql
        ├── V6__bc6_signal.sql
        ├── V7__audit.sql
        ├── R__seed_roles_groups.sql
        ├── R__seed_checklist_catalog.sql
        └── R__seed_users_demo.sql
```

> Le migrazioni della POC sono **additive**: nessun `DROP TABLE` su tabelle non vuote in produzione. Le `R__` (repeatable) vengono ri-applicate quando cambia l'hash → ideali per cataloghi statici.

## 2. Versionamento

| Versione | Scopo | Reversibile |
|---|---|---|
| V1 | Schema IAM (utenti, ruoli, gruppi, associazioni) | sì (drop iniziale) |
| V2 | Schema BC1: `practice`, `client_data`, `client_address`, `card_data`, `practice_state_history`, `related_action` | sì |
| V3 | Schema BC2: `task`, `task_assignment_history` | sì |
| V4 | Schema BC3: `attachment`, `checklist_item_catalog`, `checklist_response`, `practice_outcome` | sì |
| V5 | Schema BC4: `bpm_inbound_message`, `bpm_outbound_message`, `event_outbox` | sì |
| V6 | Schema BC6: `signal`, `signal_state_history` | sì |
| V7 | `audit_event` + indici trasversali | sì |
| R__seed_roles_groups | seed ruoli + gruppi base | idempotente (UPSERT) |
| R__seed_checklist_catalog | seed catalogo item checklist (Verbale, Carta) | idempotente |
| R__seed_users_demo | utenti demo POC (password BCrypt) | idempotente; **solo POC**, escluso in target via Flyway placeholder |

## 3. Configurazione Flyway

`application.yml` (POC):

```yaml
spring:
  datasource:
    url: jdbc:mariadb://mariadb:3306/anc?useUnicode=true&characterEncoding=utf8
    username: anc_app
    password: ${DB_PASSWORD}
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration
    placeholders:
      seed_demo_enabled: "true"     # false in target
```

Le repeatable per i seed POC includono guard:

```sql
-- R__seed_users_demo.sql
-- Eseguito solo se placeholder seed_demo_enabled = true (controllato applicativamente)
INSERT INTO app_user (username, password_hash, full_name, active)
VALUES (...)
ON DUPLICATE KEY UPDATE full_name = VALUES(full_name);
```

## 4. Convenzioni di scrittura migrazioni

- Una migrazione = una unità logica (un BC o una funzionalità).
- Mai modificare una migrazione già applicata in ambiente condiviso → creare una nuova versione `V<n+1>`.
- Vincoli `FK` aggiunti contestualmente alle tabelle (non in migrazione separata) per evitare stati intermedi inconsistenti.
- Indici secondari nella stessa migrazione che crea la tabella.
- Naming: tabelle/colonne `snake_case`, vincoli con prefisso (`pk_`, `fk_`, `uk_`, `idx_`, `chk_`).
- Charset/collation esplicitati a livello tabella.

## 5. Strategia per ambienti

| Ambiente | Comportamento Flyway |
|---|---|
| Sviluppo locale | `flyway:enabled=true`, `clean` consentito (drop+recreate ok) |
| POC condivisa | `flyway:enabled=true`, `clean-disabled=true`; seed demo abilitato |
| Target | `flyway:enabled=true`, `clean-disabled=true`, seed demo **disabilitato** |

## 6. Rollback strategy (POC)

- POC: rollback non automatizzato. In caso di necessità → ripristino DB da volume Docker o re-create con `clean`.
- Target: ogni migrazione "rischiosa" (es. drop colonna) viene preceduta da migrazione di compatibilità (deprecation window), seguita dalla rimozione effettiva. Non applicabile in POC.

## 7. Seed minimo iniziale (anteprima)

Dettagliato nel deliverable [06_Dati_POC](06_Dati_POC.md). Include:
- 3 ruoli: `OPERATORE_ANC`, `SUPERVISORE_ANC`, `ADMIN`;
- 1 gruppo: `GRUPPO_OPERATORE_ANC`;
- 4 utenti demo (operatori + supervisore + admin) con password BCrypt;
- catalogo checklist Verbale (≈8 item) e Carta (≈6 item) con causali KO baseline;
- nessuna pratica seed in DB (le pratiche demo si creano via stub BPM, esercitando il workflow reale).

## 8. Workflow CI/CD migrazioni (POC)

1. Sviluppatore crea `V<n>__nome.sql` in branch.
2. Pipeline esegue `mvn flyway:validate` su DB ephemeral.
3. Su merge → deploy POC esegue `flyway:migrate` automaticamente (idempotente).
4. Tag versione applicativa coincide con max migration version.
