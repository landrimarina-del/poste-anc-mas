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
import java.util.Objects;

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
     */
    @SuppressWarnings("unchecked")
    public OutcomeComputed computeVerbaleOutcome(
            boolean documentPresent,
            Boolean readabilityOk,
            Boolean formalOk,
            Boolean customerDataOk,
            boolean cardNumberMatchRequired,
            Boolean cardNumberMatchOk,
            List<String> koReasons) {

            boolean safeReadabilityOk = Boolean.TRUE.equals(readabilityOk);
            boolean safeFormalOk = Boolean.TRUE.equals(formalOk);
            boolean safeCustomerDataOk = Boolean.TRUE.equals(customerDataOk);
            boolean safeCardNumberMatchOk = Boolean.TRUE.equals(cardNumberMatchOk);
            List<String> safeKoReasons = koReasons != null ? koReasons : List.of();

            Map<String, Object> input = Map.of(
                "documentPresent", documentPresent,
                "readabilityOk", safeReadabilityOk,
                "formalOk", safeFormalOk,
                "customerDataOk", safeCustomerDataOk,
                "cardNumberMatchRequired", cardNumberMatchRequired,
                "cardNumberMatchOk", safeCardNumberMatchOk,
                "koReasons", safeKoReasons
            );

            OutcomeComputed dmnResult = evaluateOutcomeDecision(NS_VERBALE, MODEL_VERBALE, DECISION_VERBALE, input);
            if (dmnResult != null) {
                return dmnResult;
            }

            return computeVerbaleFallback(
                documentPresent,
                safeReadabilityOk,
                safeFormalOk,
                safeCustomerDataOk,
                cardNumberMatchRequired,
                safeCardNumberMatchOk,
                safeKoReasons
            );
    }

    /**
     * Calcola esito checklist CARTA tramite DMN.
     */
    @SuppressWarnings("unchecked")
    public OutcomeComputed computeCartaOutcome(boolean cardPresent, Boolean cardConformityOk, Boolean cardExpired) {

        boolean safeCardConformityOk = Boolean.TRUE.equals(cardConformityOk);
        boolean safeCardExpired = Boolean.TRUE.equals(cardExpired);
        Map<String, Object> input = Map.of(
                "cardPresent", cardPresent,
                "cardConformityOk", safeCardConformityOk,
                "cardExpired", safeCardExpired
        );

        OutcomeComputed dmnResult = evaluateOutcomeDecision(NS_CARTA, MODEL_CARTA, DECISION_CARTA, input);
        if (dmnResult != null) {
            return dmnResult;
        }

        return computeCartaFallback(cardPresent, safeCardConformityOk, safeCardExpired);
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
                                                   boolean formalOk,
                                                   boolean customerDataOk,
                                                   boolean cardNumberMatchRequired,
                                                   boolean cardNumberMatchOk,
                                                   List<String> koReasons) {
        List<String> koCodes = new ArrayList<>();
        if (!documentPresent) {
            koCodes.add("DOCUMENTO_ASSENTE");
        } else {
            if (!readabilityOk) {
                koCodes.add("LEGGIBILITA_KO");
            }
            if (!formalOk) {
                koCodes.addAll(koReasons.stream().filter(Objects::nonNull).toList());
            }
            if (!customerDataOk) {
                koCodes.add("DATI_CLIENTE_KO");
            }
            if (cardNumberMatchRequired && !cardNumberMatchOk) {
                koCodes.add("NUMERO_CARTA_KO");
            }
        }

        String outcome = koCodes.isEmpty() ? "APPROVATA" : "RESPINTA";
        return new OutcomeComputed(outcome, List.copyOf(koCodes));
    }

    private OutcomeComputed computeCartaFallback(boolean cardPresent, boolean cardConformityOk, boolean cardExpired) {
        if (!cardPresent) {
            return new OutcomeComputed("RESPINTA", List.of("CARTA_ASSENTE"));
        }
        if (!cardConformityOk) {
            return new OutcomeComputed("RESPINTA", List.of("CARTA_NON_CONFORME"));
        }
        if (cardExpired) {
            return new OutcomeComputed("RESPINTA", List.of("CARTA_SCADUTA"));
        }
        return new OutcomeComputed("APPROVATA", List.of());
    }

    record OutcomeComputed(String outcome, List<String> koCodes) {}
}
