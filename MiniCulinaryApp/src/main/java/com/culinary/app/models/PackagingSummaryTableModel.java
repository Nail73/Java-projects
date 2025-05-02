package com.culinary.app.models;

import javax.swing.table.AbstractTableModel;

public class PackagingSummaryTableModel extends AbstractTableModel {
    private final PackagingTableModel packagingModel;
    private final SummaryTableModel summaryModel;
    private final String[] columnNames = {"Параметр", "Себестоимость, руб"};

    public PackagingSummaryTableModel(PackagingTableModel packagingModel, SummaryTableModel summaryModel) {
        this.packagingModel = packagingModel;
        this.summaryModel = summaryModel;
    }

    @Override
    public int getRowCount() {
        return 2;
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (row == 0) {
            return switch (column) {
                case 0 -> "Стоимость упаковочных материалов";
                case 1 -> packagingModel.calculateTotalCostP();
                default -> null;
            };
        } else {
            return switch (column) {
                case 0 -> "Стоимость готового изделия";
                case 1 -> packagingModel.calculateTotalCostP() + summaryModel.calculateTotalCostS();
                default -> null;
            };
        }
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    public void refreshData() {
        fireTableDataChanged();
    }
}