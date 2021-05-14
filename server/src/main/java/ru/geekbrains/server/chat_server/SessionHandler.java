package ru.geekbrains.server.chat_server;

public interface SessionHandler {
    void handle();
    void sendMessage(String jsonMessage);
}
