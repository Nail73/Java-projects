package com.culinary.app.models;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class ProductListTableModel extends AbstractTableModel {
    private List<Object[]> data = new ArrayList<>();
    private final String[] columns = {"Код продукта", "Наименование"};

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public Object getValueAt(int row, int column) {
        return data.get(row)[column];
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }

    public void updateData(List<Object[]> newData) {
        this.data = new ArrayList<>(newData);
        fireTableDataChanged();
    }

    public void clear() {
        this.data.clear();
        fireTableDataChanged();
    }
}