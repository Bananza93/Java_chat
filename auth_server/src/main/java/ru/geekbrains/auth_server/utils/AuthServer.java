package ru.geekbrains.auth_server.utils;

import ru.geekbrains.auth_server.server.AuthServerSessionHandler;
import ru.geekbrains.chat_common.User;

import java.util.concurrent.ExecutorService;

public interface AuthServer extends Server {
    void addSession(AuthServerSessionHandler session);
    void removeSession(AuthServerSessionHandler session);
    boolean isConnectedToChatServer();
    User getUserByLoginAndPassword(String login, String password);
    ExecutorService getExecutorService();
}
