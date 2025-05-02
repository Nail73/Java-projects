package com.culinary.app.views.components;

public class TemporaryIngredient {
    private String ingredientCode;
    private String ingredientName;
    private double brutto;
    private double losses;
    private double price;
    private double cost;

    // Конструктор, геттеры и сеттеры
    public TemporaryIngredient(String ingredientCode, String ingredientName,
                               double brutto, double losses, double price) {
        this.ingredientCode = ingredientCode;
        this.ingredientName = ingredientName;
        this.brutto = brutto;
        this.losses = losses;
        this.price = price;
        this.cost = cost;
    }

    public double calculateNetto() {
        return brutto - (losses / 100 * brutto);
    }

    public String getIngredientCode() {
        return ingredientCode;
    }


    public String getIngredientName() {
        return ingredientName;
    }

    public double getBrutto() {
        return brutto;
    }


    public double getLosses() {
        return losses;
    }


    public double getPrice() {
        return price;
    }


}