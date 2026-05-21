package it.poste.anc.document.api;

/**
 * Richiesta creazione nota intermedia pratica.
 * Il tipo è fisso a LAVORAZIONE; l'autore viene estratto dall'autenticazione corrente.
 */
public record CaseNoteRequest(String testo) {
}
