package com.example.day_starter.model;

import org.json.JSONArray;
import java.util.Random;

public class TarotCard {
    private String name;
    private String img;
    private JSONArray lightMeanings;
    private JSONArray shadowMeanings;
    private Random random = new Random();

    public TarotCard(String name, String img, JSONArray lightMeanings, JSONArray shadowMeanings) {
        this.name = name;
        this.img = img;
        this.lightMeanings = lightMeanings;
        this.shadowMeanings = shadowMeanings;
    }

    public String getName() {
        return name;
    }

    public String getImg() {
        return img;
    }

    public String getRandomLightMeaning() {
        int index = random.nextInt(lightMeanings.length());
        return lightMeanings.optString(index);
    }

    public String getRandomShadowMeaning() {
        int index = random.nextInt(shadowMeanings.length());
        return shadowMeanings.optString(index);
    }
} 