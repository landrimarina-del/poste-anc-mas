package it.poste.anc.signals.application;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Locale;

@Component
public class SinergiaStubGateway {

    public SinergiaTicket openTicket(Long signalId,
                                     Long practiceId,
                                     String subject,
                                     String description,
                                     String actorUsername) {
        Instant openedAt = Instant.now();
        String ticketId = "SNG-" + signalId + "-" + openedAt.toEpochMilli();
        String note = "Apertura ticket simulata verso Sinergia stub";
        return new SinergiaTicket(ticketId, openedAt, buildSummary(practiceId, subject, description, actorUsername, note));
    }

    private String buildSummary(Long practiceId,
                                String subject,
                                String description,
                                String actorUsername,
                                String note) {
        String safeSubject = subject == null ? "" : subject.trim();
        String safeDescription = description == null ? "" : description.trim();
        String shortDescription = safeDescription.length() > 120
                ? safeDescription.substring(0, 120)
                : safeDescription;
        return String.format(
                Locale.ROOT,
                "practiceId=%d; actor=%s; subject=%s; description=%s; note=%s",
                practiceId,
                actorUsername,
                safeSubject,
                shortDescription,
                note
        );
    }

    public record SinergiaTicket(String ticketId, Instant openedAt, String summary) {
    }
}
