package com.culinary.app.views.components;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.text.DecimalFormat;

public class RecipeEconomicsPanel extends JPanel {
    private final DecimalFormat moneyFormat = new DecimalFormat("#,##0.00 ₽");
    private final DecimalFormat percentFormat = new DecimalFormat("0.0%");

    // Компоненты для секций
    private JLabel costValue, costPerKgValue, costPer100gValue;
    private JLabel revenueValue, profitValue, marginValue;
    private JLabel weightValue, weightDifferenceValue;

    public RecipeEconomicsPanel() {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS)); // Горизонтальное расположение
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setPreferredSize(new Dimension(600, 150)); // Шире и ниже

        // 1. Секция "Стоимость"
        JPanel costPanel = createSectionPanel("Стоимость");
        costValue = createValueLabel("0.00 ₽");
        costPanel.add(createPair("Себестоимость:", costValue));
        costPerKgValue = createValueLabel("0.00 ₽");
        costPanel.add(createPair("За 1 кг:", costPerKgValue));
        costPer100gValue = createValueLabel("0.00 ₽");
        costPanel.add(createPair("За 100 г:", costPer100gValue));

        // 2. Секция "Доходность"
        JPanel revenuePanel = createSectionPanel("Доходность");
        revenueValue = createValueLabel("0.00 ₽", new Color(0, 100, 0));
        revenuePanel.add(createPair("Рекомендуемая цена:", revenueValue));
        profitValue = createValueLabel("0.00 ₽", Color.BLUE);
        revenuePanel.add(createPair("Прибыль:", profitValue));
        marginValue = createValueLabel("0.0%", Color.BLUE);
        revenuePanel.add(createPair("Маржа:", marginValue));

        // 3. Секция "Вес"
        JPanel weightPanel = createSectionPanel("Вес");
        weightValue = createValueLabel("0г / 0г");
        weightPanel.add(createPair("Факт/Упаковка:", weightValue));
        weightDifferenceValue = createValueLabel("0г", Color.RED);
        weightPanel.add(createPair("Разница:", weightDifferenceValue));

        // Вертикальные разделители
        JSeparator separator1 = createVerticalSeparator();
        JSeparator separator2 = createVerticalSeparator();

        // Компоновка с разделителями
        add(costPanel);
        add(separator1);
        add(revenuePanel);
        add(separator2);
        add(weightPanel);
    }

    private JSeparator createVerticalSeparator() {
        JSeparator separator = new JSeparator(JSeparator.VERTICAL);
        separator.setForeground(new Color(200, 200, 200));
        separator.setMaximumSize(new Dimension(5, Short.MAX_VALUE));
        return separator;
    }

    private JPanel createSectionPanel(String title) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                title,
                TitledBorder.CENTER,
                TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 14),
                new Color(0, 0, 150)
        ));
        panel.setAlignmentY(Component.TOP_ALIGNMENT);
        return panel;
    }

    // Остальные методы остаются без изменений
    private JPanel createPair(String labelText, JLabel valueLabel) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 20));
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.PLAIN, 12));
        panel.add(label);
        panel.add(valueLabel);
        return panel;
    }

    private JLabel createValueLabel(String text) {
        return createValueLabel(text, Color.BLACK);
    }

    private JLabel createValueLabel(String text, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 12));
        label.setForeground(color);
        return label;
    }

    public void updateEconomics(double cost, double costPerKg, double costPer100g,
                                double revenue, double profit, double margin,
                                double actualWeight, int requiredWeight) {
        costValue.setText(moneyFormat.format(cost));
        costPerKgValue.setText(moneyFormat.format(costPerKg));
        costPer100gValue.setText(moneyFormat.format(costPer100g));

        revenueValue.setText(moneyFormat.format(revenue));
        profitValue.setText(moneyFormat.format(profit));
        marginValue.setText(percentFormat.format(margin/100));

        weightValue.setText(actualWeight + "г / " + requiredWeight + "г");
        int weightDifference = (int) (requiredWeight - actualWeight);
        weightDifferenceValue.setText(weightDifference != 0 ? weightDifference + "г (не соответствует)" : weightDifference + "г (соответствует)");
        weightDifferenceValue.setForeground(weightDifference != 0 ? Color.RED : new Color(0,71,31));
    }
}
