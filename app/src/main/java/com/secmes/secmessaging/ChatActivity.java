package com.secmes.secmessaging;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
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
import java.net.ServerSocket;
import java.net.Socket;

public class ChatActivity extends AppCompatActivity {

    private TextView chatView;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatView = findViewById(R.id.chatView);
        chatView.setMovementMethod(new ScrollingMovementMethod());
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);

        myPublicKey = RSAUtils.getPublicKey();
        myPrivateKey = RSAUtils.getPrivateKey();

        recipientIp = getIntent().getStringExtra("IP");
        recipientPublicKey = getIntent().getStringExtra("PublicKey");
        int port = getIntent().getIntExtra("PORT", -1);
        if (port == 49152){
            SERVER_PORT = 49153;
            CLIENT_PORT = 49152;
        }else{
            CLIENT_PORT = 49153;
            SERVER_PORT = 49152;
        }

        chatView.append("Mi PK: "+ myPublicKey + "\n");
        chatView.append("PK del destinatario: "+ recipientPublicKey + "\n\n");

        if (recipientIp != null && recipientPublicKey != null) {
            new Thread(() -> establishConnection()).start();
        } else {
            Toast.makeText(this, "Faltan los datos del destinatario", Toast.LENGTH_SHORT).show();
            finish();
        }

        sendButton.setOnClickListener(view -> sendMessage());
    }

    private void establishConnection() {
        createSocketServer();
        createClientSocket();
        chatView.append("INFO: ESTAS EN UN CHAT SEGURO CON TU AMIGO\n\n");
    }

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

    private String formatIpAddress(int ipAddressInt) {
        return (ipAddressInt & 0xFF) + "." +
                ((ipAddressInt >> 8) & 0xFF) + "." +
                ((ipAddressInt >> 16) & 0xFF) + "." +
                ((ipAddressInt >> 24) & 0xFF);
    }

    private void createSocketServer() {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(SERVER_PORT);
                Socket socket = serverSocket.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String message;
                while ((message = in.readLine()) != null) {
                    String finalMessage = message;
                    runOnUiThread(() -> chatView.append("Amigo: " + finalMessage + "\n")); // Actualizar UI
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
                out.println(message);
                runOnUiThread(() -> chatView.append("Yo: " + message + "\n"));
            }).start();
            messageInput.setText("");
        } else {
            Toast.makeText(this, "No se puede enviar el mensaje", Toast.LENGTH_SHORT).show();
        }
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
