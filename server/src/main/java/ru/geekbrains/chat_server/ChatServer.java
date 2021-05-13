package ru.geekbrains.chat_server;

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
        try(ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started");
            startAuthServer();
            while (true) {
                System.out.println("Waiting for connection");
                Socket socket = serverSocket.accept();
                System.out.println("Client connected (IP: " + socket.getInetAddress().getHostAddress() + ")");
                new SessionHandler(socket).handle();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void startAuthServer() throws IOException, InterruptedException {
        authServer = new SimpleAuthServer();
        authServer.start();
//        System.out.println("AFTER CREATING THREAD");
//        Thread.sleep(5000);
        authServerSocket = new Socket("localhost", 22222);
        fromAuthServer = new DataInputStream(authServerSocket.getInputStream());
        toAuthServer = new DataOutputStream(authServerSocket.getOutputStream());
        System.out.println("BEFORE NEW THREAD");
        new Thread(() -> {
            try {
                System.out.println("INSIDE NEW THREAD");
                while (!Thread.currentThread().isInterrupted()) {
                    /*String msg = fromAuthServer.readUTF();
                    System.out.println("FROM AUTH: " + msg);*/
                    System.out.println("FROM SERVER");
                    Thread.sleep(2000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
