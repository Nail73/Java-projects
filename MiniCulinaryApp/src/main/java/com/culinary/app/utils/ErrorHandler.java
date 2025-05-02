package com.culinary.app.utils;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ErrorHandler {
    private static final Logger logger = Logger.getLogger(ErrorHandler.class.getName());

    public static void handle(Component parent, Exception e, String action) {
        String errorMsg = buildErrorMessage(e, action);
        logger.log(Level.SEVERE, errorMsg, e);
        showErrorDialog(parent, errorMsg);
    }

    private static String buildErrorMessage(Exception e, String action) {
        if (e instanceof IndexOutOfBoundsException) {
            return "Ошибка доступа к данным при " + action +
                    ". Возможно, данные были изменены другим пользователем.";
        }
        // Другие специальные обработки ошибок...
        return "Ошибка при " + action + ": " + e.getMessage();
    }

    private static void showErrorDialog(Component parent, String message) {
        SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(
                        parent,
                        message,
                        "Ошибка",
                        JOptionPane.ERROR_MESSAGE
                )
        );
    }
}