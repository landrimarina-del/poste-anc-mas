-- =====================================================================
-- V108 — Rinomina flowable_task_id → kogito_task_id nella tabella task
-- Allinea la nomenclatura al motore BPM effettivamente in uso (Kogito).
-- =====================================================================

ALTER TABLE task
    DROP KEY idx_task_flowable,
    CHANGE COLUMN flowable_task_id kogito_task_id VARCHAR(200) NULL,
    ADD KEY idx_task_kogito (kogito_task_id);
