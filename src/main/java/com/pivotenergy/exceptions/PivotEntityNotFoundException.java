package com.pivotenergy.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.function.Supplier;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class PivotEntityNotFoundException extends RuntimeException implements Supplier<PivotEntityNotFoundException> {

    PivotEntityNotFoundException() {
        super("Entity Not Found");
    }

    public PivotEntityNotFoundException(String message) {
        super(message);
    }

    public PivotEntityNotFoundException(String message, String cause) {
        super(message, new Throwable(cause));
    }

    public PivotEntityNotFoundException(Class<?> clazz, String id) {
        this(String.format("%s not found", clazz.getSimpleName()),
                String.format("%s not found for id=[%s]", clazz.getSimpleName(), id));
    }

    @Override
    public PivotEntityNotFoundException get() {
        return this;
    }
}

