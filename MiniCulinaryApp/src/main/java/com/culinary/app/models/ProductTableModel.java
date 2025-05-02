package com.culinary.app.models;

import com.culinary.app.exceptions.DatabaseException;
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


public class ProductTableModel extends AbstractTableModel {
    private final String[] columnNames = {"Код продукта", "Наименование", "Вес на упаковке, г", "Себестоимость, руб", "Статус"};
    private List<Object[]> data = new ArrayList<>();
    private String selectedWorkshop = "Хлебо-булочный цех"; // По умолчанию
    private String searchText = ""; // Для поиска по наименованию
    private String selectedStatus = "Все"; // Для фильтрации по статусу
    private JTable productTable; // Объявляем переменную productTable
    private String selectedProductCode; // Для хранения текущего выбранного продукта

    public ProductTableModel() {
        loadDataFromDatabase();

    }

    public String getSelectedWorkshop() {
        return selectedWorkshop;
    }

    // Установка выбранного цеха
    public void setSelectedWorkshop(String workshop) {
        this.selectedWorkshop = workshop;
        refreshData();
    }

    // Установка текста для поиска
    public void setSearchText(String text) {
        this.searchText = text;
        refreshData();
    }

    // Установка выбранного статуса
    public void setSelectedStatus(String status) {
        this.selectedStatus = status;
        refreshData();
    }

    public void setSelectedProduct(String productCode) {
        this.selectedProductCode = productCode;

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
        if (row < 0 || row >= data.size()) return null; // Защита от выхода за границы
        Object[] rowData = data.get(row);
        return col < rowData.length ? rowData[col] : null;
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    // Получение кода следующего продукта
    public String getNextProductCode() {
        String query = "SELECT MAX(code) AS max_code FROM products WHERE workshop = ?";
        try (Connection connection = DatabaseHandler.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, selectedWorkshop);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String maxCode = resultSet.getString("max_code");
                if (maxCode != null && maxCode.matches("\\d+")) {
                    int nextCode = Integer.parseInt(maxCode) + 1;
                    return String.format("%04d", nextCode);
                }
            }
        } catch (Exception e) {
            ErrorHandler.handle(null, e, "генерация кода продукта");
        }
        return "1001"; // Дефолтное значение
    }

    public int getProductWeight(String productCode) {
        for (Object[] row : data) {
            if (productCode.equals(row[0])) {
                return ((Number) row[2]).intValue(); // Предполагая, что вес в столбце 2
            }
        }
        return 0;
    }

    public void refreshData() {
        // Сохраняем текущее выделение
        int selectedRow = productTable != null ? productTable.getSelectedRow() : -1;
        String selectedCode = selectedRow >= 0 ? (String) getValueAt(selectedRow, 0) : null;

        loadDataFromDatabase();
        fireTableDataChanged();

        // Восстанавливаем выделение
        if (selectedCode != null && productTable != null) {
            for (int i = 0; i < getRowCount(); i++) {
                if (selectedCode.equals(getValueAt(i, 0))) {
                    productTable.setRowSelectionInterval(i, i);
                    break;
                }
            }
        }
    }

    private void loadDataFromDatabase() {
        String query = "SELECT code, name, weight, cost, status FROM products " +
                "WHERE workshop = ? AND name LIKE ?";
        if (!selectedStatus.equals("Все")) {
            query += " AND status = '" + selectedStatus + "'";
        }

        data.clear();
        try (Connection connection = DatabaseHandler.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, selectedWorkshop);
            statement.setString(2, "%" + searchText + "%");

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String productCode = resultSet.getString("code");
                // Берем себестоимость из анализа, а не из products.cost
                double cost = DatabaseHandler.getProductCostFromAnalysis(productCode);

                data.add(new Object[]{
                        productCode,
                        resultSet.getString("name"),
                        resultSet.getDouble("weight"),
                        cost,  // Используем значение из анализа себестоимости
                        resultSet.getString("status")
                });
            }
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка загрузки продуктов", e);
        }
    }
    /**
     * Обновляет себестоимость выбранного продукта в таблице,
     * подтягивая данные из total_cost_per_unit и обновляя конкретную строку.
     */
    public void refreshProductCosts() {
        String productCode = this.selectedProductCode;
        if (productCode == null || productCode.isEmpty()) return;

        String query = "SELECT total_cost_without_vat FROM total_cost_per_unit WHERE product_code = ?";

        try (Connection connection = DatabaseHandler.getConnection();
             PreparedStatement stmt = connection.prepareStatement(query)) {

            stmt.setString(1, productCode);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                double cost = rs.getDouble("total_cost_without_vat");

                // Обновляем себестоимость в списке данных
                for (int i = 0; i < data.size(); i++) {
                    Object[] row = data.get(i);
                    if (productCode.equals(row[0])) { // если совпадает код продукта
                        row[3] = String.format("%.2f", cost); // обновляем себестоимость
                        fireTableCellUpdated(i, 3); // обновляем только ячейку
                        break;
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Ошибка загрузки себестоимости для продукта: " + productCode);
            e.printStackTrace();
        }

        // Можно также полностью обновить таблицу, если нужно
         loadDataFromDatabase();
    }

}
