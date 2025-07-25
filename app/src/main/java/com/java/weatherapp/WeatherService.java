package com.java.weatherapp;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherService {
    @GET("weather")
    Call<WeatherApp> getWeather(

            @Query("q") String city,
            @Query("appid") String apiKey,
            @Query("units") String units
    );
}
