package ru.geekbrains.chat_server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class SessionHandler {
    static int clientCounter = 0;
    private int clientNumber;
    private Socket socket;
    private DataOutputStream outputStream;
    private DataInputStream inputStream;


    public SessionHandler(Socket socket) {
        try {
            this.socket = socket;
            this.inputStream = new DataInputStream(socket.getInputStream());
            this.outputStream = new DataOutputStream(socket.getOutputStream());
            this.clientNumber = ++clientCounter;
            System.out.println("Handler created.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handle() {
        new Thread(() -> {
            try {
                while(!Thread.currentThread().isInterrupted() && !socket.isClosed()) {
                    String msg = inputStream.readUTF();
                    System.out.printf("Client #%d: %s\n", this.clientNumber, msg);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
