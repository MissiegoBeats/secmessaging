package com.secmes.secmessaging;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatActivity extends AppCompatActivity {

    private TextView chatView;
    private EditText messageInput;
    private Button sendButton;
    private Socket socket;
    private String recipientIp;
    private int recipientPort = 12345;  // Puerto predeterminado para la comunicación

    private String myPublicKey;
    private String myPrivateKey;
    private String recipientPublicKey; // Clave pública del destinatario

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Inicialización de vistas
        chatView = findViewById(R.id.chatView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);

        // Obtener claves públicas y privadas
        myPublicKey = RSAUtils.getPublicKey(); // Obtener la clave pública
        myPrivateKey = RSAUtils.getPrivateKey(); // Obtener la clave privada

        // Obtener la IP del destinatario y la clave pública desde el Intent
        recipientIp = getIntent().getStringExtra("IP");
        recipientPublicKey = getIntent().getStringExtra("PublicKey");

        // Verificación de la IP y clave pública del destinatario
        if (recipientIp != null && recipientPublicKey != null) {
            // Establecer la conexión con el otro dispositivo
            establishConnection();
        } else {
            Toast.makeText(this, "Faltan los datos del destinatario", Toast.LENGTH_SHORT).show();
            finish();
        }

        // No necesitamos funcionalidad para enviar mensajes en este paso, solo nos enfocamos en las claves públicas
    }

    private void establishConnection() {
        // Conectar al destinatario utilizando su IP
        new Thread(() -> {
            try {
                socket = new Socket(recipientIp, recipientPort);
                runOnUiThread(() -> Toast.makeText(ChatActivity.this, "Conexión establecida", Toast.LENGTH_SHORT).show());

                // Enviar la clave pública del escaneador al receptor para que pueda cifrar los mensajes hacia el escaneador
                sendPublicKey();

                // Ahora, la conexión está establecida, esperamos mensajes
                receiveMessages();
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(ChatActivity.this, "Error al conectar", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void sendPublicKey() {
        new Thread(() -> {
            try {
                // Enviar la clave pública del dispositivo que escaneó el QR al receptor (se utiliza la conexión establecida)
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
                writer.println("PUBLIC_KEY:" + myPublicKey);  // Enviar la clave pública de A con un prefijo para identificarla
                writer.flush();  // Asegurar que se envíe la clave pública

                // Mostrar el mensaje en el chat
                runOnUiThread(() -> chatView.append("Yo: He enviado mi clave pública.\n"));
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(ChatActivity.this, "Error al enviar clave pública", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void receiveMessages() {
        new Thread(() -> {
            try {
                // Usar BufferedReader para leer los mensajes entrantes
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                String incomingMessage;
                while (true) {
                    // Leer mensaje de forma continua
                    incomingMessage = reader.readLine();

                    if (incomingMessage != null) {
                        if (incomingMessage.startsWith("PUBLIC_KEY:")) {
                            // Si el mensaje recibido es la clave pública del otro dispositivo
                            recipientPublicKey = incomingMessage.substring("PUBLIC_KEY:".length());
                            runOnUiThread(() -> chatView.append("He recibido la clave pública de mi amigo.\n"));
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(ChatActivity.this, "Error al recibir mensaje", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}
