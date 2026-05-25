package it.poste.anc.shared.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Compatibility filter: gestisce i path che Flowable Admin 6.8.0 usa per
 * raggiungere un'istanza Flowable, strippando il context-root di Flowable UI
 * dal path in entrata e lasciando solo il segmento REST nativo.
 *
 * Flowable Admin default:  context-root=/flowable-ui  rest-root=process-api
 *   → chiamate come  /flowable-ui/process-api/...
 *   → strip /flowable-ui → forward a  /process-api/...  (Flowable 7 servlet)
 *
 * Alias alternativo:       context-root=flowable-rest  rest-root=process-api
 *   → chiamate come  /flowable-rest/process-api/...
 *   → strip /flowable-rest → forward a  /process-api/...
 */
@Component
@Order(Integer.MIN_VALUE + 10)
public class FlowableRestAliasFilter extends OncePerRequestFilter {

    /** Prefissi da strippare — corrispondono ai context-root configurabili in Flowable Admin. */
    private static final List<String> ALIAS_PREFIXES = List.of("/flowable-ui", "/flowable-rest");

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws IOException, ServletException {
        String uri = request.getRequestURI();
        for (String prefix : ALIAS_PREFIXES) {
            if (uri.startsWith(prefix + "/")) {
                String newUri = uri.substring(prefix.length()); // es. /process-api/...
                request.getServletContext().getRequestDispatcher(newUri).forward(request, response);
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
