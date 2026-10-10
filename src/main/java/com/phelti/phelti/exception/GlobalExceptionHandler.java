package com.phelti.phelti.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PublicationNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(PublicationNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildBody(ex.getMessage(), HttpStatus.NOT_FOUND));
    }

    @ExceptionHandler(InvalidPublicationDataException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidData(InvalidPublicationDataException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildBody(ex.getMessage(), HttpStatus.BAD_REQUEST));
    }

    @ExceptionHandler(ProposalNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ProposalNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(buildBody(ex.getMessage(), HttpStatus.NOT_FOUND));
    }

    @ExceptionHandler(InvalidProposalDataException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidData(InvalidProposalDataException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildBody(ex.getMessage(), HttpStatus.BAD_REQUEST));
    }

    private Map<String, Object> buildBody(String message, HttpStatus status) {
        return Map.of(
                "timestamp", Instant.now().toString(),
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", message
        );
    }
}
