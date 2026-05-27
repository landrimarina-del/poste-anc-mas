# 11 — Migrazioni Tecniche: Kogito su MariaDB + Regole DMN

**Documento per**: Development MAS (Backend Agent)  
**Versione**: 1.0  
**Data**: 2026-05-26  
**Scope**: `apps/kogito/backend`

---

## Indice

1. [Contesto e motivazione](#1-contesto-e-motivazione)
2. [Migrazione A — Kogito Persistence da PostgreSQL a MariaDB](#2-migrazione-a--kogito-persistence-da-postgresql-a-mariadb)
3. [Migrazione B — Regole di esito checklist da Java a DMN](#3-migrazione-b--regole-di-esito-checklist-da-java-a-dmn)
4. [Ordine di esecuzione](#4-ordine-di-esecuzione)
5. [Verifica post-migrazione](#5-verifica-post-migrazione)

---

## 1. Contesto e motivazione

### 1.1 Stato attuale

Il backend `apps/kogito/backend` usa **due datasource separati**:

| Datasource | DB | Schema | Uso |
|------------|----|----|-----|
| Primario (`spring.datasource`) | MariaDB:3307 | `anc` | Dati business ANC |
| Kogito (`kogito.datasource`) | PostgreSQL:5433 | `kogito` | Process instance state |

Il doppio DB aumenta la complessità infrastrutturale della POC senza vantaggio funzionale.

### 1.2 Evidenza tecnica che abilita la migrazione

Il JAR `kogito-addons-persistence-jdbc:2.44.0.Alpha` contiene **tre script SQL distinti** rilevati all'interno del file `kogito-addons-persistence-jdbc-2.44.0.Alpha.jar`:

```
db/ansi/V1.35.0__create_runtime_ansi.sql       ← BLOB + ANSI SQL
db/postgresql/V1.35.0__create_runtime_PostgreSQL.sql
db/oracle/V1.35.0__create_runtimes_Oracle.sql
```

Il `DatabaseType` interno (`POSTGRES` / `ORACLE` / `ANSI`) viene **rilevato automaticamente** da `DatabaseMetaData.getDatabaseProductName()` via JDBC. MariaDB restituisce `"MariaDB"` → non è né PostgreSQL né Oracle → **cade nel ramo ANSI**.

Schema ANSI effettivo creato da Kogito:
```sql
CREATE TABLE process_instances (
    id              CHAR(36)      NOT NULL,
    payload         BLOB          NOT NULL,   -- protobuf binario, non JSON
    process_id      VARCHAR(4000) NOT NULL,
    version         BIGINT(19),
    process_version VARCHAR(4000),
    CONSTRAINT process_instances_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_process_instances_process_id ON process_instances (process_id, id, process_version);
```

**Compatibile al 100% con MariaDB.**

> **Nota**: La funzionalità `correlation_instances` (`PostgreSQLCorrelationRepository`) rimane PostgreSQL-only ma **non è necessaria** al workflow ANC (`anc_pratica.bpmn2`).

### 1.3 Stato attuale della logica DMN

I metodi `computeVerbaleOutcome()` (riga 454) e `computeCartaOutcome()` (riga 478) in `IntakeChecklistService.java` contengono logica decisionale hardcoded che corrisponde direttamente alle matrici di controllo documentate in `MatriciControlli.xlsx`. Sono candidati naturali per DMN.

---

## 2. Migrazione A — Kogito Persistence da PostgreSQL a MariaDB

### 2.1 Obiettivo

Eliminare il secondo datasource PostgreSQL. Kogito persiste lo stato processi nello schema `kogito` su MariaDB (stessa istanza del datasource `anc`).

**Architettura risultante:**

```
MariaDB (localhost:3307 / container db:3306)
├── schema: anc     ← dati business (invariato)
└── schema: kogito  ← process state Kogito (NUOVO, stesso server)
```

### 2.2 Modifiche richieste

#### 2.2.1 `infra/db/init/01_create_schemas.sql` — Aggiungere schema `kogito`

**File**: `infra/db/init/01_create_schemas.sql`

Aggiungere in coda alle righe esistenti:

```sql
-- Schema per Kogito process persistence (ANSI JDBC adapter)
CREATE DATABASE IF NOT EXISTS kogito
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

-- Grant all'utente applicativo
GRANT ALL PRIVILEGES ON kogito.* TO 'anc'@'%';

FLUSH PRIVILEGES;
```

> **Attenzione**: Questo script viene eseguito **una sola volta** al primo avvio del container MariaDB (`docker-entrypoint-initdb.d`). Se il volume `anc-db-data` esiste già, **distruggere il volume** prima di applicare la modifica oppure creare lo schema manualmente:
> ```sql
> CREATE DATABASE IF NOT EXISTS kogito CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
> GRANT ALL PRIVILEGES ON kogito.* TO 'anc'@'%';
> FLUSH PRIVILEGES;
> ```

#### 2.2.2 `pom.xml` — Rimuovere dipendenza PostgreSQL

**File**: `apps/kogito/backend/pom.xml`

Rimuovere la dipendenza:

```xml
<!-- RIMUOVERE -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

Il driver MariaDB (`org.mariadb.jdbc:mariadb-java-client`) è già presente nel pom.xml.

#### 2.2.3 `KogitoDataSourceConfig.java` — Sostituire driver e URL default

**File**: `apps/kogito/backend/src/main/java/it/poste/anc/workflow/engine/KogitoDataSourceConfig.java`

Sostituire l'intero contenuto del file:

```java
package it.poste.anc.workflow.engine;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.kie.kogito.persistence.springboot.JDBCProcessInstancesFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * Configura il DataSource dedicato a Kogito process persistence (MariaDB, schema "kogito").
 *
 * <p>Il datasource primario (spring.datasource.*) punta allo schema "anc" (dati business ANC).
 * Kogito JDBC persistence usa un secondo DataSource bean denominato {@code kogitoDataSource}
 * che punta allo schema "kogito" sulla stessa istanza MariaDB.
 *
 * <p>Kogito rileva automaticamente il tipo DB da DatabaseMetaData e usa il dialetto ANSI
 * (BLOB per payload protobuf, SQL standard). Non è richiesto PostgreSQL.
 *
 * <p>Le property vengono passate tramite variabili d'ambiente:
 * <ul>
 *   <li>{@code KOGITO_DATASOURCE_URL} — default: jdbc:mariadb://localhost:3307/kogito</li>
 *   <li>{@code KOGITO_DATASOURCE_USERNAME} — default: anc</li>
 *   <li>{@code KOGITO_DATASOURCE_PASSWORD} — default: anc</li>
 * </ul>
 */
@Configuration
public class KogitoDataSourceConfig {

    @Bean(name = "kogitoDataSource")
    public DataSource kogitoDataSource(
            @Value("${kogito.datasource.jdbc-url:jdbc:mariadb://localhost:3307/kogito}") String url,
            @Value("${kogito.datasource.username:anc}") String username,
            @Value("${kogito.datasource.password:anc}") String password
    ) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("org.mariadb.jdbc.Driver");
        config.setPoolName("kogito-pool");
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(1);
        config.setConnectionTestQuery("SELECT 1");
        return new HikariDataSource(config);
    }

    /**
     * Override di JDBCProcessInstancesFactory per usare il datasource Kogito (schema kogito su MariaDB)
     * invece del datasource primario (schema anc).
     */
    @Bean
    @Primary
    public JDBCProcessInstancesFactory processInstancesFactory(
            @Qualifier("kogitoDataSource") DataSource kogitoDataSource,
            @Value("${kogito.persistence.optimistic.lock:false}") Boolean lock
    ) {
        return new JDBCProcessInstancesFactory(kogitoDataSource, lock);
    }
}
```

#### 2.2.4 `application.yml` — Aggiornare il blocco `kogito.datasource`

**File**: `apps/kogito/backend/src/main/resources/application.yml`

Nel blocco esistente `kogito:` sostituire:

```yaml
# PRIMA:
kogito:
  datasource:
    jdbc-url: ${KOGITO_DATASOURCE_URL:jdbc:postgresql://localhost:5433/kogito}
    username: ${KOGITO_DATASOURCE_USERNAME:kogito}
    password: ${KOGITO_DATASOURCE_PASSWORD:kogito}
    driver-class-name: org.postgresql.Driver
```

Con:

```yaml
# DOPO:
kogito:
  datasource:
    jdbc-url: ${KOGITO_DATASOURCE_URL:jdbc:mariadb://localhost:3307/kogito}
    username: ${KOGITO_DATASOURCE_USERNAME:anc}
    password: ${KOGITO_DATASOURCE_PASSWORD:anc}
    driver-class-name: org.mariadb.jdbc.Driver
```

#### 2.2.5 `application-local.yml` — Aggiornare il blocco `anc.kogito.datasource`

**File**: `apps/kogito/backend/src/main/resources/application-local.yml`

Il file già contiene una configurazione locale per MariaDB ma con lo schema `anc` (errato). Correggere:

```yaml
# PRIMA:
anc:
  kogito:
    datasource:
      jdbc-url: jdbc:mariadb://localhost:3307/anc?useSSL=false&allowPublicKeyRetrieval=true
      username: anc
      password: anc
      driver-class-name: org.mariadb.jdbc.Driver
```

```yaml
# DOPO:
kogito:
  datasource:
    jdbc-url: jdbc:mariadb://localhost:3307/kogito?useSSL=false&allowPublicKeyRetrieval=true
    username: anc
    password: anc
    driver-class-name: org.mariadb.jdbc.Driver
```

> **Nota**: Il prefisso era `anc.kogito.datasource` (errato, non corrisponde al `@Value` nella config class). Il prefisso corretto è `kogito.datasource`.

#### 2.2.6 `docker-compose.yml` — Rimuovere il servizio `postgres` / `kogito-db`

Se nel `docker-compose.yml` è presente un servizio PostgreSQL dedicato a Kogito (es. `kogito-db`, porta 5433), **rimuoverlo**. Aggiornare le variabili d'ambiente del servizio `backend` nel docker-compose:

```yaml
# RIMUOVERE (se presenti):
KOGITO_DATASOURCE_URL: jdbc:postgresql://kogito-db:5432/kogito
KOGITO_DATASOURCE_USERNAME: kogito
KOGITO_DATASOURCE_PASSWORD: kogito

# AGGIUNGERE:
KOGITO_DATASOURCE_URL: jdbc:mariadb://db:3306/kogito?useSSL=false&allowPublicKeyRetrieval=true
KOGITO_DATASOURCE_USERNAME: anc
KOGITO_DATASOURCE_PASSWORD: anc
```

#### 2.2.7 Flyway — Nessuna modifica richiesta

Kogito gestisce autonomamente la creazione dello schema `process_instances` tramite il proprio meccanismo Flyway interno (lo script `db/ansi/V1.35.0__create_runtime_ansi.sql` viene eseguito sul `kogitoDataSource`). **Non aggiungere** migration Flyway manuali per le tabelle Kogito.

Il Flyway ANC (`spring.flyway`) continua a girare sul datasource primario (schema `anc`) senza interferenze.

---

## 3. Migrazione B — Regole di esito checklist da Java a DMN

### 3.1 Obiettivo

Estrarre la logica decisionale hardcoded in `IntakeChecklistService.java` in due file DMN gestiti da Kogito:

| Metodo Java da sostituire | File DMN da creare |
|---------------------------|-------------------|
| `computeVerbaleOutcome()` | `anc_outcome_verbale.dmn` |
| `computeCartaOutcome()` | `anc_outcome_carta.dmn` |

### 3.2 Dipendenza da aggiungere al pom.xml

**File**: `apps/kogito/backend/pom.xml`

```xml
<!-- Kogito DMN/Decisions runtime (Spring Boot) — versione gestita dal BOM kogito-bom:2.44.0.Alpha -->
<dependency>
    <groupId>org.kie.kogito</groupId>
    <artifactId>kogito-decisions-spring-boot-starter</artifactId>
</dependency>
```

### 3.3 File DMN da creare

#### 3.3.1 `anc_outcome_verbale.dmn`

**Percorso**: `apps/kogito/backend/src/main/resources/processes/anc_outcome_verbale.dmn`

**Logica replicata** (da `computeVerbaleOutcome()` righe 454–475):

| Condizione | KO code aggiunto |
|------------|-----------------|
| `documentPresent = false` | `DOCUMENTO_ASSENTE` |
| `documentPresent = true AND readabilityOk = false` | `LEGGIBILITA_KO` |
| `documentPresent = true AND formalOk = false` | tutti i valori di `koReasons` |
| `documentPresent = true AND customerDataOk = false` | `DATI_CLIENTE_KO` |
| `documentPresent = true AND cardNumberMatchRequired = true AND cardNumberMatchOk = false` | `NUMERO_CARTA_KO` |
| `koCodes vuoto` | outcome = `APPROVATA` |
| `koCodes non vuoto` | outcome = `RESPINTA` |

**Contenuto XML DMN:**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="https://www.omg.org/spec/DMN/20191111/MODEL/"
             xmlns:dmndi="https://www.omg.org/spec/DMN/20191111/DMNDI/"
             xmlns:dc="http://www.omg.org/spec/DMN/20180521/DC/"
             id="anc-outcome-verbale"
             name="anc_outcome_verbale"
             namespace="https://poste.it/anc/dmn/outcome-verbale">

  <decision id="decision_verbale_outcome" name="VerbaleOutcome">
    <variable id="var_outcome" name="VerbaleOutcome" typeRef="Any"/>
    <informationRequirement id="ir1">
      <requiredInput href="#input_documentPresent"/>
    </informationRequirement>
    <informationRequirement id="ir2">
      <requiredInput href="#input_readabilityOk"/>
    </informationRequirement>
    <informationRequirement id="ir3">
      <requiredInput href="#input_formalOk"/>
    </informationRequirement>
    <informationRequirement id="ir4">
      <requiredInput href="#input_customerDataOk"/>
    </informationRequirement>
    <informationRequirement id="ir5">
      <requiredInput href="#input_cardNumberMatchRequired"/>
    </informationRequirement>
    <informationRequirement id="ir6">
      <requiredInput href="#input_cardNumberMatchOk"/>
    </informationRequirement>
    <informationRequirement id="ir7">
      <requiredInput href="#input_koReasons"/>
    </informationRequirement>
    <literalExpression id="le_verbale">
      <text><![CDATA[
        (function() {
          var koCodes = [];
          if (documentPresent = false) {
            koCodes := ["DOCUMENTO_ASSENTE"];
          } else {
            if (readabilityOk = false) then koCodes := append(koCodes, "LEGGIBILITA_KO") else koCodes;
            if (formalOk = false) then koCodes := concatenate(koCodes, koReasons) else koCodes;
            if (customerDataOk = false) then koCodes := append(koCodes, "DATI_CLIENTE_KO") else koCodes;
            if (cardNumberMatchRequired = true and cardNumberMatchOk = false) then koCodes := append(koCodes, "NUMERO_CARTA_KO") else koCodes;
          }
          {outcome: if count(koCodes) = 0 then "APPROVATA" else "RESPINTA", koCodes: koCodes}
        })()
      ]]></text>
    </literalExpression>
  </decision>

  <inputData id="input_documentPresent" name="documentPresent">
    <variable name="documentPresent" typeRef="boolean"/>
  </inputData>
  <inputData id="input_readabilityOk" name="readabilityOk">
    <variable name="readabilityOk" typeRef="boolean"/>
  </inputData>
  <inputData id="input_formalOk" name="formalOk">
    <variable name="formalOk" typeRef="boolean"/>
  </inputData>
  <inputData id="input_customerDataOk" name="customerDataOk">
    <variable name="customerDataOk" typeRef="boolean"/>
  </inputData>
  <inputData id="input_cardNumberMatchRequired" name="cardNumberMatchRequired">
    <variable name="cardNumberMatchRequired" typeRef="boolean"/>
  </inputData>
  <inputData id="input_cardNumberMatchOk" name="cardNumberMatchOk">
    <variable name="cardNumberMatchOk" typeRef="boolean"/>
  </inputData>
  <inputData id="input_koReasons" name="koReasons">
    <variable name="koReasons" typeRef="Any"/>
  </inputData>

</definitions>
```

#### 3.3.2 `anc_outcome_carta.dmn`

**Percorso**: `apps/kogito/backend/src/main/resources/processes/anc_outcome_carta.dmn`

**Logica replicata** (da `computeCartaOutcome()` righe 478–487):

| Condizione | KO code |
|------------|---------|
| `cardPresent = false` | `CARTA_ASSENTE` |
| `cardPresent = true AND cardConformityOk = false` | `CARTA_NON_CONFORME` |
| `koCodes vuoto` | outcome = `APPROVATA` |
| `koCodes non vuoto` | outcome = `RESPINTA` |

```xml
<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="https://www.omg.org/spec/DMN/20191111/MODEL/"
             xmlns:dmndi="https://www.omg.org/spec/DMN/20191111/DMNDI/"
             id="anc-outcome-carta"
             name="anc_outcome_carta"
             namespace="https://poste.it/anc/dmn/outcome-carta">

  <decision id="decision_carta_outcome" name="CartaOutcome">
    <variable id="var_carta_outcome" name="CartaOutcome" typeRef="Any"/>
    <informationRequirement id="ir_cp">
      <requiredInput href="#input_cardPresent"/>
    </informationRequirement>
    <informationRequirement id="ir_cc">
      <requiredInput href="#input_cardConformityOk"/>
    </informationRequirement>
    <literalExpression id="le_carta">
      <text><![CDATA[
        (function() {
          var koCodes = [];
          if (cardPresent = false) then
            {outcome: "RESPINTA", koCodes: ["CARTA_ASSENTE"]}
          else if (cardConformityOk = false) then
            {outcome: "RESPINTA", koCodes: ["CARTA_NON_CONFORME"]}
          else
            {outcome: "APPROVATA", koCodes: []}
        })()
      ]]></text>
    </literalExpression>
  </decision>

  <inputData id="input_cardPresent" name="cardPresent">
    <variable name="cardPresent" typeRef="boolean"/>
  </inputData>
  <inputData id="input_cardConformityOk" name="cardConformityOk">
    <variable name="cardConformityOk" typeRef="boolean"/>
  </inputData>

</definitions>
```

### 3.4 Servizio DMN da creare

Creare un servizio wrapper che incapsula le chiamate DMN e restituisce gli stessi tipi `OutcomeComputed` già usati da `IntakeChecklistService`.

**File**: `apps/kogito/backend/src/main/java/it/poste/anc/document/application/OutcomeDmnService.java`

```java
package it.poste.anc.document.application;

import org.kie.kogito.decision.DecisionModel;
import org.kie.kogito.decision.DecisionModels;
import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Wrapper per le decisioni DMN di esito checklist.
 *
 * Sostituisce i metodi computeVerbaleOutcome() e computeCartaOutcome()
 * precedentemente hardcoded in IntakeChecklistService.
 *
 * I file DMN sono in src/main/resources/processes/:
 *   - anc_outcome_verbale.dmn (namespace: https://poste.it/anc/dmn/outcome-verbale)
 *   - anc_outcome_carta.dmn   (namespace: https://poste.it/anc/dmn/outcome-carta)
 */
@Service
public class OutcomeDmnService {

    private static final String NS_VERBALE = "https://poste.it/anc/dmn/outcome-verbale";
    private static final String NS_CARTA   = "https://poste.it/anc/dmn/outcome-carta";
    private static final String MODEL_VERBALE = "anc_outcome_verbale";
    private static final String MODEL_CARTA   = "anc_outcome_carta";
    private static final String DECISION_VERBALE = "VerbaleOutcome";
    private static final String DECISION_CARTA   = "CartaOutcome";

    private final DecisionModels decisionModels;

    public OutcomeDmnService(DecisionModels decisionModels) {
        this.decisionModels = decisionModels;
    }

    /**
     * Calcola esito checklist VERBALE tramite DMN.
     *
     * @param documentPresent      documento presente
     * @param readabilityOk        leggibilità OK (null se documentPresent=false)
     * @param formalOk             conformità formale OK (null se documentPresent=false)
     * @param customerDataOk       dati cliente OK (null se documentPresent=false)
     * @param cardNumberMatchRequired controllo numero carta richiesto
     * @param cardNumberMatchOk    numero carta corrispondente (null se non richiesto)
     * @param koReasons            causali KO formali (INTESTAZIONE, FIRME, TIMBRO, DICHIARAZIONE, CARTA_PI)
     * @return OutcomeComputed con outcome (APPROVATA/RESPINTA) e lista koCodes
     */
    @SuppressWarnings("unchecked")
    public OutcomeComputed computeVerbaleOutcome(
            boolean documentPresent,
            Boolean readabilityOk,
            Boolean formalOk,
            Boolean customerDataOk,
            boolean cardNumberMatchRequired,
            Boolean cardNumberMatchOk,
            List<String> koReasons) {

        DecisionModel model = decisionModels.getDecisionModel(NS_VERBALE, MODEL_VERBALE);
        DMNContext ctx = model.newContext(Map.of(
                "documentPresent", documentPresent,
                "readabilityOk", readabilityOk != null ? readabilityOk : false,
                "formalOk", formalOk != null ? formalOk : false,
                "customerDataOk", customerDataOk != null ? customerDataOk : false,
                "cardNumberMatchRequired", cardNumberMatchRequired,
                "cardNumberMatchOk", cardNumberMatchOk != null ? cardNumberMatchOk : false,
                "koReasons", koReasons != null ? koReasons : List.of()
        ));

        DMNResult result = model.evaluateDecisionByName(ctx, DECISION_VERBALE);
        Map<String, Object> output = (Map<String, Object>) result.getDecisionResultByName(DECISION_VERBALE).getResult();

        String outcome = (String) output.get("outcome");
        List<String> koCodes = (List<String>) output.get("koCodes");
        return new OutcomeComputed(outcome, koCodes != null ? koCodes : List.of());
    }

    /**
     * Calcola esito checklist CARTA tramite DMN.
     *
     * @param cardPresent      carta presente
     * @param cardConformityOk carta conforme (null se cardPresent=false)
     * @return OutcomeComputed con outcome (APPROVATA/RESPINTA) e lista koCodes
     */
    @SuppressWarnings("unchecked")
    public OutcomeComputed computeCartaOutcome(boolean cardPresent, Boolean cardConformityOk) {

        DecisionModel model = decisionModels.getDecisionModel(NS_CARTA, MODEL_CARTA);
        DMNContext ctx = model.newContext(Map.of(
                "cardPresent", cardPresent,
                "cardConformityOk", cardConformityOk != null ? cardConformityOk : false
        ));

        DMNResult result = model.evaluateDecisionByName(ctx, DECISION_CARTA);
        Map<String, Object> output = (Map<String, Object>) result.getDecisionResultByName(DECISION_CARTA).getResult();

        String outcome = (String) output.get("outcome");
        List<String> koCodes = (List<String>) output.get("koCodes");
        return new OutcomeComputed(outcome, koCodes != null ? koCodes : List.of());
    }

    // Record condiviso — spostare in file separato se usato da più classi
    record OutcomeComputed(String outcome, List<String> koCodes) {}
}
```

### 3.5 Modifica `IntakeChecklistService.java`

**File**: `apps/kogito/backend/src/main/java/it/poste/anc/document/application/IntakeChecklistService.java`

#### 3.5.1 Aggiungere dipendenza nel costruttore

```java
// PRIMA:
public IntakeChecklistService(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
    this.jdbcTemplate = jdbcTemplate;
    this.objectMapper = objectMapper;
}

// DOPO:
private final OutcomeDmnService outcomeDmnService;

public IntakeChecklistService(JdbcTemplate jdbcTemplate,
                               ObjectMapper objectMapper,
                               OutcomeDmnService outcomeDmnService) {
    this.jdbcTemplate = jdbcTemplate;
    this.objectMapper = objectMapper;
    this.outcomeDmnService = outcomeDmnService;
}
```

#### 3.5.2 Sostituire `computeVerbaleOutcome()` con chiamata DMN

**Riga 454 — rimuovere il metodo Java e delegare a `OutcomeDmnService`:**

```java
// PRIMA (righe 454–475):
private OutcomeComputed computeVerbaleOutcome(ChecklistVerbaleRow row) {
    List<String> koCodes = new ArrayList<>();
    if (!row.documentPresent()) {
        koCodes.add("DOCUMENTO_ASSENTE");
    } else {
        if (Boolean.FALSE.equals(row.readabilityOk())) {
            koCodes.add("LEGGIBILITA_KO");
        }
        if (Boolean.FALSE.equals(row.formalOk())) {
            koCodes.addAll(row.koReasons());
        }
        if (Boolean.FALSE.equals(row.customerDataOk())) {
            koCodes.add("DATI_CLIENTE_KO");
        }
        if (row.cardNumberMatchRequired() && Boolean.FALSE.equals(row.cardNumberMatchOk())) {
            koCodes.add("NUMERO_CARTA_KO");
        }
    }
    String outcome = koCodes.isEmpty() ? OUTCOME_APPROVATA : OUTCOME_RESPINTA;
    return new OutcomeComputed(outcome, koCodes);
}

// DOPO:
private OutcomeComputed computeVerbaleOutcome(ChecklistVerbaleRow row) {
    return outcomeDmnService.computeVerbaleOutcome(
            row.documentPresent(),
            row.readabilityOk(),
            row.formalOk(),
            row.customerDataOk(),
            row.cardNumberMatchRequired(),
            row.cardNumberMatchOk(),
            row.koReasons()
    );
}
```

#### 3.5.3 Sostituire `computeCartaOutcome()` con chiamata DMN

**Riga 478 — rimuovere il metodo Java e delegare a `OutcomeDmnService`:**

```java
// PRIMA (righe 478–487):
private OutcomeComputed computeCartaOutcome(ChecklistCartaRow row) {
    List<String> koCodes = new ArrayList<>();
    if (!row.cardPresent()) {
        koCodes.add("CARTA_ASSENTE");
    } else if (Boolean.FALSE.equals(row.cardConformityOk())) {
        koCodes.add("CARTA_NON_CONFORME");
    }
    String outcome = koCodes.isEmpty() ? OUTCOME_APPROVATA : OUTCOME_RESPINTA;
    return new OutcomeComputed(outcome, koCodes);
}

// DOPO:
private OutcomeComputed computeCartaOutcome(ChecklistCartaRow row) {
    return outcomeDmnService.computeCartaOutcome(
            row.cardPresent(),
            row.cardConformityOk()
    );
}
```

#### 3.5.4 Allineamento tipo `OutcomeComputed`

`OutcomeComputed` è attualmente un `private record` dentro `IntakeChecklistService` (riga 704). Con la delega a `OutcomeDmnService`, **due opzioni**:

**Opzione A (consigliata)**: Spostare il record in `OutcomeDmnService` e importarlo in `IntakeChecklistService`:
```java
// In IntakeChecklistService sostituire:
private record OutcomeComputed(String outcome, List<String> koCodes) {}
// Con import del tipo da OutcomeDmnService:
// import it.poste.anc.document.application.OutcomeDmnService.OutcomeComputed;
```

**Opzione B**: Mantenere il record privato in entrambe le classi (duplicazione tollerabile per POC).

### 3.6 Import da aggiungere in `OutcomeDmnService.java`

```java
import org.kie.kogito.decision.DecisionModel;
import org.kie.kogito.decision.DecisionModels;
import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNResult;
```

> Questi package sono inclusi in `kogito-decisions-spring-boot-starter:2.44.0.Alpha`.

---

## 4. Ordine di esecuzione

```
[1] infra/db/init/01_create_schemas.sql     — aggiungere schema kogito
[2] pom.xml                                  — rimuovere postgresql, aggiungere kogito-decisions-starter
[3] KogitoDataSourceConfig.java             — sostituire driver PostgreSQL con MariaDB
[4] application.yml                          — aggiornare kogito.datasource defaults
[5] application-local.yml                   — correggere prefisso e schema
[6] docker-compose.yml                      — rimuovere servizio PostgreSQL, aggiornare env vars
[7] anc_outcome_verbale.dmn                 — creare file DMN verbale
[8] anc_outcome_carta.dmn                   — creare file DMN carta
[9] OutcomeDmnService.java                  — creare servizio DMN wrapper
[10] IntakeChecklistService.java            — iniettare OutcomeDmnService, delegare compute*Outcome
```

---

## 5. Verifica post-migrazione

### 5.1 Verifica schema Kogito su MariaDB

Dopo il primo avvio con il nuovo datasource:

```sql
-- Connettersi allo schema kogito su MariaDB:
USE kogito;
SHOW TABLES;
-- Atteso: process_instances
DESCRIBE process_instances;
-- Atteso: id CHAR(36), payload BLOB, process_id VARCHAR(4000), version BIGINT, process_version VARCHAR(4000)
```

### 5.2 Verifica DMN endpoints esposti da Kogito

Con `kogito-decisions-spring-boot-starter`, Kogito espone automaticamente:

```
POST /anc_outcome_verbale
Content-Type: application/json

{
  "documentPresent": true,
  "readabilityOk": true,
  "formalOk": false,
  "customerDataOk": true,
  "cardNumberMatchRequired": false,
  "cardNumberMatchOk": null,
  "koReasons": ["FIRME", "TIMBRO"]
}

-- Risposta attesa:
{
  "VerbaleOutcome": {
    "outcome": "RESPINTA",
    "koCodes": ["FIRME", "TIMBRO"]
  }
}
```

```
POST /anc_outcome_carta
Content-Type: application/json

{"cardPresent": true, "cardConformityOk": true}

-- Risposta attesa:
{"CartaOutcome": {"outcome": "APPROVATA", "koCodes": []}}
```

### 5.3 Regression test su `IntakeChecklistService`

I test esistenti per `consolidate()` e `consolidateAsBozza()` devono continuare a passare. Il comportamento esterno del servizio è **invariato**: gli stessi input producono gli stessi output (`APPROVATA`/`RESPINTA` + lista koCodes). Solo l'implementazione interna è delegata al DMN.

### 5.4 Dipendenze da rimuovere da docker-compose.yml

| Servizio da rimuovere | Motivazione |
|-----------------------|-------------|
| `kogito-db` (PostgreSQL:5433) | Kogito ora usa MariaDB schema `kogito` |
| Eventuale `depends_on: kogito-db` nel servizio `backend` | Non più necessario |

---

## Note architetturali per il Development MAS

1. **Invarianza comportamentale**: Le migrazioni A e B **non modificano** nessun contratto API, nessun workflow BPMN, nessuna struttura dati di dominio. Sono refactoring interni.

2. **Fallback**: Se il DMN non compila correttamente al primo build, è possibile mantenere temporaneamente i metodi Java originali (`computeVerbaleOutcome`, `computeCartaOutcome`) senza impatto funzionale, e delegare al DMN in un secondo momento.

3. **Evoluzione target**: In architettura target (non POC), le regole DMN possono essere gestite da un Decision Manager (es. Red Hat Decision Manager, o Kogito Management Console) separato dal runtime, con hot-reload senza rebuild. Questo non richiede modifiche applicative — solo infrastrutturali.

4. **Correlazione**: La feature `PostgreSQLCorrelationRepository` (correlation tra processi) rimane non disponibile su MariaDB. Non è utilizzata nel workflow `anc_pratica.bpmn2`. Se in futuro necessaria, richiede o il ritorno a PostgreSQL per lo schema `kogito` o una implementazione custom ANSI.
