package com.secmes.secmessaging;

import android.util.Log;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class MessageHandler {

    private static final String TAG = "MessageHandler";
    private Socket socket;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private RSAUtils rsaUtils;

    public MessageHandler(Socket socket, RSAUtils rsaUtils) {
        this.socket = socket;
        this.rsaUtils = rsaUtils;
        try {
            inputStream = new DataInputStream(socket.getInputStream());
            outputStream = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            Log.e(TAG, "Error al establecer las corrientes de entrada y salida", e);
        }
    }

    // Método para enviar un mensaje cifrado
    public void sendMessage(String message, String recipientPublicKey) {
        try {
            // Cifrar el mensaje con la clave pública del destinatario
            String encryptedMessage = RSAUtils.encrypt(message, recipientPublicKey);

            // Enviar el mensaje cifrado
            byte[] encryptedMessageBytes = encryptedMessage.getBytes();
            outputStream.writeInt(encryptedMessageBytes.length);
            outputStream.write(encryptedMessageBytes);

            Log.d(TAG, "Mensaje enviado: " + message);

        } catch (IOException e) {
            Log.e(TAG, "Error al enviar el mensaje", e);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Método para recibir un mensaje cifrado
    public String receiveMessage(String recipientPrivateKey) {
        try {
            // Leer el tamaño del mensaje cifrado
            int messageLength = inputStream.readInt();
            byte[] encryptedMessage = new byte[messageLength];

            // Leer el mensaje cifrado
            inputStream.readFully(encryptedMessage);

            // Descifrar el mensaje usando la clave privada
            String decryptedMessage = RSAUtils.decrypt(new String(encryptedMessage), recipientPrivateKey);

            Log.d(TAG, "Mensaje recibido: " + decryptedMessage);
            return decryptedMessage;
        } catch (IOException e) {
            Log.e(TAG, "Error al recibir el mensaje", e);
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Método para cerrar la conexión
    public void closeConnection() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error al cerrar la conexión", e);
        }
    }
}
