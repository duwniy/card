package org.example.card.exception;

import org.example.card.dto.ErrorResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    //400 Bad Request
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String missingFields = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getField)
                .collect(Collectors.joining(", "));


        ErrorResponseDto error = ErrorResponseDto.builder()
                .code("missing_field")
                .message("Missing required fields: " + missingFields)
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    //404 Not Found
    @ExceptionHandler(CardNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleCardNotFoundException(CardNotFoundException ex) {
        ErrorResponseDto error = ErrorResponseDto.builder()
                .code("not_found")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    //400 Bad Request
    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ErrorResponseDto> handlerInsufficientFundException(InsufficientFundsException ex) {
        ErrorResponseDto error = ErrorResponseDto.builder()
                .code("insufficient_funds")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // 400 Bad Request
    @ExceptionHandler(InvalidCardStatusException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidCardStatusException(InvalidCardStatusException ex) {
        ErrorResponseDto error = ErrorResponseDto.builder()
                .code("incompatible_status")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // 400 Bad Request
    @ExceptionHandler(CardLimitExceededException.class)
    public ResponseEntity<ErrorResponseDto> handleCardLimitExceededException(CardLimitExceededException ex) {
        ErrorResponseDto error = ErrorResponseDto.builder()
                .code("limit_exceeded")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    //400 BadRequest
    @ExceptionHandler(DuplicateTransactionException.class)
    public ResponseEntity<ErrorResponseDto> handleDuplicateTransactionException(DuplicateTransactionException ex) {
        ErrorResponseDto error = ErrorResponseDto.builder()
                .code("duplicate_transaction")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    //412 Precondition
    @ExceptionHandler(ETagMismatchException.class)
    public ResponseEntity<ErrorResponseDto> handleEtagMismatchException(ETagMismatchException ex) {
        ErrorResponseDto error = ErrorResponseDto.builder()
                .code("etag_mismatch")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).body(error);
    }

    // 500 Internal Server Error
    @ExceptionHandler(ExchangeRateException.class)
    public ResponseEntity<ErrorResponseDto> handleExchangeRateException(ExchangeRateException ex) {
        ErrorResponseDto error = ErrorResponseDto.builder()
                .code("exchange_rate_error")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    // 500 Internal Server Error
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGeneralException(Exception ex) {
        ErrorResponseDto error = ErrorResponseDto.builder()
                .code("internal_error")
                .message("An unexpected error occurred: " + ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    // 401 Unauthorized
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponseDto> handleUnauthorizedException(UnauthorizedException ex) {
        ErrorResponseDto error = ErrorResponseDto.builder()
                .code("unauthorized")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    //403 Forbidden
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponseDto> handleForbiddenException(ForbiddenException ex) {
        ErrorResponseDto error = ErrorResponseDto.builder()
                .code("forbidden")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    //400 Bad Request
    @ExceptionHandler(InvalidDataException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidDataException(InvalidDataException ex) {
        ErrorResponseDto error = ErrorResponseDto.builder()
                .code("invalid_data")
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

}