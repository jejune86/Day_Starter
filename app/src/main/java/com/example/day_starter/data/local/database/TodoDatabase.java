package com.example.day_starter.data.local.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.day_starter.data.local.dao.TodoDao;
import com.example.day_starter.data.local.entity.TodoEntity;

@Database(entities = {TodoEntity.class}, version = 2)
public abstract class TodoDatabase extends RoomDatabase {
    private static TodoDatabase instance;
    public abstract TodoDao todoDao();

    public static synchronized TodoDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                context.getApplicationContext(),
                TodoDatabase.class,
                "todo_database"
            )
            .fallbackToDestructiveMigration()
            .build();
        }
        return instance;
    }
} 