package com.secmes.secmessaging;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button generateQRButton, scanQRButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        generateQRButton = findViewById(R.id.generateQRButton);
        scanQRButton = findViewById(R.id.scanQRButton);

        generateQRButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GenerateQRActivity.class);
            startActivity(intent);
        });

        scanQRButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ScanQRActivity.class);
            startActivity(intent);
        });
    }
}
