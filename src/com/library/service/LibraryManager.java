package com.library.service;

import com.library.exception.BookNotFoundException;
import com.library.exception.InvalidDataException;
import com.library.exception.OutOfStockException;
import com.library.model.Book;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Service class that manages the library book inventory in-memory using an ArrayList<Book>,
 * and handles persistent file storage using BufferedReader and BufferedWriter.
 * 
 * Demonstrates:
 * - Java Collections Framework (ArrayList)
 * - Object-Oriented Principles (Encapsulation, Single Responsibility)
 * - File I/O Persistence (CSV reading and writing)
 * - Exception Handling (custom and checked I/O exceptions)
 * 
 * @author B.Tech CSE - CE3 Project
 */
public class LibraryManager {

    // Dynamic collection holding Book objects during runtime
    private final List<Book> books;

    // Path to the persistence file
    private final String dataFilePath;

    /**
     * Constructor initializing the inventory with a default storage file.
     * Automatically attempts to load existing records upon initialization.
     *
     * @param dataFilePath Path to the file where records are stored (e.g., "library_data.txt").
     */
    public LibraryManager(String dataFilePath) {
        this.books = new ArrayList<>();
        this.dataFilePath = dataFilePath;
        initializeInventory();
    }

    /**
     * Initializes the inventory by loading from file or seeding default data if file is absent.
     */
    private void initializeInventory() {
        File file = new File(dataFilePath);
        if (file.exists() && file.length() > 0) {
            try {
                loadFromFile();
                System.out.println("  [System] Successfully loaded " + books.size() + " book(s) from '" + dataFilePath + "'.");
            } catch (IOException e) {
                System.err.println("  [Warning] Could not load records from file: " + e.getMessage());
            }
        } else {
            // Seed sample books for immediate evaluation testing
            seedInitialBooks();
            try {
                saveToFile();
                System.out.println("  [System] Initialized fresh inventory with " + books.size() + " sample books into '" + dataFilePath + "'.");
            } catch (IOException e) {
                System.err.println("  [Warning] Could not save initial seed data: " + e.getMessage());
            }
        }
    }

    /**
     * Seeds realistic B.Tech CSE textbooks so the evaluator can test immediately.
     */
    private void seedInitialBooks() {
        books.add(new Book("B101", "Introduction to Algorithms", "Thomas H. Cormen", 4));
        books.add(new Book("B102", "Clean Code: A Handbook of Agile Software Craftsmanship", "Robert C. Martin", 2));
        books.add(new Book("B103", "Operating System Concepts", "Abraham Silberschatz", 3));
        books.add(new Book("B104", "Database System Concepts", "Abraham Silberschatz", 1));
        books.add(new Book("B105", "Computer Networks", "Andrew S. Tanenbaum", 0)); // Starts out of stock for testing
        books.add(new Book("B106", "Artificial Intelligence: A Modern Approach", "Stuart Russell", 5));
    }

    // ==========================================
    // CORE INVENTORY OPERATIONS
    // ==========================================

    /**
     * Adds a new Book to the inventory.
     * Validates that the book has a unique ID, non-empty fields, and non-negative quantity.
     *
     * @param newBook The Book object to add.
     * @throws InvalidDataException If validation fails (e.g., duplicate ID or empty title).
     */
    public synchronized void addBook(Book newBook) throws InvalidDataException {
        if (newBook == null) {
            throw new InvalidDataException("Cannot add a null book instance.");
        }
        if (newBook.getBookId().isEmpty()) {
            throw new InvalidDataException("Book ID cannot be empty or whitespace.");
        }
        if (newBook.getTitle().isEmpty()) {
            throw new InvalidDataException("Book Title cannot be empty.");
        }
        if (newBook.getAuthor().isEmpty()) {
            throw new InvalidDataException("Book Author cannot be empty.");
        }
        if (newBook.getQuantity() < 0) {
            throw new InvalidDataException("Book quantity cannot be negative.");
        }
        if (findBookByIdOptional(newBook.getBookId()) != null) {
            throw new InvalidDataException("A book with ID '" + newBook.getBookId() + "' already exists in inventory.");
        }

        books.add(newBook);
        autoSave();
    }

    /**
     * Searches for a book by its exact unique ID (case-insensitive).
     *
     * @param bookId The unique identifier of the book.
     * @return The matching Book object.
     * @throws BookNotFoundException If no book matches the ID.
     */
    public Book searchBookById(String bookId) throws BookNotFoundException {
        Book found = findBookByIdOptional(bookId);
        if (found == null) {
            throw new BookNotFoundException(bookId, "Search");
        }
        return found;
    }

    /**
     * Searches for books matching a title or author keyword (case-insensitive substring match).
     *
     * @param keyword Search query string.
     * @return List of matching books (may be empty).
     */
    public List<Book> searchBooksByKeyword(String keyword) {
        List<Book> results = new ArrayList<>();
        if (keyword == null || keyword.trim().isEmpty()) {
            return results;
        }

        String query = keyword.trim().toLowerCase();
        for (Book b : books) {
            if (b.getTitle().toLowerCase().contains(query) || 
                b.getAuthor().toLowerCase().contains(query) ||
                b.getBookId().toLowerCase().contains(query)) {
                results.add(b);
            }
        }
        return results;
    }

