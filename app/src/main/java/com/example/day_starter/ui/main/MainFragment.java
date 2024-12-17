package com.example.day_starter.ui.main;

import static android.content.Context.MODE_PRIVATE;

import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import com.example.day_starter.model.TarotCard;
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
import com.google.android.material.bottomnavigation.BottomNavigationView;
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



import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Random;


import com.example.day_starter.ui.adapter.TodoAdapter;

public class MainFragment extends Fragment implements TodoAdapter.TodoListener {
    private View view;
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
    private MaterialCalendarView calendarView;
    private RecyclerView calendarRecyclerTodos, mainRecyclerTodos;
    private TextView calendarSelectedDateText, date;
    private ColorManager colorManager;
    private ImageView weatherIcon;
    private View calendarContainer;
    private View mainContent;
    private View newsContainer;
    private List<TarotCard> tarotCards;
    private Random random = new Random();
    private ProgressBar newsLoadingSpinner;
    private FloatingActionButton fabAddTodo, fabOpenCalender;

    // 콜백 인터페이스 추가
    interface LocationCallback {
        void onLocationReceived(Location location);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_main, container, false);

        // 뉴스 로딩 스피너 초기화
        newsLoadingSpinner = view.findViewById(R.id.news_loading_spinner);

        date = view.findViewById(R.id.date);

        todoRepository = TodoRepository.getInstance(getActivity());
        currentDate = LocalDate.now();

