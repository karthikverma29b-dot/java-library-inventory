package com.library.exception;

/**
 * Custom checked exception thrown when an attempt is made to issue a book
 * that has zero available copies in stock.
 * 
 * Demonstrates domain-specific error handling for the Library Management System.
 */
public class OutOfStockException extends Exception {
    private static final long serialVersionUID = 1L;

    public OutOfStockException(String message) {
        super(message);
    }

    public OutOfStockException(String bookId, String title) {
        super(String.format("Out of Stock: Cannot issue book '%s' (ID: %s). All copies are currently issued.", title, bookId));
    }
}
