package it.poste.anc.workflow.engine;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Configura il DataSource dedicato a Kogito process persistence (PostgreSQL).
 *
 * <p>Il datasource primario (spring.datasource.*) è MariaDB per le tabelle di dominio ANC.
 * Kogito JDBC persistence richiede un secondo DataSource bean denominato {@code kogitoDataSource}
 * che punta al PostgreSQL dedicato.
 *
 * <p>Le property vengono passate tramite variabili d'ambiente:
 * <ul>
 *   <li>{@code KOGITO_DATASOURCE_URL} — default: jdbc:postgresql://localhost:5433/kogito</li>
 *   <li>{@code KOGITO_DATASOURCE_USERNAME} — default: kogito</li>
 *   <li>{@code KOGITO_DATASOURCE_PASSWORD} — default: kogito</li>
 * </ul>
 */
@Configuration
public class KogitoDataSourceConfig {

    @Bean(name = "kogitoDataSource")
    public DataSource kogitoDataSource(
            @Value("${kogito.datasource.jdbc-url:jdbc:postgresql://localhost:5433/kogito}") String url,
            @Value("${kogito.datasource.username:kogito}") String username,
            @Value("${kogito.datasource.password:kogito}") String password
    ) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("org.postgresql.Driver");
        config.setPoolName("kogito-pool");
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(1);
        config.setConnectionTestQuery("SELECT 1");
        return new HikariDataSource(config);
    }
}
