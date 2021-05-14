package ru.geekbrains.server.chat_server;

import ru.geekbrains.server.auth_server.AuthServer;
import ru.geekbrains.server.auth_server.SimpleAuthServer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ChatServer {
    private static final int PORT = 11111;
    private AuthServer authServer;
    private Socket authServerSocket;
    private DataInputStream fromAuthServer;
    private DataOutputStream toAuthServer;

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Chat server started");
            while (true) {
                System.out.println("Waiting for connection");
                Socket socket = serverSocket.accept();
                System.out.println("Client connected (IP: " + socket.getInetAddress().getHostAddress() + ")");
                new ChatServerSessionHandler(socket, this).handle();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startAuthServer() throws IOException, InterruptedException {
        authServer = new SimpleAuthServer();
        authServer.start();
        authServerSocket = new Socket("localhost", 22222);
        fromAuthServer = new DataInputStream(authServerSocket.getInputStream());
        toAuthServer = new DataOutputStream(authServerSocket.getOutputStream());
        new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    String msg = fromAuthServer.readUTF();
                    System.out.println("FROM AUTH: " + msg);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void sendAuthRequestToAuthServer(String jsonRequest) {
        try {
            toAuthServer.writeUTF(jsonRequest);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}