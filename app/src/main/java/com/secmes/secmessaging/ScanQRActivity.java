package com.secmes.secmessaging;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;

import com.journeyapps.barcodescanner.CaptureActivity;

public class ScanQRActivity extends AppCompatActivity {

    private TextView resultTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_qr);

        resultTextView = findViewById(R.id.resultTextView);

        // Configura el escáner de código QR
        initiateBarcodeScanner();
    }

    private void initiateBarcodeScanner() {
        // Usamos la API de ML Kit Barcode Scanning para escanear códigos QR
        BarcodeScanner barcodeScanner = BarcodeScanning.getClient();

        // Iniciamos la actividad para escanear el código QR
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK) {
                    String qrContent = result.getData().getStringExtra("QR_CODE_RESULT");
                    Toast.makeText(ScanQRActivity.this, qrContent, Toast.LENGTH_SHORT).show();
                    processQRContent(qrContent);
                }
            }
        }).launch(new Intent(this, CaptureActivity.class));
    }

    private void processQRContent(String qrContent) {
        if (qrContent != null) {
            String[] parts = qrContent.split(";");
            String ipAddress = parts[0];  // IP del otro dispositivo
            String publicKey = parts[1];  // Clave pública RSA

            // Aquí podrías establecer la conexión y empezar a enviar mensajes
            resultTextView.setText("IP: " + ipAddress + "\nClave pública: " + publicKey);
        }
    }
}
