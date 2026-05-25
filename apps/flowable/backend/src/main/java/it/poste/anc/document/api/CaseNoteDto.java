package it.poste.anc.document.api;

/**
 * DTO nota intermedia pratica — usato da GET/POST /practices/{id}/notes
 */
public record CaseNoteDto(Long id, String autore, String testo, String tipo, String createdAt) {
}
