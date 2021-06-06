package ru.geekbrains.auth_server;

import ru.geekbrains.auth_server.server.AuthServerWithDB;

public class AuthServerLauncher {
    public static void main(String[] args) {
        new AuthServerWithDB().start();
    }
}
