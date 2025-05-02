package com.culinary.app.utils;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public final class UIUtils {
    public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.#");
    public static final Color HEADER_COLOR = new Color(255, 200, 100);
    public static final Color ERROR_COLOR = new Color(255, 150, 150);
    public static final Color SUCCESS_COLOR = new Color(150, 255, 150);
    public static final Color DISABLED_COLOR = new Color(240, 240, 240);
    public static final Font HEADER_FONT = new Font("Arial", Font.BOLD, 14);
    public static final Font LABEL_FONT = new Font("Arial", Font.BOLD, 16);
    public static final Font MONOSPACED_FONT = new Font("Monospaced", Font.PLAIN, 12);
    public static final Border STANDARD_BORDER = BorderFactory.createEmptyBorder(5, 5, 5, 5);
    public static final Border ETCHED_BORDER = BorderFactory.createEtchedBorder();

    private UIUtils() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    public static void configureTable(JTable table) {

        // Создаем рендерер для числовых столбцов
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);

        for (int i = 0; i < table.getColumnCount(); i++) {
            TableColumn column = table.getColumnModel().getColumn(i);
            // Проверяем тип данных столбца
            Class<?> columnClass = table.getModel().getColumnClass(i);

            // Для числовых типов устанавливаем выравнивание по центру
            if (Number.class.isAssignableFrom(columnClass)) {
                column.setCellRenderer(new DefaultTableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(JTable table, Object value,
                                                                   boolean isSelected, boolean hasFocus, int row, int column) {
                        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                        if (value instanceof Number) {
                            setText(String.format("%.2f", ((Number) value).doubleValue()));
                            setHorizontalAlignment(SwingConstants.CENTER);
                        }
                        return c;
                    }
                });
            } else if (table.getColumnName(i).equals("Наименование")) {
                column.setCellRenderer(leftRenderer);
            } else {
                column.setCellRenderer(centerRenderer);
            }
        }

        // Дополнительные настройки из configureTable()
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setGridColor(Color.LIGHT_GRAY);
    }

    public static void configureCostAnalysisTable(JTable table) {
        // Настройка рендерера для вводимых руками строк
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (row == 0 || row == 2 || row == 5 || row == 8) {
                    c.setBackground(new Color(200, 255, 200)); // Светло-зеленый цвет
                } else {
                    c.setBackground(Color.WHITE); // Белый цвет для остальных строк
                }
                if (column == 1 || column == 2) { // Выравнивание числовых значений по центру
                    setHorizontalAlignment(SwingConstants.CENTER);
                }
                return c;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        // Настройка редактора для проверки ввода чисел
        table.putClientProperty("terminateEditOnFocusLost", true);
        table.setDefaultEditor(Object.class, new DefaultCellEditor(new JTextField()) {
            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                JTextField editor = (JTextField) super.getTableCellEditorComponent(table, value, isSelected, row, column);
                editor.addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyTyped(KeyEvent e) {
                        char c = e.getKeyChar();
                        if (!Character.isDigit(c) && c != '.' && c != ',') {
                            e.consume();
                        }
                    }
                });
                return editor;
            }
        });
    }

    // Заголовки
    public static void configureTableHeaders(JTable table) {
        // Настройка рендерера заголовков
        DefaultTableCellRenderer headerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER); // Выравнивание по центру
                setFont(new Font("Arial", Font.BOLD, 12)); // Шрифт и размер
                setForeground(Color.black); // Цвет текста
                setBackground(new Color(255, 200, 100)); // Цвет фона
                return c;
            }
        };

        // Применяем рендерер ко всем заголовкам
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderer);
        }
    }

    public static void configureTableCells(JTable table) {
        // Настройка рендерера ячеек
        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER); // Выравнивание по центру
                setFont(new Font("Arial", Font.PLAIN, 12)); // Шрифт и размер
                if (isSelected) {
                    setBackground(new Color(150, 255, 150)); // Цвет фона выделенной ячейки
                    setForeground(Color.BLACK); // Цвет текста выделенной ячейки
                } else {
                    setBackground(Color.WHITE); // Цвет фона обычной ячейки
                    setForeground(Color.BLACK); // Цвет текста обычной ячейки
                }
                return c;
            }
        };

        // Применяем рендерер ко всем ячейкам
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
        }
    }


    public static JFormattedTextField createNumericField() {
        NumberFormat format = NumberFormat.getNumberInstance();
        format.setGroupingUsed(false);
        format.setMaximumFractionDigits(2);

        JFormattedTextField field = new JFormattedTextField(format);
        field.setValue(0.0); // Устанавливаем начальное значение
        field.setColumns(10);

        return field;
    }

    public static JPanel createInputPanel(Object... components) {
        JPanel panel = new JPanel(new GridLayout(components.length / 2, 2, 5, 5));
        panel.setBorder(STANDARD_BORDER);

        for (Object component : components) {
            if (component instanceof Component) {
                panel.add((Component) component);
            }
        }
        return panel;
    }

    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(
                parent,
                message,
                "Ошибка",
                JOptionPane.ERROR_MESSAGE
        );
    }

    public static void showInfo(Component parent, String message) {
        JOptionPane.showMessageDialog(
                parent,
                message,
                "Информация",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    public static boolean showConfirm(Component parent, String message) {
        return JOptionPane.showConfirmDialog(
                parent,
                message,
                "Подтверждение",
                JOptionPane.YES_NO_OPTION
        ) == JOptionPane.YES_OPTION;
    }


    public static int showInputDialog(Component parent, JPanel panel, String title) {
        return JOptionPane.showConfirmDialog(
                parent,
                panel,
                title,
                JOptionPane.OK_CANCEL_OPTION
        );
    }


    // Универсальный метод для форматирования чисел
    public static String formatNumber(Number value) {
        if (value == null) return "0.00";
        if (value.doubleValue() == value.longValue()) {
            return String.format("%d", value.longValue());
        }
        return String.format("%.2f", value.doubleValue());
    }

    public static String formatCurrency(double value) {
        return String.format("%,.2f руб.", value);
    }


}