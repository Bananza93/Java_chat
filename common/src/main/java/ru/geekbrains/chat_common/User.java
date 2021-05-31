package ru.geekbrains.chat_common;

import java.util.Objects;

public class User {
    private int id;
    private String username;
    private String login;
    private String password;

    public User(String username, String login, String password) {
        this(0, username, login, password);
    }

    public User(int id, String username, String login, String password) {
        this.id = id;
        this.username = username;
        this.login = login;
        this.password = password;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id && login.equals(user.login);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, login);
    }
}
