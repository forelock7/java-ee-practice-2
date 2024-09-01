package com.library.model;

public class Book {
    // Поля класу
    private int id;
    private String title;
    private String author;
    private int year;
    private String genre;

    // Конструктор без параметрів
    public Book() {
    }

    // Конструктор з параметрами
    public Book(int id, String title, String author, int year, String genre) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.year = year;
        this.genre = genre;
    }

    // Геттер для поля id
    public int getId() {
        return id;
    }

    // Сеттер для поля id
    public void setId(int id) {
        this.id = id;
    }

    // Геттер для поля title
    public String getTitle() {
        return title;
    }

    // Сеттер для поля title
    public void setTitle(String title) {
        this.title = title;
    }

    // Геттер для поля author
    public String getAuthor() {
        return author;
    }

    // Сеттер для поля author
    public void setAuthor(String author) {
        this.author = author;
    }

    // Геттер для поля year
    public int getYear() {
        return year;
    }

    // Сеттер для поля year
    public void setYear(int year) {
        this.year = year;
    }

    // Геттер для поля genre
    public String getGenre() {
        return genre;
    }

    // Сеттер для поля genre
    public void setGenre(String genre) {
        this.genre = genre;
    }

    // Перевизначення методу toString для зручного відображення інформації про книгу
    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", year=" + year +
                ", genre='" + genre + '\'' +
                '}';
    }
}

