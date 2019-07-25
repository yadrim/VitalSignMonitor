package com.example.vitalsignmonitor;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.vitalsignmonitor.Repositories.PatientRepository;

public class MainActivity extends AppCompatActivity {

    Button cmdShowSetting;
    Button cmdShowPatients;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cmdShowSetting = findViewById(R.id.cmdShowSetting);
        cmdShowPatients = findViewById(R.id.cmdShowPatients);

        cmdShowSetting.setOnClickListener(v -> {
            Intent settingIntent = new Intent(this, ConnectionActivity.class);
            startActivity(settingIntent);
        });

        cmdShowPatients.setOnClickListener(v -> {
            Intent listIntent = new Intent(this, PatientListActivity.class);
            startActivity(listIntent);
        });

        PatientRepository.checkInitialData();
    }
}
