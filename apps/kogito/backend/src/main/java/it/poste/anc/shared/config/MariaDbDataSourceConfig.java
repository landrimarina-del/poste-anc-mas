package it.poste.anc.shared.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * Configura il DataSource primario (MariaDB) per le entity ANC.
 *
 * <p>Necessario perché {@code kogito-addons-springboot-persistence-jdbc} registra il
 * proprio DataSource PostgreSQL come primario, scavalcando l'auto-configurazione
 * Spring Boot. Questa classe ripristina MariaDB come {@code @Primary} datasource
 * usato da Hibernate JPA e Flyway per le tabelle di dominio ANC.
 */
@Configuration
public class MariaDbDataSourceConfig {

    @Bean(name = "dataSource")
    @Primary
    public DataSource dataSource(
            @Value("${spring.datasource.url:jdbc:mariadb://localhost:3307/anc?useSSL=false&allowPublicKeyRetrieval=true}") String url,
            @Value("${spring.datasource.username:anc}") String username,
            @Value("${spring.datasource.password:anc}") String password
    ) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("org.mariadb.jdbc.Driver");
        config.setPoolName("anc-mariadb-pool");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTestQuery("SELECT 1");
        return new HikariDataSource(config);
    }
}
