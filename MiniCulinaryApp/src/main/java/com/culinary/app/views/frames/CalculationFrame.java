package com.culinary.app.views.frames;

import com.culinary.app.views.panels.CalculationPanel;

import javax.swing.*;
import java.awt.*;

public class CalculationFrame extends JFrame {
    public CalculationFrame() {
        setTitle("Расчет калькуляции");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());

        // Добавляем панель калькуляции
        add(new CalculationPanel(), BorderLayout.CENTER);

        setVisible(true);
    }
}