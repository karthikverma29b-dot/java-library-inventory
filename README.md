# 📚 Library Book Inventory Management System
> **Continuous Evaluation 3 (CE-3) Project | B.Tech CSE (3rd Semester)**  
> **Course:** Object-Oriented Programming with Java  
> **Repository Structure:** Modular Multi-Package Architecture  

---

## 📌 1. Problem Statement & Objectives

Managing books manually in institutional libraries often leads to misplaced stock, unrecorded borrowings, duplicate records, and data loss upon application restart. 

The **Library Book Inventory System** is a robust, console-based Java application engineered for librarians and students to:
1. **Manage Inventory:** Add, update, view, and delete book records dynamically.
2. **Borrow & Return:** Issue books with stock decrement validation, and return books to restore stock.
3. **Search Records:** Locate books instantly by unique identifier or by keyword search across title and author.
4. **Resilient Data Persistence:** Automatically save records to a permanent flat-file (`library_data.txt`) using buffered I/O streams and reload them on startup.
5. **Crash-Resistant Experience:** Handle invalid inputs and domain errors gracefully through custom exception hierarchies and safe Scanner parsing.

---

## 🏗️ 2. Object-Oriented Architecture & Rubric Mapping

| Rubric Criteria | Implementation in Project | Description / Location |
| :--- | :--- | :--- |
| **Encapsulation** | `Book.java` | Private fields (`bookId`, `title`, `author`, `quantity`, `isAvailable`), public getters/setters, state validation. |
| **Inheritance** | Custom Exceptions | `BookNotFoundException`, `OutOfStockException`, and `InvalidDataException` extend `java.lang.Exception`. |
| **Polymorphism** | Overridden Methods | Overriding `toString()` in `Book` and method overloading across constructors. |
| **Collections Framework** | `ArrayList<Book>` | In-memory dynamic list inside `LibraryManager` providing O(1) random access and dynamic sizing. |
| **Exception Handling** | `try-catch-finally` | Robust guards against `NumberFormatException`, `InputMismatchException`, file I/O errors, and custom exceptions. |
| **File Persistence** | Buffered I/O Streams | `BufferedReader`/`FileReader` for reading and `BufferedWriter`/`FileWriter` for atomic writing to `library_data.txt`. |

---

## 📁 3. Directory & Package Structure

```
Java Project Eval/
├── src/
│   └── com/
│       └── library/
│           ├── app/
│           │   └── Main.java                 # Interactive CLI menu & safe user input handling
│           ├── model/
│           │   └── Book.java                 # Encapsulated Book entity model & CSV serializer
│           ├── service/
│           │   └── LibraryManager.java       # Inventory business logic, Collections & File I/O
│           ├── exception/
│           │   ├── BookNotFoundException.java # Thrown when book ID does not exist
│           │   ├── OutOfStockException.java   # Thrown when copies reach 0 during issue
│           │   └── InvalidDataException.java  # Thrown on duplicate IDs or malformed inputs
│           └── test/
│               └── InventoryTestRunner.java  # Automated CE-3 verification test suite
├── bin/                                      # Compiled bytecodes (.class files)
├── library_data.txt                          # Persistent CSV text file database
└── README.md                                 # Complete project documentation & rubric mapping
```

---

## ⚙️ 4. Class Responsibilities

### 1. `com.library.model.Book`
- **Fields:**
  - `private String bookId`: Unique identifier (e.g., `B101`).
  - `private String title`: Book title.
  - `private String author`: Book author.
  - `private int quantity`: Current physical copies available.
  - `private boolean isAvailable`: Synchronized boolean status flag (`quantity > 0`).
- **Key Methods:**
  - `issueOneCopy()`: Decrements copy count and toggles `isAvailable` if count reaches `0`.
  - `returnOneCopy()`: Increments copy count and marks `isAvailable` as `true`.
  - `toCsvString()` / `fromCsvString(String line)`: Handles CSV serialization and quotes escaping.

### 2. `com.library.service.LibraryManager`
- **Fields:**
  - `private final List<Book> books`: Dynamic `ArrayList<Book>` holding the active inventory.
  - `private final String dataFilePath`: Path to the persistent storage file (`library_data.txt`).
- **Key Methods:**
  - `addBook(Book book)`: Validates uniqueness and non-negative quantity before insertion.
  - `searchBookById(String bookId)`: Exact match search throwing `BookNotFoundException`.
  - `searchBooksByKeyword(String keyword)`: Case-insensitive search across ID, title, and author.
  - `issueBook(String bookId)`: Decrements stock; throws `OutOfStockException` if quantity is `0`.
  - `returnBook(String bookId)`: Restores stock and auto-saves to disk.
  - `updateBook(...)` & `deleteBook(...)`: Modifies or removes records and persists state.
  - `saveToFile()` & `loadFromFile()`: Reads and writes records via `BufferedReader` and `BufferedWriter`.

### 3. `com.library.app.Main`
- **Menu-Driven Loop:** Presents a 9-option interactive menu.
- **Crash Prevention:**
  - `readMenuChoice(int min, int max)`: Traps non-numeric string inputs using `NumberFormatException` loops without crashing or skipping lines.
  - `readRequiredString(String prompt)`: Prevents empty or blank field submissions.

