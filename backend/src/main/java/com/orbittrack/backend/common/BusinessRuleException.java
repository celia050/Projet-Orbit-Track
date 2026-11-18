package com.orbittrack.backend.common;

import org.springframework.http.HttpStatus;

/** Une règle métier n'est pas respectée. Le message est affiché tel quel à l'utilisateur. */
public class BusinessRuleException extends RuntimeException {

    private final HttpStatus status;

    public BusinessRuleException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
