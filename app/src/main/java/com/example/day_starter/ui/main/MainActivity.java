package com.example.day_starter.ui.main;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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

import android.view.ViewGroup;

import com.example.day_starter.util.ColorManager;



public class MainActivity extends AppCompatActivity implements TodoAdapter.TodoListener {

    private TodoRepository todoRepository;
    private TodoAdapter todoAdapter;
    private MaterialCalendarView calendarDialog;
    private LocalDate currentDate;
    private FusedLocationProviderClient fusedLocationClient;
    private WeatherRepository weatherRepository;
    private TextView tvTemperature, tvSky, tvPrecipitationType, tvPrecipitation, tvTempRange;
    private NewsAdapter newsAdapter;
    private RecyclerView newsRecyclerView;
    private MaterialButton headlinesButton;
    private boolean isNewsVisible = false;
    private View fullScreenCalendarView;
    private TodoAdapter fullScreenAdapter;
    private MaterialCalendarView calendarView;
    private RecyclerView fullScreenTodoRecyclerView;
    private TextView fullScreenDateTextView;
    private ColorManager colorManager;



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

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        checkLocationPermission();
        
        initializeWeatherViews();
        initializeLocationClient();
        initializeWeatherData();
        initializeFullScreenCalendarView();
        
        todoRepository = TodoRepository.getInstance(this);
        currentDate = LocalDate.now();

        
        initializeLocationClient();

