package it.poste.anc.workflow.engine;

import org.flowable.engine.IdentityService;
import org.flowable.idm.api.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Inizializza l'utente admin nel registro IDM di Flowable (ACT_ID_USER).
 *
 * Flowable REST API (child WebApplicationContext) autentica le chiamate HTTP Basic
 * contro il proprio IdmIdentityService, indipendentemente da Spring Security del contesto padre.
 * Serve quindi un utente valido nella tabella flowable.ACT_ID_USER affinché
 * Flowable Admin (e future dashboard esterne come Cogito) possano connettersi.
 *
 * La password viene salvata in chiaro nel campo PWD_ perché Flowable REST
 * usa un proprio meccanismo di confronto (non BCrypt).
 */
@Component
public class FlowableIdmInitializer {

    private static final Logger log = LoggerFactory.getLogger(FlowableIdmInitializer.class);

    @Autowired
    private IdentityService identityService;

    @EventListener(ApplicationReadyEvent.class)
    public void initAdminUser() {
        long count = identityService.createUserQuery().userId("admin").count();
        if (count == 0) {
            User admin = identityService.newUser("admin");
            admin.setFirstName("Admin");
            admin.setLastName("ANC");
            admin.setEmail("admin@anc.local");
            admin.setPassword("Demo1234!");
            identityService.saveUser(admin);
            log.info("Flowable IDM: utente 'admin' creato in ACT_ID_USER");
        } else {
            log.debug("Flowable IDM: utente 'admin' già presente in ACT_ID_USER");
        }
    }
}
