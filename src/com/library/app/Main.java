package com.library.app;

import com.library.exception.BookNotFoundException;
import com.library.exception.InvalidDataException;
import com.library.exception.OutOfStockException;
import com.library.model.Book;
import com.library.service.LibraryManager;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

/**
 * Main application class providing a robust, interactive console-based UI.
 * Designed for B.Tech CSE 3rd Semester Continuous Evaluation 3 (CE-3).
 * 
 * Demonstrates:
 * - Clean menu-driven loop
 * - Scanner input validation without crashes
 * - Explicit try-catch error handling
 * - Clean tabular display formatting
 * 
 * @author B.Tech CSE - CE3 Project
 */
public class Main {

    private static final String DATA_FILE = "library_data.txt";
    private static LibraryManager libraryManager;
    private static Scanner scanner;

    public static void main(String[] args) {
        scanner = new Scanner(System.in);
        libraryManager = new LibraryManager(DATA_FILE);

        printWelcomeBanner();

        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = readMenuChoice(1, 9);

            System.out.println();
            switch (choice) {
                case 1:
                    handleDisplayAllBooks();
                    break;
                case 2:
                    handleAddNewBook();
                    break;
                case 3:
                    handleSearchBook();
                    break;
                case 4:
                    handleIssueBook();
                    break;
                case 5:
                    handleReturnBook();
                    break;
                case 6:
                    handleUpdateBook();
                    break;
                case 7:
                    handleDeleteBook();
                    break;
                case 8:
                    handleInventoryStats();
                    break;
                case 9:
                    handleExit();
                    running = false;
                    break;
                default:
                    System.out.println("  [!] Invalid selection. Please choose an option from the menu.");
            }

            if (running) {
                pausePrompt();
            }
        }

