package com.pivotenergy.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.function.Supplier;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class PivotInvalidRequestException extends RuntimeException implements Supplier<PivotInvalidRequestException> {

    PivotInvalidRequestException() {
        super("invalid Request");
    }

    public PivotInvalidRequestException(String message) {
        super(message);
    }

    public PivotInvalidRequestException(String message, String cause) {
        super(message, new Throwable(cause));
    }

    @Override
    public PivotInvalidRequestException get() {
        return this;
    }
}