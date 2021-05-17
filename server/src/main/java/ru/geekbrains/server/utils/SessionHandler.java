package ru.geekbrains.server.utils;

public interface SessionHandler {
    void handle();
    void close();
}
