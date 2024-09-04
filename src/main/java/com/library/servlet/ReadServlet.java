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

@WebServlet("/api/books")
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
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        // Встановлюємо тип контенту як JSON
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

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
        }


        // Перетворення списку книг у JSON формат
        String json = new Gson().toJson(bookList);

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
}