    /**
     * Issues one copy of the specified book.
     * Decrements the quantity by 1.
     *
     * @param bookId Unique identifier of the book to issue.
     * @throws BookNotFoundException If the book ID does not exist.
     * @throws OutOfStockException   If the book quantity is 0.
     */
    public synchronized void issueBook(String bookId) throws BookNotFoundException, OutOfStockException {
        Book book = searchBookById(bookId);
        if (book.getQuantity() <= 0 || !book.isAvailable()) {
            throw new OutOfStockException(book.getBookId(), book.getTitle());
        }

        book.issueOneCopy();
        autoSave();
    }

    /**
     * Returns one copy of the specified book.
     * Increments the quantity by 1 and marks it as available.
     *
     * @param bookId Unique identifier of the book to return.
     * @throws BookNotFoundException If the book ID does not exist.
     */
    public synchronized void returnBook(String bookId) throws BookNotFoundException {
        Book book = searchBookById(bookId);
        book.returnOneCopy();
        autoSave();
    }

    /**
     * Updates an existing book's title, author, and quantity.
     *
     * @param bookId      ID of the book to update.
     * @param newTitle    Updated title.
     * @param newAuthor   Updated author.
     * @param newQuantity Updated non-negative quantity.
     * @throws BookNotFoundException If book ID is not found.
     * @throws InvalidDataException  If updated fields are invalid.
     */
    public synchronized void updateBook(String bookId, String newTitle, String newAuthor, int newQuantity)
            throws BookNotFoundException, InvalidDataException {
        Book book = searchBookById(bookId);

        if (newTitle == null || newTitle.trim().isEmpty()) {
            throw new InvalidDataException("Book Title cannot be empty.");
        }
        if (newAuthor == null || newAuthor.trim().isEmpty()) {
            throw new InvalidDataException("Book Author cannot be empty.");
        }
        if (newQuantity < 0) {
            throw new InvalidDataException("Quantity cannot be negative.");
        }

        book.setTitle(newTitle);
        book.setAuthor(newAuthor);
        book.setQuantity(newQuantity);
        autoSave();
    }

    /**
     * Deletes a book from the inventory.
     *
     * @param bookId Unique identifier of the book to remove.
     * @return The removed Book object.
     * @throws BookNotFoundException If book is not found.
     */
    public synchronized Book deleteBook(String bookId) throws BookNotFoundException {
        Book book = searchBookById(bookId);
        books.remove(book);
        autoSave();
        return book;
    }

    /**
     * Retrieves an unmodifiable view of all books in the inventory.
     *
     * @return List of all Book objects.
     */
    public List<Book> getAllBooks() {
        return Collections.unmodifiableList(books);
    }

    /**
     * Returns the total number of unique book titles in the library.
     */
    public int getTotalUniqueBooks() {
        return books.size();
    }

    /**
     * Returns the total count of all book copies in stock.
     */
    public int getTotalCopiesCount() {
        int count = 0;
        for (Book b : books) {
            count += b.getQuantity();
        }
        return count;
    }

    /**
     * Helper to find a book without throwing an exception.
     */
    private Book findBookByIdOptional(String bookId) {
        if (bookId == null) return null;
        for (Book b : books) {
            if (b.getBookId().equalsIgnoreCase(bookId.trim())) {
                return b;
            }
        }
        return null;
    }

    // ==========================================
    // FILE PERSISTENCE (I/O)
    // ==========================================

    /**
     * Automatically saves data after modifying operations.
     * Catches IOException to prevent uncaught runtime termination while logging a warning.
     */
    private void autoSave() {
        try {
            saveToFile();
        } catch (IOException e) {
            System.err.println("  [Warning] Auto-save to '" + dataFilePath + "' failed: " + e.getMessage());
        }
    }

    /**
     * Saves the current book inventory to the text/CSV file using BufferedWriter and FileWriter.
     * Writes a header comment followed by each book's CSV representation.
     *
     * @throws IOException If an I/O error occurs during write operations.
     */
    public synchronized void saveToFile() throws IOException {
        File file = new File(dataFilePath);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))) {
            // Write CSV header
            writer.write("# BookId,Title,Author,Quantity,IsAvailable");
            writer.newLine();

            for (Book book : books) {
                writer.write(book.toCsvString());
                writer.newLine();
            }
        }
    }

    /**
     * Loads the book inventory from the text/CSV file using BufferedReader and FileReader.
     * Clears current in-memory list and repopulates it. Gracefully skips commented or malformed lines.
     *
     * @throws IOException If an error occurs reading the file.
     */
    public synchronized void loadFromFile() throws IOException {
        File file = new File(dataFilePath);
        if (!file.exists()) {
            return;
        }

        List<Book> loadedBooks = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();

                // Skip blank lines or comments
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                try {
                    Book parsedBook = Book.fromCsvString(line);
                    loadedBooks.add(parsedBook);
                } catch (IllegalArgumentException e) {
                    System.err.println("  [Warning] Skipped corrupt record at line " + lineNumber + ": " + e.getMessage());
                }
            }
        }

        this.books.clear();
        this.books.addAll(loadedBooks);
    }
}
