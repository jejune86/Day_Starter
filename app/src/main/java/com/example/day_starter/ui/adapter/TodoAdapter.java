package com.example.day_starter.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.day_starter.R;
import com.example.day_starter.model.Todo;

import java.util.ArrayList;
import java.util.List;

import android.view.Menu;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.TodoViewHolder> {
    private List<Todo> todos = new ArrayList<>();
    private TodoListener listener;

    public interface TodoListener {
        void onTodoCheckedChanged(Todo todo, boolean isChecked);
        void onTodoDelete(Todo todo);
        void onTodoEdit(Todo todo);
        void onTodoMoveToTomorrow(Todo todo);
    }

    public void setTodoListener(TodoListener listener) {
        this.listener = listener;
    }

    public void setTodos(List<Todo> todos) {
        this.todos = new ArrayList<>(todos);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TodoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_todo, parent, false);
        return new TodoViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull TodoViewHolder holder, int position) {
        Todo todo = todos.get(position);
        holder.bind(todo);
    }

    @Override
    public int getItemCount() {
        return todos.size();
    }

    class TodoViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleText;
        private final CheckBox checkBox;
        private final ImageButton moreButton;
        private final TodoListener listener;

        TodoViewHolder(View itemView, TodoListener listener) {
            super(itemView);
            this.listener = listener;
            titleText = itemView.findViewById(R.id.text_todo_title);
            checkBox = itemView.findViewById(R.id.checkbox_todo);
            moreButton = itemView.findViewById(R.id.button_more);
        }

        void bind(Todo todo) {
            titleText.setText(todo.getTitle());
            checkBox.setChecked(todo.isCompleted());

            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onTodoCheckedChanged(todo, isChecked);
                }
            });

            moreButton.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(v.getContext(), moreButton);
                popup.getMenu().add(Menu.NONE, 1, 1, "수정하기");
                popup.getMenu().add(Menu.NONE, 2, 2, "내일하기");
                popup.getMenu().add(Menu.NONE, 3, 3, "삭제");
                
                popup.setOnMenuItemClickListener(item -> {
                    switch (item.getItemId()) {
                        case 1: // 수정하기
                            if (listener != null) {
                                listener.onTodoEdit(todo);
                            }
                            return true;
                        case 2: // 내일하기
                            if (listener != null) {
                                listener.onTodoMoveToTomorrow(todo);
                            }
                            return true;
                        case 3: // 삭제
                            if (listener != null) {
                                listener.onTodoDelete(todo);
                            }
                            return true;
                    }
                    return false;
                });
                
                popup.show();
            });
        }
    }
} 