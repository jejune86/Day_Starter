package com.example.day_starter.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.day_starter.R;
import com.example.day_starter.data.local.entity.DiaryEntity;
import com.example.day_starter.data.repository.todo.TodoRepository;
import com.example.day_starter.model.todo.Todo;


import java.util.List;

public class DiaryAdapter extends RecyclerView.Adapter<DiaryAdapter.DiaryViewHolder> {
    private List<DiaryEntity> diaries;
    private TodoRepository todoRepository;
    private Context context;
    private DiaryListener listener;
    
    public interface DiaryListener {
        void onDiaryEdit(DiaryEntity diary);
        void onDiaryDelete(DiaryEntity diary);
    }

    public void setDiaryListener(DiaryListener listener) {
        this.listener = listener;
    }

    public DiaryAdapter(TodoRepository todoRepository, Context context) {
        this.todoRepository = todoRepository;
        this.context = context;
    }

    public void setDiaries(List<DiaryEntity> diaries) {
        this.diaries = diaries;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DiaryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_diary, parent, false);
        return new DiaryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DiaryViewHolder holder, int position) {
        DiaryEntity diary = diaries.get(position);
        holder.titleTextView.setText(diary.getTitle());
        holder.dateTextView.setText(diary.getDate());
        holder.contentTextView.setText(diary.getContent());

        holder.itemView.setOnClickListener(v -> {
            if (holder.todoContainer.getVisibility() == View.GONE) {
                loadTodosForDiary(diary, holder);
                holder.todoContainer.setVisibility(View.VISIBLE);
                holder.contentTextView.setVisibility(View.VISIBLE);
            } else {
                holder.todoContainer.setVisibility(View.GONE);
                holder.contentTextView.setVisibility(View.GONE);
            }
        });

        holder.moreButton.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), holder.moreButton);
            popup.getMenu().add(Menu.NONE, 1, 1, "Edit");
            popup.getMenu().add(Menu.NONE, 2, 2, "Delete Diary");
        
            popup.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case 1: // 수정하기
                        if (listener != null) {
                            listener.onDiaryEdit(diary);
                        }
                        return true;
                    case 2: // 삭제
                        if (listener != null) {
                            listener.onDiaryDelete(diary);
                        }
                        return true;
                }
                return false;
            });
        
            popup.show();
        });
    }

    private void loadTodosForDiary(DiaryEntity diary, DiaryViewHolder holder) {
        todoRepository.getCompletedTodosByDate(diary.getDate(), todos -> {
            ((Activity) context).runOnUiThread(() -> {
                //holder.todoContainer.removeAllViews();
                for (Todo todo : todos) {
                    TextView todoTextView = new TextView(context);
                    todoTextView.setText("           -  "+todo.getTitle());
                    todoTextView.setTextColor(ContextCompat.getColor(context, R.color.md_theme_onBackground));
                    todoTextView.setTextSize(16);
                    todoTextView.setTypeface(null, Typeface.BOLD);
                    
                    // 마진 설정
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    params.setMargins(24, 0, 0, 8); // 왼쪽 16, 위 0, 오른쪽 0, 아래 8
                    todoTextView.setLayoutParams(params);
                    
                    holder.todoContainer.addView(todoTextView);
                }
            });
        });
    }

    @Override
    public int getItemCount() {
        return diaries != null ? diaries.size() : 0;
    }

    static class DiaryViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView dateTextView;
        TextView contentTextView;
        LinearLayout todoContainer;
        ImageButton moreButton;

        DiaryViewHolder(View itemView) {
            super(itemView);
            
            titleTextView = itemView.findViewById(R.id.text_diary_title);
            dateTextView = itemView.findViewById(R.id.text_diary_date);
            contentTextView = itemView.findViewById(R.id.text_diary_content);
            todoContainer = itemView.findViewById(R.id.todo_container);
            moreButton = itemView.findViewById(R.id.button_more);
        }
    }
} 