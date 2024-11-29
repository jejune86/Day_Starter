package com.example.day_starter.data.api.client;

import com.example.day_starter.data.api.service.NewsAPIService;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NewsAPIClient {
    private static final String BASE_URL = "https://newsapi.org/";
    private final Retrofit retrofit;

    public NewsAPIClient() {
        OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(chain -> {
                Request original = chain.request();
                Request request = original.newBuilder()
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36")
                    .method(original.method(), original.body())
                    .build();
                return chain.proceed(request);
            })
            .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public NewsAPIService getNewsAPIService() {
        return retrofit.create(NewsAPIService.class);
    }
} 