package com.example.day_starter;

import android.app.Application;
import com.jakewharton.threetenabp.AndroidThreeTen;

public class DayStarterApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AndroidThreeTen.init(this);
    }
}
