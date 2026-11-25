package com.phelti.phelti.exception;

import com.phelti.phelti.dto.ErrorResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            ResourceNotFoundException exception) {

        return buildResponse(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRequest(
            InvalidRequestException exception) {

        return buildResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(
            ConflictException exception) {

        return buildResponse(HttpStatus.CONFLICT, exception.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableBody(
            HttpMessageNotReadableException exception) {

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Revisa los datos enviados: faltan campos o hay un formato incorrecto."
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException exception) {

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Uno de los valores enviados no es válido."
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(
            DataIntegrityViolationException exception) {

        return buildResponse(
                HttpStatus.CONFLICT,
                "No se puede realizar la operación: hay un registro duplicado, inexistente o en uso."
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException exception) {

        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> "Revisa " + fieldName(error.getField()))
                .sorted()
                .collect(Collectors.joining("; "));

        return buildResponse(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(org.springframework.web.server.ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleAccess(org.springframework.web.server.ResponseStatusException exception) {
        return ResponseEntity.status(exception.getStatusCode()).body(new ErrorResponse(
                Instant.now().toString(), exception.getStatusCode().value(), "Solicitud rechazada", exception.getReason()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception exception) {

        if (exception instanceof org.springframework.web.ErrorResponse springError) {
            int code = springError.getStatusCode().value();
            HttpStatus resolved = HttpStatus.resolve(code);
            String reason = resolved != null ? resolved.getReasonPhrase() : "Error";

            return ResponseEntity
                    .status(springError.getStatusCode())
                    .body(new ErrorResponse(
                            Instant.now().toString(),
                            code,
                            reason,
                            "No se pudo procesar la solicitud."
                    ));
        }

        LOGGER.error("Unexpected error", exception);

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error inesperado. Intenta de nuevo."
        );
    }

    private String fieldName(String field) {
        return switch (field) {
            case "name" -> "el nombre"; case "email" -> "el correo electrónico";
            case "title" -> "el título"; case "description" -> "la descripción";
            case "durationMinutes" -> "la duración"; case "activityDate" -> "la fecha";
            case "activityTime", "startTime", "endTime" -> "la hora";
            case "amount", "pheltAmount", "proposedAmount" -> "el importe";
            case "location" -> "la ubicación"; case "virtualLink" -> "el enlace de reunión";
            case "score" -> "la puntuación"; case "comment" -> "el comentario";
            case "dayOfWeek" -> "el día de la semana"; case "categoryId" -> "la categoría";
            case "publicationType" -> "el tipo de publicación"; case "interactionMode", "preferredInteractionMode" -> "la modalidad";
            default -> "los datos del formulario";
        };
    }

    private ResponseEntity<ErrorResponse> buildResponse(
            HttpStatus status,
            String message) {

        ErrorResponse body = new ErrorResponse(
                Instant.now().toString(),
                status.value(),
                status.getReasonPhrase(),
                message
        );

        return ResponseEntity.status(status).body(body);
    }
}