        scanner.close();
    }

    // ==========================================
    // MENU ACTIONS
    // ==========================================

    /**
     * Displays all books currently in the library inventory in a formatted table.
     */
    private static void handleDisplayAllBooks() {
        System.out.println("=========================================================================================");
        System.out.println("                               LIBRARY BOOK INVENTORY                                    ");
        System.out.println("=========================================================================================");
        List<Book> books = libraryManager.getAllBooks();

        if (books.isEmpty()) {
            System.out.println("  The library inventory is currently empty.");
            return;
        }

        printTable(books);
    }

    /**
     * Collects book details from user and adds a new book to the inventory.
     */
    private static void handleAddNewBook() {
        System.out.println("-----------------------------------------------------------------------------------------");
        System.out.println("                                   ADD NEW BOOK                                          ");
        System.out.println("-----------------------------------------------------------------------------------------");

        try {
            String bookId = readRequiredString("Enter Book ID (e.g., B107): ");
            String title = readRequiredString("Enter Book Title: ");
            String author = readRequiredString("Enter Author Name: ");
            int quantity = readInt("Enter Initial Quantity (>= 0): ", 0, 10000);

            Book newBook = new Book(bookId, title, author, quantity);
            libraryManager.addBook(newBook);

            System.out.println("\n  [SUCCESS] Book added successfully!");
            System.out.println("  -> " + newBook);
        } catch (InvalidDataException e) {
            System.err.println("\n  [VALIDATION ERROR] " + e.getMessage());
        } catch (Exception e) {
            System.err.println("\n  [UNEXPECTED ERROR] An error occurred while adding book: " + e.getMessage());
        }
    }

    /**
     * Provides options to search by exact Book ID or by title/author keywords.
     */
    private static void handleSearchBook() {
        System.out.println("-----------------------------------------------------------------------------------------");
        System.out.println("                                   SEARCH BOOKS                                          ");
        System.out.println("-----------------------------------------------------------------------------------------");
        System.out.println("  1. Search by exact Book ID");
        System.out.println("  2. Search by Keyword (Title / Author / ID)");
        System.out.print("  Select search type (1-2): ");

        int type = readMenuChoice(1, 2);

        if (type == 1) {
            String bookId = readRequiredString("Enter Book ID to search: ");
            try {
                Book book = libraryManager.searchBookById(bookId);
                System.out.println("\n  [MATCH FOUND]");
                printSingleBookDetails(book);
            } catch (BookNotFoundException e) {
                System.err.println("\n  [NOT FOUND] " + e.getMessage());
            }
        } else {
            String keyword = readRequiredString("Enter search keyword: ");
            List<Book> matches = libraryManager.searchBooksByKeyword(keyword);
            if (matches.isEmpty()) {
                System.out.println("\n  No books matched keyword: \"" + keyword + "\"");
            } else {
                System.out.println("\n  Found " + matches.size() + " matching book(s):");
                printTable(matches);
            }
        }
    }

    /**
     * Issues a copy of a book to a borrower.
     */
    private static void handleIssueBook() {
        System.out.println("-----------------------------------------------------------------------------------------");
        System.out.println("                                  ISSUE A BOOK                                           ");
        System.out.println("-----------------------------------------------------------------------------------------");

        String bookId = readRequiredString("Enter Book ID to issue: ");

        try {
            libraryManager.issueBook(bookId);
            Book book = libraryManager.searchBookById(bookId);
            System.out.println("\n  [SUCCESS] Book issued successfully!");
            System.out.println("  Title: " + book.getTitle());
            System.out.println("  Remaining Copies: " + book.getQuantity() + " | Status: " + (book.isAvailable() ? "Available" : "Out of Stock"));
        } catch (BookNotFoundException e) {
            System.err.println("\n  [ERROR] " + e.getMessage());
        } catch (OutOfStockException e) {
            System.err.println("\n  [ERROR] " + e.getMessage());
        } catch (Exception e) {
            System.err.println("\n  [ERROR] An unexpected error occurred: " + e.getMessage());
        }
    }

    /**
     * Returns a previously issued copy of a book.
     */
    private static void handleReturnBook() {
        System.out.println("-----------------------------------------------------------------------------------------");
        System.out.println("                                  RETURN A BOOK                                          ");
        System.out.println("-----------------------------------------------------------------------------------------");

        String bookId = readRequiredString("Enter Book ID to return: ");

        try {
            libraryManager.returnBook(bookId);
            Book book = libraryManager.searchBookById(bookId);
            System.out.println("\n  [SUCCESS] Book returned successfully!");
            System.out.println("  Title: " + book.getTitle());
            System.out.println("  Updated Available Copies: " + book.getQuantity() + " | Status: Available");
        } catch (BookNotFoundException e) {
            System.err.println("\n  [ERROR] " + e.getMessage());
        } catch (Exception e) {
            System.err.println("\n  [ERROR] An unexpected error occurred: " + e.getMessage());
        }
    }

    /**
     * Updates details (title, author, quantity) for an existing book.
     */
    private static void handleUpdateBook() {
        System.out.println("-----------------------------------------------------------------------------------------");
        System.out.println("                               UPDATE BOOK DETAILS                                       ");
        System.out.println("-----------------------------------------------------------------------------------------");

        String bookId = readRequiredString("Enter Book ID to update: ");

        try {
            Book existing = libraryManager.searchBookById(bookId);
            System.out.println("\n  Current Details: " + existing);
            System.out.println("  (Enter new values or press Enter to keep unchanged)");

            System.out.print("  New Title [" + existing.getTitle() + "]: ");
            String title = scanner.nextLine().trim();
            if (title.isEmpty()) title = existing.getTitle();

            System.out.print("  New Author [" + existing.getAuthor() + "]: ");
            String author = scanner.nextLine().trim();
            if (author.isEmpty()) author = existing.getAuthor();

            System.out.print("  New Quantity [" + existing.getQuantity() + "]: ");
            String qtyStr = scanner.nextLine().trim();
            int qty = existing.getQuantity();
            if (!qtyStr.isEmpty()) {
                qty = Integer.parseInt(qtyStr);
            }

            libraryManager.updateBook(bookId, title, author, qty);
            System.out.println("\n  [SUCCESS] Book details updated successfully!");
            System.out.println("  -> " + libraryManager.searchBookById(bookId));
        } catch (BookNotFoundException e) {
            System.err.println("\n  [ERROR] " + e.getMessage());
        } catch (InvalidDataException e) {
            System.err.println("\n  [VALIDATION ERROR] " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("\n  [INPUT ERROR] Invalid integer entered for quantity. Update cancelled.");
        }
    }

    /**
     * Deletes a book from the inventory.
     */
    private static void handleDeleteBook() {
        System.out.println("-----------------------------------------------------------------------------------------");
        System.out.println("                                  DELETE A BOOK                                          ");
        System.out.println("-----------------------------------------------------------------------------------------");

        String bookId = readRequiredString("Enter Book ID to remove: ");

        try {
            Book existing = libraryManager.searchBookById(bookId);
            System.out.println("  Book to remove: " + existing.getTitle() + " by " + existing.getAuthor());
            System.out.print("  Are you sure you want to delete this record? (y/N): ");
            String confirm = scanner.nextLine().trim();

            if (confirm.equalsIgnoreCase("y") || confirm.equalsIgnoreCase("yes")) {
                libraryManager.deleteBook(bookId);
                System.out.println("\n  [SUCCESS] Book with ID '" + bookId + "' has been removed from inventory.");
            } else {
                System.out.println("\n  [CANCELLED] Deletion cancelled by user.");
            }
        } catch (BookNotFoundException e) {
            System.err.println("\n  [ERROR] " + e.getMessage());
        }
    }

    /**
     * Displays summary inventory statistics.
     */
    private static void handleInventoryStats() {
        System.out.println("=========================================================================================");
        System.out.println("                             INVENTORY SUMMARY & STATS                                   ");
        System.out.println("=========================================================================================");

        List<Book> all = libraryManager.getAllBooks();
        int totalTitles = all.size();
        int totalCopies = 0;
        int inStockTitles = 0;
        int outOfStockTitles = 0;

        for (Book b : all) {
            totalCopies += b.getQuantity();
            if (b.getQuantity() > 0) {
                inStockTitles++;
            } else {
                outOfStockTitles++;
            }
        }

        System.out.printf("  Total Unique Titles       : %d%n", totalTitles);
        System.out.printf("  Total Physical Copies     : %d%n", totalCopies);
        System.out.printf("  Titles Currently In Stock : %d%n", inStockTitles);
        System.out.printf("  Titles Out of Stock       : %d%n", outOfStockTitles);
        System.out.printf("  Storage File Location     : %s%n", DATA_FILE);
        System.out.println("=========================================================================================");
    }

    /**
     * Handles graceful shutdown and explicit data save.
     */
    private static void handleExit() {
        System.out.println("  Saving current inventory data to '" + DATA_FILE + "'...");
        try {
            libraryManager.saveToFile();
            System.out.println("  [SUCCESS] All records safely saved to disk.");
        } catch (IOException e) {
            System.err.println("  [ERROR] Failed to save inventory on shutdown: " + e.getMessage());
        }

        System.out.println("\n=========================================================================================");
        System.out.println("             Thank you for using the Library Book Inventory System!                      ");
        System.out.println("                           System safely exited.                                         ");
        System.out.println("=========================================================================================");
    }

    // ==========================================
    // UI & FORMATTING HELPERS
    // ==========================================

    private static void printWelcomeBanner() {
        System.out.println("=========================================================================================");
        System.out.println("                       LIBRARY BOOK INVENTORY MANAGEMENT SYSTEM                          ");
        System.out.println("                   B.Tech CSE - Continuous Evaluation 3 (CE-3)                           ");
        System.out.println("=========================================================================================");
        System.out.println("  Features: OOP Design | Collections (ArrayList) | Custom Exceptions | File Persistence");
        System.out.println("=========================================================================================\n");
    }

    private static void printMainMenu() {
        System.out.println("=========================================================================================");
        System.out.println("                                      MAIN MENU                                          ");
        System.out.println("=========================================================================================");
        System.out.println("  [1] View All Books in Inventory");
        System.out.println("  [2] Add New Book Record");
        System.out.println("  [3] Search Book (by ID or Keyword)");
        System.out.println("  [4] Issue a Book Copy");
        System.out.println("  [5] Return a Book Copy");
        System.out.println("  [6] Update Book Information");
        System.out.println("  [7] Delete a Book Record");
        System.out.println("  [8] View Inventory Statistics");
        System.out.println("  [9] Save & Exit");
        System.out.println("=========================================================================================");
        System.out.print("  Select an option [1-9]: ");
    }

    /**
     * Prints a nicely formatted ASCII table of books.
     */
    private static void printTable(List<Book> books) {
        String format = "| %-8s | %-36s | %-25s | %-8s | %-12s |%n";
        String line   = "+----------+--------------------------------------+---------------------------+----------+--------------+";

        System.out.println(line);
        System.out.printf("| %-8s | %-36s | %-25s | %-8s | %-12s |%n", "BOOK ID", "TITLE", "AUTHOR", "QUANTITY", "STATUS");
        System.out.println(line);

        for (Book b : books) {
            String title = truncate(b.getTitle(), 36);
            String author = truncate(b.getAuthor(), 25);
            String status = b.isAvailable() ? "AVAILABLE" : "OUT OF STOCK";
            System.out.printf(format, b.getBookId(), title, author, b.getQuantity(), status);
        }
        System.out.println(line);
        System.out.println("  Total items listed: " + books.size());
    }

    private static void printSingleBookDetails(Book book) {
        System.out.println("  --------------------------------------------------");
        System.out.println("  Book ID      : " + book.getBookId());
        System.out.println("  Title        : " + book.getTitle());
        System.out.println("  Author       : " + book.getAuthor());
        System.out.println("  Quantity     : " + book.getQuantity());
        System.out.println("  Status       : " + (book.isAvailable() ? "AVAILABLE" : "OUT OF STOCK"));
        System.out.println("  --------------------------------------------------");
    }

    private static String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }

    private static void pausePrompt() {
        System.out.println("\nPress [Enter] to return to the Main Menu...");
        scanner.nextLine();
    }

    // ==========================================
    // SAFE INPUT READERS (CRASH PREVENTION)
    // ==========================================

    /**
     * Safely reads an integer within range [min, max] preventing crashes when strings are entered.
     */
    private static int readMenuChoice(int min, int max) {
        while (true) {
            String input = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.printf("  [!] Please enter a number between %d and %d: ", min, max);
            } catch (NumberFormatException e) {
                System.out.printf("  [!] Invalid input. Please enter a valid number [%d-%d]: ", min, max);
            }
        }
    }

    /**
     * Safely reads an integer with custom prompt and min/max boundaries.
     */
    private static int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print("  " + prompt);
            String input = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.printf("  [!] Value must be between %d and %d.%n", min, max);
            } catch (NumberFormatException e) {
                System.out.println("  [!] Invalid input. Please enter a numeric integer value.");
            }
        }
    }

    /**
     * Reads a required non-empty string.
     */
    private static String readRequiredString(String prompt) {
        while (true) {
            System.out.print("  " + prompt);
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                return input;
            }
            System.out.println("  [!] This field cannot be left blank. Please try again.");
        }
    }
}
