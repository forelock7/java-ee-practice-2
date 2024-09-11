package com.library.servlet;

import com.google.gson.Gson;
import com.library.model.UserCredentials;
import com.library.util.DatabaseConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet("/api/login")
public class LoginServlet extends HttpServlet {

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
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Перевіряємо тип контенту запиту
        if (!request.getContentType().equalsIgnoreCase("application/json")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Expected application/json content type");
            return;
        }

        // Читаємо тіло запиту (JSON)
        StringBuilder jsonBuffer = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuffer.append(line);
            }
        }

        // Перетворюємо JSON у об'єкт
        String jsonString = jsonBuffer.toString();
        Gson gson = new Gson();
        UserCredentials credentials = gson.fromJson(jsonString, UserCredentials.class); // UserCredentials - клас для збереження email і password

        String email = credentials.getEmail();
        String password = credentials.getPassword();

        // Перевіряємо користувача в базі даних
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM public.users WHERE email = ? AND password = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);
            stmt.setString(2, password); // Пряме порівняння для простоти, але краще використовувати хешування паролів

            ResultSet rs = stmt.executeQuery();

            response.setContentType("application/json");
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            PrintWriter out = response.getWriter();
            if (rs.next()) {
                // Успішний вхід
                response.setStatus(HttpServletResponse.SC_OK); // 200 OK
                out.print("{\"success\": true}");
            } else {
                // Невірний email або пароль
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 Unauthorized
                out.print("{\"success\": false, \"message\": \"Invalid email or password\"}");
            }
            out.flush();

            out.flush();
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Database error");
        }
    }

}

