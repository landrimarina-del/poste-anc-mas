package it.poste.anc.document.application;

import org.springframework.http.HttpStatus;

public class DocumentOperationException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final int resultCode;

    public DocumentOperationException(HttpStatus httpStatus, int resultCode, String message) {
        super(message);
        this.httpStatus = httpStatus;
        this.resultCode = resultCode;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public int getResultCode() {
        return resultCode;
    }
}
