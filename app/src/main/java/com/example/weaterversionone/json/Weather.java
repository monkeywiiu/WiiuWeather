package com.example.weaterversionone.json;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Administrator on 2017/10/16 0016.
 */

public class Weather {
    public String status;
    public Basic basic;
    public Now now;
    public Suggestion suggestion;
    public AQI aqi;

    @SerializedName("daily_forecast")
    public List<ForeCast> foreCastList;
}
