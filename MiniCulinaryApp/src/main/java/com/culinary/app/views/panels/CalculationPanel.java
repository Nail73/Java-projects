package com.culinary.app.views.panels;


import com.culinary.app.exceptions.ValidationException;
import com.culinary.app.models.*;
import com.culinary.app.utils.CustomButton;
import com.culinary.app.utils.DatabaseHandler;
import com.culinary.app.utils.ErrorHandler;
import com.culinary.app.utils.UIUtils;
import com.culinary.app.views.components.RecipeEconomicsPanel;
import com.culinary.app.views.components.TemporaryIngredient;
import com.culinary.app.views.components.WorkshopComboBox;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.print.PrinterException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CalculationPanel extends JPanel {

    private static final Logger LOGGER = Logger.getLogger(CalculationPanel.class.getName());
    private static final Font LABEL_FONT = new Font("Arial", Font.BOLD, 16);

    private final ProductTableModel productModel;
    private final IngredientTableModel ingredientModel;
    private final CalculationTableModel calculationModel;
    private final PackagingTableModel packagingModel;
    private SummaryTableModel summaryModel;
    private final PackagingSummaryTableModel packagingSummaryModel;
    private final CostAnalysisTableModel costAnalysisModel;
    private JTable productTable;
    private JTable ingredientTable;
    private JTable calculationTable;
    private JTable packagingTable;
    private JTable summaryTable;
    private JTable packagingSummaryTable;
    private JTable costAnalysisTable;
    private WorkshopComboBox workshopComboBox;
    private JComboBox<String> statusComboBox;
    private JFormattedTextField  searchField;
    private final List<TemporaryIngredient> temporaryIngredients = new ArrayList<>();
    private ProductListTableModel productListModel;
    private JTable productListTable;
    private RecipeEconomicsPanel economicsPanel;
    private List<TemporaryIngredient> tempIngredients = new ArrayList<>();
    private double productWeight;
    private double brutto;
    private double losses;
    private String ingredientName;
    private double price;



    public CalculationPanel() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Инициализация моделей
        productModel = new ProductTableModel();
        ingredientModel = new IngredientTableModel();
        calculationModel = new CalculationTableModel();
        packagingModel = new PackagingTableModel();
        summaryModel = new SummaryTableModel(productModel, calculationModel);
        packagingSummaryModel = new PackagingSummaryTableModel(packagingModel, summaryModel);
        costAnalysisModel = new CostAnalysisTableModel(
                packagingSummaryModel,
                summaryModel,
                calculationModel,
                productModel
        );
        productListModel = new ProductListTableModel();
        productListModel = new ProductListTableModel();
        economicsPanel = new RecipeEconomicsPanel();


        // Инициализация таблиц
        initializeProductTable();
        initializeIngredientTable();
        initializeCalculationTable();
        initializePackagingTable();
        initializeSummaryTable();
        initializePackagingSummaryTable(); // Добавьте инициализацию новой таблицы
        initializeCostAnalysisTable();
        initProductListTable();

        // Устанавливаем режим выбора только одной строки
        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

// Устанавливаем пользовательский рендерер для изменения цвета фона выбранной строки
        productTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (isSelected) {
                    cell.setBackground(new Color(200, 230, 255)); // Цвет фона для выбранной строки
                    cell.setForeground(Color.BLACK); // Цвет текста для выбранной строки
                } else {
                    cell.setBackground(table.getBackground()); // Обычный цвет фона
                    cell.setForeground(table.getForeground()); // Обычный цвет текста
                }
                return cell;
            }
        });


        // Создание интерфейса
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(0.45);
        splitPane.setLeftComponent(createLeftPanel());
        splitPane.setRightComponent(createRightPanel());

        add(splitPane, BorderLayout.CENTER);

        UIUtils.configureTableHeaders(productTable);// Настройка заголовков
        UIUtils.configureTableHeaders(ingredientTable);
        UIUtils.configureTableHeaders(calculationTable);
        UIUtils.configureTableHeaders(packagingTable);
        UIUtils.configureTableHeaders(summaryTable);
        UIUtils.configureTableHeaders(packagingSummaryTable);
        UIUtils.configureTableHeaders(costAnalysisTable);
        UIUtils.configureTableHeaders(productListTable);

        UIUtils.configureTableCells(productTable);// Настройка ячеек
        UIUtils.configureTableCells(ingredientTable);
        UIUtils.configureTableCells(calculationTable);
        UIUtils.configureTableCells(packagingTable);
        UIUtils.configureTableCells(summaryTable);
        UIUtils.configureTableCells(packagingSummaryTable);
        UIUtils.configureTableCells(costAnalysisTable);
        UIUtils.configureTableCells(productListTable);

        // Форматирование таблиц
        UIUtils.configureTable(productTable);
        UIUtils.configureTable(ingredientTable);
        UIUtils.configureTable(calculationTable);
        UIUtils.configureTable(packagingTable);
        UIUtils.configureTable(summaryTable);
        UIUtils.configureTable(packagingSummaryTable); // Добавьте форматирование новой таблицы
        UIUtils.configureTable(costAnalysisTable);
        UIUtils.configureTable(productListTable);
        UIUtils.configureCostAnalysisTable(costAnalysisTable);

        initializeTables();

        // Первоначальное обновление данных
        synchronizeData();
        updateProductCost();
        refreshAllData();
        updateAllExistingProducts();
        setupIngredientSelectionListener();
    }


    private JPanel createLeftPanel() {
        JPanel leftPanel = new JPanel(new GridBagLayout());
        leftPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        addProductSection(leftPanel, gbc);
        addIngredientSection(leftPanel, gbc);

        return leftPanel;
    }

    private void addProductSection(JPanel panel, GridBagConstraints gbc) {
        JLabel productListLabel = new JLabel("Список продуктов:");
        productListLabel.setFont(LABEL_FONT);
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(productListLabel, gbc);

        gbc.gridy = 1;
        panel.add(createProductButtonPanel(), gbc);

        gbc.gridy = 2;
        panel.add(createWorkshopPanel(), gbc);

        gbc.gridy = 3;
        panel.add(createSearchAndStatusPanel(), gbc);

        initializeProductTable();
        JScrollPane productScrollPane = new JScrollPane(productTable);
        gbc.gridy = 4;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(productScrollPane, gbc);
    }

    private void addIngredientSection(JPanel panel, GridBagConstraints gbc) {
        JLabel ingredientListLabel = new JLabel("Список ингредиентов:");
        ingredientListLabel.setFont(LABEL_FONT);
        gbc.gridy = 5;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(ingredientListLabel, gbc);

        gbc.gridy = 6;
        panel.add(createIngredientButtonPanel(), gbc);

        initializeIngredientTable();
        JScrollPane ingredientScrollPane = new JScrollPane(ingredientTable);
        gbc.gridy = 7;
        gbc.weighty = 0.6;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(ingredientScrollPane, gbc);
    }

    private JPanel createRightPanel() {
        JPanel rightPanel = new JPanel(new BorderLayout());

        // 1. Верхняя часть - основные таблицы (калькуляция, упаковка, анализ)
        JPanel topPanel = createTopTablesPanel();

        // 2. Нижняя часть - разделенная на две равные части
        JSplitPane bottomSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        // Левая нижняя панель - Продукты с ингредиентом
        JPanel leftBottomPanel = createProductsWithIngredientPanel();

        // Правая нижняя панель - Экономика рецепта
        JPanel rightBottomPanel = createEconomicsPanel();

        // Настройка разделителя нижней части
        bottomSplit.setLeftComponent(leftBottomPanel);
        bottomSplit.setRightComponent(rightBottomPanel);
        bottomSplit.setDividerLocation(0.5); // Ровно посередине
        bottomSplit.setResizeWeight(0.5); // Равное изменение размеров

        // Общая компоновка правой панели
        JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        mainSplit.setTopComponent(topPanel);
        mainSplit.setBottomComponent(bottomSplit);
        mainSplit.setDividerLocation(0.99); // 70% верх, 30% низ
        mainSplit.setResizeWeight(1);


        leftBottomPanel.setMinimumSize(new Dimension(150, 200));
        rightBottomPanel.setMinimumSize(new Dimension(150, 200));
        bottomSplit.setDividerSize(3);
        bottomSplit.setBackground(new Color(200, 200, 200));

        rightPanel.add(mainSplit, BorderLayout.CENTER);
        return rightPanel;
    }

    private JPanel createTopTablesPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(createCalculationPanel());
        panel.add(Box.createVerticalStrut(10));
        panel.add(createSummaryPanel());
        panel.add(Box.createVerticalStrut(10));
        panel.add(createPackagingPanel());
        panel.add(Box.createVerticalStrut(10));
        panel.add(createPackagingSummaryPanel());
        panel.add(Box.createVerticalStrut(10));
        panel.add(createCostAnalysisPanel());


        return panel;
    }

    private JPanel createProductsWithIngredientPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        productListTable = new JTable(productListModel);
        productListTable.setFont(new Font("Arial", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(productListTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Ингредиент в продуктах"));

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.setPreferredSize(new Dimension(1, 1));
        return panel;
    }

    private JPanel createEconomicsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        economicsPanel = new RecipeEconomicsPanel();
        economicsPanel.setBorder(BorderFactory.createTitledBorder("Экономика рецепта"));

        panel.add(economicsPanel, BorderLayout.CENTER);
        panel.setPreferredSize(new Dimension(1, 1));
        return panel;
    }

    private JPanel createCalculationPanel() {
        return createTablePanel(
                "Калькуляция ингредиентов",
                calculationTable,
                new String[]{"Добавить", "Редактировать", "Удалить", "Напечатать"},
                new Runnable[]{this::addIngredientToCalculation, this::editCalculation,
                        this::deleteIngredientFromCalculation, this::printCalculation}
        );
    }

    private JPanel createSummaryPanel() {
        return createTablePanel(
                "Итог сырья",
                summaryTable,
                null,
                null
        );
    }

    private JPanel createPackagingPanel() {
        return createTablePanel(
                "Упаковочные материалы",
                packagingTable,
                new String[]{"Добавить", "Редактировать", "Удалить", "Создать упаковку"},
                new Runnable[]{this::addPackaging, this::editPackaging, this::deletePackaging, this::createNewPackagingMaterial}
        );
    }

    private JPanel createPackagingSummaryPanel() {
        return createTablePanel(
                "Стоимость готового изделия",
                packagingSummaryTable,
                null,
                null
        );
    }

    private JPanel createCostAnalysisPanel() {
        return createTablePanel(
                "Анализ себестоимости",
                costAnalysisTable,
                null,
                null
        );
    }

    private void initializeTables() {
        // Таблица продуктов
        productTable.setPreferredScrollableViewportSize(new Dimension(500, 400));
        // Таблица ингредиентов
        ingredientTable.setPreferredScrollableViewportSize(new Dimension(500, 300));
        // Таблица калькуляции
        calculationTable.setPreferredScrollableViewportSize(new Dimension(800, 600));
        // Таблица итогов сырья
        summaryTable.setPreferredScrollableViewportSize(new Dimension(800, 50));
        // Таблица упаковки
        packagingTable.setPreferredScrollableViewportSize(new Dimension(800, 250));
        // Таблица итогов сырья и упаковки
        packagingSummaryTable.setPreferredScrollableViewportSize(new Dimension(800, 100));
        // Таблица анализа себестоимости
        costAnalysisTable.setPreferredScrollableViewportSize(new Dimension(800, 700));
        // Таблица продуктов с ингредиентом
        productListTable.setPreferredScrollableViewportSize(new Dimension(400, 100));
    }

    // Метод для обновления данных
    private void updateEconomicsPanel() {
        // Получаем выбранный продукт
        String productCode = calculationModel.getSelectedProductCode();
        if (productCode == null || productCode.isEmpty()) return;

        // Получаем данные продукта
        double cost = 0;
        try {
            cost = DatabaseHandler.getProductCostFromAnalysis(productCode);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        int requiredWeight = productModel.getProductWeight(productCode);
        int actualWeight = (int) calculationModel.calculateTotalNetto();

        // Расчетные значения
        double costPerKg = cost * (1000.0 / requiredWeight); // Стоимость за 1 кг
        double costPer100g = cost * (100.0 / requiredWeight); // Стоимость за 100 г

        double revenue = cost * 1.3; // Примерный расчет доходности
        double profit = revenue - cost;
        double margin = (profit / revenue) * 100;

        // Обновляем панель
        economicsPanel.updateEconomics(
                cost, costPerKg, costPer100g,
                revenue, profit, margin,
                actualWeight, requiredWeight
        );
    }


    public static void setColumnWidthByName(JTable table, String columnName, int width) {
        try {
            TableColumn column = table.getColumn(columnName);
            column.setPreferredWidth(width);
            column.setMinWidth(width);
        } catch (IllegalArgumentException e) {
            System.err.println("Столбец '" + columnName + "' не найден");
        }
    }

    // Вспомогательный метод для создания панелей с таблицами
    private JPanel createTablePanel(String title, JTable table, String[] buttonLabels, Runnable[] buttonActions) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));

        if (buttonLabels != null && buttonActions != null) {
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
            for (int i = 0; i < buttonLabels.length; i++) {
                JButton button;
                switch(buttonLabels[i]) {
                    case "Добавить":
                        button = CustomButton.createAddButton(buttonActions[i]);
                        break;
                    case "Редактировать":
                        button = CustomButton.createEditButton(buttonActions[i]);
                        break;
                    case "Удалить":
                        button = CustomButton.createDeleteButton(buttonActions[i]);
                        break;
                    default:
                        button = CustomButton.createButton(
                                buttonLabels[i],
                                new Color(230, 230, 230), // серый фон
                                Color.BLACK,              // черный текст
                                buttonActions[i]
                        );
                }
                buttonPanel.add(button);
            }
            panel.add(buttonPanel, BorderLayout.NORTH);
        }

        // Настройка таблицы
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setFillsViewportHeight(true);
        setColumnWidthByName(summaryTable, "Стоимость сырья", 510);
        setColumnWidthByName(productTable, "Наименование", 200);
        setColumnWidthByName(ingredientTable, "Наименование", 200);
        setColumnWidthByName(calculationTable, "Наименование", 200);
        setColumnWidthByName(packagingTable, "Наименование", 200);
        setColumnWidthByName(packagingSummaryTable, "Параметр", 200);
        setColumnWidthByName(costAnalysisTable, "Параметр", 300);


        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void initializeProductTable() {
        productTable = new JTable(productModel);
        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Скрываем столбец статуса
        TableColumn statusColumn = productTable.getColumnModel().getColumn(4);
        productTable.getColumnModel().removeColumn(statusColumn);

        // Настройка рендерера
        productTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (isSelected) {
                    c.setBackground(new Color(200, 230, 255)); // Цвет выделения
                    c.setForeground(Color.BLACK);
                } else {
                    c.setBackground(table.getBackground());
                    c.setForeground(table.getForeground());
                }
                return c;
            }
        });


        // Обработчик выбора
        productTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = productTable.getSelectedRow();
                if (selectedRow >= 0) {
                    // Сохраняем код выбранного продукта
                    String productCode = (String) productModel.getValueAt(selectedRow, 0);
                    String productName = (String) productModel.getValueAt(selectedRow, 1);

                    // Обновляем другие модели БЕЗ сброса выделения
                    SwingUtilities.invokeLater(() -> {
                        calculationModel.setSelectedProduct(productCode, (String) productModel.getValueAt(selectedRow, 1));
                        packagingModel.setSelectedProduct(productCode);
                        productModel.setSelectedProduct(productCode);

                        // Обновляем данные
                        double cost = ((Number) productModel.getValueAt(selectedRow, 3)).doubleValue();
                        summaryModel.setProductCost(cost);
                        packagingSummaryModel.refreshData();
                        costAnalysisModel.setSelectedProduct(productCode, productName);
                        costAnalysisModel.refreshData();
                        updateEconomicsPanel();

                    });
                }
            }
        });

    }

    private void initializeIngredientTable() {
        ingredientTable = new JTable(ingredientModel);
        UIUtils.configureTable(ingredientTable);
    }

    private void initializeCalculationTable() {
        calculationTable = new JTable(calculationModel);
        // Настройка рендереров и выравнивания
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);

        for (int i = 0; i < calculationTable.getColumnCount(); i++) {
            if (i == 2) { // Наименование - выравнивание по левому краю
                calculationTable.getColumnModel().getColumn(i).setCellRenderer(leftRenderer);
            } else { // Все остальные столбцы - по центру
                calculationTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }
        UIUtils.configureTable(calculationTable);
    }

    private void initializeSummaryTable() {
        summaryModel = new SummaryTableModel(productModel, calculationModel);
        summaryTable = new JTable(summaryModel);
        UIUtils.configureTable(summaryTable);
        // Настройка форматирования
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        for (int i = 0; i < summaryTable.getColumnCount(); i++) {
            summaryTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

    }

    private void initializePackagingTable() {
        packagingTable = new JTable(packagingModel);
        UIUtils.configureTable(packagingTable);
        // Аналогичная настройка выравнивания
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);

        for (int i = 0; i < packagingTable.getColumnCount(); i++) {
            if (i == 2) { // Наименование - по левому краю
                packagingTable.getColumnModel().getColumn(i).setCellRenderer(leftRenderer);
            } else {
                packagingTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }
        // Кастомный рендерер для числовых колонок
        DefaultTableCellRenderer numberRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {

                // Форматируем числа с одним знаком после запятой
                if (value instanceof Number) {
                    value = String.format("%.2f", ((Number)value).doubleValue());
                } else if (value == null) {
                    value = "0.00";
                }

                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                return this;
            }
        };

        // Применяем рендерер ко всем колонкам
        for (int i = 0; i < packagingTable.getColumnCount(); i++) {
            packagingTable.getColumnModel().getColumn(i).setCellRenderer(numberRenderer);
        }

    }

    private void initializePackagingSummaryTable() {
        // Инициализация таблицы
        packagingSummaryTable = new JTable(packagingSummaryModel);

        // Форматирование таблицы
        UIUtils.configureTable(packagingSummaryTable);
    }

    private void initializeCostAnalysisTable() {
        // Инициализация таблицы
        costAnalysisTable = new JTable(costAnalysisModel);

        // Форматирование таблицы
        UIUtils.configureTable(packagingSummaryTable);
        UIUtils.configureCostAnalysisTable(costAnalysisTable);
    }

    private void initProductListTable() {
        productListModel = new ProductListTableModel();
        productListTable = new JTable(productListModel);

        UIUtils.configureTable(productListTable);
    }

    private void setupIngredientSelectionListener() {
        ingredientTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = ingredientTable.getSelectedRow();
                if (row >= 0) {
                    String ingredientCode = (String) ingredientModel.getValueAt(row, 0);
                    updateProductList(ingredientCode);
                } else {
                    productListModel.clear();
                }
            }
        });
    }

    private void updateProductList(String ingredientCode) {
        try {
            List<Object[]> products = DatabaseHandler.executeQuery(
                    "SELECT p.code, p.name FROM products p " +
                            "JOIN calculations c ON p.code = c.product_code " +
                            "WHERE c.ingredient_code = ? AND p.workshop = ?",
                    rs -> {
                        List<Object[]> result = new ArrayList<>();
                        while (rs.next()) {
                            result.add(new Object[]{
                                    rs.getString("code"),
                                    rs.getString("name")
                            });
                        }
                        return result;
                    },
                    ingredientCode,
                    productModel.getSelectedWorkshop()
            );

            productListModel.updateData(products);
        } catch (SQLException ex) {
            productListModel.clear();
            JOptionPane.showMessageDialog(this,
                    "Ошибка загрузки списка продуктов",
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
        }
    }


    public void synchronizeData() {
        String productCode = calculationModel.getSelectedProductCode();
        if (productCode == null) return;

        // Получаем себестоимость из анализа
        double totalCost = 0;
        try {
            totalCost = DatabaseHandler.getProductCostFromAnalysis(productCode);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // Обновляем продукт

        updateProductCost();

        // Обновляем связанные модели
        summaryModel.setProductCost(totalCost);
        summaryModel.fireTableDataChanged();
        packagingSummaryModel.refreshData();
        costAnalysisModel.refreshData();
    }

    public void updateAllExistingProducts() {
        try {
            // Если нужно именно List<Object[]>
            List<Object[]> productData = DatabaseHandler.executeQuery(
                    "SELECT code FROM products WHERE workshop = ?",
                    rs -> {
                        List<Object[]> result = new ArrayList<>();
                        while (rs.next()) {
                            result.add(new Object[]{rs.getString("code")});
                        }
                        return result;
                    },
                    productModel.getSelectedWorkshop()
            );

            // Обновляем каждый продукт
            for (Object[] row : productData) {
                String code = (String) row[0];
                double newCost = DatabaseHandler.getProductCostFromAnalysis(code);
                DatabaseHandler.executeQuery(
                        "UPDATE products SET cost = ? WHERE code = ?",
                        newCost, code
                );
            }

            productModel.refreshData();

        } catch (SQLException e) {
            ErrorHandler.handle(this, e, "массовое обновление себестоимости");
        }
    }

    private JPanel createProductButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        panel.add(CustomButton.createAddButton(this::addProduct));
        panel.add(CustomButton.createEditButton(this::editProduct));
        panel.add(CustomButton.createDeleteButton(this::deleteProduct));;
        return panel;
    }

    private JPanel createIngredientButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        panel.add(CustomButton.createAddButton(this::addIngredient));
        panel.add(CustomButton.createEditButton(this::editIngredient));
        panel.add(CustomButton.createDeleteButton(this::deleteIngredient));;
        return panel;
    }

    private JPanel createWorkshopPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        workshopComboBox = new WorkshopComboBox();
        workshopComboBox.addActionListener(e -> {
            String selectedWorkshop = (String) workshopComboBox.getSelectedItem();
            productModel.setSelectedWorkshop(selectedWorkshop);
            ingredientModel.setSelectedWorkshop(selectedWorkshop);
        });
        panel.add(new JLabel("Выберите цех:"));
        panel.add(workshopComboBox);
        return panel;
    }

    private JPanel createSearchAndStatusPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        // Увеличиваем поле для ввода поиска
        searchField = new JFormattedTextField("");
        searchField.setColumns(20); // Устанавливаем ширину поля ввода

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Поиск по наименованию:"));
        searchPanel.add(searchField);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                updateSearch();
            }

            public void removeUpdate(DocumentEvent e) {
                updateSearch();
            }

            public void changedUpdate(DocumentEvent e) {
                updateSearch();
            }

            private void updateSearch() {
                productModel.setSearchText(searchField.getText());
            }
        });

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(searchPanel, gbc);

        statusComboBox = new JComboBox<>(new String[]{"Все", "Активный", "Закрытый"});
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        statusPanel.add(new JLabel("Статус:"));
        statusPanel.add(statusComboBox);

        statusComboBox.addActionListener(e -> {
            productModel.setSelectedStatus((String) statusComboBox.getSelectedItem());
        });

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(statusPanel, gbc);

        return panel;
    }

    private boolean isDuplicateName(String name, String tableName) {
        String query = "SELECT COUNT(*) FROM " + tableName + " WHERE name = ?";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            ErrorHandler.handle(null, e, "проверка дубликатов");
        }
        return false;
    }

    private void addProduct() {
        try {
            JFormattedTextField  nameField = new JFormattedTextField ();
            JFormattedTextField  weightField = new JFormattedTextField ();

            JPanel panel = createInputPanel(
                    new JLabel("Наименование:"), nameField,
                    new JLabel("Вес на упаковке:"), weightField
            );

            if (UIUtils.showInputDialog(this, panel, "Добавить продукт") == JOptionPane.OK_OPTION) {

                String code = productModel.getNextProductCode();
                String name = nameField.getText();
                double weight = Double.parseDouble(weightField.getText());
                double cost = 0.0;
                String status = "Активный";
                String workshop = productModel.getSelectedWorkshop();

                if (isDuplicateName(name, "products")) {
                    UIUtils.showError(this, "Продукт с таким названием уже существует!");
                    return;
                }

                DatabaseHandler.executeQuery(
                        "INSERT INTO products (code, name, weight, cost, status, workshop) VALUES (?, ?, ?, ?, ?, ?)",
                        code, name, weight, cost, status, workshop
                );

                // Создаем полную запись в анализе себестоимости со всеми обязательными полями
                DatabaseHandler.executeQuery(
                        "INSERT INTO total_cost_per_unit (product_code, product_name, " +
                                "direct_cost, direct_self_cost, overhead_cost, " +
                                "total_cost_without_vat, total_cost_with_vat, " +
                                "profitability, price_without_vat, price_with_vat, " +
                                "markup, retail_price_without_vat, retail_price_with_vat) " +
                                "VALUES (?, ?, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)",
                        code, name
                );

                productModel.refreshData();
                updateProductCost();
                synchronizeData();
                // Показываем сообщение об успешном добавлении
                UIUtils.showInfo(this, "Продукт успешно добавлен!");
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error adding product", ex);
            UIUtils.showError(this, "Ошибка: " + ex.getMessage());
        }
    }

    private void editProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow < 0) {
            UIUtils.showError(this, "Выберите продукт для редактирования");
            return;
        }

        try {
            String code = (String) productModel.getValueAt(selectedRow, 0);
            String name = (String) productModel.getValueAt(selectedRow, 1);

            // Исправление: безопасное получение веса
            Object weightValue = productModel.getValueAt(selectedRow, 2);
            double weight;
            if (weightValue instanceof Double) {
                weight = (Double) weightValue;
            } else if (weightValue instanceof String) {
                weight = Double.parseDouble(((String) weightValue).replace(",", "."));
            } else {
                throw new IllegalArgumentException("Некорректный формат веса");
            }

            String status = (String) productModel.getValueAt(selectedRow, 4);

            JFormattedTextField  nameField = new JFormattedTextField (name);
            JFormattedTextField weightField = UIUtils.createNumericField();
            weightField.setValue(weight); // Устанавливаем числовое значение
            JComboBox<String> statusCombo = new JComboBox<>(new String[]{"Активный", "Закрытый"});
            statusCombo.setSelectedItem(status);

            JPanel panel = UIUtils.createInputPanel(
                    new JLabel("Наименование:"), nameField,
                    new JLabel("Вес на упаковке (г):"), weightField,
                    new JLabel("Статус:"), statusCombo
            );

            if (UIUtils.showInputDialog(this, panel, "Редактировать продукт") == JOptionPane.OK_OPTION) {
                String newName = nameField.getText().trim();
                double newWeight = ((Number) weightField.getValue()).doubleValue();
                String newStatus = (String) statusCombo.getSelectedItem();

                // Валидация
                if (newName.isEmpty()) {
                    throw new ValidationException("Наименование не может быть пустым");
                }
                if (newWeight <= 0) {
                    throw new ValidationException("Вес должен быть положительным числом");
                }

                DatabaseHandler.executeQuery(
                        "UPDATE products SET name = ?, weight = ?, status = ? WHERE code = ?",
                        newName, newWeight, newStatus, code
                );

                productModel.refreshData();
                updateProductCost();
                UIUtils.showInfo(this, "Продукт успешно обновлен");
            }
        } catch (ValidationException ex) {
            UIUtils.showError(this, ex.getMessage());
        } catch (Exception ex) {
            UIUtils.showError(this, "Ошибка при редактировании продукта: " + ex.getMessage());
            LOGGER.log(Level.SEVERE, "Error editing product", ex);
        }
    }

    private void deleteProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow < 0) {
            UIUtils.showError(this, "Выберите продукт для удаления");
            return;
        }

        String productCode = (String) productModel.getValueAt(selectedRow, 0);
        if (UIUtils.showConfirm(this, "Удалить продукт?")) {
            try {
                // Удаляем зависимые записи из таблиц product_packaging и calculations
                DatabaseHandler.executeInTransaction(conn -> {
                    // Удаляем записи из таблицы product_packaging
                    try (PreparedStatement stmt = conn.prepareStatement(
                            "DELETE FROM product_packaging WHERE product_code = ?")) {
                        stmt.setString(1, productCode);
                        stmt.executeUpdate();
                    } catch (SQLException ex) {
                        throw new RuntimeException(ex);
                    }

                    // Удаляем записи из таблицы calculations
                    try (PreparedStatement stmt = conn.prepareStatement(
                            "DELETE FROM calculations WHERE product_code = ?")) {
                        stmt.setString(1, productCode);
                        stmt.executeUpdate();
                    } catch (SQLException ex) {
                        throw new RuntimeException(ex);
                    }

                    // Удаляем записи из таблицы unit
                    try (PreparedStatement stmt = conn.prepareStatement(
                            "DELETE FROM total_cost_per_unit WHERE product_code = ?")) {
                        stmt.setString(1, productCode);
                        stmt.executeUpdate();
                    } catch (SQLException ex) {
                        throw new RuntimeException(ex);
                    }

                    // Удаляем записи из таблицы kg
                    try (PreparedStatement stmt = conn.prepareStatement(
                            "DELETE FROM total_cost_per_kg WHERE product_code = ?")) {
                        stmt.setString(1, productCode);
                        stmt.executeUpdate();
                    } catch (SQLException ex) {
                        throw new RuntimeException(ex);
                    }

                    // Удаляем продукт из таблицы products
                    try (PreparedStatement stmt = conn.prepareStatement(
                            "DELETE FROM products WHERE code = ?")) {
                        stmt.setString(1, productCode);
                        stmt.executeUpdate();
                    } catch (SQLException ex) {
                        throw new RuntimeException(ex);
                    }
                });

                // Обновляем таблицу продуктов
                productModel.refreshData();
                updateProductCost();
                UIUtils.showInfo(this, "Продукт успешно удален!");
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Error deleting product", ex);
                UIUtils.showError(this, "Ошибка при удалении продукта: " + ex.getMessage());
            }
        }
    }



    private void addIngredient() {
        try {
            JFormattedTextField  nameField = new JFormattedTextField ();
            JFormattedTextField  priceField = new JFormattedTextField ();

            JPanel panel = createInputPanel(
                    new JLabel("Наименование:"), nameField,
                    new JLabel("Цена:"), priceField
            );

            if (UIUtils.showInputDialog(this, panel, "Добавить ингредиент") == JOptionPane.OK_OPTION) {
                String code = ingredientModel.getNextIngredientCode();
                String name = nameField.getText();
                double price = Double.parseDouble(priceField.getText());
                String workshop = ingredientModel.getSelectedWorkshop();
                if (isDuplicateName(name, "ingredients")) {
                    UIUtils.showError(this, "Ингредиент с таким названием уже существует!");
                    return;
                }

                DatabaseHandler.executeQuery(
                        "INSERT INTO ingredients (code, name, price, workshop) VALUES (?, ?, ?, ?)",
                        code, name, price, workshop
                );

                ingredientModel.refreshData();
                synchronizeData();
                updateEconomicsPanel();
                updateProductCost();

                // Показываем сообщение об успешном добавлении
                UIUtils.showInfo(this, "Ингредиент успешно добавлен!");
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error adding ingredient", ex);
            UIUtils.showError(this, "Ошибка: " + ex.getMessage());
        }
    }

    private void editIngredient() {
        int selectedRow = ingredientTable.getSelectedRow();
        if (selectedRow < 0) {
            UIUtils.showError(this, "Выберите ингредиент для редактирования");
            return;
        }

        try {
            String code = (String) ingredientModel.getValueAt(selectedRow, 0);
            String name = (String) ingredientModel.getValueAt(selectedRow, 1);
            double price = (Double) ingredientModel.getValueAt(selectedRow, 2);

            JFormattedTextField  nameField = new JFormattedTextField (name);
            JFormattedTextField  priceField = new JFormattedTextField (String.valueOf(price));

            JPanel panel = createInputPanel(
                    new JLabel("Наименование:"), nameField,
                    new JLabel("Цена:"), priceField
            );

            if (UIUtils.showInputDialog(this, panel, "Редактировать ингредиент") == JOptionPane.OK_OPTION) {
                String newName = nameField.getText();
                double newPrice = Double.parseDouble(priceField.getText());

                DatabaseHandler.executeQuery(
                        "UPDATE ingredients SET name = ?, price = ? WHERE code = ?",
                        newName, newPrice, code
                );

                ingredientModel.refreshData();
                synchronizeData();
                updateEconomicsPanel();
                updateProductCost();
                // Показываем сообщение об успешном редактировании
                UIUtils.showInfo(this, "Ингредиент успешно отредактирован!");
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error editing ingredient", ex);
            UIUtils.showError(this, "Ошибка: " + ex.getMessage());
        }
    }

    private void deleteIngredient() {
        int selectedRow = ingredientTable.getSelectedRow();
        if (selectedRow < 0) {
            UIUtils.showError(this, "Выберите ингредиент для удаления");
            return;
        }

        String code = (String) ingredientModel.getValueAt(selectedRow, 0);
        if (UIUtils.showConfirm(this, "Удалить ингредиент?")) {
            try {
                DatabaseHandler.executeQuery("DELETE FROM ingredients WHERE code = ?", code);
                ingredientModel.refreshData();
                synchronizeData();
                updateEconomicsPanel();
                updateProductCost();
                // Показываем сообщение об успешном удалении
                UIUtils.showInfo(this, "Ингредиент успешно удален!");
            } catch (SQLException ex) {
                LOGGER.log(Level.SEVERE, "Error deleting ingredient", ex);
                UIUtils.showError(this, "Ошибка при удалении ингредиента: " + ex.getMessage());
            }
        }
    }

    private void addIngredientToCalculation() {
        try {
            // Проверка выбора продукта
            int productRow = productTable.getSelectedRow();
            if (productRow < 0) {
                UIUtils.showError(this, "Сначала выберите продукт");
                return;
            }

            String productCode = (String) productModel.getValueAt(productRow, 0);
            String productName = (String) productModel.getValueAt(productRow, 1);
            double productWeight = parseDouble(productModel.getValueAt(productRow, 2).toString());

            // Создаем диалоговое окно
            JDialog dialog = new JDialog();
            dialog.setTitle("Калькуляция для: " + productName);
            dialog.setLayout(new BorderLayout());
            dialog.setSize(700, 550);
            dialog.setLocationRelativeTo(this);

            // Инициализация компонентов
            DefaultComboBoxModel<String> ingredientsModel = getIngredientsList();
            JComboBox<String> ingredientCombo = new JComboBox<>(ingredientsModel);
            JFormattedTextField bruttoField = UIUtils.createNumericField();
            JFormattedTextField lossesField = UIUtils.createNumericField();
            lossesField.setValue(0.0); // Устанавливаем начальное значение
            JFormattedTextField priceField = UIUtils.createNumericField();
            priceField.setEditable(false); // Цена не редактируется

            // Очищаем временный список перед началом новой операции
            tempIngredients.clear();

            // Загружаем существующие ингредиенты для этого продукта
            calculationModel.setSelectedProduct(productCode, productName);
            for (int i = 0; i < calculationModel.getRowCount(); i++) {
                String ingCode = (String) calculationModel.getValueAt(i, 1);
                String ingName = (String) calculationModel.getValueAt(i, 2);
                double brutto = (Double) calculationModel.getValueAt(i, 3);
                double losses = (Double) calculationModel.getValueAt(i, 4);
                double price = (Double) calculationModel.getValueAt(i, 6);

                tempIngredients.add(new TemporaryIngredient(ingCode, ingName, brutto, losses, price));
            }

            // Создаем таблицу предпросмотра
            JTable previewTable = createPreviewTable();
            updatePreviewTable(previewTable, tempIngredients, productWeight);

            JButton addButton = new JButton("Добавить");
            JButton editButton = new JButton("Изменить");
            editButton.setEnabled(false);
            JButton saveButton = new JButton("Сохранить");
            JButton cancelButton = new JButton("Отменить");
            JButton deleteButton = new JButton("Удалить");

            // Панель ввода
            JPanel inputPanel = new JPanel(new GridLayout(5, 2, 5, 5));
            inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            inputPanel.add(new JLabel("Ингредиент:"));
            inputPanel.add(ingredientCombo);
            inputPanel.add(new JLabel("Брутто (г):"));
            inputPanel.add(bruttoField);
            inputPanel.add(new JLabel("Потери (%):"));
            inputPanel.add(lossesField);
            inputPanel.add(addButton);
            inputPanel.add(editButton);

            // Компоновка окна
            dialog.add(inputPanel, BorderLayout.NORTH);
            dialog.add(new JScrollPane(previewTable), BorderLayout.CENTER);

            JPanel buttonPanel = new JPanel();
            buttonPanel.add(saveButton);
            buttonPanel.add(cancelButton);
            buttonPanel.add(deleteButton);
            dialog.add(buttonPanel, BorderLayout.SOUTH);

            // Обработчики событий
            addButton.addActionListener(e -> {
                try {
                    // Получаем значения из форматированных полей
                    String bruttoText = bruttoField.getText();
                    String lossesText = lossesField.getText();

                    // Проверка на пустые значения
                    if (bruttoText.isEmpty() || lossesText.isEmpty()) {
                        UIUtils.showError(dialog, "Все поля должны быть заполнены!");
                        return;
                    }

                    double brutto = parseDouble(bruttoText);
                    double losses = parseDouble(lossesText);

                    // Валидация
                    if (brutto <= 0) {
                        UIUtils.showError(dialog, "Брутто должно быть положительным");
                        return;
                    }

                    if (losses < 0 || losses > 100) {
                        UIUtils.showError(dialog, "Потери должны быть от 0 до 100%");
                        return;
                    }

                    // Получаем цену из таблицы ингредиентов
                    String selectedIngredient = (String) ingredientCombo.getSelectedItem();
                    String ingCode = selectedIngredient.split(" - ")[0];
                    double price = getIngredientPrice(ingCode);
                    priceField.setValue(price); // Обновляем поле цены

                    // Добавляем ингредиент
                    String ingName = selectedIngredient.split(" - ")[1];
                    double netto = brutto * (1 - losses / 100);
                    double cost = brutto * price / 1000;

                    // Проверка на дубликат
                    if (tempIngredients.stream().anyMatch(i -> i.getIngredientCode().equals(ingCode))) {
                        UIUtils.showError(dialog, "Ингредиент уже добавлен");
                        return;
                    }

                    // Добавляем в список
                    tempIngredients.add(new TemporaryIngredient(ingCode, ingName, brutto, losses, price));
                    updatePreviewTable(previewTable, tempIngredients, productWeight);

                    // Сбрасываем поля
                    bruttoField.setValue(null);
                    lossesField.setValue(0.0);

                    refreshAllData();

                    // Обновляем себестоимость в таблице "Продукты"
                    updateProductCost();
                    productModel.fireTableDataChanged();

                    // Обновляем итоговые значения
                    updateTotalCosts(productCode);
                    synchronizeData();

                } catch (Exception ex) {
                    ErrorHandler.handle(dialog, ex, "добавление ингредиента");
                }
            });

            // Редактирование
            editButton.addActionListener(e -> {
                int row = previewTable.getSelectedRow();
                if (row >= 0 && row < tempIngredients.size()) {
                    TemporaryIngredient ing = tempIngredients.get(row);
                    ingredientCombo.setSelectedItem(ing.getIngredientCode() + " - " + ing.getIngredientName());
                    bruttoField.setValue(ing.getBrutto());
                    lossesField.setValue(ing.getLosses());
                    priceField.setValue(ing.getPrice());
                    tempIngredients.remove(row);
                    updatePreviewTable(previewTable, tempIngredients, productWeight);
                }
            });

            // Удаление
            deleteButton.addActionListener(e -> {
                int row = previewTable.getSelectedRow();
                if (row >= 0 && row < tempIngredients.size()) {
                    tempIngredients.remove(row);
                    updatePreviewTable(previewTable, tempIngredients, productWeight);
                    updateTotalCosts(productCode); // Обновляем итоговые значения
                }
            });

            // Выбор строки в таблице
            previewTable.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    int row = previewTable.getSelectedRow();
                    editButton.setEnabled(row >= 0 && row < tempIngredients.size());
                }
            });

            // Сохранение
            saveButton.addActionListener(e -> {
                try {
                    if (tempIngredients.isEmpty()) {
                        UIUtils.showError(dialog, "Добавьте ингредиенты");
                        return;
                    }

                    // Проверка веса
                    double totalNetto = tempIngredients.stream()
                            .mapToDouble(TemporaryIngredient::calculateNetto)
                            .sum();

                    if (Math.abs(totalNetto - productWeight) > 0.01) {
                        showWeightWarning(dialog, productWeight, totalNetto);
                        return;
                    }

                    // Сохраняем в транзакции
                    DatabaseHandler.executeInTransaction(conn -> {
                        // 1. Удаляем старые записи
                        try (PreparedStatement stmt = conn.prepareStatement(
                                "DELETE FROM calculations WHERE product_code = ?")) {
                            stmt.setString(1, productCode);
                            stmt.executeUpdate();
                        } catch (SQLException ex) {
                            throw new RuntimeException(ex);
                        }

                        // 2. Добавляем новые ингредиенты
                        double ingredientsCost = 0;
                        for (TemporaryIngredient ing : tempIngredients) {
                            double netto = ing.getBrutto() * (1 - ing.getLosses() / 100);
                            double cost = ing.getBrutto() * ing.getPrice() / 1000;
                            ingredientsCost += cost;
                            try (PreparedStatement stmt = conn.prepareStatement(
                                    "INSERT INTO calculations (product_code, ingredient_code, ingredient_name, brutto, losses, netto, price, cost) " +
                                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {

                                stmt.setString(1, productCode);
                                stmt.setString(2, ing.getIngredientCode());
                                stmt.setString(3, ing.getIngredientName());
                                stmt.setDouble(4, ing.getBrutto());
                                stmt.setDouble(5, ing.getLosses());
                                stmt.setDouble(6, netto);
                                stmt.setDouble(7, ing.getPrice());
                                stmt.setDouble(8, cost);
                                stmt.executeUpdate();
                            } catch (SQLException ex) {
                                throw new RuntimeException(ex);
                            }
                        }
                        // 3. Обновляем себестоимость продукта
                        ingredientsCost = tempIngredients.stream()
                                .mapToDouble(ing -> ing.getBrutto() * ing.getPrice() / 1000)
                                .sum();

                        try (PreparedStatement stmt = conn.prepareStatement(
                                "UPDATE products SET cost = ? WHERE code = ?")) {
                            stmt.setDouble(1, ingredientsCost);
                            stmt.setString(2, productCode);
                            stmt.executeUpdate();
                        } catch (SQLException ex) {
                            throw new RuntimeException(ex);
                        }
                    });

                    // Обновляем все таблицы
                    refreshAllData();
                    // Обновляем себестоимость в таблице "Продукты"

                    productModel.fireTableDataChanged();

                    // Обновляем итоговые значения
                    updateTotalCosts(productCode);
                    synchronizeData();

                    dialog.dispose();
                    UIUtils.showInfo(this, "Калькуляция сохранена. Себестоимость: " +
                            UIUtils.formatCurrency(calculationModel.calculateTotalCost()));

                } catch (Exception ex) {
                    ErrorHandler.handle(dialog, ex, "сохранение калькуляции");
                }
            });

            cancelButton.addActionListener(e -> dialog.dispose());
            dialog.setVisible(true);

        } catch (Exception ex) {
            ErrorHandler.handle(this, ex, "открытие калькуляции");
        }
    }

    private double getIngredientPrice(String ingredientCode) {
        String query = "SELECT price FROM ingredients WHERE code = ?";
        try (Connection conn = DatabaseHandler.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, ingredientCode);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("price");
            }
        } catch (SQLException e) {
            ErrorHandler.handle(null, e, "получение цены ингредиента");
        }
        return 0.0;
    }

    private void updateTotalCosts(String productCode) {
        double totalBrutto = tempIngredients.stream()
                .mapToDouble(TemporaryIngredient::getBrutto)
                .sum();
        double totalLosses = tempIngredients.stream()
                .mapToDouble(ing -> ing.getBrutto() * ing.getLosses() / 100)
                .sum();
        double totalNetto = totalBrutto - totalLosses;
        double totalCost = tempIngredients.stream()
                .mapToDouble(ing -> ing.getBrutto() * ing.getPrice() / 1000)
                .sum();

        // Обновляем модели
        calculationModel.refreshData();
        summaryModel.fireTableDataChanged();
        costAnalysisModel.refreshData();
updateProductCost();
        // Обновляем себестоимость продукта
        //productModel.updateProductCost(productCode, totalCost);
        productModel.fireTableDataChanged();
    }




    // Метод для обновления всех данных
    private void refreshAllData() {
        calculationModel.refreshData();
        productModel.refreshData();
        summaryModel.fireTableDataChanged();
        costAnalysisModel.refreshData();
updateProductCost();
    }

    // Создание таблицы предпросмотра
    private JTable createPreviewTable() {
        String[] columns = {"Ингредиент", "Брутто (г)", "Потери (%)", "Нетто (г)", "Цена (руб/кг)", "Стоимость"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // Только кнопка удаления
            }
        };

        JTable table = new JTable(model);

        // Настройка выравнивания
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        for (int i = 1; i <= 5; i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(rightRenderer);
        }

        return table;
    }

    // Обновление таблицы предпросмотра
    private void updatePreviewTable(JTable table, List<TemporaryIngredient> ingredients, double productWeight) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0);

        // Добавляем ингредиенты
        for (TemporaryIngredient ing : ingredients) {
            model.addRow(new Object[]{
                    ing.getIngredientName(),
                    UIUtils.formatNumber(ing.getBrutto()),
                    UIUtils.formatNumber(ing.getLosses()),
                    UIUtils.formatNumber(ing.calculateNetto()),
                    UIUtils.formatNumber(ing.getPrice()),
                    UIUtils.formatCurrency(ing.getBrutto() * ing.getPrice() / 1000)
            });
        }

        // Итоговая строка
        double totalBrutto = ingredients.stream().mapToDouble(TemporaryIngredient::getBrutto).sum();
        double totalNetto = ingredients.stream().mapToDouble(TemporaryIngredient::calculateNetto).sum();
        double totalCost = ingredients.stream().mapToDouble(ing -> ing.getBrutto() * ing.getPrice() / 1000).sum();
        double totalLosses = totalBrutto > 0 ? (1 - totalNetto / totalBrutto) * 100 : 0;

        model.addRow(new Object[]{
                "<html><b>Итого:</b></html>",
                "<html><b>" + UIUtils.formatNumber(totalBrutto) + "</b></html>",
                "<html><b>" + UIUtils.formatNumber(totalLosses) + "</b></html>",
                "<html><b>" + UIUtils.formatNumber(totalNetto) + "</b></html>",
                "",
                "<html><b>" + UIUtils.formatCurrency(totalCost) + "</b></html>",
                ""
        });

        // Подсветка
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {

                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (row == table.getRowCount() - 1) {
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                    if (Math.abs(totalNetto - productWeight) > 0.01) {
                        c.setBackground(new Color(255, 200, 200));
                    } else {
                        c.setBackground(new Color(200, 255, 200));
                    }
                }
                return c;
            }
        });
    }

    // Показ предупреждения о несоответствии веса
    private void showWeightWarning(Component parent, double required, double actual) {
        double difference = actual - required;
        String message = String.format(
                "<html><b>Несоответствие веса!</b><br><br>" +
                        "Требуемый вес: %.2f г<br>" +
                        "Фактический вес: %.2f г<br>" +
                        "Отклонение: %s%.2f г<br><br>" +
                        "Скорректируйте количество ингредиентов</html>",
                required,
                actual,
                difference > 0 ? "+" : "",
                difference
        );

        UIUtils.showError(parent, message);
    }


    private class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    private class ButtonEditor extends DefaultCellEditor {
        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
        }

        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            JButton button = new JButton("Удалить");
            button.addActionListener(e -> {
                fireEditingStopped();
                DefaultTableModel model = (DefaultTableModel) table.getModel();
                if (row < model.getRowCount() - 1) { // Не удаляем итоговую строку
                    model.removeRow(row);
                }
            });
            return button;
        }
    }

    private void editCalculation() {
        int selectedRow = calculationTable.getSelectedRow();
        if (selectedRow < 0) {
            UIUtils.showError(this, "Выберите строку для редактирования");
            return;
        }

        try {
            // Получаем значения из модели
            String productCode = (String) calculationModel.getValueAt(selectedRow, 0);
            String ingredientCode = (String) calculationModel.getValueAt(selectedRow, 1);
            String ingredientName = (String) calculationModel.getValueAt(selectedRow, 2);

            Object bruttoObj = calculationModel.getValueAt(selectedRow, 3);
            Object lossesObj = calculationModel.getValueAt(selectedRow, 4);
            Object priceObj = calculationModel.getValueAt(selectedRow, 6);

            // Преобразуем значения в нужный формат
            double brutto = parseDouble(String.valueOf(bruttoObj));
            double losses = parseDouble(String.valueOf(lossesObj));
            double price = parseDouble(String.valueOf(priceObj));

            // Создаем поля ввода
            JFormattedTextField bruttoField = UIUtils.createNumericField();
            bruttoField.setValue(brutto);

            JFormattedTextField lossesField = UIUtils.createNumericField();
            lossesField.setValue(losses);

            JFormattedTextField priceField = UIUtils.createNumericField();
            priceField.setValue(price);
            priceField.setEditable(false); // Цена не редактируется

            JPanel panel = createInputPanel(
                    new JLabel("Наименование:"), new JLabel(ingredientName),
                    new JLabel("Брутто (г):"), bruttoField,
                    new JLabel("Потери (%):"), lossesField,
                    new JLabel("Цена:"), priceField
            );

            if (UIUtils.showInputDialog(this, panel, "Редактировать калькуляцию") == JOptionPane.OK_OPTION) {
                // Получаем значения из форматированных полей
                double newBrutto = ((Number) bruttoField.getValue()).doubleValue();
                double newLosses = ((Number) lossesField.getValue()).doubleValue();
                double newPrice = ((Number) priceField.getValue()).doubleValue();
                double newNetto = newBrutto * (1 - newLosses / 100);

                // Проверка соответствия веса
                int productRow = productTable.getSelectedRow();
                if (productRow < 0) {
                    UIUtils.showError(this, "Не выбран продукт");
                    return;
                }

                double productWeight = (Double) productModel.getValueAt(productRow, 2);
                double currentTotalNetto = calculationModel.calculateTotalNetto();
                double currentRowNetto = brutto * (1 - losses / 100);
                double newTotalNetto = currentTotalNetto - currentRowNetto + newNetto;

                if (!checkWeightCompliance(productWeight, newTotalNetto)) {
                    return;
                }

                // Обновляем данные в базе
                double newCost = newBrutto * newPrice / 1000;

                DatabaseHandler.executeQuery(
                        "UPDATE calculations SET brutto = ?, losses = ?, netto = ?, price = ?, cost = ? " +
                                "WHERE product_code = ? AND ingredient_code = ?",
                        newBrutto, newLosses, newNetto, newPrice, newCost, productCode, ingredientCode
                );

                // Обновляем отображение
                calculationModel.refreshData();

                summaryModel.fireTableDataChanged();
                costAnalysisModel.refreshData();
                synchronizeData();
                updateProductCost();
            }
        } catch (Exception ex) {
            UIUtils.showError(this, "Ошибка при редактировании: " + ex.getMessage());
            LOGGER.log(Level.SEVERE, "Error editing calculation", ex);
        }
    }



    private double parseDouble(String value) {
        try {
            // Убираем все, кроме чисел, точек и запятых
            String normalized = value.replaceAll("[^\\d.,]", "");
            // Заменяем запятые на точки
            normalized = normalized.replace(",", ".");
            return Double.parseDouble(normalized);
        } catch (NumberFormatException e) {
            UIUtils.showError(this, "Некорректный числовой формат: " + value);
            return 0.0;
        }
    }

    private void deleteIngredientFromCalculation() {
        int selectedRow = calculationTable.getSelectedRow();
        if (selectedRow < 0) {
            UIUtils.showError(this, "Выберите ингредиент для удаления");
            return;
        }

        String ingredientCode = (String) calculationModel.getValueAt(selectedRow, 1);
        if (UIUtils.showConfirm(this, "Удалить ингредиент?")) {
            try {
                // Удаляем ингредиент из базы данных
                calculationModel.removeIngredient(calculationModel.getSelectedProductCode(), ingredientCode);
                calculationModel.refreshData();
                summaryModel.fireTableDataChanged();
                summaryModel.updateData();
                costAnalysisModel.refreshData();
                updateProductCost();
                synchronizeData();

                // Показываем сообщение об успешном удалении
                UIUtils.showInfo(this, "Ингредиент успешно удален из калькуляции!");
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Error deleting ingredient from calculation", ex);
                UIUtils.showError(this, "Ошибка при удалении ингредиента: " + ex.getMessage());
            }
        }
    }


    private void printCalculation() {
        try {
            boolean printed = calculationTable.print();
            if (printed) {
                UIUtils.showInfo(this, "Документ отправлен на печать");
            }
        } catch (PrinterException ex) {
            LOGGER.log(Level.SEVERE, "Error printing calculation", ex);
            UIUtils.showError(this, "Ошибка печати: " + ex.getMessage());
        }
    }

    private DefaultComboBoxModel<String> getIngredientsList() throws SQLException {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        DatabaseHandler.executeQuery(
                "SELECT code, name FROM ingredients WHERE workshop = ?",
                rs -> {
                    while (true) {
                        try {
                            if (!rs.next()) break;
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                        try {
                            model.addElement(rs.getString("code") + " - " + rs.getString("name"));
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }
                },
                ingredientModel.getSelectedWorkshop()
        );
        return model;
    }

    public void updateProductCost() {
        try {
            String productCode = calculationModel.getSelectedProductCode();
            if (productCode == null || productCode.isEmpty()) {
                return;
            }

            // Получаем себестоимость из таблицы total_cost_per_unit
            double cost = DatabaseHandler.getProductCostFromAnalysis(productCode);

            // Обновляем стоимость продукта в базе
            DatabaseHandler.executeQuery(
                    "UPDATE products SET cost = ? WHERE code = ?",
                    cost, productCode
            );

            // Обновляем отображение
            productModel.refreshData();
            summaryModel.fireTableDataChanged();

        } catch (SQLException ex) {
            UIUtils.showError(this, "Ошибка обновления себестоимости: " + ex.getMessage());
        }
    }

    private void addPackaging() {
        try {
            if (calculationModel.getSelectedProductCode().isEmpty()) {
                UIUtils.showError(this, "Сначала выберите продукт");
                return;
            }

            DefaultComboBoxModel<String> materialsModel = getPackagingMaterials();
            if (materialsModel.getSize() == 0) {
                UIUtils.showError(this, "Нет доступных упаковочных материалов");
                return;
            }

            JComboBox<String> materialCombo = new JComboBox<>(materialsModel);
            JFormattedTextField bruttoField = UIUtils.createNumericField();
            JFormattedTextField lossesField = UIUtils.createNumericField();
            lossesField.setValue(0.0);

            JPanel panel = createInputPanel(
                    new JLabel("Материал:"), materialCombo,
                    new JLabel("Брутто (г):"), bruttoField,
                    new JLabel("Потери (%):"), lossesField
            );

            if (UIUtils.showInputDialog(this, panel, "Добавить упаковку") == JOptionPane.OK_OPTION) {
                // Получаем значения из полей
                double brutto = ((Number) bruttoField.getValue()).doubleValue();
                double losses = ((Number) lossesField.getValue()).doubleValue();
                String selected = (String) materialCombo.getSelectedItem();
                String[] parts = selected.split(" - ");
                String materialCode = parts[0];
                String materialName = parts[1]; // Получаем название материала

                // Получаем цену из базы данных
                double price = packagingModel.getPackagingMaterialPrice(materialCode);
                double netto = brutto * (1 - losses / 100);
                double sum = brutto * price / 1000;

                DatabaseHandler.executeQuery(
                        "INSERT INTO product_packaging (product_code, material_code, name, brutto, losses, netto, price, sum) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                        calculationModel.getSelectedProductCode(),
                        materialCode,
                        materialName, // Добавляем название материала
                        brutto,
                        losses,
                        netto,
                        price,
                        sum
                );

                packagingModel.refreshData();
                updateProductCost();
                synchronizeData();
                UIUtils.showInfo(this, "Упаковочный материал добавлен");
            }
        } catch (Exception ex) {
            UIUtils.showError(this, "Ошибка добавления упаковки: " + ex.getMessage());
        }
    }

    private void editPackaging() {
        int selectedRow = packagingTable.getSelectedRow();
        if (selectedRow < 0) {
            UIUtils.showError(this, "Выберите упаковку для редактирования");
            return;
        }

        try {
            String productCode = (String) packagingModel.getValueAt(selectedRow, 0);
            String materialCode = (String) packagingModel.getValueAt(selectedRow, 1);
            String materialName = (String) packagingModel.getValueAt(selectedRow, 2);

            // Исправленный парсинг чисел с заменой запятой на точку
            double brutto = parseLocalizedDouble(packagingModel.getValueAt(selectedRow, 3).toString());
            double losses = parseLocalizedDouble(packagingModel.getValueAt(selectedRow, 4).toString());
            double price = parseLocalizedDouble(packagingModel.getValueAt(selectedRow, 6).toString());
            double currentSum = parseLocalizedDouble(packagingModel.getValueAt(selectedRow, 7).toString());

            JFormattedTextField bruttoField = UIUtils.createNumericField();
            bruttoField.setValue(brutto);

            JFormattedTextField lossesField = UIUtils.createNumericField();
            lossesField.setValue(losses);

            JFormattedTextField priceField = UIUtils.createNumericField();
            priceField.setValue(price);
            priceField.setEditable(true); // Изменено с false на true

            JPanel panel = createInputPanel(
                    new JLabel("Материал:"), new JLabel(materialName + " (" + materialCode + ")"),
                    new JLabel("Брутто (г):"), bruttoField,
                    new JLabel("Потери (%):"), lossesField,
                    new JLabel("Цена:"), priceField
            );

            if (UIUtils.showInputDialog(this, panel, "Редактировать упаковку") == JOptionPane.OK_OPTION) {
                double newBrutto = ((Number) bruttoField.getValue()).doubleValue();
                double newLosses = ((Number) lossesField.getValue()).doubleValue();
                double newPrice = ((Number) priceField.getValue()).doubleValue(); // Добавлено

                // Проверка допустимости значений
                if (newBrutto <= 0) {
                    UIUtils.showError(this, "Брутто должно быть положительным числом");
                    return;
                }

                if (newLosses < 0 || newLosses > 100) {
                    UIUtils.showError(this, "Потери должны быть в диапазоне от 0 до 100%");
                    return;
                }

                // Пересчитываем нетто и сумму
                double newNetto = newBrutto * (1 - newLosses / 100);
                double newSum = newBrutto * price / 1000;

                // Обновляем запись в базе данных
                DatabaseHandler.executeQuery(
                        "UPDATE product_packaging SET brutto = ?, losses = ?, netto = ?, sum = ?, price = ? " + // Добавлено price
                                "WHERE product_code = ? AND material_code = ?",
                        newBrutto,
                        newLosses,
                        newNetto,
                        newSum,
                        newPrice, // Добавлено
                        productCode,
                        materialCode
                );

                // Обновляем данные и пересчитываем себестоимость
                packagingModel.refreshData();
                updateProductCost();
                synchronizeData();

                UIUtils.showInfo(this, "Упаковочный материал успешно обновлен. Новая себестоимость: " +
                        UIUtils.formatCurrency(newSum));
            }
        } catch (Exception ex) {
            UIUtils.showError(this, "Ошибка редактирования упаковки: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private double parseLocalizedDouble(String value) throws NumberFormatException {
        return Double.parseDouble(value.replace(',', '.'));
    }

    private void deletePackaging() {
        int selectedRow = packagingTable.getSelectedRow();
        if (selectedRow < 0) {
            UIUtils.showError(this, "Выберите упаковку для удаления");
            return;
        }

        String productCode = (String) packagingModel.getValueAt(selectedRow, 0);
        String materialCode = (String) packagingModel.getValueAt(selectedRow, 1);

        if (UIUtils.showConfirm(this, "Удалить выбранную упаковку?")) {
            try {
                DatabaseHandler.executeQuery(
                        "DELETE FROM product_packaging WHERE product_code = ? AND material_code = ?",
                        productCode, materialCode
                );
                packagingModel.refreshData();
                updateProductCost();
                synchronizeData();

            } catch (SQLException ex) {
                UIUtils.showError(this, "Ошибка удаления упаковки: " + ex.getMessage());
            }
        }
    }

    private void createNewPackagingMaterial() {
        try {
            JFormattedTextField nameField = new JFormattedTextField();
            JFormattedTextField priceField = UIUtils.createNumericField();

            JPanel panel = createInputPanel(
                    new JLabel("Наименование:"), nameField,
                    new JLabel("Цена (руб/кг):"), priceField
            );

            if (UIUtils.showInputDialog(this, panel, "Создать новый упаковочный материал") == JOptionPane.OK_OPTION) {
                String name = nameField.getText().trim();
                double price = parseDouble(priceField.getText());

                if (name.isEmpty()) {
                    UIUtils.showError(this, "Название не может быть пустым");
                    return;
                }

                // Проверяем дубликаты в таблице packaging_materials
                String checkQuery = "SELECT COUNT(*) FROM packaging_materials WHERE name = ?";
                try (Connection conn = DatabaseHandler.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(checkQuery)) {
                    stmt.setString(1, name);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next() && rs.getInt(1) > 0) {
                        UIUtils.showError(this, "Упаковочный материал с таким названием уже существует!");
                        return;
                    }
                }

                // Генерируем код
                String code = packagingModel.generateNewPackagingMaterialCode();

                // Сохраняем новый материал
                DatabaseHandler.executeQuery(
                        "INSERT INTO packaging_materials (code, name, price) VALUES (?, ?, ?)",
                        code, name, price
                );

                // Обновляем модель
                packagingModel.refreshData();

                UIUtils.showInfo(this,
                        "<html><b>Новый упаковочный материал создан!</b><br>" +
                                "Код: " + code + "<br>" +
                                "Название: " + name + "<br>" +
                                "Цена: " + UIUtils.formatCurrency(price) + " руб/кг</html>");
            }
        } catch (Exception ex) {
            UIUtils.showError(this, "Ошибка создания упаковочного материала: " + ex.getMessage());
        }
    }


    private DefaultComboBoxModel<String> getPackagingMaterials() throws SQLException {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        DatabaseHandler.executeQuery(
                "SELECT code, name FROM packaging_materials",
                rs -> {
                    while (true) {
                        try {
                            if (!rs.next()) break;
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                        try {
                            model.addElement(rs.getString("code") + " - " + rs.getString("name"));
                        } catch (SQLException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
        );
        return model;
    }

    private boolean checkWeightCompliance(double productWeight, double newTotalNetto) {
        // Добавляем настраиваемый допуск (1% от веса продукта)
        double tolerance = productWeight * 0.01;
        double difference = newTotalNetto - productWeight;

        if (Math.abs(difference) > tolerance) {
            String message = String.format(
                    "<html><b>Несоответствие веса!</b><br><br>" +
                            "Вес на упаковке: %.2f г<br>" +
                            "Текущий вес нетто: %.2f г<br>" +
                            "Отклонение: %s%.2f г (допуск: ±%.2f г)<br><br>" +
                            "Скорректируйте вес ингредиентов</html>",
                    productWeight,
                    newTotalNetto,
                    difference > 0 ? "избыток " : "недостаток ",
                    Math.abs(difference),
                    tolerance
            );

            UIUtils.showError(this, message);
            return false;
        }
        return true;
    }

    private JPanel createInputPanel(Object... components) {
        JPanel panel = new JPanel(new GridLayout(components.length / 2, 2));
        for (Object component : components) {
            if (component instanceof Component) {
                if (component instanceof JFormattedTextField ) {
                    ((JFormattedTextField ) component).setToolTipText("Введите значение");
                }
                panel.add((Component) component);
            }
        }
        return panel;
    }
}