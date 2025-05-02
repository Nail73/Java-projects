package com.culinary.app.models;

import com.culinary.app.exceptions.DatabaseException;
import com.culinary.app.utils.DatabaseHandler;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CalculationTableModel extends AbstractTableModel {
    private static final String[] COLUMN_NAMES = {
            "Код продукта", "Код ингредиента", "Наименование",
            "Брутто, г", "Потери, %", "Нетто, г", "Цена, руб", "Себестоимость, руб"
    };

    private static final Class<?>[] COLUMN_CLASSES = {
            String.class, String.class, String.class,
            Double.class, Double.class, Double.class, Double.class, Double.class
    };


        private final List<CalculationRecord> data = new ArrayList<>();
        private String productCode = "";
        private String selectedProductName = "";

        public CalculationTableModel() {
            loadDataFromDatabase();
        }

        private void loadDataFromDatabase() {
            List<CalculationRecord> newData = new ArrayList<>();

            if (productCode == null || productCode.isEmpty()) {
                updateData(newData);
                return;
            }

            String query = "SELECT c.product_code, c.ingredient_code, i.name AS ingredient_name, " +
                    "c.brutto, c.losses, c.netto, c.price, c.cost " +
                    "FROM calculations c " +
                    "JOIN ingredients i ON c.ingredient_code = i.code " +
                    "WHERE c.product_code = ?";

            try (Connection connection = DatabaseHandler.getConnection();
                 PreparedStatement statement = connection.prepareStatement(query)) {

                statement.setString(1, productCode);
                ResultSet resultSet = statement.executeQuery();

                while (resultSet.next()) {
                    newData.add(new CalculationRecord(
                            resultSet.getString("product_code"),
                            resultSet.getString("ingredient_code"),
                            resultSet.getString("ingredient_name"),
                            resultSet.getDouble("brutto"),
                            resultSet.getDouble("losses"),
                            resultSet.getDouble("netto"),
                            resultSet.getDouble("price"),
                            resultSet.getDouble("cost")
                    ));
                }
            } catch (SQLException e) {
                throw new DatabaseException("Ошибка загрузки данных", e);
            }

            updateData(newData);
        }

        private synchronized void updateData(List<CalculationRecord> newData) {
            this.data.clear();
            this.data.addAll(newData);
            SwingUtilities.invokeLater(this::fireTableDataChanged);
        }

        public void setSelectedProduct(String productCode, String productName) {
            this.productCode = productCode;
            this.selectedProductName = productName;
            loadDataFromDatabase();
        }
    public int calculateTotalWeight() {
        int total = 0;
        for (int i = 0; i < getRowCount(); i++) {
            total += ((Number) getValueAt(i, 3)).intValue(); // Предполагая, что вес в столбце 3
        }
        return total;
    }

    public String getSelectedProductCode() {
        return productCode;
    }

    public String getSelectedProductName() {
        return selectedProductName;
    }

    public double calculateTotalBrutto() {
        return data.stream().mapToDouble(CalculationRecord::brutto).sum();
    }

    // Добавляем проверку деления на ноль
    public double calculateTotalLosses() {
        double totalBrutto = calculateTotalBrutto();
        if (totalBrutto == 0) return 0;
        return (1 - calculateTotalNetto() / totalBrutto) * 100;
    }


    public double calculateTotalNetto() {
        return data.stream().mapToDouble(CalculationRecord::netto).sum();
    }

    public double calculateTotalPrice() {
        if (data.isEmpty()) return 0;
        return data.stream().mapToDouble(CalculationRecord::price).sum();
    }

    public double calculateTotalCost() {
        return data.stream()
                .mapToDouble(CalculationRecord::cost)
                .sum();
    }

    public void removeIngredient(String productCode, String ingredientCode) {
        try {
            DatabaseHandler.executeQuery(
                    "DELETE FROM calculations WHERE product_code=? AND ingredient_code=?",
                    productCode, ingredientCode
            );
            loadDataFromDatabase();
            fireTableDataChanged();
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка удаления ингредиента", e);
        }
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }

    @Override
    public Object getValueAt(int row, int col) {
        CalculationRecord r = data.get(row);
        return switch (col) {
            case 0 -> r.productCode();
            case 1 -> r.ingredientCode();
            case 2 -> r.ingredientName();
            case 3 -> r.brutto();
            case 4 -> r.losses(); // Исправлено здесь
            case 5 -> r.netto();
            case 6 -> r.price();
            case 7 -> r.cost();
            default -> null;
        };
    }


    @Override
    public String getColumnName(int col) {
        return COLUMN_NAMES[col];
    }

    @Override
    public Class<?> getColumnClass(int col) {
        return COLUMN_CLASSES[col];
    }

    public record CalculationRecord(
            String productCode, String ingredientCode, String ingredientName,
            double brutto, double losses, double netto, double price, double cost
    ) {
    }

    private void updateSelfCostFromDatabase() {
        if (productCode == null || productCode.isEmpty()) return;

        String query = "SELECT total_cost_without_vat FROM total_cost_per_unit WHERE product_code = ?";
        try (Connection connection = DatabaseHandler.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setString(1, productCode);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                double updatedCost = rs.getDouble("total_cost_without_vat");

                // Обновляем себестоимость у всех ингредиентов
                for (int i = 0; i < data.size(); i++) {
                    CalculationRecord record = data.get(i);

                    // Предположим, что себестоимость хранится в 7-м столбце ("Себестоимость, руб")
                    // Можно просто перезаписать значение или пересчитать по формуле

                    double newCost = record.brutto() * record.price() / 1000.0; // например: себестоимость на грамм

                    // Обновляем запись
                    data.set(i, new CalculationRecord(
                            record.productCode(),
                            record.ingredientCode(),
                            record.ingredientName(),
                            record.brutto(),
                            record.losses(),
                            record.netto(),
                            record.price(),
                            newCost // Обновлённая себестоимость
                    ));

                    fireTableCellUpdated(i, 7); // Обновляем только ячейку "Себестоимость"
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка загрузки себестоимости из БД", e);
        }
    }

    public void refreshData() {
        loadDataFromDatabase(); // Перезагрузка рецепта
        updateSelfCostFromDatabase(); // Подгрузка новой себестоимости
        fireTableDataChanged();
    }
}