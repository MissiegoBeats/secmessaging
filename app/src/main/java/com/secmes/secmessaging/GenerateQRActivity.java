package com.secmes.secmessaging;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.common.BitMatrix;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.util.Base64;

public class GenerateQRActivity extends AppCompatActivity {

    private ImageView qrImageView;
    private PublicKey publicKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_qr);

        qrImageView = findViewById(R.id.qrImageView);

        try {
            KeyPair keyPair = generateKeyPair();
            publicKey = keyPair.getPublic();

            String publicKeyString = Base64.getEncoder().encodeToString(publicKey.getEncoded());
            Bitmap qrBitmap = generateQRCode(publicKeyString);
            qrImageView.setImageBitmap(qrBitmap);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        return keyGen.generateKeyPair();
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
