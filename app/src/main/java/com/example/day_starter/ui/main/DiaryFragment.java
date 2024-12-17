package com.example.day_starter.ui.main;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.day_starter.R;
import com.example.day_starter.data.local.entity.DiaryEntity;
import com.example.day_starter.data.repository.diary.DiaryRepository;
import com.example.day_starter.data.repository.todo.TodoRepository;
import com.example.day_starter.model.todo.Todo;
import com.example.day_starter.ui.adapter.DiaryAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DiaryFragment extends Fragment implements DiaryAdapter.DiaryListener {
    private RecyclerView recyclerView;
    private DiaryAdapter diaryAdapter;
    private DiaryRepository diaryRepository;
    private TodoRepository todoRepository;
    private FloatingActionButton fabAddDiary;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_diary, container, false);
        
        todoRepository = TodoRepository.getInstance(getActivity());
        recyclerView = view.findViewById(R.id.recycler_diary);
        diaryAdapter = new DiaryAdapter(todoRepository, getContext());
        diaryAdapter.setDiaryListener(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(diaryAdapter);
        fabAddDiary = view.findViewById(R.id.fab_add_diary);
        diaryRepository = DiaryRepository.getInstance(getActivity());

        loadDiaries();

        // FloatingActionButton 클릭 리스너 추가
        view.findViewById(R.id.fab_add_diary).setOnClickListener(v -> showAddDiaryDialog());

        return view;
    }

    public void loadDiaries() {
        diaryRepository.getAllDiaries(new DiaryRepository.DiaryCallback() {
            @Override
            public void onDiaryInserted(DiaryEntity diary) {
                loadDiaries(); 
            }

            @Override
            public void onDiariesLoaded(List<DiaryEntity> diaries) {
                // 다이어리 목록을 최근 날짜 기준으로 정렬
                diaries.sort((d1, d2) -> d2.getDate().compareTo(d1.getDate()));
                // UI 업데이트를 메인 스레드에서 수행
                getActivity().runOnUiThread(() -> diaryAdapter.setDiaries(diaries));
            }
        });
    }

    private void showAddDiaryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_diary, null);
        builder.setView(dialogView);

        EditText editTitle = dialogView.findViewById(R.id.edit_diary_title);
        EditText editContent = dialogView.findViewById(R.id.edit_diary_content);
        EditText editDate = dialogView.findViewById(R.id.edit_diary_date);
        Button buttonSave = dialogView.findViewById(R.id.button_save_diary);
        Button buttonCancel = dialogView.findViewById(R.id.button_cancel_diary);

        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        editDate.setText(todayDate);

        AlertDialog dialog = builder.create();
        
        buttonSave.setOnClickListener(v -> {
            String title = editTitle.getText().toString();
            String content = editContent.getText().toString();
            String date = editDate.getText().toString();

            if (!title.isEmpty() && !content.isEmpty()) {
                DiaryEntity newDiary = new DiaryEntity(date, title, content);
                diaryRepository.insertDiary(newDiary, new DiaryRepository.DiaryCallback() {
                    @Override
                    public void onDiaryInserted(DiaryEntity diary) {
                        loadDiaries(); // RecyclerView 업데이트
                    }

                    @Override
                    public void onDiariesLoaded(List<DiaryEntity> diaries) {
                        // 필요 없음
                    }
                });
                dialog.dismiss();
            }
        });

        buttonCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void loadTodosForDiary(DiaryEntity diary) {
        // 해당 날짜의 완료된 Todo를 로드
        todoRepository.getCompletedTodosByDate(diary.getDate(), todos -> {
            getActivity().runOnUiThread(() -> {
                LinearLayout todoContainer = recyclerView.findViewById(R.id.todo_container);
                todoContainer.removeAllViews(); // 기존 Todo 항목 제거

                for (Todo todo : todos) {
                    TextView todoTextView = new TextView(getContext());
                    todoTextView.setText(todo.getTitle());
                    todoTextView.setTextSize(16);
                    todoContainer.addView(todoTextView);
                }
            });
        });
    }

    @Override
    public void onDiaryEdit(DiaryEntity diary) {
        // 수정 다이얼로그를 띄우는 메서드 호출
        showEditDiaryDialog(diary);
    }

    private void showEditDiaryDialog(DiaryEntity diary) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_diary, null);
        builder.setView(dialogView);

        EditText editTitle = dialogView.findViewById(R.id.edit_diary_title);
        EditText editContent = dialogView.findViewById(R.id.edit_diary_content);
        EditText editDate = dialogView.findViewById(R.id.edit_diary_date);
        Button buttonSave = dialogView.findViewById(R.id.button_save_diary);
        Button buttonCancel = dialogView.findViewById(R.id.button_cancel_diary);

        // 기존 다이어리 정보로 EditText 초기화
        editTitle.setText(diary.getTitle());
        editContent.setText(diary.getContent());
        editDate.setText(diary.getDate());

        AlertDialog dialog = builder.create();

        buttonSave.setOnClickListener(v -> {
            String title = editTitle.getText().toString();
            String content = editContent.getText().toString();
            String date = editDate.getText().toString();

            if (!title.isEmpty() && !content.isEmpty()) {
                DiaryEntity updatedDiary = new DiaryEntity(date, title, content);
                updatedDiary.setId(diary.getId());
                diaryRepository.updateDiary(updatedDiary, new DiaryRepository.DiaryCallback() {
                    @Override
                    public void onDiaryInserted(DiaryEntity diary) {
                        loadDiaries(); // RecyclerView 업데이트
                    }

                    @Override
                    public void onDiariesLoaded(List<DiaryEntity> diaries) {
                        loadDiaries();
                    }
                });
                dialog.dismiss();
            }
        });

        buttonCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();

    }

    @Override
    public void onDiaryDelete(DiaryEntity diary) {
        diaryRepository.deleteDiary(diary, new DiaryRepository.DiaryCallback() {
            @Override
            public void onDiaryInserted(DiaryEntity diary) {
                // 필요 없음
            }

            @Override
            public void onDiariesLoaded(List<DiaryEntity> diaries) {
                loadDiaries(); // 다이어리 목록을 다시 로드하여 UI 업데이트
            }
        });
    }
} 