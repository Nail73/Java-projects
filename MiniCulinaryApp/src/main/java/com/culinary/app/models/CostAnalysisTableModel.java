package com.culinary.app.models;

import com.culinary.app.utils.DatabaseHandler;
import com.culinary.app.utils.UIUtils;

import javax.swing.table.AbstractTableModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CostAnalysisTableModel extends AbstractTableModel {
    private final PackagingSummaryTableModel packagingSummaryModel;
    private final SummaryTableModel summaryModel;
    private final CalculationTableModel calculationModel; // Добавлено
    private final String[] columnNames = {"Параметр", "На 1 кг ГП", "На 1 ед. ГП"};
    private final List<String[]> data;
    private ProductTableModel productTableModel;

    public CostAnalysisTableModel(
            PackagingSummaryTableModel packagingSummaryModel,
            SummaryTableModel summaryModel,
            CalculationTableModel calculationModel,
            ProductTableModel productTableModel
    ) {
        this.packagingSummaryModel = packagingSummaryModel;
        this.summaryModel = summaryModel;
        this.calculationModel = calculationModel;
        this.productTableModel = productTableModel;
        this.data = new ArrayList<>();

        // Инициализация данных
        data.add(new String[]{"Прямые расходы", "0.00", "0.00"});
        data.add(new String[]{"Прямая себестоимость", "0.00", "0.00"});
        data.add(new String[]{"Накладные расходы", "0.00", "0.00"});
        data.add(new String[]{"Полная себестоимость без НДС", "0.00", "0.00"});
        data.add(new String[]{"Полная себестоимость с НДС", "0.00", "0.00"});
        data.add(new String[]{"Норма доходности", "0.00", "0.00"});
        data.add(new String[]{"Цена отгрузки без НДС", "0.00", "0.00"});
        data.add(new String[]{"Цена отгрузки с НДС", "0.00", "0.00"});
        data.add(new String[]{"Наценка покупателя", "0.00", "0.00"});
        data.add(new String[]{"Розничная цена без НДС", "0.00", "0.00"});
        data.add(new String[]{"Розничная цена с НДС", "0.00", "0.00"});

        // Первоначальное обновление данных
        refreshData();
    }

    public void loadFromDatabase() {
        String productCode = calculationModel.getSelectedProductCode();
        if (productCode == null || productCode.isEmpty()) return;

        // Загрузка данных на 1 кг
        String queryKg = "SELECT * FROM total_cost_per_kg WHERE product_code = ?";
        try (Connection connection = DatabaseHandler.getConnection();
             PreparedStatement stmt = connection.prepareStatement(queryKg)) {
            stmt.setString(1, productCode);
            var rs = stmt.executeQuery();
            if (rs.next()) {
                data.get(0)[1] = formatDouble(rs.getDouble("direct_cost"));
                data.get(1)[1] = formatDouble(rs.getDouble("direct_self_cost"));
                data.get(2)[1] = formatDouble(rs.getDouble("overhead_cost"));
                data.get(3)[1] = formatDouble(rs.getDouble("total_cost_without_vat"));
                data.get(4)[1] = formatDouble(rs.getDouble("total_cost_with_vat"));
                data.get(5)[1] = formatDouble(rs.getDouble("profitability"));
                data.get(6)[1] = formatDouble(rs.getDouble("price_without_vat"));
                data.get(7)[1] = formatDouble(rs.getDouble("price_with_vat"));
                data.get(8)[1] = formatDouble(rs.getDouble("markup"));
                data.get(9)[1] = formatDouble(rs.getDouble("retail_price_without_vat"));
                data.get(10)[1] = formatDouble(rs.getDouble("retail_price_with_vat"));
            }
        } catch (SQLException e) {
            System.err.println("Ошибка загрузки данных из total_cost_per_kg: " + e.getMessage());
        }

        // Загрузка данных на 1 ед.
        String queryUnit = "SELECT * FROM total_cost_per_unit WHERE product_code = ?";
        try (Connection connection = DatabaseHandler.getConnection();
             PreparedStatement stmt = connection.prepareStatement(queryUnit)) {
            stmt.setString(1, productCode);
            var rs = stmt.executeQuery();
            if (rs.next()) {
                data.get(0)[2] = formatDouble(rs.getDouble("direct_cost"));
                data.get(1)[2] = formatDouble(rs.getDouble("direct_self_cost"));
                data.get(2)[2] = formatDouble(rs.getDouble("overhead_cost"));
                data.get(3)[2] = formatDouble(rs.getDouble("total_cost_without_vat"));
                data.get(4)[2] = formatDouble(rs.getDouble("total_cost_with_vat"));
                data.get(5)[2] = formatDouble(rs.getDouble("profitability"));
                data.get(6)[2] = formatDouble(rs.getDouble("price_without_vat"));
                data.get(7)[2] = formatDouble(rs.getDouble("price_with_vat"));
                data.get(8)[2] = formatDouble(rs.getDouble("markup"));
                data.get(9)[2] = formatDouble(rs.getDouble("retail_price_without_vat"));
                data.get(10)[2] = formatDouble(rs.getDouble("retail_price_with_vat"));
            }
        } catch (SQLException e) {
            System.err.println("Ошибка загрузки данных из total_cost_per_unit: " + e.getMessage());
        }

        fireTableDataChanged();
    }

    private String formatDouble(double value) {
        return String.format("%.2f", value);
    }

    public void setSelectedProduct(String productCode, String productName) {
        this.calculationModel.setSelectedProduct(productCode, productName);
        loadFromDatabase(); // Загрузка данных из БД
        refreshData();
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
    public Object getValueAt(int row, int column) {
        return data.get(row)[column];
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        // Вводимые руками строки: 0, 2, 5, 8
        return row == 0 || row == 2 || row == 5 || row == 8;
    }

    @Override
    public void setValueAt(Object aValue, int row, int column) {
        try {
            double value = Double.parseDouble(aValue.toString().replace(",", "."));
            data.get(row)[column] = String.format("%.2f", value);
            saveDataToDatabase(); // Сохраняем данные в базе данных
            refreshData(); // Пересчитываем после сохранения
        } catch (NumberFormatException e) {
            System.err.println("Ошибка ввода числа: " + aValue);
        }
    }

    public void refreshData() {
        // Обновление расчетных значений
        double packagingCost = packagingSummaryModel.getValueAt(1, 1) != null ?
                Double.parseDouble(packagingSummaryModel.getValueAt(1, 1).toString().replace(",", ".")) : 0.0;

        // Прямая себестоимость
        double directCost1 = packagingCost + Double.parseDouble(data.get(0)[1].replace(",", "."));
        double directCost2 = packagingCost + Double.parseDouble(data.get(0)[2].replace(",", "."));
        data.get(1)[1] = String.format("%.2f", directCost1);
        data.get(1)[2] = String.format("%.2f", directCost2);

        // Полная себестоимость без НДС
        double fullCostWithoutVAT1 = directCost1 + Double.parseDouble(data.get(2)[1].replace(",", "."));
        double fullCostWithoutVAT2 = directCost2 + Double.parseDouble(data.get(2)[2].replace(",", "."));
        data.get(3)[1] = String.format("%.2f", fullCostWithoutVAT1);
        data.get(3)[2] = String.format("%.2f", fullCostWithoutVAT2);

        // Полная себестоимость с НДС
        double fullCostWithVAT1 = fullCostWithoutVAT1 * 1.1;
        double fullCostWithVAT2 = fullCostWithoutVAT2 * 1.1;
        data.get(4)[1] = String.format("%.2f", fullCostWithVAT1);
        data.get(4)[2] = String.format("%.2f", fullCostWithVAT2);

        // Цена отгрузки без НДС
        double priceWithoutVAT1 = fullCostWithoutVAT1 + Double.parseDouble(data.get(5)[1].replace(",", "."));
        double priceWithoutVAT2 = fullCostWithoutVAT2 + Double.parseDouble(data.get(5)[2].replace(",", "."));
        data.get(6)[1] = String.format("%.2f", priceWithoutVAT1);
        data.get(6)[2] = String.format("%.2f", priceWithoutVAT2);

        // Цена отгрузки с НДС
        double priceWithVAT1 = fullCostWithVAT1 + Double.parseDouble(data.get(5)[1].replace(",", "."));
        double priceWithVAT2 = fullCostWithVAT2 + Double.parseDouble(data.get(5)[2].replace(",", "."));
        data.get(7)[1] = String.format("%.2f", priceWithVAT1);
        data.get(7)[2] = String.format("%.2f", priceWithVAT2);

        // Розничная цена без НДС
        double retailPriceWithoutVAT1 = priceWithoutVAT1 + Double.parseDouble(data.get(8)[1].replace(",", "."));
        double retailPriceWithoutVAT2 = priceWithoutVAT2 + Double.parseDouble(data.get(8)[2].replace(",", "."));
        data.get(9)[1] = String.format("%.2f", retailPriceWithoutVAT1);
        data.get(9)[2] = String.format("%.2f", retailPriceWithoutVAT2);

        // Розничная цена с НДС
        double retailPriceWithVAT1 = priceWithVAT1 + Double.parseDouble(data.get(8)[1].replace(",", "."));
        double retailPriceWithVAT2 = priceWithVAT2 + Double.parseDouble(data.get(8)[2].replace(",", "."));
        data.get(10)[1] = String.format("%.2f", retailPriceWithVAT1);
        data.get(10)[2] = String.format("%.2f", retailPriceWithVAT2);

        fireTableDataChanged();

        saveDataToDatabase(); // Сохраняем данные в базе данных
        // 🔁 Обновляем данные в "Экономике рецепта"
        if (calculationModel != null) {
            calculationModel.refreshData();
        }

        // 🔁 Обновляем данные в таблице "Продукты"
        if (productTableModel != null) {
            productTableModel.refreshProductCosts();
        }
    }


    public void saveDataToDatabase() {
        String productCode = calculationModel.getSelectedProductCode();
        if (productCode == null || productCode.isEmpty()) {
            // Убираем System.err.println чтобы не засорять логи
            return;
        }
        saveDataToDatabasePerKg();
        saveDataToDatabasePerUnit();
    }

    private void saveDataToDatabasePerKg() {
        String productCode = calculationModel.getSelectedProductCode();
        String productName = calculationModel.getSelectedProductName();
        String query = "INSERT INTO total_cost_per_kg (product_code, product_name, direct_cost, direct_self_cost, overhead_cost, " +
                "total_cost_without_vat, total_cost_with_vat, profitability, price_without_vat, price_with_vat, " +
                "markup, retail_price_without_vat, retail_price_with_vat) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "direct_cost = VALUES(direct_cost), " +
                "direct_self_cost = VALUES(direct_self_cost), " +
                "overhead_cost = VALUES(overhead_cost), " +
                "total_cost_without_vat = VALUES(total_cost_without_vat), " +
                "total_cost_with_vat = VALUES(total_cost_with_vat), " +
                "profitability = VALUES(profitability), " +
                "price_without_vat = VALUES(price_without_vat), " +
                "price_with_vat = VALUES(price_with_vat), " +
                "markup = VALUES(markup), " +
                "retail_price_without_vat = VALUES(retail_price_without_vat), " +
                "retail_price_with_vat = VALUES(retail_price_with_vat)";

        try (Connection connection = DatabaseHandler.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, productCode);
            statement.setString(2, productName);
            statement.setDouble(3, Double.parseDouble(data.get(0)[1].replace(",", ".")));
            statement.setDouble(4, Double.parseDouble(data.get(1)[1].replace(",", ".")));
            statement.setDouble(5, Double.parseDouble(data.get(2)[1].replace(",", ".")));
            statement.setDouble(6, Double.parseDouble(data.get(3)[1].replace(",", ".")));
            statement.setDouble(7, Double.parseDouble(data.get(4)[1].replace(",", ".")));
            statement.setDouble(8, Double.parseDouble(data.get(5)[1].replace(",", ".")));
            statement.setDouble(9, Double.parseDouble(data.get(6)[1].replace(",", ".")));
            statement.setDouble(10, Double.parseDouble(data.get(7)[1].replace(",", ".")));
            statement.setDouble(11, Double.parseDouble(data.get(8)[1].replace(",", ".")));
            statement.setDouble(12, Double.parseDouble(data.get(9)[1].replace(",", ".")));
            statement.setDouble(13, Double.parseDouble(data.get(10)[1].replace(",", ".")));
            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Ошибка сохранения данных в таблицу total_cost_per_kg: " + e.getMessage());
            e.printStackTrace(); // Для детального анализа ошибок
        }
    }

    private void saveDataToDatabasePerUnit() {
        String productCode = calculationModel.getSelectedProductCode(); // Исправлено
        String productName = calculationModel.getSelectedProductName(); // Исправлено

        String query = "INSERT INTO total_cost_per_unit (product_code, product_name, direct_cost, direct_self_cost, overhead_cost, " +
                "total_cost_without_vat, total_cost_with_vat, profitability, price_without_vat, price_with_vat, " +
                "markup, retail_price_without_vat, retail_price_with_vat) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "direct_cost = VALUES(direct_cost), " +
                "direct_self_cost = VALUES(direct_self_cost), " +
                "overhead_cost = VALUES(overhead_cost), " +
                "total_cost_without_vat = VALUES(total_cost_without_vat), " +
                "total_cost_with_vat = VALUES(total_cost_with_vat), " +
                "profitability = VALUES(profitability), " +
                "price_without_vat = VALUES(price_without_vat), " +
                "price_with_vat = VALUES(price_with_vat), " +
                "markup = VALUES(markup), " +
                "retail_price_without_vat = VALUES(retail_price_without_vat), " +
                "retail_price_with_vat = VALUES(retail_price_with_vat)";

        try (Connection connection = DatabaseHandler.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, productCode);
            statement.setString(2, productName);
            statement.setDouble(3, Double.parseDouble(data.get(0)[2].replace(",", ".")));
            statement.setDouble(4, Double.parseDouble(data.get(1)[2].replace(",", ".")));
            statement.setDouble(5, Double.parseDouble(data.get(2)[2].replace(",", ".")));
            statement.setDouble(6, Double.parseDouble(data.get(3)[2].replace(",", ".")));
            statement.setDouble(7, Double.parseDouble(data.get(4)[2].replace(",", ".")));
            statement.setDouble(8, Double.parseDouble(data.get(5)[2].replace(",", ".")));
            statement.setDouble(9, Double.parseDouble(data.get(6)[2].replace(",", ".")));
            statement.setDouble(10, Double.parseDouble(data.get(7)[2].replace(",", ".")));
            statement.setDouble(11, Double.parseDouble(data.get(8)[2].replace(",", ".")));
            statement.setDouble(12, Double.parseDouble(data.get(9)[2].replace(",", ".")));
            statement.setDouble(13, Double.parseDouble(data.get(10)[2].replace(",", ".")));

            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Ошибка сохранения данных в таблицу total_cost_per_unit: " + e.getMessage());
        }
    }

}