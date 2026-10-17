package com.cs152.safetycompanion.controller;

import com.cs152.safetycompanion.service.ApiException;
import com.cs152.safetycompanion.view.ErrorView;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/** Turns errors into {"error": "..."} JSON with the right HTTP status. */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorView> handleApiException(ApiException e) {
        return ResponseEntity.status(e.getStatus()).body(new ErrorView(e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorView> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest().body(new ErrorView(message.isEmpty() ? "Invalid request." : message));
    }
}
