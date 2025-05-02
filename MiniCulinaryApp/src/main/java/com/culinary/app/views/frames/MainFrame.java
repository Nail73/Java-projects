package com.culinary.app.views.frames;

import com.culinary.app.models.ProductTableModel;
import com.culinary.app.views.panels.CalculationPanel;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private CalculationPanel calculationPanel;

    public MainFrame() {
        setTitle("Система калькуляции сырья");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        // Создаем панель заранее
        calculationPanel = new CalculationPanel();


        // Используем CardLayout для возможного расширения
        JPanel mainPanel = new JPanel(new CardLayout());
        mainPanel.add(calculationPanel, "calculation");

        add(mainPanel);
        setVisible(true);
    }


}