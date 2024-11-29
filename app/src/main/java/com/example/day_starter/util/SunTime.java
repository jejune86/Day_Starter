package com.example.day_starter.util;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class SunTime {
    private static final String TAG = "SunTime";
    private String sunrise;
    private String sunset;

    public SunTime(Context context) {
        loadSunData(context);
    }

    public String getSunrise() {
        return sunrise;
    }

    public String getSunset() {
        return sunset;
    }

    private void loadSunData(Context context) {
        try {
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(context.getAssets().open("sun.csv"))
            );
            
        
            
            // 1월 1일부터 현재 날짜까지의 일수 계산
            Calendar cal = Calendar.getInstance();
            int dayOfYear = cal.get(Calendar.DAY_OF_YEAR);
            
            // 헤더 라인 건너뛰기
            reader.readLine();
            
            // 원하는 라인으로 직접 이동
            for (int i = 1; i < dayOfYear; i++) {
                reader.readLine();
            }
            
            // 현재 날짜의 데이터 읽기
            String line = reader.readLine();
            if (line != null) {
                String[] data = line.split(",");
                this.sunrise = data[2].substring(0, 5).replace(":", "");
                this.sunset = data[3].substring(0, 5).replace(":", "");
            }
            
            reader.close();
        } catch (IOException e) {
            Log.e(TAG, "Error reading sun.csv: " + e.getMessage());
        }
    }
}