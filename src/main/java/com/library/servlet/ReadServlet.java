package com.library.servlet;

import com.library.model.Book;
import com.library.util.DatabaseConnection;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

@WebServlet("/api/books")
public class ReadServlet extends HttpServlet {
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
        String json = new com.google.gson.Gson().toJson(bookList);

        PrintWriter out = response.getWriter();
        out.print(json);
        out.flush();

    }
}

