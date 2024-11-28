package com.example.day_starter.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.day_starter.data.local.entity.TodoEntity;

import java.util.List;

@Dao
public interface TodoDao {
    @Query("SELECT * FROM todos")
    List<TodoEntity> getAllTodos();

    @Query("SELECT * FROM todos WHERE date >= :startOfDay AND date < :endOfDay")
    List<TodoEntity> getTodosByDate(long startOfDay, long endOfDay);

    @Insert
    long insert(TodoEntity todo);

    @Update
    void update(TodoEntity todo);

    @Delete
    void delete(TodoEntity todo);
} 