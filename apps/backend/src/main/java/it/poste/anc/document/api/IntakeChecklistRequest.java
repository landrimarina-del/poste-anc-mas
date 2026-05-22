package it.poste.anc.document.api;

import java.util.List;

public record IntakeChecklistRequest(
        Boolean documentPresent,
        Boolean readabilityOk,
        Boolean formalOk,
        Boolean customerDataOk,
        Boolean cardNumberMatchRequired,
        Boolean cardNumberMatchOk,
        String noteLegibility,
        String noteFormalSuitability,
        String noteClientDataConsistency,
        String noteCardNumberMatch,
        String finalNotePractice,
        Boolean cardPresent,
        Boolean cardConformityOk,
        List<String> koReasons,
        String internalNotes,
        Long codiceCausaleId
) {
}
