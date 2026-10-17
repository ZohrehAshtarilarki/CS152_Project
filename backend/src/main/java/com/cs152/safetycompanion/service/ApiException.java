package com.cs152.safetycompanion.service;

import org.springframework.http.HttpStatus;

/** Throw this from services; ApiExceptionHandler turns it into {"error": "..."} with the right status. */
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
