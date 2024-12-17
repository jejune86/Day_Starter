package com.example.day_starter.data.repository.diary;

import android.content.Context;

import com.example.day_starter.data.local.dao.DiaryDao;
import com.example.day_starter.data.local.database.TodoDatabase;
import com.example.day_starter.data.local.entity.DiaryEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DiaryRepository {
    private DiaryDao diaryDao;
    private static DiaryRepository instance;
    private final ExecutorService executorService;

    private DiaryRepository(Context context) {
        Context applicationContext = context.getApplicationContext();
        TodoDatabase database = TodoDatabase.getInstance(applicationContext);
        diaryDao = database.diaryDao();
        executorService = Executors.newFixedThreadPool(4);
    }

    public static DiaryRepository getInstance(Context context) {
        if (instance == null) {
            instance = new DiaryRepository(context);
        }
        return instance;
    }

    public void insertDiary(DiaryEntity diary, DiaryCallback callback) {
        executorService.execute(() -> {
            long id = diaryDao.insert(diary);
            diary.setId((int) id);
            callback.onDiaryInserted(diary);
        });
    }

    public void getDiariesByDate(String date, DiaryCallback callback) {
        executorService.execute(() -> {
            List<DiaryEntity> diaries = diaryDao.getDiariesByDate(date);
            callback.onDiariesLoaded(diaries);
        });
    }

    public void getAllDiaries(DiaryCallback callback) {
        executorService.execute(() -> {
            List<DiaryEntity> diaries = diaryDao.getAllDiaries();
            callback.onDiariesLoaded(diaries);
        });
    }

    public void deleteAllDiaries(DiaryCallback callback) {
        executorService.execute(() -> {
            diaryDao.deleteAll();
            callback.onDiariesLoaded(null);
        });
    }

    public void deleteDiary(DiaryEntity diary, DiaryCallback callback) {
        executorService.execute(() -> {
            diaryDao.delete(diary);
            callback.onDiariesLoaded(null);
        });
    }

    public void updateDiary(DiaryEntity diary, DiaryCallback callback) {
        executorService.execute(() -> {
            diaryDao.update(diary);
            callback.onDiaryInserted(null);
        });
    }

    public interface DiaryCallback {
        void onDiaryInserted(DiaryEntity diary);
        void onDiariesLoaded(List<DiaryEntity> diaries);
    }
} 