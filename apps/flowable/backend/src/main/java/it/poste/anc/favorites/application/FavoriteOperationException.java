package it.poste.anc.favorites.application;

import org.springframework.http.HttpStatus;

public class FavoriteOperationException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final int resultCode;

    public FavoriteOperationException(HttpStatus httpStatus, int resultCode, String message) {
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
