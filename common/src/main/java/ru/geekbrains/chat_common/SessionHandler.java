package ru.geekbrains.chat_common;

public interface SessionHandler {
    void handle();
    void close();
}
