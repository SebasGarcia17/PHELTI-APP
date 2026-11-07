package com.phelti.phelti.exception;

import java.time.Instant;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(PublicationNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handlePublicationNotFound(
            PublicationNotFoundException exception) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(buildBody(
                        exception.getMessage(),
                        HttpStatus.NOT_FOUND
                ));
    }

    @ExceptionHandler(InvalidPublicationDataException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidPublicationData(
            InvalidPublicationDataException exception) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(buildBody(
                        exception.getMessage(),
                        HttpStatus.BAD_REQUEST
                ));
    }

    @ExceptionHandler(ProposalNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleProposalNotFound(
            ProposalNotFoundException exception) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(buildBody(
                        exception.getMessage(),
                        HttpStatus.NOT_FOUND
                ));
    }

    @ExceptionHandler(InvalidProposalDataException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidProposalData(
            InvalidProposalDataException exception) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(buildBody(
                        exception.getMessage(),
                        HttpStatus.BAD_REQUEST
                ));
    }

    @ExceptionHandler(WalletNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleWalletNotFound(
            WalletNotFoundException exception) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(buildBody(
                        exception.getMessage(),
                        HttpStatus.NOT_FOUND
                ));
    }

    @ExceptionHandler(WalletAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleWalletAlreadyExists(
            WalletAlreadyExistsException exception) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(buildBody(
                        exception.getMessage(),
                        HttpStatus.CONFLICT
                ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(
            IllegalArgumentException exception) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(buildBody(
                        exception.getMessage(),
                        HttpStatus.BAD_REQUEST
                ));
    }

    private Map<String, Object> buildBody(
            String message,
            HttpStatus status) {

        return Map.of(
                "timestamp", Instant.now().toString(),
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", message
        );
    }
}