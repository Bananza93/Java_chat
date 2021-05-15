package ru.geekbrains.server;

import ru.geekbrains.server.auth_server.SimpleAuthServer;

public class AuthServerStarter {
    public static void main(String[] args) {
        new SimpleAuthServer().start();
    }
}
