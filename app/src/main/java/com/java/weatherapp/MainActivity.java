package com.java.weatherapp;

import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.airbnb.lottie.LottieAnimationView;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    SearchView searchView;
    TextView tvCity, tvTemp, tvDesc, tvMax, tvMin, tvHumidity, tvWind, tvSunrise, tvSunset, tvPressure,tvCondition,tvDay,tvDate;
    LottieAnimationView lottieView;
    ImageView iconView;
    WeatherService service;
    View main;

    ConstraintLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        searchView = findViewById(R.id.searchView);
        tvCity = findViewById(R.id.textView2);
        tvTemp = findViewById(R.id.textView5);
        tvDesc = findViewById(R.id.textView4);
        tvDate=findViewById(R.id.textView7);
        tvDay = findViewById(R.id.textView6);
        tvMax = findViewById(R.id.textView3);
        tvMin = findViewById(R.id.textView8);
        tvHumidity = findViewById(R.id.linearLayout7).findViewById(R.id.humidity_text);
        tvWind = findViewById(R.id.linearLayout8).findViewById(R.id.wind_text);
        tvSunrise = findViewById(R.id.linearLayout10).findViewById(R.id.sunrise_text);
        tvSunset = findViewById(R.id.linearLayout11).findViewById(R.id.sunset_text);
        tvPressure = findViewById(R.id.linearLayout12).findViewById(R.id.pressure_text);
        tvCondition = findViewById(R.id.conditions_text);
        lottieView = findViewById(R.id.lottieView);

        layout = findViewById(R.id.main);
        lottieView.setAnimation(R.raw.sun);
        lottieView.playAnimation();

        // Retrofit setup
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/data/2.5/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        service = retrofit.create(WeatherService.class);

        // Get default weather data (e.g., Delhi)
        getWeatherData("Dehradun");

        // SearchView listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                getWeatherData(query.trim());
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });


        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault()); // e.g., Monday
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()); // e.g., 25 July 2025

        String day = dayFormat.format(calendar.getTime());
        String date = dateFormat.format(calendar.getTime());


        tvDay.setText(day);
        tvDate.setText(date);
    }

    private void getWeatherData(String cityName) {
        Call<WeatherApp> call = service.getWeather(cityName, "f42591c721a300a2e67cc3ddb1b095b1", "metric");

        call.enqueue(new Callback<WeatherApp>() {
            @Override
            public void onResponse(@NonNull Call<WeatherApp> call, @NonNull Response<WeatherApp> response) {
                if (response.isSuccessful() && response.body() != null) {
                    WeatherApp weather = response.body();

                    tvCity.setText(weather.getName());
                    tvTemp.setText(String.format(Locale.ENGLISH, "%.1f °C", weather.getMain().getTemp()));
                    tvDesc.setText(weather.getWeather().get(0).getMain());

                    tvMax.setText(String.format(Locale.ENGLISH, "Max: %.1f °C", weather.getMain().getTempMax()));
                    tvMin.setText(String.format(Locale.ENGLISH, "Min: %.1f °C", weather.getMain().getTempMin()));
                    tvHumidity.setText(String.format(Locale.ENGLISH, "%d %%", weather.getMain().getHumidity()));
                    tvWind.setText(String.format(Locale.ENGLISH, "%.1f m/s", weather.getWind().getSpeed()));
                    tvPressure.setText(String.format(Locale.ENGLISH, "%d hPa", weather.getMain().getPressure()));
                    tvSunrise.setText(getTime(weather.getSys().getSunrise()));
                    tvSunset.setText(getTime(weather.getSys().getSunset()));

                    long sunrise = weather.getSys().getSunrise(); // in seconds
                    long sunset = weather.getSys().getSunset();   // in seconds
                    long currentTime = System.currentTimeMillis() / 1000L; // in seconds

                    String condition = weather.getWeather().get(0).getMain().toLowerCase(Locale.ROOT);


                    boolean isNight = currentTime < sunrise || currentTime > sunset;

                    if (isNight) {
                        switch (condition) {
                            case "rain":
                                lottieView.setAnimation(R.raw.night_rain);  // You should add this animation to your project
                                tvCondition.setText("RAINY NIGHT");
                                layout.setBackgroundResource(R.drawable.rain_background);
                                break;
                            case "clouds":
                                lottieView.setAnimation(R.raw.night_cloud);  // Add night cloud animation
                                tvCondition.setText("CLOUDY NIGHT");
                                layout.setBackgroundResource(R.drawable.colud_background);
                                break;
                            case "clear":
                                lottieView.setAnimation(R.raw.night_clear);  // Add moon/star animation
                                tvCondition.setText("CLEAR NIGHT");
                                break;
                            default:
                                lottieView.setAnimation(R.raw.night_clear);  // Generic fallback night animation
                                tvCondition.setText("NIGHT");
                        }
                    } else {
                        switch (condition) {
                            case "rain":
                                lottieView.setAnimation(R.raw.rain);
                                tvCondition.setText("RAINY");
                                layout.setBackgroundResource(R.drawable.rain_background);
                                break;
                            case "clouds":
                                lottieView.setAnimation(R.raw.cloud);
                                tvCondition.setText("CLOUDY");
                                layout.setBackgroundResource(R.drawable.colud_background);
                                break;
                            case "clear":
                                lottieView.setAnimation(R.raw.sun);
                                tvCondition.setText("CLEAR");
                                break;
                            default:
                                lottieView.setAnimation(R.raw.sun);
                                tvCondition.setText("SUNNY");
                        }
                    }
                    lottieView.playAnimation();

                }
            }

            @Override
            public void onFailure(@NonNull Call<WeatherApp> call, @NonNull Throwable t) {
                // Optional: Show a Toast or log the error
                t.printStackTrace();
            }
        });
    }

    private String getTime(long unixTime) {
        Date date = new Date(unixTime * 1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
        return sdf.format(date);
    }
}
