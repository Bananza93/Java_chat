package ru.geekbrains.server.auth_server;

import ru.geekbrains.chat_common.User;
import ru.geekbrains.server.utils.Server;

public interface AuthServer extends Server {
    User getUserByLoginAndPassword(String login, String password);
}
