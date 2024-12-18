package com.secmes.secmessaging;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.IOException;
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

        // Configurar el botón para enviar el mensaje
        sendButton.setOnClickListener(view -> {
            String message = messageInput.getText().toString();
            if (!message.isEmpty()) {
                sendMessage(message);
            }
        });
    }

    private void establishConnection() {
        // Conectar al destinatario utilizando su IP
        new Thread(() -> {
            try {
                socket = new Socket(recipientIp, recipientPort);
                runOnUiThread(() -> Toast.makeText(ChatActivity.this, "Conexión establecida", Toast.LENGTH_SHORT).show());

                // Ahora, la conexión está establecida, esperamos mensajes
                receiveMessages();
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(ChatActivity.this, "Error al conectar", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void sendMessage(String message) {
        new Thread(() -> {
            try {
                // Cifrar el mensaje antes de enviarlo usando la clave pública del destinatario
                String encryptedMessage = RSAUtils.encrypt(message, recipientPublicKey); // Enviar mensaje cifrado

                // Enviar el mensaje a través de la conexión
                socket.getOutputStream().write(encryptedMessage.getBytes());

                // Mostrar el mensaje enviado en el chat
                runOnUiThread(() -> chatView.append("Yo: " + message + "\n"));

                // Limpiar el campo de entrada
                runOnUiThread(() -> messageInput.setText(""));

            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(ChatActivity.this, "Error al enviar mensaje", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void receiveMessages() {
        new Thread(() -> {
            try {
                byte[] buffer = new byte[1024];
                while (true) {
                    int bytesRead = socket.getInputStream().read(buffer);
                    if (bytesRead != -1) {
                        String encryptedMessage = new String(buffer, 0, bytesRead);

                        // Descifrar el mensaje recibido utilizando la clave privada local
                        String decryptedMessage = RSAUtils.decrypt(encryptedMessage, myPrivateKey);

                        // Mostrar el mensaje en el chat
                        runOnUiThread(() -> chatView.append("Amigo: " + decryptedMessage + "\n"));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(ChatActivity.this, "Error al recibir mensaje", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}
