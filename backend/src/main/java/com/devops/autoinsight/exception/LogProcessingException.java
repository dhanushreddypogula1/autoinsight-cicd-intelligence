package com.devops.autoinsight.exception;

/**
 * Thrown when log file parsing or processing encounters a non-recoverable error.
 */
public class LogProcessingException extends RuntimeException {

    public LogProcessingException(String message) {
        super(message);
    }

    public LogProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
