package com.example.day_starter.data.repository.weather;


import android.util.Log;

import com.example.day_starter.BuildConfig;
import com.example.day_starter.data.api.client.WeatherAPIClient;
import com.example.day_starter.data.api.service.WeatherAPIService;
import com.example.day_starter.model.weather.Weather;
import com.example.day_starter.model.weather.WeatherResponse;
import com.example.day_starter.util.GridXY;

import java.util.Calendar;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WeatherRepository {
    private static final String TAG = "WeatherService";
    private WeatherAPIService weatherAPIService;

    public WeatherRepository() {
        WeatherAPIClient weatherAPIClient = new WeatherAPIClient();
        weatherAPIService = weatherAPIClient.getWeatherAPIService();
    }
    
    // WeatherCallback 인터페이스 정의
    public interface WeatherCallback {
        void onWeatherDataReceived();
    }

    public void getWeatherData(double latitude, double longitude, WeatherCallback callback) {
        GridXY grid = new GridXY(latitude, longitude);

        // Calculate the current date and the closest base time
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        Log.d(TAG, "adjusted hour = "+hour);
        List<String> baseHours = List.of("02", "05", "08", "11", "14", "17", "20", "23");

        // Find the nearest previous base hour
        String baseHour = "-1";
        for (String bh : baseHours) {
            int bhInt = Integer.parseInt(bh);
            if (bhInt < hour) {
                baseHour = bh;
            } else {
                break;
            }
        }
        int baseHourInt;
        if (baseHour.equals("-1")) {
            calendar.add(Calendar.DATE, -1);
            baseHour = baseHours.get(baseHours.size() - 1); // "23"
        }
        baseHourInt = Integer.parseInt(baseHour);


        calendar.set(Calendar.HOUR_OF_DAY, baseHourInt);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        // Set the date and time
        String baseDate = String.format("%tY%<tm%<td", calendar);
        String baseTime = String.format("%02d00", baseHourInt);
        Log.d(TAG, "baseDate = " + baseDate + "  baseTime = " + baseTime);
        Log.d(TAG, "X = " + grid.x + "  Y = " + grid.y);
        String serviceKey = BuildConfig.WEATHER_API_KEY;
        weatherAPIService.getWeather(
            serviceKey,
            "400",
            "1",
            "JSON",
            baseDate,
            baseTime,
            grid.x,
            grid.y
        ).enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    processWeatherResponse(response.body());
                    if (callback != null) {
                        callback.onWeatherDataReceived();
                    }
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                Log.e(TAG, "API call failed: " + t.getMessage());
            }
        });
    }

    private void processWeatherResponse(WeatherResponse response) {
        if (response != null && response.getResponse() != null) {
            String resultCode = response.getResponse().getHeader().getResultCode();

            if ("00".equals(resultCode)) {
                WeatherResponse.Response.Body body = response.getResponse().getBody();
                if (body != null && body.getItems() != null) {
                    List<WeatherResponse.Response.Body.Items.Item> items = body.getItems().getItem();
                    
                    Calendar now = Calendar.getInstance();
                    String today = String.format("%tY%<tm%<td", now);
                    String currentHour = String.format("%02d00", now.get(Calendar.HOUR_OF_DAY));
                    
                    double temp = 0;
                    int sky = 0;
                    int pty = 0;
                    double minTemp = 50;
                    double maxTemp = -50;
                    String pcp = "0mm";

                    for (WeatherResponse.Response.Body.Items.Item item : items) {
                        Log.d(TAG, "fcstTime : "+ item.getFcstTime());
                        if (item.getFcstTime().equals(currentHour)) {
                            switch (item.getCategory()) {
                                case "SKY":
                                    Log.d(TAG, "SKY value: " + item.getFcstValue());
                                    sky = item.getFcstValueAsInt();
                                    break;
                                case "TMP":
                                    Log.d(TAG, "TMP value: " + item.getFcstValue());
                                    temp = item.getFcstValueAsDouble();
                                    break;
                                case "PTY":
                                    Log.d(TAG, "PTY value: " + item.getFcstValue());
                                    pty = item.getFcstValueAsInt();
                                    break;
                                case "PCP":
                                    String value = item.getFcstValue();
                                    Log.d(TAG, "PCP value: " + value);
                                    pcp = value;
                                    break;
                            }
                        }
                        if (item.getFcstDate().equals(today) && item.getCategory().equals("TMP")) {
                            int fcstTime = Integer.parseInt(item.getFcstTime());
                            Log.d(TAG, fcstTime+": "+item.getFcstValue());
                            int currentTime = Integer.parseInt(currentHour);
                            
                            // 현재 시간 이후의 예보만 처리
                            if (fcstTime >= currentTime) {
                                if (minTemp > item.getFcstValueAsDouble()) {
                                    Log.d(TAG, "Min TMP value = " + item.getFcstValue() + " at " + item.getFcstTime());
                                    minTemp = item.getFcstValueAsDouble();
                                }
                                if (maxTemp < item.getFcstValueAsDouble()) {
                                    Log.d(TAG, "Max TMP value = " + item.getFcstValue() + " at " + item.getFcstTime());
                                    maxTemp = item.getFcstValueAsDouble();
                                }
                            }
                        }

                        if (Integer.parseInt(item.getFcstDate()) > Integer.parseInt(today)) {
                            break;
                        }
                    }
                    
                    // Weather 인스턴스 초기화
                    Weather.setInstance(temp, minTemp, maxTemp, sky, pty, pcp);
                }
            } else {
                Log.e(TAG, "API Error: " + response.getResponse().getHeader().getResultMsg());
                Weather.setInstance(0, 0, 0, 0, 0, "0");
            }
        }
    }

}
