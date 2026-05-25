package it.poste.anc.workflow.engine;

import com.zaxxer.hikari.HikariDataSource;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.EngineConfigurationConfigurer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * Defines two separate datasources:
 *
 * - dataSource (PRIMARY) -> schema "anc": app tables, used by Flyway and JPA.
 *   Required because defining any DataSource bean disables
 *   DataSourceAutoConfiguration (@ConditionalOnMissingBean).
 *
 * - flowableEngineDataSource -> schema "flowable": ACT_* / FLW_* engine tables.
 *   Wired to the Flowable engine via EngineConfigurationConfigurer
 *   (official Flowable 7.x Spring Boot API).
 */
@Configuration
public class FlowableEngineConfig {

    // ------------------------------------------------------------------
    // PRIMARY -- schema "anc" (Flyway + JPA + everything else)
    // ------------------------------------------------------------------

    @Bean("primaryDataSourceProperties")
    @Primary
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSourceProperties primaryDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean("dataSource")
    @Primary
    public DataSource primaryDataSource(
            @Qualifier("primaryDataSourceProperties") DataSourceProperties props) {
        return props.initializeDataSourceBuilder().build();
    }

    // ------------------------------------------------------------------
    // FLOWABLE -- schema "flowable" (ACT_*/FLW_*)
    // ------------------------------------------------------------------

    @Bean("flowableEngineDataSource")
    @ConfigurationProperties(prefix = "anc.flowable.datasource")
    public DataSource flowableEngineDataSource() {
        return new HikariDataSource();
    }

    /**
     * EngineConfigurationConfigurer is the official Flowable 7.x way to
     * override the engine datasource. The configurer is called before the
     * ProcessEngine is built, setting the datasource on
     * SpringProcessEngineConfiguration (which also covers the CommonEngine
     * responsible for ACT_GE_PROPERTY).
     */
    @Bean
    public EngineConfigurationConfigurer<SpringProcessEngineConfiguration> flowableDataSourceConfigurer(
            @Qualifier("flowableEngineDataSource") DataSource ds) {
        return config -> config.setDataSource(ds);
    }
}
