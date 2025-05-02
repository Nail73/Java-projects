package com.culinary.app.models;

import com.culinary.app.exceptions.DatabaseException;
import com.culinary.app.utils.DatabaseHandler;

import javax.swing.table.AbstractTableModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class IngredientTableModel extends AbstractTableModel {
    private final String[] columnNames = {"Код", "Наименование", "Цена"};
    private final List<Object[]> data = new ArrayList<>();
    private String selectedWorkshop = "Хлебо-булочный цех"; // По умолчанию

    public IngredientTableModel() {
        loadDataFromDatabase();
    }

    private void loadDataFromDatabase() {
        String query = "SELECT code, name, price FROM ingredients WHERE workshop = ?";
        try (Connection connection = DatabaseHandler.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, selectedWorkshop);
            ResultSet resultSet = statement.executeQuery();

            data.clear(); // Очищаем старые данные
            while (resultSet.next()) {
                Object[] row = {
                        resultSet.getString("code"),
                        resultSet.getString("name"),
                        resultSet.getDouble("price")
                };
                data.add(row); // Добавляем строку в данные таблицы
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void refreshData() {
        loadDataFromDatabase();
        fireTableDataChanged(); // Уведомляем таблицу об изменении данных
    }


    public String getSelectedWorkshop() {
        return selectedWorkshop;
    }

    public void setSelectedWorkshop(String workshop) {
        this.selectedWorkshop = workshop;
        refreshData();
    }

    // Получение кода следующего ингредиента
    public String getNextIngredientCode() {
        String query = "SELECT MAX(code) AS max_code FROM ingredients WHERE workshop = ?";
        try (Connection connection = DatabaseHandler.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, selectedWorkshop);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String maxCode = resultSet.getString("max_code");
                if (maxCode != null) {
                    int nextCode = Integer.parseInt(maxCode.substring(2)) + 1;
                    return String.format("GL%03d", nextCode); // Форматируем код (например, 0001)
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Ошибка генерации кода ингредиента", e);
        }
        return "GL101"; // Если ингредиентов нет, начинаем с 0001
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
    public Object getValueAt(int rowIndex, int columnIndex) {
        return data.get(rowIndex)[columnIndex];
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }
}