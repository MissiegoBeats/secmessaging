package com.secmes.secmessaging;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.net.Socket;
import javax.crypto.SecretKey;

public class ChatActivity extends AppCompatActivity {

    private TextView chatView;
    private EditText messageInput;
    private Button sendButton;

    private Socket socket;
    private SecretKey aesKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatView = findViewById(R.id.chatView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);

        String ipAddress = getIntent().getStringExtra("ipAddress");
        String publicKey = getIntent().getStringExtra("publicKey");

        // Conectar con el otro dispositivo (esto puede usar la IP y la clave pública)
        // Establecer una conexión TCP o usar otro protocolo según lo necesites

        sendButton.setOnClickListener(v -> {
            String message = messageInput.getText().toString();
            if (!message.isEmpty()) {
                // Enviar el mensaje cifrado con AES
                sendMessage(message);
                chatView.append("Tú: " + message + "\n");
                messageInput.setText("");
            }
        });
    }

    private void sendMessage(String message) {
        // Enviar el mensaje cifrado
    }

    private void receiveMessage(String message) {
        chatView.append("Amigo: " + message + "\n");
    }
}
