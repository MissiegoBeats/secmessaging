package com.secmes.secmessaging;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.zxing.WriterException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class GenerateQRActivity extends AppCompatActivity {

    private static final int REQUEST_WIFI_PERMISSION = 101;
    private static final String TAG = "GenerateQRActivity";

    private ImageView qrImageView;
    private String publicKey;
    private String clientPublicKey;
    private String ipAddress;
    private ServerSocket serverSocket; // Para escuchar conexiones entrantes
    private CountDownLatch serverSocketLatch = new CountDownLatch(1); // Sincronización para esperar que el servidor se inicie
    private String clientIpAddress; // IP del cliente conectado

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_qr);

        qrImageView = findViewById(R.id.qrCodeImageView);

        // Comprobar si el permiso de Wi-Fi está concedido
        if (checkPermissions()) {
            // Obtener la IP local
            ipAddress = getDeviceIp();

            // Obtener la clave pública RSA
            publicKey = RSAUtils.getPublicKey();

            // Si la IP y la clave pública están disponibles, generamos el QR
            if (ipAddress != null && publicKey != null) {
                // Concatenamos la IP y la clave pública para formar el contenido del QR
                String qrContent = "IP:" + ipAddress + ";PublicKey:" + publicKey;

                try {
                    // Generar el QR con el contenido
                    Bitmap qrBitmap = QRUtils.generateQRCode(qrContent);
                    qrImageView.setImageBitmap(qrBitmap);

                    // Mostrar un Toast con el contenido del QR para verificar
                    Toast.makeText(this, "QR generado: " + qrContent, Toast.LENGTH_LONG).show();

                    // Inicializar el servidor para recibir la conexión
                    startServer();

                    // Esperar a que el cliente se conecte
                    waitForConnection();

                } catch (WriterException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error al generar el QR", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "No se pudo obtener la IP o la clave pública", Toast.LENGTH_SHORT).show();
            }
        } else {
            requestPermissions(); // Solicitar permisos si no están concedidos
        }
    }

    // Comprobar si el permiso ACCESS_WIFI_STATE está concedido
    private boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE) == PackageManager.PERMISSION_GRANTED;
    }

    // Solicitar permisos necesarios en tiempo de ejecución
    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_WIFI_STATE}, REQUEST_WIFI_PERMISSION);
    }

    // Manejar los resultados de la solicitud de permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_WIFI_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, volver a intentar obtener la IP
                ipAddress = getDeviceIp();
                if (ipAddress != null && publicKey != null) {
                    String qrContent = "IP:" + ipAddress + ";PublicKey:" + publicKey;
                    try {
                        Bitmap qrBitmap = QRUtils.generateQRCode(qrContent);
                        qrImageView.setImageBitmap(qrBitmap);

                        // Mostrar el contenido en un Toast
                        Toast.makeText(this, "QR generado: " + qrContent, Toast.LENGTH_LONG).show();
                        startServer();
                        waitForConnection();
                    } catch (WriterException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error al generar el QR", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                Toast.makeText(this, "Permiso para acceder al estado de la red Wi-Fi no concedido", Toast.LENGTH_SHORT).show();
                finish(); // Finalizar la actividad si el permiso no es concedido
            }
        }
    }

    // Método para inicializar el servidor
    private void startServer() {
        // Iniciar el servidor en un hilo separado
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(12345); // Puerto de escucha
                Log.d(TAG, "Servidor iniciado, esperando conexión...");
                serverSocketLatch.countDown(); // El servidor se ha iniciado correctamente
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "Error al iniciar el servidor");
                serverSocketLatch.countDown(); // Asegurarnos de que el hilo no quede bloqueado en caso de error
            }
        }).start();
    }

    // Esperar a que se conecte el cliente
    private void waitForConnection() {
        new Thread(() -> {
            try {
                serverSocketLatch.await();
                if (serverSocket != null && !serverSocket.isClosed()) {
                    Socket clientSocket = serverSocket.accept();
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    String clientInfo = in.readLine();
                    Log.d(TAG, "Cliente conectado: " + clientInfo);
                    String[] clientInfoSplit = clientInfo.split(";");
                    clientIpAddress = clientInfoSplit[0];
                    clientPublicKey = URLEncoderUtils.decodeFromUrl(clientInfoSplit[1]);

                    Log.d(TAG, "Cliente conectado desde: " + clientIpAddress);

                    runOnUiThread(this::connectToChatActivity);
                    clientSocket.close();
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                Log.e(TAG, "Error al esperar la conexión del cliente");
            }
        }).start();
    }

    // Método para mover a la actividad de chat después de generar el QR
    private void connectToChatActivity() {
        try {
            // Cerrar la conexión con el cliente si está activa
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Error al cerrar el servidor");
        }

        // Crear un Intent para ir a la actividad de chat
        Intent intent = new Intent(GenerateQRActivity.this, ChatActivity.class);
        intent.putExtra("IP", clientIpAddress); // Pasar la IP del cliente
        intent.putExtra("PublicKey", clientPublicKey); // Clave pública si es necesario
        intent.putExtra("PORT", 49153); // Clave pública del cliente
        intent.putExtra("isServer", false);

        startActivity(intent); // Iniciar la actividad de chat
        finish(); // Finalizar la actividad actual para evitar que el usuario regrese a ella
    }

    // Obtener la dirección IP del dispositivo
    private String getDeviceIp() {
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo != null) {
                int ipAddressInt = wifiInfo.getIpAddress();
                return formatIpAddress(ipAddressInt);
            }
        }
        return null;
    }

    // Formatear la dirección IP del dispositivo desde un valor int
    private String formatIpAddress(int ipAddressInt) {
        return (ipAddressInt & 0xFF) + "." +
                ((ipAddressInt >> 8) & 0xFF) + "." +
                ((ipAddressInt >> 16) & 0xFF) + "." +
                ((ipAddressInt >> 24) & 0xFF);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Error al cerrar el servidor en onDestroy");
        }
    }
}
