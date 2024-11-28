package com.example.musicrecommendation.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import com.example.musicrecommendation.data.model.weather.Weather;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BackgroundColorManager {
    private final SunTime sunTime;

    public BackgroundColorManager(Context context) {
        this.sunTime = new SunTime(context);
    }

    public GradientDrawable getBackgroundDrawable() {
        Weather weather = Weather.getInstance();
        SimpleDateFormat timeFormat = new SimpleDateFormat("HHmm", Locale.getDefault());
        String currentTime = timeFormat.format(new Date());
        
        int current = Integer.parseInt(currentTime);
        int sunrise = Integer.parseInt(sunTime.getSunrise());
        int sunset = Integer.parseInt(sunTime.getSunset());

        GradientDrawable gradient = new GradientDrawable();
        gradient.setShape(GradientDrawable.RECTANGLE);

        if (current < sunrise) {
            // 새벽
            gradient.setColors(new int[]{
                Color.rgb(25, 25, 112),  // 미드나이트 블루
                Color.rgb(15, 15, 40)    // 더 어두운 블루
            });
        } else if (current < sunrise + 100) {
            // 일출
            int[] colors = getSunriseGradientColors(weather);
            gradient.setColors(colors);
        } else if (current < sunset - 100) {
            // 낮
            int[] colors = getDaytimeGradientColors(weather);
            gradient.setColors(colors);
        } else if (current < sunset + 100) {
            // 일몰
            int[] colors = getSunsetGradientColors(weather);
            gradient.setColors(colors);
        } else {
            // 밤
            gradient.setColors(new int[]{
                Color.rgb(25, 25, 112),  // 미드나이트 블루
                Color.rgb(15, 15, 40)    // 더 어두운 블루
            });
        }

        gradient.setOrientation(GradientDrawable.Orientation.TOP_BOTTOM);
        return gradient;
    }

    private int[] getSunriseGradientColors(Weather weather) {
        if (weather.getSky() <= 5) {
            return new int[]{
                Color.rgb(255, 127, 80),  // 맑은 코랄
                Color.rgb(200, 100, 63)   // 더 어두운 코랄
            };
        } else if (weather.getSky() <= 8) {
            return new int[]{
                Color.rgb(230, 116, 71),  // 구름 많은 일출
                Color.rgb(180, 85, 55)    // 더 어두운 구름 많은 일출
            };
        } else {
            return new int[]{
                Color.rgb(210, 105, 67),  // 흐린 일출
                Color.rgb(150, 75, 48)    // 더 어두운 일출
            };
        }
    }

    private int[] getSunsetGradientColors(Weather weather) {
        if (weather.getSky() <= 5) {
            return new int[]{
                Color.rgb(255, 69, 0),    // 맑은 오렌지레드
                Color.rgb(200, 54, 0)     // 더 어두운 오렌지레드
            };
        } else if (weather.getSky() <= 8) {
            return new int[]{
                Color.rgb(230, 63, 0),    // 구름 많은 일몰
                Color.rgb(180, 48, 0)     // 더 어두운 구름 많은 일몰
            };
        } else {
            return new int[]{
                Color.rgb(210, 57, 0),    // 흐린 일몰
                Color.rgb(150, 41, 0)     // 더 어두운 일몰
            };
        }
    }

    private int[] getDaytimeGradientColors(Weather weather) {
        if (weather.getSky() <= 5) {
            return new int[]{
                Color.rgb(135, 206, 235), // 하늘색
                Color.rgb(100, 155, 176)  // 더 어두운 하늘색
            };
        } else if (weather.getSky() <= 8) {
            return new int[]{
                Color.rgb(176, 196, 222), // 연한 강철 블루
                Color.rgb(132, 147, 167)  // 더 어두운 강철 블루
            };
        } else {
            return new int[]{
                Color.rgb(128, 128, 128), // 회색
                Color.rgb(96, 96, 96)     // 더 어두운 회색
            };
        }
    }
}