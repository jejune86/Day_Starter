package com.example.day_starter.model.weather;

public class Weather {
    private static Weather instance;
    private final double temperature;
    private final int sky;
    private final int precipitationType;
    private final String precipitation;

    private Weather(double temperature, int sky, int precipitationType, String precipitation) {
        this.temperature = temperature;
        this.sky = sky;
        this.precipitationType = precipitationType;
        this.precipitation = precipitation;
    }

    public static Weather getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Weather instance is not initialized.");
        }
        return instance;
    }

    public static void setInstance(double temperature, int sky, int precipitationType, String precipitation) {
        instance = new Weather(temperature, sky, precipitationType, precipitation);
    }

    public double getTemperature() {
        return temperature;
    }

    public int getSky() {
        return sky;
    }

    public int getPrecipitationType() {
        return precipitationType;
    }

    public String getPrecipitation() {
        return precipitation;
    }
}
