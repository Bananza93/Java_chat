package ru.geekbrains.chat_common;

import com.google.gson.Gson;

import java.util.Date;
import java.util.List;

public class Message {
    private MessageType messageType;
    private String messageBody;
    private List<?> messageUtilList;
    private User fromUser;
    private User toUser;
    private Date messageDate;

    public Message() {
    }

    public Message(MessageType messageType, String messageBody) {
        this.messageType = messageType;
        this.messageBody = messageBody;
    }

    public Message(MessageType messageType, List<?> messageUtilList) {
        this.messageType = messageType;
        this.messageUtilList = messageUtilList;
    }

    public String messageToJson() {
        return new Gson().toJson(this);
    }

    public static Message messageFromJson(String json) {
        return new Gson().fromJson(json, Message.class);
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }

    public User getFromUser() {
        return fromUser;
    }

    public void setFromUser(User fromUser) {
        this.fromUser = fromUser;
    }

    public User getToUser() {
        return toUser;
    }

    public void setToUser(User toUser) {
        this.toUser = toUser;
    }

    public Date getMessageDate() {
        return messageDate;
    }

    public void setMessageDate(Date messageDate) {
        this.messageDate = messageDate;
    }

    public List<?> getMessageUtilList() {
        return messageUtilList;
    }

    public void setMessageUtilList(List<?> messageUtilList) {
        this.messageUtilList = messageUtilList;
    }
}
