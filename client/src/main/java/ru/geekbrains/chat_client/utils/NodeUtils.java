package ru.geekbrains.chat_client.utils;

import javafx.scene.control.Label;
import javafx.scene.control.TextInputControl;

public class NodeUtils {
    private NodeUtils() {}

    public static void clearErrorLabels(Label... labels) {
        for (Label label : labels) {
            label.setText("");
        }
    }

    public static void clearTextInputs(TextInputControl... textInputElement) {
        for (TextInputControl textInputControl : textInputElement) {
            textInputControl.clear();
        }
    }
}
