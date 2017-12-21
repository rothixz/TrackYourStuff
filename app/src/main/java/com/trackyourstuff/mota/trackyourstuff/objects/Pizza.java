package com.trackyourstuff.mota.trackyourstuff.objects;

/**
 * Created by mota on 10/3/17.
 */

public class Pizza {
    String name, description;
    String ingredients;
    double cost;

    public Pizza() {
    }

    public Pizza(String name, String description, String ingredients, double cost) {
        this.name = name;
        this.description = description;
        this.ingredients = ingredients;
        this.cost = cost;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }
}
