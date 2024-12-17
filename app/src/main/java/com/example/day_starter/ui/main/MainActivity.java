package com.example.day_starter.ui.main;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.example.day_starter.R;
import com.example.day_starter.data.repository.weather.WeatherRepository;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    private BottomNavigationView bottomNavigationView;
    private FusedLocationProviderClient fusedLocationClient;
    private WeatherRepository weatherRepository;
    private MainFragment mainFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = findViewById(R.id.view_pager);
        viewPager.setOffscreenPageLimit(1);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // MainFragment 인스턴스 생성
        mainFragment = new MainFragment();
        viewPager.setAdapter(new FragmentAdapter(this, mainFragment));

        // 기본적으로 MainFragment를 띄우기 위해 현재 페이지를 1로 설정
        viewPager.setCurrentItem(1, false);
        bottomNavigationView.setSelectedItemId(R.id.action_main);

        viewPager.setPageTransformer(new ViewPager2.PageTransformer() {
            @Override
            public void transformPage(@NonNull View page, float position) {
                final float scaleFactor = 0.85f; // 화면이 작아지는 비율
                final float alphaFactor = 0.5f;  // 투명도 설정

                if (position < -1) { // 화면이 왼쪽으로 완전히 벗어남
                    page.setAlpha(0f);
                } else if (position <= 1) { // 중심 화면과 주변 화면에 대한 설정
                    float scale = Math.max(scaleFactor, 1 - Math.abs(position));
                    float alpha = Math.max(alphaFactor, 1 - Math.abs(position));

                    page.setScaleX(scale); // X축 크기 조절
                    page.setScaleY(scale); // Y축 크기 조절
                    page.setAlpha(alpha);  // 투명도 조절
                } else { // 화면이 오른쪽으로 완전히 벗어남
                    page.setAlpha(0f);
                }
            }
        });

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.action_menu) {
                viewPager.setCurrentItem(0);
                return true;
            } else if (item.getItemId() == R.id.action_main) {
                viewPager.setCurrentItem(1);
                return true;
            } else if (item.getItemId() == R.id.action_tomorrow) {
                viewPager.setCurrentItem(2);
                return true;
            } else {
                return false;
            }
        });

        // ViewPager의 페이지 변경 리스너 설정
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        bottomNavigationView.setSelectedItemId(R.id.action_menu);
                        break;
                    case 1:
                        bottomNavigationView.setSelectedItemId(R.id.action_main);
                        break;
                    case 2:
                        bottomNavigationView.setSelectedItemId(R.id.action_tomorrow);
                        break;
                }
            }
        });

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        checkLocationPermission();
    }

    private class FragmentAdapter extends FragmentStateAdapter {
        private final MainFragment mainFragment;
        private final DiaryFragment diaryFragment;
        private final MenuFragment menuFragment;

        public FragmentAdapter(FragmentActivity fa, MainFragment mainFragment) {
            super(fa);
            this.mainFragment = mainFragment;
            this.diaryFragment = new DiaryFragment();
            this.menuFragment = new MenuFragment();
        }

        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return menuFragment;
                case 1:
                    return mainFragment;
                case 2:
                    return diaryFragment;
                default:
                    return mainFragment;
            }
        }

        @Override
        public int getItemCount() {
            return 3;
        }
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
                        // 현재 페이지의 Fragment를 가져오기
                        Fragment fragment = getSupportFragmentManager().findFragmentByTag("f" + viewPager.getCurrentItem());
                        if (fragment instanceof MainFragment) {
                            fetchWeatherData(location);
                        }
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
                            mainFragment.updateWeatherUI();
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
            runOnUiThread(() -> mainFragment.updateWeatherUI());
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