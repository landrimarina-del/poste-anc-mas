package it.poste.anc.signals.application;

import it.poste.anc.signals.api.SignalCreateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SignalServiceTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private SinergiaStubGateway sinergiaStubGateway;

    private SignalService signalService;

    @BeforeEach
    void setUp() {
        signalService = new SignalService(jdbcTemplate, sinergiaStubGateway);
    }

    @Test
    void createSignalRejectsUserWithoutOperatorOrSupervisorRole() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), eq("utente.no.role"))).thenReturn(10L);
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(10L), eq("OPERATORE_ANC"))).thenReturn(0);
        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq(10L), eq("SUPERVISORE_ANC"))).thenReturn(0);

        SignalOperationException ex = assertThrows(SignalOperationException.class,
                () -> signalService.createSignal("utente.no.role",
                        new SignalCreateRequest(100L, "Oggetto", "Descrizione")));

        assertEquals(HttpStatus.FORBIDDEN, ex.getHttpStatus());
        assertEquals(7012, ex.getResultCode());
    }

    @Test
    void listMySignalsRejectsInvalidStateFilter() {
        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), eq("op.rossi"))).thenReturn(5L);

        SignalOperationException ex = assertThrows(SignalOperationException.class,
                () -> signalService.listMySignals("op.rossi", "APERTO", null, null));

        assertEquals(HttpStatus.BAD_REQUEST, ex.getHttpStatus());
        assertEquals(7018, ex.getResultCode());
    }
}
