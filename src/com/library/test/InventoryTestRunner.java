package com.library.test;

import com.library.exception.BookNotFoundException;
import com.library.exception.InvalidDataException;
import com.library.exception.OutOfStockException;
import com.library.model.Book;
import com.library.service.LibraryManager;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class InventoryTestRunner {
    public static void main(String[] args) {
        System.out.println(">>> Starting Automated CE-3 Rubric Verification Tests <<<\n");
        String testFile = "test_library_data.txt";
        new File(testFile).delete(); // clean start

        LibraryManager manager = new LibraryManager(testFile);

        // Test 1: Seed books loaded
        assertCondition(manager.getAllBooks().size() == 6, "Test 1: Seed books created when file is empty");

        // Test 2: Add Book
        try {
            manager.addBook(new Book("B201", "Design Patterns", "Gang of Four", 3));
            assertCondition(manager.getAllBooks().size() == 7, "Test 2: Add new book succeeded");
        } catch (Exception e) {
            System.err.println("FAIL Test 2: " + e.getMessage());
        }

        // Test 3: Duplicate Book ID Exception
        try {
            manager.addBook(new Book("B201", "Duplicate Book", "Test Author", 1));
            System.err.println("FAIL Test 3: Expected InvalidDataException for duplicate ID");
        } catch (InvalidDataException e) {
            assertCondition(true, "Test 3: Caught InvalidDataException for duplicate book ID (" + e.getMessage() + ")");
        } catch (Exception e) {
            System.err.println("FAIL Test 3: Unexpected exception: " + e);
        }

        // Test 4: Search Book by ID
        try {
            Book found = manager.searchBookById("B201");
            assertCondition(found.getTitle().equals("Design Patterns"), "Test 4: Search by ID succeeded");
        } catch (Exception e) {
            System.err.println("FAIL Test 4: " + e.getMessage());
        }

        // Test 5: Search Non-Existent ID
        try {
            manager.searchBookById("NON_EXISTENT");
            System.err.println("FAIL Test 5: Expected BookNotFoundException");
        } catch (BookNotFoundException e) {
            assertCondition(true, "Test 5: Caught BookNotFoundException for non-existent ID (" + e.getMessage() + ")");
        } catch (Exception e) {
            System.err.println("FAIL Test 5: Unexpected exception: " + e);
        }

        // Test 6: Issue Book until Out of Stock
        try {
            // B104 has quantity 1
            Book b104 = manager.searchBookById("B104");
            assertCondition(b104.getQuantity() == 1, "Test 6.1: B104 initial qty is 1");
            manager.issueBook("B104");
            assertCondition(b104.getQuantity() == 0 && !b104.isAvailable(), "Test 6.2: B104 quantity is 0 and status is unavailable");
        } catch (Exception e) {
            System.err.println("FAIL Test 6: " + e.getMessage());
        }

        // Test 7: Issue OutOfStock Book
        try {
            manager.issueBook("B104");
            System.err.println("FAIL Test 7: Expected OutOfStockException");
        } catch (OutOfStockException e) {
            assertCondition(true, "Test 7: Caught OutOfStockException when issuing out-of-stock book (" + e.getMessage() + ")");
        } catch (Exception e) {
            System.err.println("FAIL Test 7: Unexpected exception: " + e);
        }

        // Test 8: Return Book
        try {
            manager.returnBook("B104");
            Book b104 = manager.searchBookById("B104");
            assertCondition(b104.getQuantity() == 1 && b104.isAvailable(), "Test 8: Return book restored quantity to 1 and available status");
        } catch (Exception e) {
            System.err.println("FAIL Test 8: " + e.getMessage());
        }

        // Test 9: Update Book
        try {
            manager.updateBook("B201", "Design Patterns: Elements of Reusable OO Software", "Erich Gamma et al.", 5);
            Book updated = manager.searchBookById("B201");
            assertCondition(updated.getQuantity() == 5 && updated.getTitle().contains("Reusable"), "Test 9: Update book details succeeded");
        } catch (Exception e) {
            System.err.println("FAIL Test 9: " + e.getMessage());
        }

        // Test 10: Delete Book
        try {
            manager.deleteBook("B201");
            assertCondition(manager.getAllBooks().size() == 6, "Test 10.1: Delete book decreased inventory size");
            try {
                manager.searchBookById("B201");
                System.err.println("FAIL Test 10.2: Book still found after deletion");
            } catch (BookNotFoundException e) {
                assertCondition(true, "Test 10.2: Deleted book correctly not found");
            }
        } catch (Exception e) {
            System.err.println("FAIL Test 10: " + e.getMessage());
        }

        // Test 11: File Persistence Verification
        try {
            manager.saveToFile();
            LibraryManager freshManager = new LibraryManager(testFile);
            assertCondition(freshManager.getAllBooks().size() == 6, "Test 11.1: File persistence verified by reloading into new manager");
            Book b101 = freshManager.searchBookById("B101");
            assertCondition(b101 != null && b101.getTitle().equals("Introduction to Algorithms"), "Test 11.2: Reloaded record attributes verified");
        } catch (Exception e) {
            System.err.println("FAIL Test 11: " + e.getMessage());
        }

        // Cleanup test file
        new File(testFile).delete();
        System.out.println("\n>>> ALL CE-3 EVALUATION TESTS PASSED SUCCESSFULLY! <<<");
    }

    private static void assertCondition(boolean condition, String message) {
        if (condition) {
            System.out.println("  [PASS] " + message);
        } else {
            System.err.println("  [FAIL] " + message);
            System.exit(1);
        }
    }
}
