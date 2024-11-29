package com.example.day_starter.util;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;

import com.example.day_starter.model.weather.Weather;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ColorManager {
    private static final int CLEAR_NIGHT1 = Color.rgb(45, 45, 132);
    private static final int CLEAR_NIGHT2 = Color.rgb(35, 35, 80);
    private static final int CLEAR_SUNRISE1 = Color.rgb(255, 147, 100);
    private static final int CLEAR_SUNRISE2 = Color.rgb(220, 120, 83);
    private static final int CLEAR_SUNSET1 = Color.rgb(255, 89, 20);    // 더 밝은 오렌지레드
    private static final int CLEAR_SUNSET2 = Color.rgb(220, 74, 20);    // 더 밝은 다크 오렌지레드
    private static final int CLOUDY_SUNRISE1 = Color.rgb(250, 136, 91);
    private static final int CLOUDY_SUNRISE2 = Color.rgb(200, 105, 75);
    private static final int CLOUDY_SUNSET1 = Color.rgb(250, 83, 20);   // 더 밝은 구름 많은 일몰
    private static final int CLOUDY_SUNSET2 = Color.rgb(200, 68, 20);   // 더 밝은 다크 구름 많은 일몰
    private static final int RAINY_SUNRISE1 = Color.rgb(230, 125, 87);
    private static final int RAINY_SUNRISE2 = Color.rgb(170, 95, 68);
    private static final int RAINY_SUNSET1 = Color.rgb(230, 77, 20);    // 더 밝은 흐린 일몰
    private static final int RAINY_SUNSET2 = Color.rgb(170, 61, 20);    // 밝은 다크 흐린 일몰
    private static final int DEFAULT_SKY1 = Color.rgb(255, 255, 255);
    private static final int DEFAULT_SKY2 = Color.rgb(255, 255, 255);
    private static final int DAYTIME_CLEAR1 = Color.rgb(155, 226, 255); // 더 밝은 하늘색
    private static final int DAYTIME_CLEAR2 = Color.rgb(120, 175, 196); // 더 밝은 다크 하늘색
    private static final int DAYTIME_CLOUDY1 = Color.rgb(196, 216, 242); // 더 밝은 강철 블루
    private static final int DAYTIME_CLOUDY2 = Color.rgb(152, 167, 187); // 더 밝은 다크 강철 블루
    private static final int DAYTIME_RAINY1 = Color.rgb(148, 148, 148);  // 더 밝은 회색
    private static final int DAYTIME_RAINY2 = Color.rgb(116, 116, 116);  // 더 밝은 다크 회색
    

    private final SunTime sunTime;
    private int backgroundColor1;
    private int backgroundColor2;
    private GradientDrawable backgroundDrawable;
    
    public ColorManager(Context context) {
        this.sunTime = new SunTime(context);
        this.backgroundDrawable = new GradientDrawable();
        calculateBackgroundColor();
    }

    public int getTextColor() {


        // 밝기 계산 (0.299*R + 0.587*G + 0.114*B)
        int red = Color.red(backgroundColor1);
        int green = Color.green(backgroundColor1);
        int blue = Color.blue(backgroundColor1);
        double brightness = (0.299 * red + 0.587 * green + 0.114 * blue);

        // 밝기가 128 이상이면 검은색, 그렇지 않으면 흰색
        return brightness >= 128 ? Color.BLACK : Color.WHITE;
    }

    public GradientDrawable getBackgroundDrawable() {
        return backgroundDrawable;
    }

    private void calculateBackgroundColor() {
        GradientDrawable gradient = new GradientDrawable();
        gradient.setShape(GradientDrawable.RECTANGLE);
        
        // 기본 배경색 설정
        gradient.setColors(new int[]{
            DEFAULT_SKY1,
            DEFAULT_SKY2
        });
        backgroundColor1 = DEFAULT_SKY1;
        backgroundColor2 = DEFAULT_SKY2;
        gradient.setOrientation(GradientDrawable.Orientation.TOP_BOTTOM);
        
        try {
            Weather weather = Weather.getInstance();
            SimpleDateFormat timeFormat = new SimpleDateFormat("HHmm", Locale.getDefault());
            String currentTime = timeFormat.format(new Date());
            
            int current = Integer.parseInt(currentTime);
            int sunrise = Integer.parseInt(sunTime.getSunrise());
            int sunset = Integer.parseInt(sunTime.getSunset());

            if (current < sunrise) {
                backgroundColor1 = CLEAR_NIGHT1;
                backgroundColor2 = CLEAR_NIGHT2;
            } else if (current < sunrise + 100) {
                backgroundColor1 = getSunriseGradientColors(weather)[0];
                backgroundColor2 = getSunriseGradientColors(weather)[1];
            } else if (current < sunset - 100) {
                backgroundColor1 = getDaytimeGradientColors(weather)[0];
                backgroundColor2 = getDaytimeGradientColors(weather)[1];
            } else if (current < sunset + 100) {
                backgroundColor1 = getSunsetGradientColors(weather)[0];
                backgroundColor2 = getSunsetGradientColors(weather)[1];
            } else {
                backgroundColor1 = CLEAR_NIGHT1;
                backgroundColor2 = CLEAR_NIGHT2;
            }
        } catch (IllegalStateException e) {
            // Weather 인스턴스가 초기화되지 않은 경우 기본 배경색 유지
        }
        backgroundDrawable.setColors(new int[]{
            backgroundColor1,
            backgroundColor2
        });
    }

    private int[] getSunriseGradientColors(Weather weather) {
        if (weather.getSky() <= 5) {
            return new int[]{
                CLEAR_SUNRISE1,
                CLEAR_SUNRISE2
            };
        } else if (weather.getSky() <= 8) {
            return new int[]{
                CLOUDY_SUNRISE1,
                CLOUDY_SUNRISE2
            };
        } else {
            return new int[]{
                RAINY_SUNRISE1,
                RAINY_SUNRISE2
            };
        }
    }

    private int[] getSunsetGradientColors(Weather weather) {
        if (weather.getSky() <= 5) {
            return new int[]{
                CLEAR_SUNSET1,
                CLEAR_SUNSET2
            };
        } else if (weather.getSky() <= 8) {
            return new int[]{
                CLOUDY_SUNSET1,
                CLOUDY_SUNSET2
            };
        } else {
            return new int[]{
                RAINY_SUNSET1,
                RAINY_SUNSET2
            };
        }
    }

    private int[] getDaytimeGradientColors(Weather weather) {
        if (weather.getSky() <= 5) {
            return new int[]{
                DAYTIME_CLEAR1,
                DAYTIME_CLEAR2
            };
        } else if (weather.getSky() <= 8) {
            return new int[]{
                DAYTIME_CLOUDY1,
                DAYTIME_CLOUDY2
            };
        } else {
            return new int[]{
                DAYTIME_RAINY1,
                DAYTIME_RAINY2
            };
        }
    }

    public int getToolbarColor() {
        return backgroundColor2;
    }
}