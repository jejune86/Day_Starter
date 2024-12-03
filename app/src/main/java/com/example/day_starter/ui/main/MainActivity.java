package com.example.day_starter.ui.main;

import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.day_starter.R;
import com.example.day_starter.BuildConfig;
import com.example.day_starter.data.api.client.NewsAPIClient;
import com.example.day_starter.data.repository.todo.TodoRepository;
import com.example.day_starter.data.repository.weather.WeatherRepository;
import com.example.day_starter.model.news.NewsResponse;
import com.example.day_starter.model.todo.Todo;
import com.example.day_starter.model.weather.Weather;
import com.example.day_starter.ui.adapter.TodoAdapter;
import com.example.day_starter.ui.adapter.NewsAdapter;
import com.example.day_starter.ui.decorator.EventDecorator;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.HashSet;
import java.util.Set;

import android.Manifest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.IOException;
import android.util.Log;

import com.example.day_starter.util.ColorManager;



public class MainActivity extends AppCompatActivity implements TodoAdapter.TodoListener {

    private TodoRepository todoRepository;
    private TodoAdapter todoAdapter, calendarTodoAdapter;
    private LocalDate currentDate;
    private FusedLocationProviderClient fusedLocationClient;
    private WeatherRepository weatherRepository;
    private TextView tvTemperature, tvSky, tvPrecipitationType, tvPrecipitation, tvTempRange;
    private NewsAdapter newsAdapter;
    private RecyclerView newsRecyclerView;
    private MaterialButton headlinesButton;
    private boolean isNewsVisible = false;
    private View fullScreenCalendarView;
    private MaterialCalendarView calendarView;
    private RecyclerView calendarRecyclerTodos, mainRecyclerTodos;
    private TextView calendarSelectedDateText, date;
    private ColorManager colorManager;
    private ImageView weatherIcon;
    private View calendarContainer;
    private View mainContent;
    private View newsContainer;


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

        date = findViewById(R.id.date);

        todoRepository = TodoRepository.getInstance(this);
        currentDate = LocalDate.now();

        date.setText(currentDate.format(DateTimeFormatter.ofPattern("yyyy MM dd")));

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        checkLocationPermission();
        
        initializeWeatherViews();
        initializeLocationClient();
        initializeWeatherData();
        initializeCalendarView();
        initializeNewsViews();
        initializeTodoViews();

        
        


        FloatingActionButton fabAddTodo = findViewById(R.id.fab_add_todo);
        fabAddTodo.setOnClickListener(v -> showAddTodoDialog());

