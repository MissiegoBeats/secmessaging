package com.secmes.secmessaging;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.security.KeyPair;
import java.util.Collections;

public class GenerateQRActivity extends AppCompatActivity {

    private ImageView qrImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_qr);

        qrImageView = findViewById(R.id.qrImageView);

        try {
            // Obtener la IP local del dispositivo
            String ipAddress = getLocalIpAddress();

            // Generar un par de claves RSA
            KeyPair keyPair = RSAUtils.generateKeyPair();
            String publicKey = RSAUtils.getPublicKeyString(keyPair.getPublic());

            // Contenido del QR: IP + clave pública
            String qrContent = ipAddress + ";" + publicKey;

            // Generar el QR
            Bitmap qrCode = generateQRCode(qrContent);
            qrImageView.setImageBitmap(qrCode);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getLocalIpAddress() {
        try {
            for (NetworkInterface networkInterface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                for (InetAddress inetAddress : Collections.list(networkInterface.getInetAddresses())) {
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Bitmap generateQRCode(String data) throws WriterException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, 512, 512);

        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bitmap.setPixel(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF); // Negro para "1", blanco para "0".
            }
        }

        return bitmap;
    }
}
