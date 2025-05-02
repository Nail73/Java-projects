package com.culinary.app;

import com.culinary.app.utils.DatabaseHandler;
import com.culinary.app.views.frames.MainFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        try {

            // Запуск GUI
            SwingUtilities.invokeLater(() -> {
                new MainFrame().setVisible(true);
            });
        } catch (Exception e) {
            System.err.println("Фатальная ошибка при запуске приложения:");
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Не удалось запустить приложение:\n" + e.getMessage(),
                    "Ошибка запуска", JOptionPane.ERROR_MESSAGE);
        }
    }

}