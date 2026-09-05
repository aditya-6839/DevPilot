package com.devPilot.backend.exception;

public class InvalidOAuthUserException extends RuntimeException {

    public InvalidOAuthUserException(String message) {
        super(message);
    }

    public InvalidOAuthUserException(String message, Throwable cause) {
        super(message, cause);
    }
}