package com.devops.autoinsight.exception;

/**
 * Thrown when an uploaded file is invalid (wrong type, too large, empty, etc.)
 */
public class InvalidFileException extends RuntimeException {

    public InvalidFileException(String message) {
        super(message);
    }
}
