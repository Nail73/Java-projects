package com.culinary.app.models;

import com.culinary.app.utils.DatabaseHandler;
import com.culinary.app.utils.ErrorHandler;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PackagingTableModel extends AbstractTableModel {
    private String[] columnNames = {"Код продукта", "Код материала", "Наименование", "Брутто, г", "Потери, %", "Нетто, г", "Цена, руб", "Себестоимость, руб"};
    private List<Object[]> data = new ArrayList<>();
    private String selectedProductCode = "";

    public PackagingTableModel() {
        loadDataFromDatabase();
    }

    private void loadDataFromDatabase() {
        List<Object[]> newData = new ArrayList<>();
        if (selectedProductCode == null || selectedProductCode.trim().isEmpty()) {
            updateData(newData);
            return;
        }

        String query = "SELECT pp.product_code, pp.material_code, pm.name, " +
                "pp.brutto, pp.losses, pp.netto, pp.price, pp.sum " +
                "FROM product_packaging pp " +
                "JOIN packaging_materials pm ON pp.material_code = pm.code " +
                "WHERE pp.product_code = ?";

        try (Connection connection = DatabaseHandler.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, selectedProductCode.trim());
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                newData.add(new Object[]{
                        resultSet.getString("product_code"),
                        resultSet.getString("material_code"),
                        resultSet.getString("name"),
                        resultSet.getDouble("brutto"),
                        resultSet.getDouble("losses"),
                        resultSet.getDouble("netto"),
                        resultSet.getDouble("price"),
                        resultSet.getDouble("sum")
                });
            }
        } catch (SQLException e) {
            System.err.println("Error loading packaging data:");
            e.printStackTrace();
        }

        updateData(newData);
    }

    private synchronized void updateData(List<Object[]> newData) {
        this.data = new ArrayList<>(newData);
        SwingUtilities.invokeLater(this::fireTableDataChanged);
    }

    public void refreshData() {
        loadDataFromDatabase();
        fireTableDataChanged();
    }

    public void setSelectedProduct(String productCode) {
        this.selectedProductCode = productCode != null ? productCode : "";
        loadDataFromDatabase();
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public Object getValueAt(int row, int col) {
        if (row < 0 || row >= data.size()) return null;
        Object[] rowData = data.get(row);

        if (col < rowData.length) {
            Object value = rowData[col];

            // Для числовых колонок форматируем с одним знаком после запятой
            if (col >= 3 && col <= 7) { // Колонки с числами: 3-7
                if (value == null) {
                    return "0.00"; // Возвращаем строку с форматированием
                }
                try {
                    double num = Double.parseDouble(value.toString());
                    return String.format("%.2f", num);
                } catch (NumberFormatException e) {
                    return "0.00"; // Если не число - возвращаем строку "0.0"
                }
            }
            return value;
        }
        return null;
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
            case 1:
            case 2: // Текстовые колонки
                return String.class;
            case 3:
            case 4:
            case 5:
            case 6:
            case 7: // Числовые колонки
                return String.class;
            default:
                return String.class;
        }
    }

    public double calculateTotalCostP() {
        return data.stream()
                .mapToDouble(row -> (Double) row[7]) // Индекс столбца с суммой (себестоимость)
                .sum();
    }

    public String generateNewPackagingMaterialCode() {
        String query = "SELECT MAX(code) AS max_code FROM packaging_materials";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String maxCode = rs.getString("max_code");
                if (maxCode != null) {
                    int nextCode = Integer.parseInt(maxCode.substring(2)) + 1;
                    return String.format("UP%03d", nextCode); // Форматируем код (например, PM0001)
                }
            }
        } catch (SQLException e) {
            ErrorHandler.handle(null, e, "генерация кода упаковочного материала");
        }
        return "UP001"; // Если упаковочных материалов нет, начинаем с PM0001
    }

    public double getPackagingMaterialPrice(String materialCode) throws SQLException {
        String query = "SELECT price FROM packaging_materials WHERE code = ?";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, materialCode);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("price");
            }
        }
        return 0.0;
    }


}