package com.library.exception;

/**
 * Custom checked exception thrown when an operation references a Book ID
 * that does not exist in the library inventory.
 * 
 * Demonstrates custom exception handling and inheritance in Java (CE-3).
 */
public class BookNotFoundException extends Exception {
    private static final long serialVersionUID = 1L;

    public BookNotFoundException(String message) {
        super(message);
    }

    public BookNotFoundException(String bookId, String operation) {
        super(String.format("Error during %s: Book with ID '%s' was not found in the inventory.", operation, bookId));
    }
}
