package com.example.day_starter.ui.main;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.example.day_starter.R;
import com.example.day_starter.data.repository.weather.WeatherRepository;
import com.example.day_starter.util.ColorManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {
    private FusedLocationProviderClient fusedLocationClient;
    private WeatherRepository weatherRepository;
    private MainFragment mainfragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainfragment = (MainFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        checkLocationPermission();

        // Fragment 초기화
        if (savedInstanceState == null) {
            loadFragment(mainfragment);
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.action_main);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.action_menu) {
                // 다른 Fragment로 전환하는 로직 추가
                return true;
            } else if (item.getItemId() == R.id.action_main) {
                loadFragment(mainfragment);
                return true;
            } else if (item.getItemId() == R.id.action_tomorrow) {
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            getLastKnownLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, 
                                           String[] permissions, 
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastKnownLocation();
            } else {
                Toast.makeText(this, "Location permission is required.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        // MainFragment에 위치 정보를 전달
                        MainFragment fragment = (MainFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                        fetchWeatherData(location);

                    }
                })
                .addOnFailureListener(e -> 
                    Toast.makeText(this, "Could not get location", Toast.LENGTH_SHORT).show()
                );
    }

    
    private void fetchWeatherData(Location location) {
        if (location != null) {
            weatherRepository = new WeatherRepository();
            weatherRepository.getWeatherData(
                location.getLatitude(), 
                location.getLongitude(), 
                new WeatherDataCallback() {
                    @Override
                    public void onWeatherDataReceived() {
                        runOnUiThread(() -> {
                            mainfragment.updateWeatherUI();
                        });
                    }
                    
                    @Override
                    public void onWeatherDataFailed() {
                        runOnUiThread(() -> {
                            showRetryButton();
                        });
                    }
                }
            );
        }
    }

    private class WeatherDataCallback implements WeatherRepository.WeatherCallback {
        @Override
        public void onWeatherDataReceived() {
            runOnUiThread(() -> mainfragment.updateWeatherUI());
        }
        @Override
        public void onWeatherDataFailed() {
            runOnUiThread(() -> showRetryButton());
        }
    }


    private void showRetryButton() {
        findViewById(R.id.loading_progress).setVisibility(View.GONE);
        findViewById(R.id.loading_text).setVisibility(View.GONE);

        MaterialButton retryButton = findViewById(R.id.retry_button);


        retryButton.setVisibility(View.VISIBLE);
        retryButton.setOnClickListener(v -> {
            retryButton.setVisibility(View.GONE);
            findViewById(R.id.loading_progress).setVisibility(View.VISIBLE);
            findViewById(R.id.loading_text).setVisibility(View.VISIBLE);

            // 위치 정보 다시 가져오기
            checkLocationPermission();
        });
    }
} 