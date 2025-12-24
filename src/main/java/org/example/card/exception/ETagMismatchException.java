package org.example.card.exception;

public class ETagMismatchException extends RuntimeException {
    public ETagMismatchException(String message) {
        super(message);
    }
}
