package com.example.day_starter.ui.main;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.day_starter.R;
import com.example.day_starter.data.repository.TodoRepository;
import com.example.day_starter.model.Todo;
import com.example.day_starter.ui.adapter.TodoAdapter;
import com.example.day_starter.ui.decorator.EventDecorator;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;
import org.threeten.bp.Instant;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements TodoAdapter.TodoListener {

    private TodoRepository todoRepository;
    private TodoAdapter todoAdapter;
    private MaterialCalendarView calendarDialog;
    private long currentDate;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        todoRepository = TodoRepository.getInstance(this);
        currentDate = getTodayMillis(); // 오늘 날짜로 초기화

        // 상단 툴바에 달력 버튼 추가
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        RecyclerView recyclerView = findViewById(R.id.recycler_todos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        todoAdapter = new TodoAdapter();
        todoAdapter.setTodoListener(this);
        recyclerView.setAdapter(todoAdapter);

        FloatingActionButton fabAddTodo = findViewById(R.id.fab_add_todo);
        fabAddTodo.setOnClickListener(v -> showAddTodoDialog());

        loadTodosByDate(currentDate);
        setupCalendarDialog();
    }

    private void setupCalendarDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_calendar, null);
        calendarDialog = dialogView.findViewById(R.id.calendarView);
        
        calendarDialog.setOnDateChangedListener((widget, date, selected) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(date.getYear(), date.getMonth() - 1, date.getDay());
            currentDate = calendar.getTimeInMillis();
            loadTodosByDate(currentDate);
            builder.create().dismiss();
        });

        builder.setView(dialogView);
        builder.setTitle("날짜 선택");
        
        todoRepository.getAllTodos(todos -> {
            Set<CalendarDay> dates = new HashSet<>();
            for (Todo todo : todos) {
                Instant instant = Instant.ofEpochMilli(todo.getDate());
                LocalDate localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate();
                dates.add(CalendarDay.from(localDate));
            }
            runOnUiThread(() -> calendarDialog.addDecorator(new EventDecorator(dates)));
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_calendar) {
            showCalendarDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadTodosByDate(long date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        long startOfDay = calendar.getTimeInMillis();
        
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        long endOfDay = calendar.getTimeInMillis();

        todoRepository.getTodosByDate(startOfDay, endOfDay, todos -> 
            runOnUiThread(() -> todoAdapter.setTodos(todos))
        );
    }

    private long getTodayMillis() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private void showAddTodoDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_todo_with_date, null);
        EditText editText = dialogView.findViewById(R.id.todo_title_input);
        MaterialCalendarView calendarView = dialogView.findViewById(R.id.calendarView);
        
        // 현재 날짜 선택
        Instant instant = Instant.ofEpochMilli(currentDate);
        LocalDate currentLocalDate = instant.atZone(ZoneId.systemDefault()).toLocalDate();
        calendarView.setSelectedDate(CalendarDay.from(currentLocalDate));

        new AlertDialog.Builder(this)
                .setTitle("새 할 일 추가")
                .setView(dialogView)
                .setPositiveButton("추가", (dialog, which) -> {
                    String title = editText.getText().toString().trim();
                    if (!title.isEmpty()) {
                        CalendarDay selectedDate = calendarView.getSelectedDate();
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(selectedDate.getYear(), selectedDate.getMonth() - 1, selectedDate.getDay());
                        
                        Todo newTodo = new Todo(title, calendar.getTimeInMillis());
                        todoRepository.insertTodo(newTodo, todos -> 
                            runOnUiThread(() -> loadTodosByDate(currentDate))
                        );
                    } else {
                        Toast.makeText(this, "할 일 제목을 입력하세요.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("취소", null)
                .show();
    }

    @Override
    public void onTodoCheckedChanged(Todo todo, boolean isChecked) {
        todo.setCompleted(isChecked);
        todoRepository.updateTodo(todo, todos -> 
            runOnUiThread(() -> todoAdapter.setTodos(todos))
        );
    }

    @Override
    public void onTodoDelete(Todo todo) {
        todoRepository.deleteTodo(todo, todos -> 
            runOnUiThread(() -> todoAdapter.setTodos(todos))
        );
    }

    private void showCalendarDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_calendar, null);
        MaterialCalendarView calendarView = dialogView.findViewById(R.id.calendarView);
        
        // 현재 선택된 날짜 설정
        Instant instant = Instant.ofEpochMilli(currentDate);
        LocalDate currentLocalDate = instant.atZone(ZoneId.systemDefault()).toLocalDate();
        calendarView.setSelectedDate(CalendarDay.from(currentLocalDate));
        
        builder.setView(dialogView);
        builder.setTitle("날짜 선택");
        
        AlertDialog dialog = builder.create();
        
        // 날짜 선택 리스너 설정
        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            Calendar selectedCalendar = Calendar.getInstance();
            selectedCalendar.set(date.getYear(), date.getMonth() - 1, date.getDay());
            currentDate = selectedCalendar.getTimeInMillis();
            loadTodosByDate(currentDate);
            dialog.dismiss();
        });

        // Todo가 있는 날짜에 점 표시
        todoRepository.getAllTodos(todos -> {
            Set<CalendarDay> dates = new HashSet<>();
            for (Todo todo : todos) {
                Instant todoInstant = Instant.ofEpochMilli(todo.getDate());
                LocalDate localDate = todoInstant.atZone(ZoneId.systemDefault()).toLocalDate();
                dates.add(CalendarDay.from(localDate));
            }
            runOnUiThread(() -> calendarView.addDecorator(new EventDecorator(dates)));
        });

        dialog.show();
    }
} 