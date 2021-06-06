package ru.geekbrains.chat_server;

import ru.geekbrains.chat_server.server.ChatServer;

public class ChatServerLauncher {
    public static void main(String[] args) {
        new ChatServer().start();
    }
}
