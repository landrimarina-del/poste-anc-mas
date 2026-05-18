package it.poste.anc.shared.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.NotBlank;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import it.poste.anc.shared.common.ApiResponse;

import java.util.Set;

/**
 * Endpoint M-IAM previsti da API §2.1.
 *  - POST /api/v1/auth/login   -> autentica e crea sessione (cookie JSESSIONID = token semplice)
 *  - POST /api/v1/auth/logout  -> invalida sessione + SecurityContext
 *  - GET  /api/v1/auth/me      -> profilo utente corrente + ruoli + gruppi
 *
 * Compatibile anche con HTTP Basic (smoke test rapidi).
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;

    public AuthController(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {}

    public record UserInfo(String username, String fullName, String email,
                           Set<String> roles, Set<String> groups, String sessionId) {}

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserInfo>> login(@RequestBody LoginRequest body, HttpServletRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(body.username(), body.password()));

        // Salva il SecurityContext nella sessione HTTP (token semplice = JSESSIONID).
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        HttpSession session = request.getSession(true);
        session.setAttribute("SPRING_SECURITY_CONTEXT", context);

        AppUserDetails details = (AppUserDetails) auth.getPrincipal();
        AppUser u = details.getDomainUser();
        UserInfo info = new UserInfo(
                u.getUsername(),
                u.getFullName(),
                u.getEmail(),
                details.getRoleCodes(),
                details.getGroupCodes(),
                session.getId());

        return ResponseEntity.ok(ApiResponse.ok(info));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserInfo>> me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AppUserDetails details = (AppUserDetails) auth.getPrincipal();
        AppUser u = details.getDomainUser();
        UserInfo info = new UserInfo(
                u.getUsername(),
                u.getFullName(),
                u.getEmail(),
                details.getRoleCodes(),
                details.getGroupCodes(),
                null);
        return ResponseEntity.ok(ApiResponse.ok(info));
    }
}
