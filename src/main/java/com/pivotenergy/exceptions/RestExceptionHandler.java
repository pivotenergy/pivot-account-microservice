package com.pivotenergy.exceptions;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.sql.SQLIntegrityConstraintViolationException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value={PivotEntityNotFoundException.class})
    protected ResponseEntity<Object> handleException(PivotEntityNotFoundException ex) {
        ApiError apiError = new ApiError(NOT_FOUND, ex.getMessage(), ex.getCause());
        return buildResponseEntity(apiError);
    }

    @ExceptionHandler(value={PivotAuthenticationFailureException.class})
    protected ResponseEntity<Object> handleException(PivotAuthenticationFailureException ex) {
        ApiError apiError = new ApiError(BAD_REQUEST, ex.getMessage(), ex.getCause());
        return buildResponseEntity(apiError);
    }

    @ExceptionHandler(value={IllegalArgumentException.class})
    protected ResponseEntity<Object> handleException(IllegalArgumentException ex) {
        ApiError apiError = new ApiError(BAD_REQUEST, ex.getMessage(), ex.getCause());
        return buildResponseEntity(apiError);
    }

    @ExceptionHandler(value={DataIntegrityViolationException.class})
    protected ResponseEntity<Object> handleException(DataIntegrityViolationException ex) {
        ApiError apiError = new ApiError(BAD_REQUEST, ex.getCause().getMessage(), ex.getCause().getCause());
        return buildResponseEntity(apiError);
    }

    @ExceptionHandler(value={SQLIntegrityConstraintViolationException.class})
    protected ResponseEntity<Object> handleException(SQLIntegrityConstraintViolationException ex) {
        ApiError apiError = new ApiError(BAD_REQUEST, ex.getCause().getMessage(), ex.getCause().getCause());
        return buildResponseEntity(apiError);
    }

    private ResponseEntity<Object> buildResponseEntity(ApiError apiError) {
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }
}
