package com.example.day_starter.model;

public class Todo {
    private int id;
    private String title;
    private boolean isCompleted;
    private long date;

    public Todo(String title, long date) {
        this.title = title;
        this.date = date;
        this.isCompleted = false;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public long getDate() { return date; }
    public void setDate(long date) { this.date = date; }
} 