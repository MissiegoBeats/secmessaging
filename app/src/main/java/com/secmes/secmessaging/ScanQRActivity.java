package com.secmes.secmessaging;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class ScanQRActivity extends AppCompatActivity {

    private static final String TAG = "ScanQRActivity";
    private static final int REQUEST_CAMERA_PERMISSION = 101;
    private static final int REQUEST_WIFI_PERMISSION = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Comprobar permisos antes de iniciar el escaneo del QR
        if (checkPermissions()) {
            startQRScanner();
        } else {
            requestPermissions();
        }
    }

    // Comprobar si los permisos necesarios están concedidos
    private boolean checkPermissions() {
        boolean hasCameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        boolean hasWifiPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED;
        return hasCameraPermission && hasWifiPermission;
    }

    // Solicitar los permisos necesarios en tiempo de ejecución
    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA, Manifest.permission.ACCESS_WIFI_STATE},
                REQUEST_CAMERA_PERMISSION);
    }

    // Manejar los resultados de la solicitud de permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startQRScanner();
            } else {
                Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
                finish(); // Finalizar actividad si no se concede el permiso
            }
        }
    }

    // Iniciar el escáner de QR
    private void startQRScanner() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("Escanea el QR");
        integrator.setCameraId(0);  // Cámara trasera
        integrator.setBeepEnabled(false);
        integrator.setBarcodeImageEnabled(false);
        integrator.initiateScan();
    }

    // Manejar el resultado del escaneo del QR
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            String qrContent = result.getContents();
            if (qrContent != null) {
                // Procesar el contenido del QR
                handleQRContent(qrContent);
            } else {
                Toast.makeText(this, "No se pudo leer el QR", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Procesar el contenido escaneado del QR
    private void handleQRContent(String qrContent) {
        // Dividir el contenido recibido (formato: IP;PublicKey)
        String[] parts = qrContent.split(";");
        if (parts.length == 2) {
            String ipAddress = parts[0].trim().split(":")[1]; // Obtener la IP
            String publicKey = parts[1].trim().split(":")[1]; // Obtener la clave pública

            // Mostrar la IP y la clave pública en un Toast para verificar
            Toast.makeText(this, "IP: " + ipAddress + "\nPublicKey: " + publicKey, Toast.LENGTH_LONG).show();

            // Iniciar la actividad de chat, pasando la IP y la clave pública
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("IP", ipAddress);
            intent.putExtra("PublicKey", publicKey);
            startActivity(intent);
        } else {
            Toast.makeText(this, "QR no válido", Toast.LENGTH_SHORT).show();
        }
    }
}