        date.setText(currentDate.format(DateTimeFormatter.ofPattern("yyyy MM dd (E)")));

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());



        initializeWeatherViews();
        initializeLocationClient();
        initializeCalendarView();
        initializeNewsViews();
        initializeTodoViews();
        initializeTarotCards();
        setupTarotButton();

        fabAddTodo = view.findViewById(R.id.fab_add_todo);
        fabAddTodo.setOnClickListener(v -> showAddTodoDialog());

        fabOpenCalender = view.findViewById(R.id.fab_open_calendar);
        fabOpenCalender.setOnClickListener(v -> toggleCalendarVisibility());

        return view;
    }


    private void initializeTodoViews() {
        mainRecyclerTodos = view.findViewById(R.id.recycler_todos);
        mainRecyclerTodos.setLayoutManager(new LinearLayoutManager(getActivity()));
        todoAdapter = new TodoAdapter();
        todoAdapter.setTodoListener(this);
        mainRecyclerTodos.setAdapter(todoAdapter);
        loadTodosByDate(LocalDate.now(), todoAdapter);
        updateCalendarDecorations();
    }

    private void initializeNewsViews() {
        newsContainer = view.findViewById(R.id.news_container);
        newsRecyclerView = view.findViewById(R.id.recycler_news);
        newsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        newsAdapter = new NewsAdapter();
        newsRecyclerView.setAdapter(newsAdapter);
        headlinesButton = view.findViewById(R.id.btn_headlines);
        headlinesButton.setOnClickListener(v -> toggleNewsVisibility());

        newsLoadingSpinner = view.findViewById(R.id.news_loading_spinner);

        // 뉴스 로드 시작 시
        newsLoadingSpinner.setVisibility(View.VISIBLE);
        loadNewsHeadlines();
    }

    private void initializeWeatherViews() {
        mainContent = view.findViewById(R.id.main_content);
        tvTemperature = view.findViewById(R.id.tv_temperature_value);
        tvTempRange = view.findViewById(R.id.tv_temp_range_value);
        tvSky = view.findViewById(R.id.tv_sky_value);
        tvPrecipitationType = view.findViewById(R.id.tv_precipitation_type_value);
        tvPrecipitation = view.findViewById(R.id.tv_precipitation_value);
        weatherIcon = view.findViewById(R.id.iv_weather);
    }
    
    private void initializeLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
    }   



    
    public void updateWeatherUI() {
        Weather weather = Weather.getInstance();
        colorManager = new ColorManager(getActivity());

        // 날씨 UI 업데이트
        tvTemperature.setText(String.format("%.1f°C", weather.getTemperature()));
        tvTempRange.setText(String.format("%.1f°C/%.1f°C", 
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
        tvSky.setText(skyStatus);
        
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
        tvPrecipitationType.setText(precipitationType);
        String precipitationAmount = weather.getPrecipitation();
        if (precipitationAmount.equals("강수없음")) {
            precipitationAmount = "0mm";
        }
        else if (precipitationAmount.equals("1mm 미만")) {
            precipitationAmount = "< 1mm";
        }
        else if (precipitationAmount.equals("50.0mm 이상")) {
            precipitationAmount = "> 50mm";
        }
        tvPrecipitation.setText(precipitationAmount);

        ImageView weatherIcon = view.findViewById(R.id.iv_weather);

        // 강수 형태가 ��을 때는 하늘 상태에 따라 아이콘 설정
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
        view.findViewById(R.id.loading_layout).setVisibility(View.GONE);
    }

    /**
     * 새로운 할 일을 추가하는 다이얼로그를 표시합니다.
     * - 제목 입력
     * - 날짜 선택 가능
     */
    private void showAddTodoDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_todo, null);
        EditText editText = dialogView.findViewById(R.id.todo_title_input);
        MaterialCalendarView calendarView = dialogView.findViewById(R.id.calendarView);
        
        calendarView.setSelectedDate(CalendarDay.from(currentDate));

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle("Add New Task")
                .setView(dialogView)
                .setPositiveButton("Add", (dialogInterface, which) -> {
                    String title = editText.getText().toString().trim();
                    if (!title.isEmpty()) {
                        CalendarDay selectedDate = calendarView.getSelectedDate();
                        LocalDate localDate = LocalDate.of(selectedDate.getYear(), 
                                                        selectedDate.getMonth(), 
                                                        selectedDate.getDay());
                        String todoDate = localDate.toString();  // YYYY-MM-DD 형식으로 변환
                        
                        Todo newTodo = new Todo(title, todoDate);
                        todoRepository.insertTodo(newTodo, todos -> 
                            getActivity().runOnUiThread(() -> {
                                loadTodosByDate(currentDate, todoAdapter);
                                updateCalendarDecorations();
                            })
                        );
                    } else {
                        Toast.makeText(getActivity(), "Please enter a task title.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();

        // 다이얼로그가 보여질 때 버튼 색상 설정
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.blue));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.blue));
        });
        
        dialog.show();
    }


    @Override
    public void onTodoCheckedChanged(Todo todo, boolean isChecked) {
        todo.setCompleted(isChecked);
        todoRepository.updateTodo(todo, todos -> 
            getActivity().runOnUiThread(() -> {
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
            getActivity().runOnUiThread(() -> {
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
        new AlertDialog.Builder(getActivity())
                .setTitle("Edit Task")
                .setView(dialogView)
                .setPositiveButton("Edit", (dialog, which) -> {
                    String title = editText.getText().toString().trim();
                    if (!title.isEmpty()) {
                        todo.setTitle(title);
                        todoRepository.updateTodo(todo, todos -> 
                            getActivity().runOnUiThread(() -> {
                                loadTodosByDate(currentDate, todoAdapter);
                                if (todoAdapter != null) {
                                    loadTodosByDate(currentDate, todoAdapter);
                                }
                                updateCalendarDecorations();
                                loadTodosByDate(date, todoAdapter);
                            })
                        );
                    }
                })
                .setNegativeButton("Cancel", null)
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
            getActivity().runOnUiThread(() -> {
                loadTodosByDate(currentDate, todoAdapter);
                if (todoAdapter != null) {
                    loadTodosByDate(currentDate, todoAdapter);
                }
                updateCalendarDecorations();
                Toast.makeText(getActivity(), "Moved to tomorrow.", Toast.LENGTH_SHORT).show();
                loadTodosByDate(date, todoAdapter);
            })
        );
    }

    private void toggleNewsVisibility() {
        View newsContainer = view.findViewById(R.id.news_container);
        View mainContent = view.findViewById(R.id.main_content);
        ImageButton closeButton = view.findViewById(R.id.btn_close_news);

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
        Log.d("NewsAPI", "News data loading started");
        NewsAPIClient newsAPIClient = new NewsAPIClient();
        newsAPIClient.getNewsAPIService().getTopHeadlines("us", BuildConfig.NEWS_API_KEY)
            .enqueue(new Callback<NewsResponse>() {
                @Override
                public void onResponse(Call<NewsResponse> call, Response<NewsResponse> response) {
                    Log.d("NewsAPI", "Response received: " + response.code());
                    if (response.isSuccessful()) {
                        Log.d("NewsAPI", "Response successful");
                        if (response.body() != null) {
                            Log.d("NewsAPI", "Article count: " + response.body().getArticles().size());
                            getActivity().runOnUiThread(() -> {
                                newsAdapter.setArticles(response.body().getArticles());
                                newsLoadingSpinner.setVisibility(View.INVISIBLE);
                            });
                        } else {
                            Log.e("NewsAPI", "Response body is null");
                            showErrorToast("Failed to load news data.");
                        }
                    } else {
                        try {
                            String errorBody = response.errorBody().string();
                            Log.e("NewsAPI", "Error response: " + errorBody);
                            showErrorToast("Server error: " + errorBody);
                        } catch (IOException e) {
                            Log.e("NewsAPI", "Failed to read error response", e);
                            showErrorToast("Failed to read error response.");
                        }
                    }
                }

                @Override
                public void onFailure(Call<NewsResponse> call, Throwable t) {
                    Log.e("NewsAPI", "API call failed", t);
                    getActivity().runOnUiThread(() -> {
                        showErrorToast("News loading failed: " + t.getMessage());
                        newsLoadingSpinner.setVisibility(View.GONE);
                        MaterialButton retryButton = view.findViewById(R.id.retry_button);
                        retryButton.setVisibility(View.VISIBLE);
                        retryButton.setOnClickListener(v -> {
                            retryButton.setVisibility(View.GONE);
                            newsLoadingSpinner.setVisibility(View.VISIBLE);
                            loadNewsHeadlines();
                        });
                    });
                }
            });
    }

    private void showErrorToast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
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
            getActivity().runOnUiThread(() -> {
                // 메인 달력 업데이트
                MaterialCalendarView mainCalendarView = view.findViewById(R.id.calendar_view);
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
        calendarContainer = view.findViewById(R.id.calendar_container);
        calendarView = view.findViewById(R.id.calendar_view);
        calendarRecyclerTodos = view.findViewById(R.id.calendar_recycler_todos);
        calendarSelectedDateText = view.findViewById(R.id.tv_selected_date);
        calendarRecyclerTodos.setLayoutManager(new LinearLayoutManager(getActivity()));
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


        calendarRecyclerTodos.setLayoutManager(new LinearLayoutManager(getActivity()));
        calendarRecyclerTodos.setAdapter(calendarTodoAdapter);
        
        // 달 이벤트 설정
        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            LocalDate selectedDate = LocalDate.of(date.getYear(), date.getMonth(), date.getDay());
            calendarSelectedDateText.setText(selectedDate.format(DateTimeFormatter.ofPattern("yyyy MM dd (E)")));
            loadTodosByDate(selectedDate, calendarTodoAdapter);
        });

    }

    public void loadTodosByDate(LocalDate date, TodoAdapter adapter) {
        String dateStr = date.toString();
        todoRepository.getTodosByDate(dateStr, todos ->
            getActivity().runOnUiThread(() -> adapter.setTodos(todos))

        );
    }

    private void toggleCalendarVisibility() {
        if (calendarContainer.getVisibility() == View.VISIBLE) {
            calendarContainer.setVisibility(View.GONE);
        } else {
            calendarContainer.setVisibility(View.VISIBLE);
            isNewsVisible = false;

            calendarSelectedDateText.setText(currentDate.format(DateTimeFormatter.ofPattern("yyyy MM dd (E)")));
            loadTodosByDate(currentDate, calendarTodoAdapter);
            
            calendarView.setOnDateChangedListener((widget, date, selected) -> {
                LocalDate selectedDate = LocalDate.of(date.getYear(), date.getMonth(), date.getDay());
                calendarSelectedDateText.setText(selectedDate.format(DateTimeFormatter.ofPattern("yyyy MM dd (E)")));
                loadTodosByDate(selectedDate, calendarTodoAdapter);
            });
        }
    }



    private void initializeTarotCards() {
        tarotCards = new ArrayList<>();
        try {
            InputStream is = getActivity().getAssets().open("tarot/tarot-images.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);
            JSONObject jsonObject = new JSONObject(json);
            JSONArray cardsArray = jsonObject.getJSONArray("cards");

            for (int i = 0; i < cardsArray.length(); i++) {
                JSONObject cardObject = cardsArray.getJSONObject(i);
                String name = cardObject.getString("name");
                String img = cardObject.getString("img");
                JSONArray lightMeanings = cardObject.getJSONObject("meanings").getJSONArray("light");
                JSONArray shadowMeanings = cardObject.getJSONObject("meanings").getJSONArray("shadow");

                tarotCards.add(new TarotCard(name, img, lightMeanings, shadowMeanings));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupTarotButton() {
        MaterialButton btnTarot = view.findViewById(R.id.btn_tarot);
        btnTarot.setOnClickListener(v -> showTarotReading());
    }

    private void showTarotReading() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("TarotPrefs", MODE_PRIVATE);
        String savedCardName = sharedPreferences.getString("selectedCardName", null);
        boolean savedPositive = sharedPreferences.getBoolean("isPositive", true);
        LocalDate savedDate = LocalDate.parse(sharedPreferences.getString("selectedDate", LocalDate.now().toString()));

        TarotCard selectedCard;
        boolean positive;

        // 오늘 ���짜와 저장된 날짜가 다르면 새로운 카드를 선택
        if (!LocalDate.now().equals(savedDate) || savedCardName == null) {
            selectedCard = getRandomTarotCard();
            positive = isPositive();

            // 선택된 카드��� 상태를 저장
            sharedPreferences.edit()
                .putString("selectedCardName", selectedCard.getName())
                .putBoolean("isPositive", positive)
                .putString("selectedDate", LocalDate.now().toString())
                .apply();
        } else {
            // 저장된 카드 정보를 불러옴
            selectedCard = tarotCards.stream()
                .filter(card -> card.getName().equals(savedCardName))
                .findFirst()
                .orElse(getRandomTarotCard()); // 만약 저장된 카드가 없으면 랜덤으로 선택
            positive = savedPositive;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.TarotDialog);

        // 타이틀과 메시지를 가운데 정렬하기 위해 커스텀 레이아웃 사용
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_tarot_reading, null);
        TextView titleView = dialogView.findViewById(R.id.dialog_title);
        TextView messageView = dialogView.findViewById(R.id.dialog_message);
        ImageView imageView = dialogView.findViewById(R.id.dialog_image);

        titleView.setText(selectedCard.getName());
        String message = positive ? selectedCard.getRandomLightMeaning() : selectedCard.getRandomShadowMeaning();
        messageView.setText(message);

        try {
            InputStream is = getActivity().getAssets().open("tarot/" + selectedCard.getImg());
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            imageView.setImageBitmap(bitmap);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!positive) {
            imageView.setRotation(180);
        }

        builder.setView(dialogView);
        builder.setPositiveButton("Confirm", null);
        builder.show();
    }

    private TarotCard getRandomTarotCard() {
        int index = random.nextInt(tarotCards.size());
        return tarotCards.get(index);
    }

    private boolean isPositive() {
        return random.nextBoolean();
    }

    public TodoAdapter getTodoAdapter() {
        return todoAdapter;
    }
}
