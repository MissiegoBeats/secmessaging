package com.secmes.secmessaging;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class ChatActivity extends AppCompatActivity {

    private TextView chatTitle;
    private EditText messageInput;
    private Button sendButton;
    private DatabaseReference database;
    private SecretKey secretKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatTitle = findViewById(R.id.chatTitle);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);

        database = FirebaseDatabase.getInstance().getReference("chats");

        String publicKeyString = getIntent().getStringExtra("publicKey");
        String chatName = "Chat Seguro";

        chatTitle.setText(chatName);

        try {
            secretKey = generateSecretKey();
        } catch (Exception e) {
            e.printStackTrace();
        }

        sendButton.setOnClickListener(v -> {
            try {
                String message = messageInput.getText().toString();
                String encryptedMessage = encryptMessage(message, secretKey);
                sendMessage(chatName, encryptedMessage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private SecretKey generateSecretKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(128);
        return keyGenerator.generateKey();
    }

    private String encryptMessage(String message, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedBytes = cipher.doFinal(message.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    private void sendMessage(String chatId, String encryptedMessage) {
        database.child(chatId).push().setValue(encryptedMessage);
    }
}
