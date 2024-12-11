package com.example.day_starter.ui.main;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.TextView;
import com.example.day_starter.R;

public class NewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new);

        TextView titleTextView = findViewById(R.id.title_text);
        titleTextView.setText("New Activity Title");
    }
} 