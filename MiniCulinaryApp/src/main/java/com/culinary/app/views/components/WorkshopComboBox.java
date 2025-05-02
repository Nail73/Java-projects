package com.culinary.app.views.components;

import javax.swing.*;

public class WorkshopComboBox extends JComboBox<String> {
    public WorkshopComboBox() {
        super(new String[]{"Хлебо-булочный цех", "Салатный цех", "Цех полуфабрикатов"});
    }
}