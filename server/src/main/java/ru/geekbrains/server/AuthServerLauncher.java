package ru.geekbrains.server;

import ru.geekbrains.server.auth_server.AuthServerWithDB;

public class AuthServerLauncher {
    public static void main(String[] args) {
        new AuthServerWithDB().start();
    }
}
