package it.poste.anc.signals.application;

import org.springframework.http.HttpStatus;

public class SignalOperationException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final int resultCode;

    public SignalOperationException(HttpStatus httpStatus, int resultCode, String message) {
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
