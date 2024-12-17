package com.example.day_starter.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.day_starter.data.local.entity.DiaryEntity;

import java.util.List;

@Dao
public interface DiaryDao {
    @Query("SELECT * FROM diary WHERE date = :date")
    List<DiaryEntity> getDiariesByDate(String date);

    @Query("SELECT * FROM diary")
    List<DiaryEntity> getAllDiaries();

    @Insert
    long insert(DiaryEntity diary);

    @Update
    void update(DiaryEntity diary);

    @Delete
    void delete(DiaryEntity diary);

    @Query("DELETE FROM diary")
    void deleteAll();
}
