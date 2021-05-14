package ru.geekbrains.server.auth_server;

import ru.geekbrains.server.chat_server.ChatServerSessionHandler;
import ru.geekbrains.server.chat_server.Server;

public interface AuthServer extends Server {
    void authorizeUser(String jsonMessage, ChatServerSessionHandler userSession);

}