### 4. Custom Exception Classes (`com.library.exception`)
- `BookNotFoundException`: Indicates that an queried Book ID was absent.
- `OutOfStockException`: Indicates that no physical copies remain for lending.
- `InvalidDataException`: Indicates duplicate primary keys, empty fields, or negative inputs.

---

## 🚀 5. Compilation & Execution Guide

### Prerequisites
- Java Development Kit (JDK 8 or higher; tested on OpenJDK 21 LTS).
- Any standard terminal (PowerShell, Command Prompt, or Bash).

### Option A: Running from the Command Line (Terminal / PowerShell / CMD)

1. Open a terminal in the root directory:
   ```powershell
   cd "c:\Users\karth\OneDrive\Desktop\Java Project Eval"
   ```

2. Compile all source files into the `bin/` directory:
   ```powershell
   javac -d bin -sourcepath src src/com/library/app/Main.java src/com/library/test/InventoryTestRunner.java
   ```
   > **Note on Windows:** If compiling packages manually using wildcards (`*`), Windows requires backslashes (`\`) instead of forward slashes:
   > ```powershell
   > javac -d bin src\com\library\model\*.java src\com\library\exception\*.java src\com\library\service\*.java src\com\library\app\*.java src\com\library\test\*.java
   > ```

3. Run the interactive application:
   ```powershell
   java -cp bin com.library.app.Main
   ```

4. Run the automated rubric test suite:
   ```powershell
   java -cp bin com.library.test.InventoryTestRunner
   ```

---

## 🧪 6. Sample Test Scenarios & Expected Behavior

### Scenario 1: Displaying Inventory
Select Option `[1]`:
```text
+----------+--------------------------------------+---------------------------+----------+--------------+
| BOOK ID  | TITLE                                | AUTHOR                    | QUANTITY | STATUS       |
+----------+--------------------------------------+---------------------------+----------+--------------+
| B101     | Introduction to Algorithms           | Thomas H. Cormen          | 4        | AVAILABLE    |
| B102     | Clean Code: A Handbook of Agile S... | Robert C. Martin          | 2        | AVAILABLE    |
| B103     | Operating System Concepts            | Abraham Silberschatz      | 3        | AVAILABLE    |
| B104     | Database System Concepts             | Abraham Silberschatz      | 1        | AVAILABLE    |
| B105     | Computer Networks                    | Andrew S. Tanenbaum       | 0        | OUT OF STOCK |
| B106     | Artificial Intelligence: A Modern... | Stuart Russell            | 5        | AVAILABLE    |
+----------+--------------------------------------+---------------------------+----------+--------------+
```

### Scenario 2: Issuing an Out-of-Stock Book
Select Option `[4]` and enter ID `B105`:
```text
Enter Book ID to issue: B105

  [ERROR] Out of Stock: Cannot issue book 'Computer Networks' (ID: B105). All copies are currently issued.
```
*Validation:* Handled cleanly by `OutOfStockException` without program termination.

### Scenario 3: Non-Numeric Input Prevention
At the main menu prompt:
```text
Select an option [1-9]: hello
  [!] Invalid input. Please enter a valid number [1-9]: 12
  [!] Please enter a number between 1 and 9: 1
```
*Validation:* Safe scanner parsing loop catches non-integers without crashing.

### Scenario 4: Data Persistence (`library_data.txt`)
Whenever a book is added, issued, returned, updated, or when the user selects `[9] Exit`, records are committed to `library_data.txt`:
```csv
# BookId,Title,Author,Quantity,IsAvailable
B101,"Introduction to Algorithms","Thomas H. Cormen",4,true
B102,"Clean Code: A Handbook of Agile Software Craftsmanship","Robert C. Martin",2,true
B103,"Operating System Concepts","Abraham Silberschatz",3,true
B104,"Database System Concepts","Abraham Silberschatz",1,true
B105,"Computer Networks","Andrew S. Tanenbaum",0,false
B106,"Artificial Intelligence: A Modern Approach","Stuart Russell",5,true
```
When restarted, all changes (borrowed status, remaining quantity, new titles) are restored immediately.

---

## 👨‍💻 7. Evaluation Viva Questions & Answers

**Q1: Why use an `ArrayList` instead of a static array?**  
*Answer:* Static arrays in Java have fixed memory boundaries defined at declaration. A library inventory dynamically grows and shrinks as books are added or deleted. `ArrayList<Book>` resizes automatically and provides standard collection methods like `add()`, `remove()`, and fast O(1) indexed iteration.

**Q2: How does the application handle input mismatches when reading numbers?**  
*Answer:* Instead of invoking `scanner.nextInt()` directly (which leaves a trailing newline that disrupts subsequent `scanner.nextLine()` calls and throws unhandled `InputMismatchException`), the app reads the line as a `String` and uses `Integer.parseInt(input)` inside a `try-catch (NumberFormatException)` loop until a valid integer in range is supplied.

**Q3: How is data persisted and restored?**  
*Answer:* Using Java's character-based streams with buffering: `BufferedWriter` and `FileWriter` write formatted CSV strings to `library_data.txt`. On startup, `BufferedReader` and `FileReader` read line-by-line, parse each token, and reconstruct `Book` objects via `Book.fromCsvString()`.

---

## 📄 License
Academic Project submission for B.Tech Computer Science and Engineering.
