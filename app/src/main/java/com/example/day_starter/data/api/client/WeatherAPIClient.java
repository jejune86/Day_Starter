package com.example.day_starter.data.api.client;

import com.example.day_starter.data.api.service.WeatherAPIService;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WeatherAPIClient {
    private static final String BASE_URL = "https://apis.data.go.kr/";

    private final Retrofit retrofit;

    public WeatherAPIClient() {
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public WeatherAPIService getWeatherAPIService() {
        return retrofit.create(WeatherAPIService.class);
    }
}
