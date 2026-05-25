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
    public SecurityFilterChain filterChain(HttpSecurity http, BpmInboundApiKeyFilter bpmInboundApiKeyFilter) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(new AntPathRequestMatcher("/actuator/health/**"),
                                 new AntPathRequestMatcher("/actuator/info")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/v1/auth/login")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/v1/technical/workflow/readiness")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/v1/bpm/practices")).permitAll()
                .requestMatchers(new AntPathRequestMatcher("/api/v1/auth/**")).authenticated()
                .requestMatchers(new AntPathRequestMatcher("/api/v1/**")).authenticated()
                .anyRequest().permitAll())
            .addFilterBefore(bpmInboundApiKeyFilter, BasicAuthenticationFilter.class)
            .httpBasic(basic -> {})
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((req, res, e) -> {
                    res.setHeader("WWW-Authenticate", "Basic realm=\"ANC\"");
                    res.setStatus(HttpStatus.UNAUTHORIZED.value());
                    res.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    res.getWriter().write("{\"resultCode\":1002,\"resultMessage\":\"Autenticazione richiesta\"}");
                }));
        return http.build();
    }
}
