package it.poste.anc.bpmgw.inbound;

import org.springframework.http.HttpStatus;

public class BpmAckOperationException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final int resultCode;

    public BpmAckOperationException(HttpStatus httpStatus, int resultCode, String message) {
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
