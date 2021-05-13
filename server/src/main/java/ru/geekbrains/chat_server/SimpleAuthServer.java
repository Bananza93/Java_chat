package ru.geekbrains.chat_server;

import ru.geekbrains.chat_common.User;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleAuthServer implements AuthServer {

    private static final int PORT = 22222;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private List<User> users;
    private Map<User, SessionHandler> onlineUsers;

    public SimpleAuthServer() {
        users = List.of(new User("user1", "log1", "pass"),
                new User("user2", "log2", "pass"),
                new User("user3", "log3", "pass")
        );
        onlineUsers = new HashMap<>();
    }

    @Override
    public void start() {
        new Thread(() -> {
            try (ServerSocket authServerSocket = new ServerSocket(PORT)) {
                System.out.println("Auth server started");
                socket = authServerSocket.accept();
                in = new DataInputStream(socket.getInputStream());
                out = new DataOutputStream(socket.getOutputStream());
                while (!Thread.currentThread().isInterrupted() && !socket.isClosed()) {
                /*String rawMessage = in.readUTF();
                System.out.println("FROM SERVER: " + rawMessage);*/
                    System.out.println("FROM AUTH SERVER");
                    Thread.sleep(2000);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public void stop() {

    }

    @Override
    public void authorizeUser(String jsonMessage, SessionHandler userSession) {

    }
}
