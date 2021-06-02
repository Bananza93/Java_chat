package ru.geekbrains.server.auth_server;

import ru.geekbrains.chat_common.User;
import ru.geekbrains.server.utils.Server;

import java.sql.SQLException;
import java.util.concurrent.ExecutorService;

public interface AuthServer extends Server {
    void addSession(AuthServerSessionHandler session);
    void removeSession(AuthServerSessionHandler session);
    boolean isConnectedToChatServer();
    User getUserByLoginAndPassword(String login, String password);
    ExecutorService getExecutorService();
}
