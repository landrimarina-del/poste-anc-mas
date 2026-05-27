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
 * Kogito JDBC persistence richiede un secondo DataSource bean denominato {@code kogitoDataSource}
 * che punta allo schema "kogito" sulla stessa istanza MariaDB.
 *
 * <p>Kogito rileva automaticamente il tipo DB da DatabaseMetaData e usa il dialetto ANSI
 * (BLOB per payload protobuf, SQL standard).
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
