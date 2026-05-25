package it.poste.anc.shared.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.core.annotation.Order;

/**
 * Configurazione di sicurezza Sprint 0.
 *
 * Scelte:
 *  - HTTP Basic abilitato su tutte le API protette (consente smoke test con `curl -u user:pwd ...`).
 *  - Sessione HTTP IF_REQUIRED: l'endpoint POST /auth/login crea una sessione e il client puo'
 *    usare il cookie JSESSIONID per le chiamate successive ("token semplice" di sessione).
 *  - CSRF disabilitato: API JSON, no form login, no cookie cross-site nel POC.
 *  - Niente JWT/OIDC/Keycloak (vincolo Sprint 0).
 *  - Tutti gli endpoint sotto /api/v1/** richiedono autenticazione tranne /auth/login.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(UserDetailsService uds, PasswordEncoder encoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(uds);
        provider.setPasswordEncoder(encoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(DaoAuthenticationProvider provider) {
        return new org.springframework.security.authentication.ProviderManager(provider);
    }

    @Bean
    @Order(-1) // Priorità alta: la nostra chain batte quella del Flowable REST API (che usa Flowable IDM)
    public SecurityFilterChain filterChain(HttpSecurity http, BpmInboundApiKeyFilter bpmInboundApiKeyFilter) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            // Con flowable-rest nel classpath ci sono due DispatcherServlet:
            // il main a "/" e quello Flowable a "/process-api". Spring Security 6.x richiede
            // AntPathRequestMatcher esplicito per evitare l'ambiguità MVC vs non-MVC.
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(new AntPathRequestMatcher("/actuator/health/**"),
                                 new AntPathRequestMatcher("/actuator/info")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/v1/auth/login")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/v1/technical/workflow/readiness")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/v1/bpm/practices")).permitAll()
                // Flowable REST API — usata da Flowable UI Admin e future dashboard Cogito
                // /flowable-ui/**   è l'alias per Flowable Admin 6.8.0 (context-root default /flowable-ui)
                // /flowable-rest/** è l'alias alternativo (context-root=flowable-rest)
                // /process-api/**   è il path nativo Flowable 7.0.1
                .requestMatchers(new AntPathRequestMatcher("/flowable-ui/**"),
                                 new AntPathRequestMatcher("/flowable-rest/**"),
                                 new AntPathRequestMatcher("/process-api/**"),
                                 new AntPathRequestMatcher("/cmmn-api/**"),
                                 new AntPathRequestMatcher("/dmn-api/**"),
                                 new AntPathRequestMatcher("/form-api/**"),
                                 new AntPathRequestMatcher("/content-api/**"),
                                 new AntPathRequestMatcher("/event-registry-api/**"),
                                 new AntPathRequestMatcher("/idm-api/**")).authenticated()
                .requestMatchers(new AntPathRequestMatcher("/api/v1/auth/**")).authenticated()
                .requestMatchers(new AntPathRequestMatcher("/api/v1/**")).authenticated()
                .anyRequest().permitAll())
            .addFilterBefore(bpmInboundApiKeyFilter, BasicAuthenticationFilter.class)
            .httpBasic(basic -> {})
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((req, res, e) -> {
                    // WWW-Authenticate: Basic è necessario perché i client HTTP (es. Flowable Admin,
                    // Cogito dashboard) che usano Apache HttpClient in modalità non-preemptiva
                    // si aspettano questo header per fare il retry con le credenziali.
                    res.setHeader("WWW-Authenticate", "Basic realm=\"ANC\"");
                    res.setStatus(HttpStatus.UNAUTHORIZED.value());
                    res.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    res.getWriter().write("{\"resultCode\":1002,\"resultMessage\":\"Autenticazione richiesta\"}");
                }));
        return http.build();
    }
}
