package com.secmes.secmessaging;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class ScanQRActivity extends AppCompatActivity {

    private static final String TAG = "ScanQRActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Iniciar escaneo del QR
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("Escanea el código QR para conectar");
        integrator.setBeepEnabled(true);
        integrator.setCameraId(0); // Usar la cámara trasera
        integrator.setOrientationLocked(false);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() != null) {
                // Obtener contenido del QR
                String qrContent = result.getContents();
                Log.d(TAG, "Contenido escaneado: " + qrContent);

                // Procesar contenido del QR
                handleQRContent(qrContent);
            } else {
                // Escaneo cancelado
                Log.e(TAG, "Escaneo cancelado o sin contenido");
                Toast.makeText(this, "Escaneo cancelado", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void handleQRContent(String qrContent) {
        if (qrContent == null || qrContent.isEmpty()) {
            Toast.makeText(this, "El contenido del QR es inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        // Dividir el contenido recibido (formato: IP;ClavePública)
        String[] parts = qrContent.split(";");
        if (parts.length == 2) {
            String ipAddress = parts[0].trim();
            String publicKey = parts[1].trim();

            Log.d(TAG, "IP recibida: " + ipAddress);
            Log.d(TAG, "Clave pública recibida: " + publicKey);

            Toast.makeText(this, "Contenido del QR: "+ qrContent, Toast.LENGTH_LONG).show();

            // Validar la IP y la clave pública antes de proceder
            if (isValidIp(ipAddress) && !publicKey.isEmpty()) {
                Toast.makeText(this, "Conexión recibida\nIP: " + ipAddress, Toast.LENGTH_LONG).show();

                // Iniciar conexión segura con la información recibida
                startSecureConnection(ipAddress, publicKey);
            } else {
                Toast.makeText(this, "El QR no contiene información válida", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "El QR no contiene información válida", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isValidIp(String ip) {
        String ipRegex =
                "^((25[0-5]|2[0-4][0-9]|[0-1]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[0-1]?[0-9][0-9]?)$";
        return ip.matches(ipRegex);
    }

    private void startSecureConnection(String ipAddress, String publicKey) {
        // Aquí puedes iniciar la conexión con el dispositivo remoto
        // usando sockets y un canal de comunicación segura

        // Redirigir al ChatActivity para continuar
        Intent chatIntent = new Intent(this, ChatActivity.class);
        chatIntent.putExtra("ipAddress", ipAddress);
        chatIntent.putExtra("publicKey", publicKey);
        startActivity(chatIntent);

        // Finalizar esta actividad
        finish();
    }
}
