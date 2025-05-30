/**
 * The Library Management System is a simple Java console-based application
 * that manages library operations like adding, displaying, issuing, and returning books.
 * 
 * This project is built using Java SE and JDBC (which connects to a MySQL database
 * to store and manage data).
 * 
 * It contains two main entities:
 * - Librarians, who manage adding, deleting, and viewing book records.
 * - Users, who can register, log in, issue books, and return books.
 */

import java.sql.*;
import java.util.Scanner;

public class LibraryManagementSystem {

    static final String JDBC_URL = "jdbc:mysql://localhost:3306/librarydb";
    static final String USER = "root";  // Your MySQL username
    static final String PASS = "Ayush1234"; // Your MySQL password

    static Scanner scanner = new Scanner(System.in);
    static Connection conn;

    public static void main(String[] args) {
        try
         {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(JDBC_URL, USER, PASS);
            System.out.println("Connected to database.");
         
            while(true) {
                System.out.println("\n--- Library Management System ---");
                System.out.println("1. Librarian Login");
                System.out.println("2. User Login");
                System.out.println("3. User Register");
                System.out.println("4. Exit");
                System.out.print("Choose: ");
                int choice = Integer.parseInt(scanner.nextLine());

                switch(choice) {
                    case 1:
                        librarianMenu();
                        break;
                    case 2:
                        userLogin();
                        break;
                    case 3:
                        userRegister();
                        break;
                    case 4:
                        System.out.println("Goodbye!");
                        conn.close();
                        System.exit(0);
                    default:
                        System.out.println("Invalid choice.");
                }
            }

        }catch (ClassNotFoundException e) {
        System.out.println("MySQL JDBC Driver not found!");
        e.printStackTrace();
        }
         catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Librarian Menu
    static void librarianMenu() {
        while(true) {
            System.out.println("\n-- Librarian Menu --");
            System.out.println("1. Add Book");
            System.out.println("2. View Books");
            System.out.println("3. Delete Book");
            System.out.println("4. Logout");
            System.out.print("Choose: ");
            int choice = Integer.parseInt(scanner.nextLine());

            try {
                switch(choice) {
                    case 1:
                        addBook();
                        break;
                    case 2:
                        viewBooks();
                        break;
                    case 3:
                        deleteBook();
                        break;
                    case 4:
                        return;
                    default:
                        System.out.println("Invalid choice.");
                }
            } catch(SQLException e) {
                e.printStackTrace();
            }
        }
    }

    static void addBook() throws SQLException {
        System.out.print("Enter book title: ");
        String title = scanner.nextLine();
        System.out.print("Enter author name: ");
        String author = scanner.nextLine();

        String sql = "INSERT INTO books (title, author) VALUES (?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, title);
        stmt.setString(2, author);
        int rows = stmt.executeUpdate();

        if(rows > 0) {
            System.out.println("Book added successfully.");
        }
    }

    static void viewBooks() throws SQLException {
        String sql = "SELECT * FROM books";
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql);

        System.out.println("\nBook ID | Title | Author | Issued");
        while(rs.next()) {
            int id = rs.getInt("book_id");
            String title = rs.getString("title");
            String author = rs.getString("author");
            boolean issued = rs.getBoolean("is_issued");
            System.out.printf("%7d | %s | %s | %s\n", id, title, author, issued ? "Yes" : "No");
        }
    }

    static void deleteBook() throws SQLException {
        System.out.print("Enter book ID to delete: ");
        int bookId = Integer.parseInt(scanner.nextLine());

        String sql = "DELETE FROM books WHERE book_id = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, bookId);

        int rows = stmt.executeUpdate();
        if(rows > 0) {
            System.out.println("Book deleted successfully.");
        } else {
            System.out.println("Book not found.");
        }
    }

    // User Registration
    static void userRegister() throws SQLException {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, username);
        stmt.setString(2, password);

        int rows = stmt.executeUpdate();
        if(rows > 0) {
            System.out.println("User registered successfully.");
        }
    }

    // User Login & Menu
    static void userLogin() throws SQLException {
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        String sql = "SELECT user_id FROM users WHERE username = ? AND password = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, username);
        stmt.setString(2, password);

        ResultSet rs = stmt.executeQuery();

        if(rs.next()) {
            int userId = rs.getInt("user_id");
            System.out.println("Login successful.");
            userMenu(userId);
        } else {
            System.out.println("Invalid credentials.");
        }
    }

    static void userMenu(int userId) throws SQLException {
        while(true) {
            System.out.println("\n-- User Menu --");
            System.out.println("1. View Books");
            System.out.println("2. Issue Book");
            System.out.println("3. Return Book");
            System.out.println("4. Logout");
            System.out.print("Choose: ");
            int choice = Integer.parseInt(scanner.nextLine());

            switch(choice) {
                case 1:
                    viewBooks();
                    break;
                case 2:
                    issueBook(userId);
                    break;
                case 3:
                    returnBook(userId);
                    break;
                case 4:
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    static void issueBook(int userId) throws SQLException {
        System.out.print("Enter Book ID to issue: ");
        int bookId = Integer.parseInt(scanner.nextLine());

        // Check if book is available
        String checkSql = "SELECT is_issued FROM books WHERE book_id = ?";
        PreparedStatement checkStmt = conn.prepareStatement(checkSql);
        checkStmt.setInt(1, bookId);
        ResultSet rs = checkStmt.executeQuery();

        if(rs.next()) {
            boolean isIssued = rs.getBoolean("is_issued");
            if(isIssued) {
                System.out.println("Book is already issued.");
                return;
            }
        } else {
            System.out.println("Book not found.");
            return;
        }

        // Issue book
        String issueSql = "INSERT INTO issued_books (user_id, book_id, issue_date) VALUES (?, ?, CURDATE())";
        PreparedStatement issueStmt = conn.prepareStatement(issueSql);
        issueStmt.setInt(1, userId);
        issueStmt.setInt(2, bookId);
        issueStmt.executeUpdate();

        // Update book status
        String updateSql = "UPDATE books SET is_issued = TRUE WHERE book_id = ?";
        PreparedStatement updateStmt = conn.prepareStatement(updateSql);
        updateStmt.setInt(1, bookId);
        updateStmt.executeUpdate();

        System.out.println("Book issued successfully.");
    }

    static void returnBook(int userId) throws SQLException {
        System.out.print("Enter Book ID to return: ");
        int bookId = Integer.parseInt(scanner.nextLine());

        // Check if book is issued to user
        String checkSql = "SELECT issue_id FROM issued_books WHERE user_id = ? AND book_id = ? AND return_date IS NULL";
        PreparedStatement checkStmt = conn.prepareStatement(checkSql);
        checkStmt.setInt(1, userId);
        checkStmt.setInt(2, bookId);
        ResultSet rs = checkStmt.executeQuery();

        if(rs.next()) {
            int issueId = rs.getInt("issue_id");

            // Update return date
            String returnSql = "UPDATE issued_books SET return_date = CURDATE() WHERE issue_id = ?";
            PreparedStatement returnStmt = conn.prepareStatement(returnSql);
            returnStmt.setInt(1, issueId);
            returnStmt.executeUpdate();

            // Update book status
            String updateSql = "UPDATE books SET is_issued = FALSE WHERE book_id = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateSql);
            updateStmt.setInt(1, bookId);
            updateStmt.executeUpdate();

            System.out.println("Book returned successfully.");
        } else {
            System.out.println("No record of this book issued to you.");
        }
    }
}
