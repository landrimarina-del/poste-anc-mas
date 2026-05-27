package it.poste.anc.document.application;

import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNDecisionResult;
import org.kie.dmn.api.core.DMNResult;
import org.kie.kogito.decision.DecisionModel;
import org.kie.kogito.decision.DecisionModels;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Wrapper per le decisioni DMN di esito checklist.
 *
 * Sostituisce i metodi computeVerbaleOutcome() e computeCartaOutcome()
 * precedentemente hardcoded in IntakeChecklistService.
 *
 * I file DMN sono in src/main/resources/processes/:
 *   - anc_outcome_verbale.dmn (namespace: https://poste.it/anc/dmn/outcome-verbale)
 *   - anc_outcome_carta.dmn   (namespace: https://poste.it/anc/dmn/outcome-carta)
 */
@Service
public class OutcomeDmnService {

    private static final String NS_VERBALE = "https://poste.it/anc/dmn/outcome-verbale";
    private static final String NS_CARTA = "https://poste.it/anc/dmn/outcome-carta";
    private static final String MODEL_VERBALE = "anc_outcome_verbale";
    private static final String MODEL_CARTA = "anc_outcome_carta";
    private static final String DECISION_VERBALE = "VerbaleOutcome";
    private static final String DECISION_CARTA = "CartaOutcome";

    private final DecisionModels decisionModels;

    public OutcomeDmnService(DecisionModels decisionModels) {
        this.decisionModels = decisionModels;
    }

    /**
     * Calcola esito checklist VERBALE tramite DMN.
     * Le sotto-voci di "idoneo al controllo formale" sono valorizzate individualmente
     * e mappate ai codici PPEZ027–PPEZ031.
     */
    @SuppressWarnings("unchecked")
    public OutcomeComputed computeVerbaleOutcome(
            boolean documentPresent,
            Boolean readabilityOk,
            Boolean intestazioneOk,
            Boolean firmeOk,
            Boolean intestazioneConformeAlTimbroOk,
            Boolean dichiarazioneConformeAlleFirmeOk,
            Boolean cartaPosteItalianeOk,
            Boolean customerDataOk,
            boolean cardNumberMatchRequired,
            Boolean cardNumberMatchOk) {

            boolean safeReadabilityOk = Boolean.TRUE.equals(readabilityOk);
            boolean safeIntestazione = Boolean.TRUE.equals(intestazioneOk);
            boolean safeFirme = Boolean.TRUE.equals(firmeOk);
            boolean safeConformeTimbro = Boolean.TRUE.equals(intestazioneConformeAlTimbroOk);
            boolean safeConformeFirme = Boolean.TRUE.equals(dichiarazioneConformeAlleFirmeOk);
            boolean safeCartaPI = Boolean.TRUE.equals(cartaPosteItalianeOk);
            boolean safeCustomerDataOk = Boolean.TRUE.equals(customerDataOk);
            boolean safeCardNumberMatchOk = Boolean.TRUE.equals(cardNumberMatchOk);

            Map<String, Object> input = new java.util.HashMap<>();
            input.put("documentPresent", documentPresent);
            input.put("readabilityOk", safeReadabilityOk);
            input.put("intestazioneOk", safeIntestazione);
            input.put("firmeOk", safeFirme);
            input.put("intestazioneConformeAlTimbroOk", safeConformeTimbro);
            input.put("dichiarazioneConformeAlleFirmeOk", safeConformeFirme);
            input.put("cartaPosteItalianeOk", safeCartaPI);
            input.put("customerDataOk", safeCustomerDataOk);
            input.put("cardNumberMatchRequired", cardNumberMatchRequired);
            input.put("cardNumberMatchOk", safeCardNumberMatchOk);

            OutcomeComputed dmnResult = evaluateOutcomeDecision(NS_VERBALE, MODEL_VERBALE, DECISION_VERBALE, input);
            if (dmnResult != null) {
                return dmnResult;
            }

            return computeVerbaleFallback(
                documentPresent,
                safeReadabilityOk,
                safeIntestazione,
                safeFirme,
                safeConformeTimbro,
                safeConformeFirme,
                safeCartaPI,
                safeCustomerDataOk,
                cardNumberMatchRequired,
                safeCardNumberMatchOk
            );
    }

    /**
     * Calcola esito checklist CARTA tramite DMN.
     * legibilityOk → PPEZ034, cardConformityOk → PPEZ035.
     */
    @SuppressWarnings("unchecked")
    public OutcomeComputed computeCartaOutcome(boolean cardPresent, Boolean legibilityOk, Boolean cardConformityOk) {

        boolean safeLegibilityOk = Boolean.TRUE.equals(legibilityOk);
        boolean safeCardConformityOk = Boolean.TRUE.equals(cardConformityOk);
        Map<String, Object> input = Map.of(
                "cardPresent", cardPresent,
                "legibilityOk", safeLegibilityOk,
                "cardConformityOk", safeCardConformityOk
        );

        OutcomeComputed dmnResult = evaluateOutcomeDecision(NS_CARTA, MODEL_CARTA, DECISION_CARTA, input);
        if (dmnResult != null) {
            return dmnResult;
        }

        return computeCartaFallback(cardPresent, safeLegibilityOk, safeCardConformityOk);
    }

    @SuppressWarnings("unchecked")
    private OutcomeComputed evaluateOutcomeDecision(String namespace,
                                                    String modelName,
                                                    String decisionName,
                                                    Map<String, Object> input) {
        DecisionModel model = decisionModels.getDecisionModel(namespace, modelName);
        if (model == null) {
            return null;
        }

        DMNContext context = model.newContext(input);
        if (context == null) {
            return null;
        }

        DMNResult result = model.evaluateAll(context);
        if (result == null) {
            return null;
        }

        DMNDecisionResult decisionResult = result.getDecisionResultByName(decisionName);
        if (decisionResult == null || decisionResult.getResult() == null) {
            return null;
        }

        if (!(decisionResult.getResult() instanceof Map<?, ?> outputRaw)) {
            return null;
        }

        Object outcomeRaw = outputRaw.get("outcome");
        if (!(outcomeRaw instanceof String outcome) || outcome.isBlank()) {
            return null;
        }

        List<String> koCodes = normalizeKoCodes(outputRaw.get("koCodes"));
        return new OutcomeComputed(outcome, koCodes);
    }

    private List<String> normalizeKoCodes(Object rawKoCodes) {
        if (!(rawKoCodes instanceof List<?> list)) {
            return List.of();
        }

        List<String> normalized = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof String value && !value.isBlank()) {
                normalized.add(value);
            }
        }
        return List.copyOf(normalized);
    }

    private OutcomeComputed computeVerbaleFallback(boolean documentPresent,
                                                   boolean readabilityOk,
                                                   boolean intestazioneOk,
                                                   boolean firmeOk,
                                                   boolean intestazioneConformeAlTimbroOk,
                                                   boolean dichiarazioneConformeAlleFirmeOk,
                                                   boolean cartaPosteItalianeOk,
                                                   boolean customerDataOk,
                                                   boolean cardNumberMatchRequired,
                                                   boolean cardNumberMatchOk) {
        List<String> koCodes = new ArrayList<>();
        if (!documentPresent) {
            koCodes.add("DOCUMENTO_ASSENTE");
        } else {
            if (!readabilityOk) koCodes.add("PPEZ026");
            if (!intestazioneOk) koCodes.add("PPEZ027");
            if (!firmeOk) koCodes.add("PPEZ028");
            if (!intestazioneConformeAlTimbroOk) koCodes.add("PPEZ029");
            if (!dichiarazioneConformeAlleFirmeOk) koCodes.add("PPEZ030");
            if (!cartaPosteItalianeOk) koCodes.add("PPEZ031");
            if (!customerDataOk) koCodes.add("PPEZ032");
            if (cardNumberMatchRequired && !cardNumberMatchOk) koCodes.add("PPEZ033");
        }

        String outcome = koCodes.isEmpty() ? "APPROVATA" : "RESPINTA";
        return new OutcomeComputed(outcome, List.copyOf(koCodes));
    }

    private OutcomeComputed computeCartaFallback(boolean cardPresent, boolean legibilityOk, boolean cardConformityOk) {
        if (!cardPresent) {
            return new OutcomeComputed("RESPINTA", List.of("CARTA_ASSENTE"));
        }
        List<String> koCodes = new ArrayList<>();
        if (!legibilityOk) koCodes.add("PPEZ034");
        if (!cardConformityOk) koCodes.add("PPEZ035");
        if (!koCodes.isEmpty()) {
            return new OutcomeComputed("RESPINTA", List.copyOf(koCodes));
        }
        return new OutcomeComputed("APPROVATA", List.of());
    }

    record OutcomeComputed(String outcome, List<String> koCodes) {}
}
