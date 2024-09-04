package com.library.servlet;

import com.google.gson.Gson;
import com.library.model.Book;
import com.library.util.DatabaseConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

@WebServlet("/api/books/*")
public class ReadServlet extends HttpServlet {

    @Override
    public void init() throws ServletException {
        try {
            Class.forName("org.postgresql.Driver");
            // Тестове з'єднання з базою даних
            try (Connection connection = DatabaseConnection.getConnection()) {
                // Лог для успішного підключення
                System.out.println("Database connected successfully in init method");
            } catch (SQLException e) {
                throw new ServletException("Database connection test failed", e);
            }
        } catch (ClassNotFoundException e) {
            throw new ServletException("PostgreSQL JDBC Driver not found", e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Встановлюємо тип контенту як JSON
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Отримуємо шлях з URL
        String pathInfo = request.getPathInfo(); // Отримуємо шлях після "/api/books", тобто /{book_id}

        // Якщо шлях не переданий або порожній, повертаємо список книг
        if (pathInfo == null || pathInfo.equals("/")) {
            getAllBooks(response);
        } else {
            // Якщо переданий шлях, то отримуємо конкретну книгу за ID
            String[] pathParts = pathInfo.split("/");
            if (pathParts.length == 2) {
                try {
                    int bookId = Integer.parseInt(pathParts[1]);
                    getBookById(bookId, response);
                } catch (NumberFormatException e) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid book ID format");
                }
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid URL format");
            }
        }
    }

    private void getAllBooks(HttpServletResponse response) throws IOException {
        ArrayList<Book> bookList = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM public.books";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Book book = new Book(rs.getInt("id"), rs.getString("title"), rs.getString("author"), rs.getInt("year"), rs.getString("genre"));
                bookList.add(book);
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
            return;
        }

        // Перетворення списку книг у JSON формат
        String json = new Gson().toJson(bookList);

        PrintWriter out = response.getWriter();
        out.print(json);
        out.flush();
    }

    private void getBookById(int bookId, HttpServletResponse response) throws IOException {
        Book book = null;

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM public.books WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, bookId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                book = new Book(rs.getInt("id"), rs.getString("title"), rs.getString("author"), rs.getInt("year"), rs.getString("genre"));
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Book not found");
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
            return;
        }

        // Перетворення книги у JSON формат
        String json = new Gson().toJson(book);

        PrintWriter out = response.getWriter();
        out.print(json);
        out.flush();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try (BufferedReader reader = request.getReader();
             Connection conn = DatabaseConnection.getConnection()) {
            // Читаємо JSON з тіла запиту
            Book newBook = new Gson().fromJson(reader, Book.class);

            // Підготовка SQL-запиту для вставки нової книги
            String sql = "INSERT INTO public.books (title, author, year, genre, user_id) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, newBook.getTitle());
                stmt.setString(2, newBook.getAuthor());
                stmt.setInt(3, newBook.getYear());
                stmt.setString(4, newBook.getGenre());
                stmt.setInt(5, 1); // update after login func
                int rowsInserted = stmt.executeUpdate();

                if (rowsInserted > 0) {
                    response.setStatus(HttpServletResponse.SC_CREATED);
                    response.getWriter().write("{\"message\":\"Book added successfully\"}");
                } else {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    response.getWriter().write("{\"message\":\"Failed to add book\"}");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"message\":\"An error occurred while processing your request.\"}");
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Встановлюємо тип контенту як JSON
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Отримуємо ID книги з шляху
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Book ID is missing");
            return;
        }

        String[] pathParts = pathInfo.split("/");
        if (pathParts.length != 2) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid URL format");
            return;
        }

        int bookId;
        try {
            bookId = Integer.parseInt(pathParts[1]);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid book ID format");
            return;
        }

        // Читаємо всі поля з запиту (JSON)
        BufferedReader reader = request.getReader();
        Gson gson = new Gson();
        Book updatedBook = gson.fromJson(reader, Book.class);

        // Перевірка чи всі поля присутні в тілі запиту
        if (updatedBook.getTitle() == null || updatedBook.getAuthor() == null || updatedBook.getGenre() == null || updatedBook.getYear() == 0) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing required fields");
            return;
        }

        // Оновлення книги в базі даних
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE public.books SET title = ?, author = ?, genre = ?, year = ? WHERE id = ?";

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, updatedBook.getTitle());
            stmt.setString(2, updatedBook.getAuthor());
            stmt.setString(3, updatedBook.getGenre());
            stmt.setInt(4, updatedBook.getYear());
            stmt.setInt(5, bookId);

            int rowsUpdated = stmt.executeUpdate();

            if (rowsUpdated > 0) {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT); // 204 No Content, оновлення успішне
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Book not found");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
        }
    }


    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Отримати ідентифікатор книги з URL
        String pathInfo = request.getPathInfo(); // /{book_id}
        if (pathInfo == null || pathInfo.equals("/")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Book ID is missing");
            return;
        }

        String[] pathParts = pathInfo.split("/");
        if (pathParts.length != 2) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid URL format");
            return;
        }

        String bookIdStr = pathParts[1];
        int bookId;
        try {
            bookId = Integer.parseInt(bookIdStr);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid book ID format");
            return;
        }

        // Видалення книги з бази даних
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("DELETE FROM books WHERE id = ?")) {
            statement.setInt(1, bookId);
            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT); // 204 No Content
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Book not found");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
        }
    }
}

