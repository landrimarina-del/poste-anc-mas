package it.poste.anc.bpmgw.inbound;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.poste.anc.document.ingestion.AttachmentFetcher;
import it.poste.anc.document.ingestion.AttachmentStorage;
import it.poste.anc.shared.common.ApiResponse;
import it.poste.anc.ticketing.TicketingClient;
import org.flowable.engine.RuntimeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class BpmPracticeInboundServiceIdempotencyTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    private BpmPracticeInboundService inboundService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        AttachmentFetcher attachmentFetcher = Mockito.mock(AttachmentFetcher.class);
        AttachmentStorage attachmentStorage = Mockito.mock(AttachmentStorage.class);
        BpmInboundMessageWriter inboundMessageWriter = Mockito.mock(BpmInboundMessageWriter.class);
        // PlatformTransactionManager no-op: nel path di duplicato non serve transazione reale,
        // ma TransactionTemplate.execute() richiede comunque un manager non nullo.
        PlatformTransactionManager transactionManager = new PlatformTransactionManager() {
            @Override
            public TransactionStatus getTransaction(TransactionDefinition definition) {
                return new SimpleTransactionStatus();
            }

            @Override
            public void commit(TransactionStatus status) {
                // no-op
            }

            @Override
            public void rollback(TransactionStatus status) {
                // no-op
            }
        };
        TicketingClient ticketingClient = Mockito.mock(TicketingClient.class);
        RuntimeService runtimeService = Mockito.mock(RuntimeService.class);
        inboundService = new BpmPracticeInboundService(
                jdbcTemplate,
                objectMapper,
                attachmentFetcher,
                attachmentStorage,
                inboundMessageWriter,
                transactionManager,
                ticketingClient,
                runtimeService,
                false  // ticketingEnabled: false nei test di idempotenza per isolare dalla chiamata ticketing
        );
    }

    @Test
    void duplicateIdWorkItemDoesNotInsertNewPractice() throws Exception {
        JsonNode payload = objectMapper.readTree("""
                {
                  "CANALE": "BPM",
                  "ID_WORKITEM": "WI-DUP-001",
                  "NUM_PRATICA": "REQ-DUP-001",
                  "CF_CLIENTE": "RSSMRA80A01H501U",
                  "CODICE_CLIENTE": "AUC-DUP-001",
                  "DATA_INSERIMENTO_RICHIESTA": "14/05/2026 11:15:30",
                  "CLIENTE": {
                    "NOME": "Mario",
                    "COGNOME": "Rossi",
                    "SESSO": "M",
                    "DATANASCITA": "1980-01-01",
                    "COMUNENASCITA": "Roma",
                    "PROVINCIANASCITA": "RM",
                    "NAZIONENASCITA": "IT",
                    "CITTADINANZA": "Italiana"
                  },
                  "DATI_CARTA_BLOCCATA": {
                    "I_NUMERO_CARTA": "1234567890123456",
                    "I_TIPO_CARTA": "PostePay Evolution",
                    "I_INTEST_CARTA": "Mario Rossi"
                  },
                  "DOCUMENTI": [
                    {
                      "CODICE_DOC_ID": 1,
                      "CONTENUTI": [
                        {
                          "NOME_FILE": "verbale.pdf",
                          "ESTENSIONE": "pdf",
                          "ID_DOC": "DOC-1",
                          "LINKDOWNLOAD": "https://example.local/doc/1"
                        }
                      ]
                    }
                  ]
                }
                """);

        when(jdbcTemplate.queryForObject(anyString(), eq(Integer.class), eq("WI-DUP-001"))).thenReturn(1);

        ApiResponse<BpmPracticeOpenResponse> response = inboundService.openPractice(payload);

        assertEquals(-5, response.resultCode());
        assertEquals("Idempotenza violata: ID_WORKITEM gia' presente", response.resultMessage());
        verify(jdbcTemplate, times(1)).queryForObject(anyString(), eq(Integer.class), eq("WI-DUP-001"));
        verify(jdbcTemplate, never()).update(any(PreparedStatementCreator.class), any(KeyHolder.class));
        verify(jdbcTemplate, times(1)).update(
                startsWith("INSERT INTO bpm_inbound_message"),
                eq("WI-DUP-001"),
                anyString(),
                eq(-5),
                contains("Idempotenza violata"),
                isNull()
        );
    }
}
