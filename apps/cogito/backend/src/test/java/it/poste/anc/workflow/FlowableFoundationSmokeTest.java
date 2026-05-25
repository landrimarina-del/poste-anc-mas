package it.poste.anc.workflow;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ClassUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Smoke test Sprint 0: verifica avvio engine Flowable embedded e deploy placeholder tecnico.
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "spring.datasource.url=jdbc:h2:mem:anc;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.flyway.enabled=false",
                "flowable.database-schema-update=true",
                "flowable.async-executor-activate=false"
        }
)
class FlowableFoundationSmokeTest {

    @Autowired
        private ApplicationContext applicationContext;

    @Test
        void engineIsUpAndPlaceholderProcessIsDeployedWhenFlowableIsAvailable() throws Exception {
                boolean flowableOnClasspath = ClassUtils.isPresent(
                                "org.flowable.engine.ProcessEngine",
                                getClass().getClassLoader()
                );

                if (!flowableOnClasspath) {
                        assertThat(applicationContext.containsBean("processEngine")).isFalse();
                        return;
                }

                assertThat(applicationContext.containsBean("processEngine")).isTrue();
                assertThat(applicationContext.containsBean("repositoryService")).isTrue();

                Object repositoryService = applicationContext.getBean("repositoryService");
                Object query = invoke(repositoryService, "createProcessDefinitionQuery");
                Object filteredQuery = query.getClass()
                                .getMethod("processDefinitionKey", String.class)
                                .invoke(query, "tech.foundation.placeholder");
                long placeholderCount = ((Number) invoke(filteredQuery, "count")).longValue();

                assertThat(placeholderCount).isGreaterThan(0L);
        }

        private Object invoke(Object target, String methodName) throws Exception {
                Method method = target.getClass().getMethod(methodName);
                return method.invoke(target);
    }
}
