package ru.geekbrains.chat_server;

public interface AuthServer extends Server {
    void authorizeUser(String jsonMessage, SessionHandler userSession);

}
