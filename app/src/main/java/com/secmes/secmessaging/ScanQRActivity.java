package com.secmes.secmessaging;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class ScanQRActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new IntentIntegrator(this).initiateScan(); // Inicia el escaneo del QR
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                String scannedPublicKey = result.getContents();
                // Aquí procesamos la clave pública recibida
                Intent intent = new Intent(this, ChatActivity.class);
                intent.putExtra("publicKey", scannedPublicKey);
                startActivity(intent);
            }
        }
    }
}
