package it.poste.anc.document.api;

/**
 * DTO causale KO checklist — restituita da GET /practices/{id}/intake/checklist/causali
 */
public record CausaleChecklistDto(Long id, String codice, String descrizione) {
}
