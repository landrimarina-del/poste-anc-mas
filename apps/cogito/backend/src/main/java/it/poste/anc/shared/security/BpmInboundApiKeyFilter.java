package it.poste.anc.shared.security;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Technical API key guard for BPM inbound create-practice endpoint.
 * This keeps the endpoint system-to-system and independent from user auth flows.
 */
@Component
public class BpmInboundApiKeyFilter extends OncePerRequestFilter {

    private final String expectedApiKey;
    private final String headerName;

    public BpmInboundApiKeyFilter(
            @Value("${anc.security.bpm.inbound.api-key:}") String expectedApiKey,
            @Value("${anc.security.bpm.inbound.header-name:X-SD-API-Key}") String headerName
    ) {
        this.expectedApiKey = expectedApiKey;
        this.headerName = headerName;
    }

    @Override
        protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        return !(
                "POST".equalsIgnoreCase(request.getMethod())
                        && "/api/v1/bpm/practices".equals(request.getRequestURI())
        );
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String providedKey = request.getHeader(headerName);
        if (expectedApiKey == null || expectedApiKey.isBlank() || !expectedApiKey.equals(providedKey)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"resultCode\":1003,\"resultMessage\":\"API key non valida\",\"details\":null}");
            return;
        }
        filterChain.doFilter(request, response);
    }
}