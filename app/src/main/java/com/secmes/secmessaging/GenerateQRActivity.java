package com.secmes.secmessaging;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.net.InetAddress;

public class GenerateQRActivity extends AppCompatActivity {

    private ImageView qrImageView;
    private String ipAddress;
    private String publicKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_qr);

        qrImageView = findViewById(R.id.qrImageView);

        try {
            // Obtener la IP del dispositivo
            ipAddress = getLocalIpAddress();
            Log.d("GenerateQR", "IP obtenida: " + ipAddress);

            // Obtener la clave pública generada dinámicamente
            publicKey = RSAUtils.generateKeyPair().getPublic().toString();
            Log.d("GenerateQR", "Clave pública generada: " + publicKey);

            // Crear contenido del QR
            String qrContent = ipAddress + ";" + publicKey;
            Log.d("GenerateQR", "Contenido del QR: " + qrContent);

            // Mostrar contenido del QR en un Toast
            Toast.makeText(this, "Contenido del QR: " + qrContent, Toast.LENGTH_LONG).show();

            // Generar el QR y mostrarlo en la pantalla
            Bitmap qrBitmap = generateQRCode(qrContent);
            if (qrBitmap != null) {
                qrImageView.setImageBitmap(qrBitmap);
            } else {
                Toast.makeText(this, "Error al generar el código QR", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.e("GenerateQR", "Error al generar el QR", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Método para obtener la dirección IP del dispositivo
    private String getLocalIpAddress() {
        try {
            for (java.util.Enumeration<java.net.NetworkInterface> en = java.net.NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                java.net.NetworkInterface networkInterface = en.nextElement();
                for (java.util.Enumeration<InetAddress> enumIpAddr = networkInterface.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress.isSiteLocalAddress()) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            Log.e("GenerateQR", "Error al obtener la IP local", e);
        }
        return null;
    }

    // Método para generar el QR
    private Bitmap generateQRCode(String content) throws WriterException {
        if (content == null || content.isEmpty()) {
            Log.e("GenerateQR", "Contenido del QR vacío o nulo");
            return null;
        }

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(content, com.google.zxing.BarcodeFormat.QR_CODE, 512, 512);
        Bitmap bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.RGB_565);
        for (int x = 0; x < 512; x++) {
            for (int y = 0; y < 512; y++) {
                bitmap.setPixel(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }
        return bitmap;
    }
}
