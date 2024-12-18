package com.secmes.secmessaging;

import android.graphics.Bitmap;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class QRUtils {

    public static Bitmap generateQRCode(String content) throws WriterException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        return toBitmap(qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, 512, 512));
    }

    private static Bitmap toBitmap(BitMatrix matrix) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                bmp.setPixel(x, y, matrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF); // Negro o blanco
            }
        }
        return bmp;
    }
}