        RecyclerView recyclerView = findViewById(R.id.recycler_todos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        todoAdapter = new TodoAdapter();
        todoAdapter.setTodoListener(this);
        recyclerView.setAdapter(todoAdapter);

        FloatingActionButton fabAddTodo = findViewById(R.id.fab_add_todo);
        fabAddTodo.setOnClickListener(v -> showAddTodoDialog());

        FloatingActionButton fabOpenCalender = findViewById(R.id.fab_open_calendar);
        fabOpenCalender.setOnClickListener(v->showFullScreenTodoList());



        loadTodosByDate(LocalDate.now());
        setupCalendarDialog();

        newsRecyclerView = findViewById(R.id.recycler_news);
        newsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        newsAdapter = new NewsAdapter();
        newsRecyclerView.setAdapter(newsAdapter);

        headlinesButton = findViewById(R.id.btn_headlines);
        headlinesButton.setOnClickListener(v -> toggleNewsVisibility());

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
                new WeatherDataCallback() {
                    @Override
                    public void onWeatherDataReceived() {
                        runOnUiThread(() -> {
                            colorManager = new ColorManager(MainActivity.this);
                            updateWeatherUI();
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
        
        // 날씨 UI 업데이트
        tvTemperature.setText(String.format("현재 온도: %.1f°C", weather.getTemperature()));
        tvTempRange.setText(String.format("최저/최고: %.1f°C/%.1f°C", 
            weather.getMinTemperature(), 
            weather.getMaxTemperature()));
        
        String skyStatus;
        int sky = weather.getSky();
        if (sky <= 5) {
            skyStatus = "맑음";
        } else if (sky <= 8) {
            skyStatus = "구름 많음";
        } else {
            skyStatus = "흐림";
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
            case 4 :
                precipitationType = "소나기";
                break;
            default:
                precipitationType = "알 수 없음";
        }
        tvPrecipitationType.setText("강수 형태: " + precipitationType);
        tvPrecipitation.setText("강수량: " + weather.getPrecipitation());

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
        
        // 배경색 설정
        View mainContent = findViewById(R.id.main_content);


        // 로딩 화면 숨기고 메인 컨텐츠 표시
        findViewById(R.id.loading_layout).setVisibility(View.GONE);
        mainContent.setVisibility(View.VISIBLE);
    }



    /**
     * 달력 다이얼로그의 기본 설정
     * - 날짜 선택 리스너 설정
     * - Todo가 있는 날에 점 표시
     */
    private void setupCalendarDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_calendar, null);
        calendarDialog = dialogView.findViewById(R.id.calendarView);
        
        calendarDialog.setOnDateChangedListener((widget, date, selected) -> {
            // 날짜 선택시 전체화면 달력 뷰로 전환
            showFullScreenTodoList();
            builder.create().dismiss();
        });

        builder.setView(dialogView);
        builder.setTitle("날짜 선택");
        
        todoRepository.getAllTodos(todos -> {
            Set<CalendarDay> dates = new HashSet<>();
            for (Todo todo : todos) {
                try {
                    LocalDate localDate = LocalDate.parse(todo.getDate());
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
        LocalDate date = LocalDate.parse(todo.getDate());  // String을 LocalDate로 환산 
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

    private void showFullScreenTodoList() {
        if (fullScreenCalendarView.getParent() != null) {
            ((ViewGroup) fullScreenCalendarView.getParent()).removeView(fullScreenCalendarView);
        }

        if (isNewsVisible) {
            View newsContainer = findViewById(R.id.news_container);
            View mainContent = findViewById(R.id.main_content);
            newsContainer.setVisibility(View.GONE);
            mainContent.setVisibility(View.VISIBLE);
            isNewsVisible = false;
        }
        
        View mainContent = findViewById(R.id.main_content);
        ViewGroup rootView = findViewById(android.R.id.content);
        mainContent.setVisibility(View.GONE);
        rootView.addView(fullScreenCalendarView);
        
        // 현재 날짜 표시
        fullScreenDateTextView.setText(currentDate.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")));
        
        // Todo가 있는 날짜에 점 표시
        updateCalendarDecorations();
        
        // 초기 할일 목록 로드
        loadTodosByDate(currentDate, fullScreenAdapter);
    }

    private void hideFullScreenTodoList() {
        ViewGroup rootView = findViewById(android.R.id.content);
        rootView.removeView(fullScreenCalendarView);
        View mainContent = findViewById(R.id.main_content);
        mainContent.setVisibility(View.VISIBLE);
        loadTodosByDate(currentDate);  // 메인 화면 할일 목록 갱신
    }

    private void updateCalendarDecorations() {
        todoRepository.getAllTodos(todos -> {
            Set<CalendarDay> dates = new HashSet<>();
            for (Todo todo : todos) {
                LocalDate todoDate = LocalDate.parse(todo.getDate());
                dates.add(CalendarDay.from(todoDate));
            }
            runOnUiThread(() -> calendarView.addDecorator(new EventDecorator(dates)));
        });
    }

    private void initializeFullScreenCalendarView() {
        fullScreenCalendarView = getLayoutInflater().inflate(R.layout.full_screen_todo_list, null);
        
        calendarView = fullScreenCalendarView.findViewById(R.id.calendar_view);
        fullScreenTodoRecyclerView = fullScreenCalendarView.findViewById(R.id.recycler_todos);
        fullScreenDateTextView = fullScreenCalendarView.findViewById(R.id.tv_selected_date);
        ImageButton closeButton = fullScreenCalendarView.findViewById(R.id.btn_close);
        
        fullScreenTodoRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        fullScreenAdapter = new TodoAdapter();
        fullScreenAdapter.setTodoListener(this);
        fullScreenTodoRecyclerView.setAdapter(fullScreenAdapter);
        
        // 달 이벤트 설정
        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            LocalDate selectedDate = LocalDate.of(date.getYear(), date.getMonth(), date.getDay());
            fullScreenDateTextView.setText(selectedDate.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")));
            loadTodosByDate(selectedDate, fullScreenAdapter);
        });
        
        closeButton.setOnClickListener(v -> hideFullScreenTodoList());
    }

    private void loadTodosByDate(LocalDate date, TodoAdapter adapter) {
        String dateStr = date.toString();
        todoRepository.getTodosByDate(dateStr, todos -> 
            runOnUiThread(() -> adapter.setTodos(todos))
        );
    }
} 