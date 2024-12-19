package com.secmes.secmessaging;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;

public class ChatActivity extends AppCompatActivity {

    private LinearLayout chatView;
    private EditText messageInput;
    private Button sendButton;
    private String myPublicKey;
    private String myPrivateKey;
    private String recipientPublicKey;
    private String recipientIp;
    private ServerSocket serverSocket;
    private PrintWriter out;
    private int SERVER_PORT = 49153;
    private int CLIENT_PORT = 49152;
    private boolean seeEncryptedMessages = false;
    private Button setEncryptionVisibilityButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatView = findViewById(R.id.chatView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);
        setEncryptionVisibilityButton = findViewById(R.id.setEncryptionVisibilityButton);

        myPublicKey = RSAUtils.getPublicKey();
        myPrivateKey = RSAUtils.getPrivateKey();

        recipientIp = getIntent().getStringExtra("IP");
        recipientPublicKey = getIntent().getStringExtra("PublicKey");
        int port = getIntent().getIntExtra("PORT", -1);
        if (port == 49152){
            SERVER_PORT = 49153;
            CLIENT_PORT = 49152;
        } else {
            CLIENT_PORT = 49153;
            SERVER_PORT = 49152;
        }

        chatView.setPadding(16, 8, 16, 8);

        chatView.addView(createMessageTextView("Mi PK: "+ myPublicKey, false));
        chatView.addView(createMessageTextView("PK del destinatario: "+ recipientPublicKey, false));

        if (recipientIp != null && recipientPublicKey != null) {
            new Thread(() -> establishConnection()).start();
        } else {
            Toast.makeText(this, "Faltan los datos del destinatario", Toast.LENGTH_SHORT).show();
            finish();
        }

        sendButton.setOnClickListener(view -> sendMessage());

        setEncryptionVisibilityButton.setOnClickListener(view -> setEncryptionVisibility());
    }

    private void establishConnection() {
        createSocketServer();
        createClientSocket();
        runOnUiThread(() -> chatView.addView(createMessageTextView("INFO: ESTAS EN UN CHAT SEGURO CON TU AMIGO", false)));
    }

    private void setEncryptionVisibility() {
        seeEncryptedMessages = !seeEncryptedMessages;
        if (seeEncryptedMessages) {
            setEncryptionVisibilityButton.setText("Ocultar mensajes\nencriptados");
        } else {
            setEncryptionVisibilityButton.setText("Mostrar mensajes\nencriptados");
        }
    }

    private void createSocketServer() {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(SERVER_PORT);
                Socket socket = serverSocket.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String message;
                while ((message = in.readLine()) != null) {
                    if (!seeEncryptedMessages) {
                        String encryptedMessage = URLEncoderUtils.decodeFromUrl(message);
                        String finalMessage = RSAUtils.decrypt(encryptedMessage, myPrivateKey);
                        runOnUiThread(() -> chatView.addView(createMessageTextView("Amigo: " + finalMessage, false)));
                    } else {
                        String encryptedMessage = URLEncoderUtils.decodeFromUrl(message);
                        runOnUiThread(() -> chatView.addView(createMessageTextView("Amigo (Encriptado): " + encryptedMessage, false)));
                    }
                }
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void createClientSocket() {
        new Thread(() -> {
            boolean keepTryingConnection = true;
            while(keepTryingConnection) {
                try {
                    Socket socket = new Socket(recipientIp, CLIENT_PORT);
                    out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
                    keepTryingConnection = false;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void sendMessage() {
        String message = messageInput.getText().toString().trim();
        if (!message.isEmpty() && out != null) {
            new Thread(() -> {
                String encryptedMessage = null;
                try {
                    encryptedMessage = URLEncoderUtils.encodeToUrl(RSAUtils.encrypt(message, recipientPublicKey));
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
                out.println(encryptedMessage);
                if(!seeEncryptedMessages) {
                    runOnUiThread(() -> chatView.addView(createMessageTextView("Yo: " + message, true)));
                }else{
                    try {
                        String finalEncryptedMessage = URLEncoderUtils.decodeFromUrl(encryptedMessage);
                        runOnUiThread(() -> chatView.addView(createMessageTextView("Yo (Encriptado): " + finalEncryptedMessage, true)));
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();
            messageInput.setText("");
        } else {
            Toast.makeText(this, "No se puede enviar el mensaje", Toast.LENGTH_SHORT).show();
        }
    }

    private TextView createMessageTextView(String message, boolean sent) {
        TextView textView = new TextView(this);
        textView.setText(message);
        textView.setTextSize(14);
        textView.setTextColor(getResources().getColor(android.R.color.black));
        textView.setPadding(16, 12, 16, 12);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );

        if (sent) {
            textView.setGravity(Gravity.START);
            textView.setBackgroundResource(R.drawable.message_sent_background);
            params.setMargins(50, 8, 16, 8);
        } else {
            textView.setGravity(Gravity.START);
            textView.setBackgroundResource(R.drawable.message_received_background);
            params.setMargins(50, 8, 16, 8);
        }

        textView.setLayoutParams(params);

        return textView;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (out != null) {
            out.close();
        }
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}