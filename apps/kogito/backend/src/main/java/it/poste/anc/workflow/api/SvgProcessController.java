package it.poste.anc.workflow.api;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Espone il diagramma SVG del processo richiesto dalla Kogito Management Console
 * tramite Data Index GraphQL field {@code diagram}.
 *
 * <p>URL chiamato dal Data Index:
 * {@code GET /svg/processes/{processId}/instances/{processInstanceId}}
 *
 * <p>Per la POC viene restituito un SVG statico che rappresenta visivamente
 * il workflow ANC. Non necessita di librerie esterne: il diagramma è generato
 * inline in Java.
 */
@RestController
@RequestMapping("/svg/processes")
public class SvgProcessController {

    private static final String SVG_ANC_PRATICA = buildAncPraticaSvg();

    /**
     * Restituisce il diagramma SVG dell'istanza di processo.
     * Il processInstanceId è accettato ma non usato (diagramma statico in POC).
     */
    @GetMapping(value = "/{processId}/instances/{processInstanceId}",
                produces = "image/svg+xml")
    public ResponseEntity<String> getProcessInstanceDiagram(
            @PathVariable String processId,
            @PathVariable String processInstanceId) {

        if (!"anc_pratica".equals(processId)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("image/svg+xml"))
                .body(SVG_ANC_PRATICA);
    }

    // ------------------------------------------------------------------
    // SVG statico del processo ANC
    // ------------------------------------------------------------------

    private static String buildAncPraticaSvg() {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <svg xmlns="http://www.w3.org/2000/svg"
                     xmlns:xlink="http://www.w3.org/1999/xlink"
                     width="700" height="200" viewBox="0 0 700 200"
                     style="background:#f8f8f8; font-family: Arial, sans-serif;">

                  <!-- Titolo -->
                  <text x="350" y="22" text-anchor="middle"
                        font-size="13" font-weight="bold" fill="#333">
                    ANC – Gestione Pratica
                  </text>

                  <!-- Pool lane -->
                  <rect x="20" y="35" width="660" height="130" rx="6"
                        fill="#fff" stroke="#aaa" stroke-width="1.5"/>
                  <rect x="20" y="35" width="30" height="130" rx="6"
                        fill="#e8e8e8" stroke="#aaa" stroke-width="1.5"/>
                  <text x="35" y="105" text-anchor="middle"
                        font-size="10" fill="#555"
                        transform="rotate(-90, 35, 105)">GRUPPO_OPERATORE_ANC</text>

                  <!-- Freccia: Start → Task -->
                  <line x1="118" y1="100" x2="240" y2="100"
                        stroke="#555" stroke-width="1.5"
                        marker-end="url(#arrow)"/>

                  <!-- Freccia: Task → End -->
                  <line x1="450" y1="100" x2="570" y2="100"
                        stroke="#555" stroke-width="1.5"
                        marker-end="url(#arrow)"/>

                  <!-- Marker freccia -->
                  <defs>
                    <marker id="arrow" markerWidth="8" markerHeight="8"
                            refX="6" refY="3" orient="auto">
                      <path d="M0,0 L0,6 L8,3 z" fill="#555"/>
                    </marker>
                  </defs>

                  <!-- START EVENT (cerchio verde) -->
                  <circle cx="98" cy="100" r="20"
                          fill="#d4edda" stroke="#28a745" stroke-width="2"/>
                  <text x="98" y="140" text-anchor="middle"
                        font-size="10" fill="#155724">Pratica Aperta</text>

                  <!-- HUMAN TASK (rettangolo azzurro) -->
                  <rect x="240" y="72" width="210" height="56" rx="8"
                        fill="#cce5ff" stroke="#004085" stroke-width="2"/>
                  <!-- icona utente -->
                  <circle cx="258" cy="88" r="6" fill="#004085"/>
                  <path d="M248,104 Q258,96 268,104" fill="none"
                        stroke="#004085" stroke-width="1.5"/>
                  <text x="345" y="96" text-anchor="middle"
                        font-size="11" font-weight="bold" fill="#004085">
                    Lavorazione Pratica
                  </text>
                  <text x="345" y="114" text-anchor="middle"
                        font-size="10" fill="#004085">[ Human Task ]</text>
                  <text x="345" y="152" text-anchor="middle"
                        font-size="10" fill="#555">task_lavorazione</text>

                  <!-- END EVENT (cerchio rosso) -->
                  <circle cx="590" cy="100" r="20"
                          fill="#f8d7da" stroke="#dc3545" stroke-width="3"/>
                  <text x="590" y="140" text-anchor="middle"
                        font-size="10" fill="#721c24">Pratica Chiusa</text>

                </svg>
                """;
    }
}
