package com.example.day_starter.data.repository.todo;

import android.content.Context;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.example.day_starter.data.local.dao.TodoDao;
import com.example.day_starter.data.local.database.TodoDatabase;
import com.example.day_starter.data.local.entity.TodoEntity;
import com.example.day_starter.model.todo.Todo;

public class TodoRepository {
    private TodoDao todoDao;
    private static TodoRepository instance;
    private final ExecutorService executorService;

    private TodoRepository(Context context) {
        TodoDatabase database = TodoDatabase.getInstance(context);
        todoDao = database.todoDao();
        executorService = Executors.newFixedThreadPool(4);
    }

    public static TodoRepository getInstance(Context context) {
        if (instance == null) {
            instance = new TodoRepository(context);
        }
        return instance;
    }

    public void insertTodo(Todo todo, TodoCallback callback) {
        TodoEntity entity = new TodoEntity(todo.getTitle(), todo.getDate());
        executorService.execute(() -> {
            long id = todoDao.insert(entity);
            entity.setId((int)id);
            List<TodoEntity> entities = todoDao.getTodosByDate(todo.getDate());
            List<Todo> todos = entities.stream()
                .map(e -> {
                    Todo t = new Todo(e.getTitle(), e.getDate());
                    t.setId(e.getId());
                    t.setCompleted(e.isCompleted());
                    return t;
                })
                .collect(java.util.stream.Collectors.toList());
            callback.onTodosLoaded(todos);
        });
    }

    public void updateTodo(Todo todo, TodoCallback callback) {
        TodoEntity entity = new TodoEntity(todo.getTitle(), todo.getDate());
        entity.setId(todo.getId());
        entity.setCompleted(todo.isCompleted());
        executorService.execute(() -> {
            todoDao.update(entity);
            List<TodoEntity> entities = todoDao.getTodosByDate(todo.getDate());
            List<Todo> todos = entities.stream()
                .map(e -> {
                    Todo t = new Todo(e.getTitle(), e.getDate());
                    t.setId(e.getId());
                    t.setCompleted(e.isCompleted());
                    return t;
                })
                .collect(java.util.stream.Collectors.toList());
            callback.onTodosLoaded(todos);
        });
    }

    public void deleteTodo(Todo todo, TodoCallback callback) {
        TodoEntity entity = new TodoEntity(todo.getTitle(), todo.getDate());
        entity.setId(todo.getId());
        executorService.execute(() -> {
            todoDao.delete(entity);
            List<TodoEntity> entities = todoDao.getTodosByDate(todo.getDate());
            List<Todo> todos = entities.stream()
                .map(e -> {
                    Todo t = new Todo(e.getTitle(), e.getDate());
                    t.setId(e.getId());
                    t.setCompleted(e.isCompleted());
                    return t;
                })
                .collect(java.util.stream.Collectors.toList());
            callback.onTodosLoaded(todos);
        });
    }

    public interface TodoCallback {
        void onTodosLoaded(List<Todo> todos);
    }

    public void getAllTodos(TodoCallback callback) {
        executorService.execute(() -> {
            List<TodoEntity> entities = todoDao.getAllTodos();
            List<Todo> todos = entities.stream()
                .map(entity -> {
                    Todo todo = new Todo(entity.getTitle(), entity.getDate());
                    todo.setId(entity.getId());
                    todo.setCompleted(entity.isCompleted());
                    return todo;
                })
                .collect(java.util.stream.Collectors.toList());
            callback.onTodosLoaded(todos);
        });
    }

    public void getTodosByDate(String date, TodoCallback callback) {
        executorService.execute(() -> {
            List<TodoEntity> entities = todoDao.getTodosByDate(date);
            List<Todo> todos = entities.stream()
                .map(entity -> {
                    Todo todo = new Todo(entity.getTitle(), entity.getDate());
                    todo.setId(entity.getId());
                    todo.setCompleted(entity.isCompleted());
                    return todo;
                })
                .collect(java.util.stream.Collectors.toList());
            callback.onTodosLoaded(todos);
        });
    }
} 