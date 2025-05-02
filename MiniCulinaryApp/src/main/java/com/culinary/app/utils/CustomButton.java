package com.culinary.app.utils;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CustomButton {

    public static JButton createAddButton(Runnable action) {
        return createButton("Добавить", new Color(59, 89, 152), Color.WHITE, action);
    }

    public static JButton createEditButton(Runnable action) {
        return createButton("Редактировать", Color.LIGHT_GRAY, Color.BLACK, action);
    }

    public static JButton createDeleteButton(Runnable action) {
        return createButton("Удалить", new Color(255, 102, 102), Color.WHITE, action);
    }

    public static JButton createButton(String text, Color backgroundColor, Color foregroundColor, Runnable action) {
        // 1. Создаем кнопку с явными параметрами отрисовки
        JButton button = new JButton(text) {
            @Override
            public void paintComponent(Graphics g) {
                // Включаем антиалиасинг для плавного текста
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Рисуем фон
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                // Рисуем текст (гарантированно поверх фона)
                g2.setColor(getForeground());
                FontMetrics fm = g2.getFontMetrics();
                Rectangle textBounds = fm.getStringBounds(getText(), g2).getBounds();
                int x = (getWidth() - textBounds.width) / 2;
                int y = (getHeight() - textBounds.height) / 2 + fm.getAscent();
                g2.drawString(getText(), x, y);

                g2.dispose();
            }

            @Override
            protected void paintBorder(Graphics g) {
                // Рисуем скругленную границу
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground().darker());
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);
                g2.dispose();
            }
        };

        // 2. Настраиваем основные параметры
        button.setContentAreaFilled(false); // Важно для кастомной отрисовки
        button.setOpaque(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);

        // 3. Устанавливаем цвета
        button.setBackground(backgroundColor != null ? backgroundColor : Color.LIGHT_GRAY);
        button.setForeground(foregroundColor != null ? foregroundColor : Color.BLACK);
        button.setFont(new Font("Arial", Font.BOLD, 13));

        // 4. Эффекты при наведении
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(brightenColor(button.getBackground()));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(backgroundColor);
            }
        });

        // 5. Размеры и отступы
        int padding = 1;
        Dimension size = button.getPreferredSize();
        button.setPreferredSize(new Dimension(
                size.width + padding,
                size.height + padding
        ));

        // 6. Обработчик действия
        if (action != null) {
            button.addActionListener(e -> action.run());
        }

        return button;
    }

    // Остальные методы без изменений
    private static Border createRoundedBorder(int radius) {
        return new RoundedBorder(radius);
    }

    private static class RoundedBorder implements Border {
        private final int radius;

        public RoundedBorder(int radius) {
            this.radius = radius;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(c.getBackground());
            g2.fillRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2.setColor(c.getForeground());
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(radius, radius, radius, radius);
        }

        @Override
        public boolean isBorderOpaque() {
            return true;
        }
    }

    private static Color brightenColor(Color color) {
        if (color == null) {
            return null;
        }
        int r = Math.min(255, color.getRed() + 30);
        int g = Math.min(255, color.getGreen() + 30);
        int b = Math.min(255, color.getBlue() + 30);
        return new Color(r, g, b);
    }
}