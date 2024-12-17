package com.example.day_starter.ui.main;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;

import com.example.day_starter.R;
import com.example.day_starter.data.local.entity.DiaryEntity;
import com.example.day_starter.data.repository.diary.DiaryRepository;
import com.example.day_starter.data.repository.todo.TodoRepository;

import org.threeten.bp.LocalDate;

import java.util.List;

public class MenuFragment extends Fragment {

    private DiaryRepository diaryRepository;
    private TodoRepository todoRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_menu, container, false);

        // 예시로 텍스트뷰 설정
        TextView textView = view.findViewById(R.id.menu_text);


        // 리셋 버튼 추가
        Button buttonTodoReset = view.findViewById(R.id.button_todo_reset);
        Button buttonDiaryReset = view.findViewById(R.id.button_diary_reset);
        buttonTodoReset.setOnClickListener(v -> showTodoResetConfirmationDialog());
        buttonDiaryReset.setOnClickListener(v -> showDiaryResetConfirmationDialog());
        diaryRepository = DiaryRepository.getInstance(getActivity());
        todoRepository = TodoRepository.getInstance(getActivity());

        return view;
    }

    private void resetDiaryData() {
        diaryRepository.deleteAllDiaries(new DiaryRepository.DiaryCallback() {
            @Override
            public void onDiaryInserted(DiaryEntity diary) {
                // 필요 없음
            }

            @Override
            public void onDiariesLoaded(List<DiaryEntity> diaries) {
                // 다이어리 RecyclerView 업데이트
                DiaryFragment diaryFragment = (DiaryFragment) getActivity().getSupportFragmentManager().findFragmentByTag("f" + 2); // ViewPager에서 DiaryFragment의 태그를 사용
                if (diaryFragment != null) {
                    diaryFragment.loadDiaries(); // 다이어리 목록을 새로 고침
                }
            }
        });
    }

    private void resetTodoData() {
        // 할 일 초기화
        todoRepository.deleteAllTodos(todos -> {
            MainFragment mainFragment = (MainFragment) getActivity().getSupportFragmentManager().findFragmentByTag("f" + 1); // ViewPager에서 MainFragment의 태그를 사용
            if (mainFragment != null) {
                mainFragment.loadTodosByDate(LocalDate.now(), mainFragment.getTodoAdapter()); // 할 일 목록을 새로 고침
            }
        });
    }

    private void showTodoResetConfirmationDialog() {
        new AlertDialog.Builder(getActivity())
            .setTitle("Confirm Reset")
            .setMessage("Are you sure you want to reset todo?")
            .setPositiveButton("Yes", (dialog, which) -> {
                resetTodoData();
            })
            .setNegativeButton("No", null)
            .show();
    }

    private void showDiaryResetConfirmationDialog() {
        new AlertDialog.Builder(getActivity())
            .setTitle("Confirm Reset")
            .setMessage("Are you sure you want to reset diary?")
            .setPositiveButton("Yes", (dialog, which) -> {
                resetDiaryData();
            })
            .setNegativeButton("No", null)
            .show();
    }
} 