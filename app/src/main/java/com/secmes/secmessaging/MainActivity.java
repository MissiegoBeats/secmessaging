package com.secmes.secmessaging;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button generateQRButton;
    private Button scanQRButton;
    private ImageView backgroundImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        backgroundImage = findViewById(R.id.backgroundImage);
        applyBlur();

        generateQRButton = findViewById(R.id.generateQRButton);
        scanQRButton = findViewById(R.id.scanQRButton);

        generateQRButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Generar QR con la IP local y la clave pública RSA
                Intent intent = new Intent(MainActivity.this, GenerateQRActivity.class);
                startActivity(intent);
            }
        });

        scanQRButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Escanear QR para obtener la IP y clave pública
                Intent intent = new Intent(MainActivity.this, ScanQRActivity.class);
                startActivity(intent);
            }
        });
    }

    private void applyBlur() {
        Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.background);
        RenderScript rs = RenderScript.create(this);
        Allocation input = Allocation.createFromBitmap(rs, originalBitmap);
        Allocation output = Allocation.createTyped(rs, input.getType());
        ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        blurScript.setRadius(10f);
        blurScript.setInput(input);
        blurScript.forEach(output);
        output.copyTo(originalBitmap);
        backgroundImage.setImageBitmap(originalBitmap);
        rs.destroy();
    }
}
