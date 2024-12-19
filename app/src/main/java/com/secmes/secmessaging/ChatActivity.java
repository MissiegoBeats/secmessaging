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
    private final int PORT = 12345;
    private String myPublicKey;
    private String myPrivateKey;
    private String recipientPublicKey;
    private String recipientIp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatView = findViewById(R.id.chatView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);

        myPublicKey = RSAUtils.getPublicKey();
        myPrivateKey = RSAUtils.getPrivateKey();

        recipientIp = getIntent().getStringExtra("IP");
        recipientPublicKey = getIntent().getStringExtra("PublicKey");

        if (recipientIp != null && recipientPublicKey != null) {
            establishConnection();
        } else {
            Toast.makeText(this, "Faltan los datos del destinatario", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void establishConnection() {
        createSocketServer();
        createClientSocket();
    }

    private void createSocketServer(){
        
    }

    private void createClientSocket(){

    }
}
