package it.poste.anc.shared.common;

/**
 * Response uniforme verso il FE come da Architect cap. 05 §4.
 * resultCode = 0  -> operazione riuscita
 * resultCode != 0 -> errore (es. credenziali errate, validazione, ecc.)
 * I codici negativi specifici di BC4 (-4, -5) sono baseline IA v1.4 e fuori scope Sprint 0.
 */
public record ApiResponse<T>(int resultCode, String resultMessage, T details) {

    public static <T> ApiResponse<T> ok(T details) {
        return new ApiResponse<>(0, "OK", details);
    }

    public static <T> ApiResponse<T> ok() {
        return new ApiResponse<>(0, "OK", null);
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}
