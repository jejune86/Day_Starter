package com.example.day_starter.ui.main;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.day_starter.R;
import com.example.day_starter.data.repository.todo.TodoRepository;
import com.example.day_starter.data.repository.weather.WeatherRepository;
import com.example.day_starter.model.todo.Todo;
import com.example.day_starter.model.weather.Weather;
import com.example.day_starter.ui.adapter.TodoAdapter;
import com.example.day_starter.ui.decorator.EventDecorator;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import org.threeten.bp.LocalDate;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import android.Manifest;

public class MainActivity extends AppCompatActivity implements TodoAdapter.TodoListener {

    private TodoRepository todoRepository;
    private TodoAdapter todoAdapter;
    private MaterialCalendarView calendarDialog;
    private LocalDate currentDate;
    private FusedLocationProviderClient fusedLocationClient;
    private WeatherRepository weatherRepository;
    private TextView tvTemperature, tvSky, tvPrecipitationType, tvPrecipitation, tvTempRange;
    // 콜백 인터페이스 추가
    interface LocationCallback {
        void onLocationReceived(Location location);
    }


    /**
     * 앱이 시작될 때 초기 설정을 수행합니다.
     * - TodoRepository 초기화
     * - 현재 날짜 설정
     * - UI 컴포넌트 초기화
     * - RecyclerView 및 어댑터 설정
     */

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 기존 초기화 코드...
        initializeWeatherViews();
        initializeLocationClient();
        initializeWeatherData();
        
        todoRepository = TodoRepository.getInstance(this);
        currentDate = LocalDate.now();

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

    private void initializeWeatherViews() {
        tvTemperature = findViewById(R.id.tv_temperature);
        tvTempRange = findViewById(R.id.tv_temp_range);
        tvSky = findViewById(R.id.tv_sky);
        tvPrecipitationType = findViewById(R.id.tv_precipitation_type);
        tvPrecipitation = findViewById(R.id.tv_precipitation);
    }
    
