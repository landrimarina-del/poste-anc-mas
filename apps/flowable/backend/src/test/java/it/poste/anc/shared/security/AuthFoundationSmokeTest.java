package it.poste.anc.shared.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Smoke test Sprint 0: login tecnico base con endpoint auth locale.
 */
@WebMvcTest(controllers = AuthController.class)
@Import(SecurityConfig.class)
class AuthFoundationSmokeTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private AppUserDetailsService appUserDetailsService;

    @Test
    void loginReturnsOkForValidCredentials() throws Exception {
        AppUser user = mock(AppUser.class);
        when(user.getUsername()).thenReturn("op.rossi");
        when(user.getFullName()).thenReturn("Mario Rossi");
        when(user.getEmail()).thenReturn("op.rossi@poste.it");

        AppUserDetails details = mock(AppUserDetails.class);
        when(details.getDomainUser()).thenReturn(user);
        when(details.getRoleCodes()).thenReturn(Set.of("OPERATORE_ANC"));
        when(details.getGroupCodes()).thenReturn(Set.of("GRUPPO_OPERATORE_ANC"));

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(details);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);

        String payload = Objects.requireNonNull(
            objectMapper.writeValueAsString(new LoginBody("op.rossi", "Demo1234!"))
        );

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType("application/json")
                .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value(0))
                .andExpect(jsonPath("$.details.username").value("op.rossi"))
                .andExpect(jsonPath("$.details.roles[0]").value("OPERATORE_ANC"));
    }

    private record LoginBody(String username, String password) {
    }
}
