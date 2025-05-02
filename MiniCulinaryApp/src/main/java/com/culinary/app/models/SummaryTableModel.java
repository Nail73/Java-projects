package com.culinary.app.models;


import javax.swing.table.AbstractTableModel;

public class SummaryTableModel extends AbstractTableModel {
    private final CalculationTableModel calculationModel;
    private final ProductTableModel productModel;
    public double productCost;
    private final String[] columnNames = {
            "Стоимость сырья", "Брутто, г", "Потери, %", "Нетто, г", "Цена, руб", "Себестоимость, руб"
    };

    public SummaryTableModel(ProductTableModel productModel, CalculationTableModel calculationModel) {
        this.productModel = productModel;
        this.calculationModel = calculationModel;
    }

    @Override
    public int getRowCount() {
        return 1; // Две строки: ингредиенты и упаковка
    }

    @Override
    public Object getValueAt(int row, int column) {
        String productCode = calculationModel.getSelectedProductCode();
        String productName = calculationModel.getSelectedProductName();
        if (productCode == null || productCode.isEmpty()) {
            return column == 0 ? productName : "";
        }

        if (row == 0) { // Ингредиенты
            return switch (column) {
                case 0 -> productName;
                case 1 -> String.format("%.2f", calculationModel.calculateTotalBrutto());
                case 2 -> String.format("%.2f", calculationModel.calculateTotalLosses());
                case 3 -> String.format("%.2f", calculationModel.calculateTotalNetto());
                case 4 -> String.format("%.2f", calculationModel.calculateTotalPrice());
                case 5 -> String.format("%.2f", calculationModel.calculateTotalCost());
                default -> null;
            };
        }
        return null;
    }


    @Override
    public int getColumnCount() {
        return columnNames.length;
    }


    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    public void updateData() {
        fireTableDataChanged();
    }

    public void setProductCost(double cost) {
        this.productCost = cost;
        fireTableDataChanged();
    }

    public double calculateTotalCostS() {
        return calculationModel.calculateTotalCost();
    }


}