    private void initializeLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }   

    @Override
    protected void onStart() {
        super.onStart();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        checkLocationPermission();
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            getLastKnownLocation(this::fetchWeatherData);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, 
                                        String[] permissions, 
                                        int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastKnownLocation(this::fetchWeatherData);
            } else {
                Toast.makeText(this, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void getLastKnownLocation(LocationCallback callback) {
        if (ActivityCompat.checkSelfPermission(this, 
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        callback.onLocationReceived(location);
                    }
                })
                .addOnFailureListener(e -> 
                    Toast.makeText(this, "위치를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
                );
    }

    private void fetchWeatherData(Location location) {
        if (location != null) {
            weatherRepository = new WeatherRepository();
            weatherRepository.getWeatherData(
                location.getLatitude(), 
                location.getLongitude(), 
                new WeatherDataCallback()
            );
        }
    }

    private class WeatherDataCallback implements WeatherRepository.WeatherCallback {
        @Override
        public void onWeatherDataReceived() {
            runOnUiThread(() -> updateWeatherUI());
        }
    }

    private void initializeWeatherData() {
        tvTemperature.setText("현재 온도: 로딩 중...");
        tvTempRange.setText("최저/최고: --/--");
        tvSky.setText("하늘 상태: 로딩 중...");
        tvPrecipitationType.setText("강수 형태: 로딩 중...");
        tvPrecipitation.setText("강수량: 로딩 중...");
    }
    
    private void updateWeatherUI() {
        Weather weather = Weather.getInstance();
        tvTemperature.setText(String.format("현재 온도: %.1f°C", weather.getTemperature()));
        tvTempRange.setText(String.format("최저/최고: %.1f°C/%.1f°C", 
            weather.getMinTemperature(), 
            weather.getMaxTemperature()));
        
        String skyStatus;
        switch (weather.getSky()) {
            case 1:
                skyStatus = "맑음";
                break;

            case 3:
                skyStatus = "구름 많음";
                break;
            case 4:
                skyStatus = "흐림";
                break;
            default:
                skyStatus = "알 수 없음";
        }
        tvSky.setText("하늘 상태: " + skyStatus);
        
        String precipitationType;
        switch (weather.getPrecipitationType()) {
            case 0:
                precipitationType = "없음";
                break;
            case 1:
                precipitationType = "비";
                break;
            case 2:
                precipitationType = "비/눈";
                break;
            case 3:
                precipitationType = "눈";
                break;
            default:
                precipitationType = "알 수 없음";
        }
        tvPrecipitationType.setText("강수 형태: " + precipitationType);
        tvPrecipitation.setText("강수량: " + weather.getPrecipitation());
    }



    /**
     * 달력 다이얼로그의 기본 설정을 수행합니다.
     * - 날짜 선택 리스너 설정
     * - Todo가 있는 날에 점 표시
     */
    private void setupCalendarDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_calendar, null);
        calendarDialog = dialogView.findViewById(R.id.calendarView);
        
        calendarDialog.setOnDateChangedListener((widget, date, selected) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(date.getYear(), date.getMonth() - 1, date.getDay());
            currentDate = LocalDate.of(date.getYear(), date.getMonth(), date.getDay());
            loadTodosByDate(currentDate);
            builder.create().dismiss();
        });

        builder.setView(dialogView);
        builder.setTitle("날짜 선택");
        
        todoRepository.getAllTodos(todos -> {
            Set<CalendarDay> dates = new HashSet<>();
            for (Todo todo : todos) {
                try {
                    LocalDate localDate = LocalDate.parse(todo.getDate());  // YYYY-MM-DD 형식의 문자열을 파싱
                    dates.add(CalendarDay.from(localDate.getYear(), 
                                             localDate.getMonthValue(), 
                                             localDate.getDayOfMonth()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            runOnUiThread(() -> calendarDialog.addDecorator(new EventDecorator(dates)));
        });
    }

    /**
     * 상단 툴바의 메뉴를 생성합니다.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    /**
     * 툴바 메뉴 아이템 선택 처리를 수행합니다.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_calendar) {
            showCalendarDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 특정 날짜의 할 일 목록을 로드하여 화면에 표시합니다.
     * @param date 표시할 날짜
     */
    private void loadTodosByDate(LocalDate date) {
        String dateStr = date.toString(); // YYYY-MM-DD 형식
        todoRepository.getTodosByDate(dateStr, todos -> 
            runOnUiThread(() -> todoAdapter.setTodos(todos))
        );
    }

    /**
     * 새로운 할 일을 추가하는 다이얼로그를 표시합니다.
     * - 제목 입력
     * - 날짜 선택 가능
     */
    private void showAddTodoDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_todo_with_date, null);
        EditText editText = dialogView.findViewById(R.id.todo_title_input);
        MaterialCalendarView calendarView = dialogView.findViewById(R.id.calendarView);
        
        calendarView.setSelectedDate(CalendarDay.from(currentDate));

        new AlertDialog.Builder(this)
                .setTitle("새 할 일 추가")
                .setView(dialogView)
                .setPositiveButton("추가", (dialog, which) -> {
                    String title = editText.getText().toString().trim();
                    if (!title.isEmpty()) {
                        CalendarDay selectedDate = calendarView.getSelectedDate();
                        LocalDate localDate = LocalDate.of(selectedDate.getYear(), 
                                                        selectedDate.getMonth(), 
                                                        selectedDate.getDay());
                        String todoDate = localDate.toString();  // YYYY-MM-DD 형식으로 변환
                        
                        Todo newTodo = new Todo(title, todoDate);
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

    /**
     * 할 일의 완료 상태가 변경되었을 때 처리합니다.
     */
    @Override
    public void onTodoCheckedChanged(Todo todo, boolean isChecked) {
        todo.setCompleted(isChecked);
        todoRepository.updateTodo(todo, todos -> 
            runOnUiThread(() -> todoAdapter.setTodos(todos))
        );
    }

    /**
     * 할 일을 삭제합니다.
     */
    @Override
    public void onTodoDelete(Todo todo) {
        todoRepository.deleteTodo(todo, todos -> 
            runOnUiThread(() -> todoAdapter.setTodos(todos))
        );
    }

    /**
     * 할 일을 정하는 다이얼로그를 표시합니다.
     */
    @Override
    public void onTodoEdit(Todo todo) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_todo, null);
        EditText editText = dialogView.findViewById(R.id.todo_title_input);
        editText.setText(todo.getTitle());

        new AlertDialog.Builder(this)
                .setTitle("할 일 수정")
                .setView(dialogView)
                .setPositiveButton("수정", (dialog, which) -> {
                    String title = editText.getText().toString().trim();
                    if (!title.isEmpty()) {
                        todo.setTitle(title);
                        todoRepository.updateTodo(todo, todos -> 
                            runOnUiThread(() -> loadTodosByDate(currentDate))
                        );
                    }
                })
                .setNegativeButton("취소", null)
                .show();
    }

    /**
     * 선택된 할 일을 다음 날로 이동합니다.
     */
    @Override
    public void onTodoMoveToTomorrow(Todo todo) {
        LocalDate date = LocalDate.parse(todo.getDate());  // String을 LocalDate로 변환
        LocalDate tomorrow = date.plusDays(1);      // 1일 추가
        String tomorrowStr = tomorrow.toString();    // YYYY-MM-DD 형식으로 변환
        todo.setDate(tomorrowStr);
        
        todoRepository.updateTodo(todo, todos -> 
            runOnUiThread(() -> {
                loadTodosByDate(currentDate);
                Toast.makeText(this, "내일로 이동되었습니다.", Toast.LENGTH_SHORT).show();
            })
        );
    }

    /**
     * 달력 다이얼로그를 표시합니다.
     * - 현재 선택된 날짜 표시
     * - 할 일이 있는 날짜에 점 표시
     */
    private void showCalendarDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_calendar, null);
        MaterialCalendarView calendarView = dialogView.findViewById(R.id.calendarView);
        
        // 현재 선택된 날짜 설정
        calendarView.setSelectedDate(CalendarDay.from(currentDate.getYear(), 
                                                    currentDate.getMonthValue(), 
                                                    currentDate.getDayOfMonth()));
        
        builder.setView(dialogView);
        builder.setTitle("날짜 선택");
        
        AlertDialog dialog = builder.create();
        
        // 날짜 선택 리스너 설정
        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            currentDate = LocalDate.of(date.getYear(), date.getMonth(), date.getDay());
            loadTodosByDate(currentDate);
            dialog.dismiss();  // 현재 다이얼로그를 닫음
        });

        // Todo가 있는 날짜에 점 표시
        todoRepository.getAllTodos(todos -> {
            Set<CalendarDay> dates = new HashSet<>();
            for (Todo todo : todos) {
                try {
                    String[] dateParts = todo.getDate().split("-");
                    if (dateParts.length == 3) {
                        int year = Integer.parseInt(dateParts[0]);
                        int month = Integer.parseInt(dateParts[1]);
                        int day = Integer.parseInt(dateParts[2]);
                        dates.add(CalendarDay.from(year, month, day));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            runOnUiThread(() -> {
                calendarView.addDecorator(new EventDecorator(dates));
                dialog.show();
            });
        });
    }
} 