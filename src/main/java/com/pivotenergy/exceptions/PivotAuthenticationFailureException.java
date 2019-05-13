package com.pivotenergy.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.function.Supplier;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class PivotAuthenticationFailureException extends RuntimeException implements Supplier<PivotAuthenticationFailureException> {

    PivotAuthenticationFailureException() {
        super("Authentication Failure");
    }

    public PivotAuthenticationFailureException(String message) {
        super(message);
    }

    public PivotAuthenticationFailureException(String message, String cause) {
        super(message, new Throwable(cause));
    }

    @Override
    public PivotAuthenticationFailureException get() {
        return this;
    }
}

