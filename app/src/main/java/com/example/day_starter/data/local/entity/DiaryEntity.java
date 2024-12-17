package com.example.day_starter.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "diary")
public class DiaryEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String date;
    private String title;
    private String content;

    public DiaryEntity(String date, String title, String content) {
        this.date = date;
        this.title = title;
        this.content = content;
    }

    // Getter와 Setter 메서드
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }
}
