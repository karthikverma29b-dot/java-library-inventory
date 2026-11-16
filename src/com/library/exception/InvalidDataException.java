package com.library.exception;

/**
 * Custom checked exception thrown when invalid book details are provided,
 * such as duplicate Book IDs, blank fields, or negative quantity values.
 */
public class InvalidDataException extends Exception {
    private static final long serialVersionUID = 1L;

    public InvalidDataException(String message) {
        super(message);
    }
}
