package ru.geekbrains.chat_client.network;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.geekbrains.chat_client.utils.MessageHistory;
import ru.geekbrains.chat_common.SessionHandler;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientSessionHandler implements SessionHandler {
    private static final Logger LOGGER = LogManager.getRootLogger();
    private static MessageProcessor messageProcessor;
    private final Socket socket;
    private final DataInputStream inputStream;
    private final DataOutputStream outputStream;
    private Thread sessionThread;
    private boolean isClosed;

    public ClientSessionHandler(Socket socket) throws IOException {
        try {
            if (messageProcessor == null) messageProcessor = MessageProcessor.getInstance();
            this.socket = socket;
            inputStream = new DataInputStream(socket.getInputStream());
            outputStream = new DataOutputStream(socket.getOutputStream());
            isClosed = false;
        } catch (IOException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void handle() {
        (sessionThread = new Thread(this::readMessage)).start();
    }

    @Override
    public void close() {
        try {
            LOGGER.info("Connection with ("
                    + socket.getLocalAddress() + ":" + socket.getPort()
                    + ") closed");
            isClosed = true;
            socket.close();
            sessionThread.interrupt();
        } catch (IOException e) {/*do nothing*/}
    }

    private void readMessage() {
        try {
            while (!Thread.currentThread().isInterrupted() && !socket.isClosed()) {
                String message = inputStream.readUTF();
                messageProcessor.incomingMessage(message);
            }
        } catch (IOException e) {
            /*do nothing*/
        } finally {
            close();
        }
    }

    public void sendMessage(String message) {
        try {
            LOGGER.debug("Message send: " + message);
            outputStream.writeUTF(message);
        } catch (IOException e) {
            LOGGER.debug("EXCEPTION!", e);
        }
    }

    public boolean isClosed() {
        return isClosed;
    }
}
