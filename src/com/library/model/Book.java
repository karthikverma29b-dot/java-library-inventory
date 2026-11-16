package com.library.model;

import java.io.Serializable;

/**
 * Model class representing a Book in the Library Inventory.
 * Demonstrates OOP Encapsulation with private attributes, 
 * parameterized constructors, and public getter/setter methods.
 * 
 * @author B.Tech CSE - CE3 Project
 */
public class Book implements Serializable {
    private static final long serialVersionUID = 1L;

    // Encapsulated private attributes
    private String bookId;
    private String title;
    private String author;
    private int quantity;
    private boolean isAvailable;

    /**
     * Default constructor.
     */
    public Book() {
        this.bookId = "";
        this.title = "";
        this.author = "";
        this.quantity = 0;
        this.isAvailable = false;
    }

    /**
     * Parameterized constructor to initialize a Book object.
     *
     * @param bookId   Unique alphanumeric identifier for the book.
     * @param title    Title of the book.
     * @param author   Author of the book.
     * @param quantity Total quantity of the book currently in stock.
     */
    public Book(String bookId, String title, String author, int quantity) {
        this.bookId = bookId.trim();
        this.title = title.trim();
        this.author = author.trim();
        this.quantity = Math.max(0, quantity);
        this.isAvailable = this.quantity > 0;
    }

    /**
     * Overloaded constructor including explicit availability status.
     *
     * @param bookId      Unique identifier.
     * @param title       Title of the book.
     * @param author      Author of the book.
     * @param quantity    Available quantity.
     * @param isAvailable Availability boolean flag.
     */
    public Book(String bookId, String title, String author, int quantity, boolean isAvailable) {
        this.bookId = bookId.trim();
        this.title = title.trim();
        this.author = author.trim();
        this.quantity = Math.max(0, quantity);
        this.isAvailable = (this.quantity > 0) && isAvailable;
    }

    // Getters and Setters (Encapsulation)

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId != null ? bookId.trim() : "";
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title != null ? title.trim() : "";
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author != null ? author.trim() : "";
    }

    public int getQuantity() {
        return quantity;
    }

    /**
     * Updates the quantity and automatically syncs the availability status.
     *
     * @param quantity New non-negative quantity.
     */
    public void setQuantity(int quantity) {
        this.quantity = Math.max(0, quantity);
        this.isAvailable = this.quantity > 0;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        this.isAvailable = available && (this.quantity > 0);
    }

    /**
     * Decreases quantity by 1 when a book is issued.
     */
    public void issueOneCopy() {
        if (this.quantity > 0) {
            this.quantity--;
            this.isAvailable = this.quantity > 0;
        }
    }

    /**
     * Increases quantity by 1 when a book is returned.
     */
    public void returnOneCopy() {
        this.quantity++;
        this.isAvailable = true;
    }

    /**
     * Converts the Book instance into a comma-delimited CSV record for file storage.
     * Escapes commas and special characters in title and author if necessary.
     *
     * @return Formatted CSV record line.
     */
    public String toCsvString() {
        // Escape quotes and wrap in quotes if contains delimiter
        String safeTitle = title.replace("\"", "\"\"");
        String safeAuthor = author.replace("\"", "\"\"");
        return String.format("%s,\"%s\",\"%s\",%d,%b", bookId, safeTitle, safeAuthor, quantity, isAvailable);
    }

    /**
     * Parses a CSV formatted line back into a Book object.
     *
     * @param csvLine A single line from library_data.txt.
     * @return A constructed Book instance.
     * @throws IllegalArgumentException If line is malformed.
     */
    public static Book fromCsvString(String csvLine) throws IllegalArgumentException {
        if (csvLine == null || csvLine.trim().isEmpty()) {
            throw new IllegalArgumentException("Empty or null line cannot be parsed into a Book.");
        }

        // Parse line taking into account quoted fields
        String[] parts = parseCsvLine(csvLine);
        if (parts.length < 5) {
            throw new IllegalArgumentException("Invalid CSV record format: expected 5 fields, got " + parts.length);
        }

        String id = parts[0].trim();
        String title = parts[1].trim();
        String author = parts[2].trim();
        int qty = Integer.parseInt(parts[3].trim());
        boolean avail = Boolean.parseBoolean(parts[4].trim());

        return new Book(id, title, author, qty, avail);
    }

    /**
     * Helper to parse CSV lines with quoted strings containing commas.
     */
    private static String[] parseCsvLine(String line) {
        java.util.List<String> tokens = new java.util.ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '\"') {
                    sb.append('\"');
                    i++; // skip escaped quote
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                tokens.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        tokens.add(sb.toString());

        return tokens.toArray(new String[0]);
    }

    @Override
    public String toString() {
        return String.format("Book [ID=%s, Title='%s', Author='%s', Qty=%d, Available=%s]",
                bookId, title, author, quantity, isAvailable ? "Yes" : "No");
    }
}
