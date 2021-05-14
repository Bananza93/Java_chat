package ru.geekbrains.server;

import ru.geekbrains.server.chat_server.ChatServer;

public class ServerApp {
    public static void main(String[] args) {
        new ChatServer().start();
    }
}