        FloatingActionButton fabOpenCalender = findViewById(R.id.fab_open_calendar);
        fabOpenCalender.setOnClickListener(v -> toggleCalendarVisibility());

    }

    private void initializeTodoViews() {
        mainRecyclerTodos = findViewById(R.id.recycler_todos);
        mainRecyclerTodos.setLayoutManager(new LinearLayoutManager(this));
        todoAdapter = new TodoAdapter();
        todoAdapter.setTodoListener(this);
        mainRecyclerTodos.setAdapter(todoAdapter);
        loadTodosByDate(LocalDate.now());
        updateCalendarDecorations();
    }

    private void initializeNewsViews() {
        newsContainer = findViewById(R.id.news_container);
        newsRecyclerView = findViewById(R.id.recycler_news);
        newsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        newsAdapter = new NewsAdapter();
        newsRecyclerView.setAdapter(newsAdapter);
        headlinesButton = findViewById(R.id.btn_headlines);
        headlinesButton.setOnClickListener(v -> toggleNewsVisibility());
    }

    private void initializeWeatherViews() {
        mainContent = findViewById(R.id.main_content);
        tvTemperature = findViewById(R.id.tv_temperature);
        tvTempRange = findViewById(R.id.tv_temp_range);
        tvSky = findViewById(R.id.tv_sky);
        tvPrecipitationType = findViewById(R.id.tv_precipitation_type);
        tvPrecipitation = findViewById(R.id.tv_precipitation);
        weatherIcon = findViewById(R.id.iv_weather);
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
                new WeatherDataCallback() {
                    @Override
                    public void onWeatherDataReceived() {
                        runOnUiThread(() -> {
                            colorManager = new ColorManager(MainActivity.this);
                            updateWeatherUI();
                        });
                    }
                    
                    @Override
                    public void onWeatherDataFailed() {
                        runOnUiThread(() -> {
                            showRetryButton();
                        });
                    }
                }
            );
        }
    }

    private class WeatherDataCallback implements WeatherRepository.WeatherCallback {
        @Override
        public void onWeatherDataReceived() {
            runOnUiThread(() -> updateWeatherUI());
        }
        @Override
        public void onWeatherDataFailed() {
            runOnUiThread(() -> showRetryButton());
        }
    }

    private void initializeWeatherData() {
        tvTemperature.setText("Current Temp: Loading...");
        tvTempRange.setText("Min/Max: --/--");
        tvSky.setText("Sky Status: Loading...");
        tvPrecipitationType.setText("Precipitation Type: Loading...");
        tvPrecipitation.setText("Precipitation: Loading...");
    }
    
    private void updateWeatherUI() {
        Weather weather = Weather.getInstance();
        
        // 날씨 UI 업데이트
        tvTemperature.setText(String.format("Current Temp: %.1f°C", weather.getTemperature()));
        tvTempRange.setText(String.format("Min/Max: %.1f°C/%.1f°C", 
            weather.getMinTemperature(), 
            weather.getMaxTemperature()));
        
        String skyStatus;
        int sky = weather.getSky();
        if (sky <= 5) {
            skyStatus = "Clear";
        } else if (sky <= 8) {
            skyStatus = "Partly Cloudy";
        } else {
            skyStatus = "Cloudy";
        }
        tvSky.setText("Sky Status: " + skyStatus);
        
        String precipitationType;
        switch (weather.getPrecipitationType()) {
            case 0:
                precipitationType = "None";
                break;
            case 1:
                precipitationType = "Rain";
                break;
            case 2:
                precipitationType = "Rain/Snow";
                break;
            case 3:
                precipitationType = "Snow";
                break;
            case 4:
                precipitationType = "Shower";
                break;
            default:
                precipitationType = "Unknown";
        }
        tvPrecipitationType.setText("Precipitation Type: " + precipitationType);
        String precipitationAmount = weather.getPrecipitation();
        if (precipitationAmount.equals("강수없음")) {
            precipitationAmount = "0mm";
        }
        else if (precipitationAmount.equals("1.0mm 미만")) {
            precipitationAmount = "< 1mm";
        }
        else if (precipitationAmount.equals("50.0mm 이상")) {
            precipitationAmount = "> 50mm";
        }
        tvPrecipitation.setText("Precipitation: " + precipitationAmount);

        ImageView weatherIcon = findViewById(R.id.iv_weather);

        // 강수 형태가 없을 때는 하늘 상태에 따라 아이콘 설정
        if (weather.getPrecipitationType() == 0) {
            if (weather.getSky() <= 5) {
                weatherIcon.setImageResource(R.drawable.ic_sunny);
            } else if (weather.getSky() <= 8) {
                weatherIcon.setImageResource(R.drawable.ic_cloudy);
            } else if (weather.getSky() <= 10) {
                weatherIcon.setImageResource(R.drawable.ic_cloudy);
            } else {
                weatherIcon.setImageResource(R.drawable.ic_sunny);
            }
        } else {
            // 강수 형태가 있을 때
            switch (weather.getPrecipitationType()) {
                case 1:
                    weatherIcon.setImageResource(R.drawable.ic_rainy);
                    break;
                case 2:
                    weatherIcon.setImageResource(R.drawable.ic_mix_rain);
                    break;
                case 3:
                    weatherIcon.setImageResource(R.drawable.ic_snow);
                    break;
                case 4:
                    weatherIcon.setImageResource(R.drawable.ic_rainy);
                    break;
                default:
                    weatherIcon.setImageResource(R.drawable.ic_sunny);
            }
        }


        weatherIcon.setBackground(colorManager.getBackgroundDrawable());
        
        GradientDrawable background = (GradientDrawable) weatherIcon.getBackground();
        background.setCornerRadius(50);

        // 로딩 화면 숨기고 메인 컨텐츠 표시
        findViewById(R.id.loading_layout).setVisibility(View.GONE);
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
                            runOnUiThread(() -> {
                                loadTodosByDate(currentDate);
                                updateCalendarDecorations();
                            })
                        );
                    } else {
                        Toast.makeText(this, "Please enter a task title.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    @Override
    public void onTodoCheckedChanged(Todo todo, boolean isChecked) {
        todo.setCompleted(isChecked);
        todoRepository.updateTodo(todo, todos -> 
            runOnUiThread(() -> {
                todoAdapter.setTodos(todos);
                if (todoAdapter != null) {
                    loadTodosByDate(currentDate, todoAdapter);
                }
            })
        );
    }

    /**
     * 할 일을 삭제합니다.
     */
    @Override
    public void onTodoDelete(Todo todo) {
        LocalDate date = LocalDate.parse(todo.getDate());
        todoRepository.deleteTodo(todo, todos -> 
            runOnUiThread(() -> {
                todoAdapter.setTodos(todos);
                if (todoAdapter != null) {
                    loadTodosByDate(currentDate, todoAdapter);
                }
                updateCalendarDecorations();
                loadTodosByDate(date, todoAdapter);
            })
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
        LocalDate date = LocalDate.parse(todo.getDate());
        new AlertDialog.Builder(this)
                .setTitle("할 일 수정")
                .setView(dialogView)
                .setPositiveButton("수정", (dialog, which) -> {
                    String title = editText.getText().toString().trim();
                    if (!title.isEmpty()) {
                        todo.setTitle(title);
                        todoRepository.updateTodo(todo, todos -> 
                            runOnUiThread(() -> {
                                loadTodosByDate(currentDate);
                                if (todoAdapter != null) {
                                    loadTodosByDate(currentDate, todoAdapter);
                                }
                                updateCalendarDecorations();
                                loadTodosByDate(date, todoAdapter);
                            })
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
        LocalDate date = LocalDate.parse(todo.getDate());
        LocalDate tomorrow = date.plusDays(1);
        String tomorrowStr = tomorrow.toString();
        todo.setDate(tomorrowStr);
        
        todoRepository.updateTodo(todo, todos -> 
            runOnUiThread(() -> {
                loadTodosByDate(currentDate);
                if (todoAdapter != null) {
                    loadTodosByDate(currentDate, todoAdapter);
                }
                updateCalendarDecorations();
                Toast.makeText(this, "내일로 이동되었습니다.", Toast.LENGTH_SHORT).show();
                loadTodosByDate(date, todoAdapter);
            })
        );
    }

    private void toggleNewsVisibility() {
        View newsContainer = findViewById(R.id.news_container);
        View mainContent = findViewById(R.id.main_content);
        ImageButton closeButton = findViewById(R.id.btn_close_news);
        
        isNewsVisible = !isNewsVisible;
        
        if (isNewsVisible) {
            newsContainer.setVisibility(View.VISIBLE);
            mainContent.setVisibility(View.GONE);
            loadNewsHeadlines();
            
            closeButton.setOnClickListener(v -> {
                newsContainer.setVisibility(View.GONE);
                mainContent.setVisibility(View.VISIBLE);
                isNewsVisible = false;
            });
        } else {
            newsContainer.setVisibility(View.GONE);
            mainContent.setVisibility(View.VISIBLE);
        }
    }

    private void loadNewsHeadlines() {
        Log.d("NewsAPI", "뉴스 데이 로딩 시작");
        NewsAPIClient newsAPIClient = new NewsAPIClient();
        newsAPIClient.getNewsAPIService().getTopHeadlines("us", BuildConfig.NEWS_API_KEY)
            .enqueue(new Callback<NewsResponse>() {
                @Override
                public void onResponse(Call<NewsResponse> call, Response<NewsResponse> response) {
                    Log.d("NewsAPI", "응답 받음: " + response.code());
                    if (response.isSuccessful()) {
                        Log.d("NewsAPI", "응답 성공");
                        if (response.body() != null) {
                            Log.d("NewsAPI", "기사 개수: " + response.body().getArticles().size());
                            runOnUiThread(() -> 
                                newsAdapter.setArticles(response.body().getArticles())
                            );
                        } else {
                            Log.e("NewsAPI", "응답 바디가 null입니다");
                        }
                    } else {
                        try {
                            Log.e("NewsAPI", "에러 응답: " + response.errorBody().string());
                        } catch (IOException e) {
                            Log.e("NewsAPI", "에러 응답을 읽는데 실패했습니다", e);
                        }
                    }
                }

                @Override
                public void onFailure(Call<NewsResponse> call, Throwable t) {
                    Log.e("NewsAPI", "API 호출 실패", t);
                    runOnUiThread(() -> 
                        Toast.makeText(MainActivity.this, 
                            "뉴스를 불러오는데 실패했습니다: " + t.getMessage(), 
                            Toast.LENGTH_SHORT).show()
                    );
                }
            });
    }


    private void updateCalendarDecorations() {
        todoRepository.getAllTodos(todos -> {
            Set<CalendarDay> dates = new HashSet<>();
            for (Todo todo : todos) {
                try {
                    LocalDate todoDate = LocalDate.parse(todo.getDate());
                    dates.add(CalendarDay.from(
                        todoDate.getYear(),
                        todoDate.getMonthValue(),
                        todoDate.getDayOfMonth()
                    ));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            runOnUiThread(() -> {
                // 메인 달력 업데이트
                MaterialCalendarView mainCalendarView = findViewById(R.id.calendar_view);
                if (mainCalendarView != null) {
                    mainCalendarView.removeDecorators();
                    mainCalendarView.addDecorator(new EventDecorator(dates));
                }
                
                // 전화면 달력 업데이트
                if (calendarView != null) {
                    calendarView.removeDecorators();
                    calendarView.addDecorator(new EventDecorator(dates));
                }

            });
        });
    }

    private void initializeCalendarView() {
        calendarContainer = findViewById(R.id.calendar_container);
        calendarView = findViewById(R.id.calendar_view);
        calendarRecyclerTodos = findViewById(R.id.calendar_recycler_todos);
        calendarSelectedDateText = findViewById(R.id.tv_selected_date);
        calendarRecyclerTodos.setLayoutManager(new LinearLayoutManager(this));
        calendarTodoAdapter = new TodoAdapter();
        calendarTodoAdapter.setTodoListener(this);

        int nightModeFlags = getResources().getConfiguration().uiMode & 
        Configuration.UI_MODE_NIGHT_MASK;
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            // 다크모드일 때 캘린더 텍스트 색상 설정
            calendarView.setHeaderTextAppearance(R.style.CustomHeaderTitle);
            calendarView.setWeekDayTextAppearance(R.style.CustomHeaderTitle);
            calendarView.setDateTextAppearance(R.style.CustomHeaderTitle);
        }


        calendarRecyclerTodos.setLayoutManager(new LinearLayoutManager(this));
        calendarRecyclerTodos.setAdapter(calendarTodoAdapter);
        
        // 달 이벤트 설정
        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            LocalDate selectedDate = LocalDate.of(date.getYear(), date.getMonth(), date.getDay());
            calendarSelectedDateText.setText(selectedDate.format(DateTimeFormatter.ofPattern("yyyy MM dd")));
            loadTodosByDate(selectedDate, calendarTodoAdapter);
        });

    }

    private void loadTodosByDate(LocalDate date, TodoAdapter adapter) {
        String dateStr = date.toString();
        todoRepository.getTodosByDate(dateStr, todos ->
            runOnUiThread(() -> adapter.setTodos(todos))

        );
    }

    private void toggleCalendarVisibility() {
        if (calendarContainer.getVisibility() == View.VISIBLE) {
            calendarContainer.setVisibility(View.GONE);
        } else {
            calendarContainer.setVisibility(View.VISIBLE);
            newsContainer.setVisibility(View.GONE);
            isNewsVisible = false;

            calendarSelectedDateText.setText(currentDate.format(DateTimeFormatter.ofPattern("yyyy MM dd")));
            loadTodosByDate(currentDate, calendarTodoAdapter);
            
            calendarView.setOnDateChangedListener((widget, date, selected) -> {
                LocalDate selectedDate = LocalDate.of(date.getYear(), date.getMonth(), date.getDay());
                calendarSelectedDateText.setText(selectedDate.format(DateTimeFormatter.ofPattern("yyyy MM dd")));
                loadTodosByDate(selectedDate, calendarTodoAdapter);
            });
        }
    }

    private void showRetryButton() {
        findViewById(R.id.loading_progress).setVisibility(View.GONE);
        findViewById(R.id.loading_text).setVisibility(View.GONE);
        
        MaterialButton retryButton = findViewById(R.id.retry_button);
    
        
        retryButton.setVisibility(View.VISIBLE);
        retryButton.setOnClickListener(v -> {
            retryButton.setVisibility(View.GONE);
            findViewById(R.id.loading_progress).setVisibility(View.VISIBLE);
            findViewById(R.id.loading_text).setVisibility(View.VISIBLE);
            
            // 위치 정보 다시 가져오기
            checkLocationPermission();
        });
    }

} 