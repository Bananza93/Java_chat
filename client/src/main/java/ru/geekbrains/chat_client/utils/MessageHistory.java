package ru.geekbrains.chat_client.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.geekbrains.chat_common.User;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MessageHistory {
    private static final Logger LOGGER = LogManager.getRootLogger();
    private static final String HIST_DIR = System.getenv("LOCALAPPDATA") + "\\POGGERS chat\\history\\";
    private static final int RETRIEVED_SIZE = 100;
    private static File history;
    private static User currentUser;
    private static BufferedWriter br;

    private MessageHistory() {}

    public static void writeToHistory(String message) {
        try {
            br.write(message);
            br.flush();
        } catch (IOException e) {
            LOGGER.debug("EXCEPTION!", e);
        }
    }

    public static List<String> readFromHistory() {
        List<String> result = null;
        try (BufferedReader br = new BufferedReader(new FileReader(history))) {
            List<String> allHist = br.lines().collect(Collectors.toCollection(ArrayList::new));
            if (allHist.size() <= RETRIEVED_SIZE) {
                result = allHist;
            } else {
                result = new ArrayList<>(100);
                for (int i = allHist.size() - RETRIEVED_SIZE; i <= allHist.size(); i++) {
                    result.add(allHist.get(i));
                }
            }
        } catch (IOException e) {
            LOGGER.debug("EXCEPTION!", e);
        }
        return result;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
        history = new File( HIST_DIR + currentUser.getLogin() + ".hist");
        if (!history.exists()) {
            new File(HIST_DIR).mkdirs();
        }
        try {
            br = new BufferedWriter(new FileWriter(history, true));
        } catch (IOException e) {
            LOGGER.debug("EXCEPTION!", e);
        }
    }

    public static void close() {
        if (br != null) {
            try {
                br.close();
            } catch (IOException e) {/*do nothing*/}
        }
    }
}
