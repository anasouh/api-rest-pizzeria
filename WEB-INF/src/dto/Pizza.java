package dto;

import java.util.ArrayList;
import java.util.List;

public class Pizza {
    private int id;
    private String name;
    private int basePrice;
    private String dough;
    private List<Ingredient> ingredients;

    public Pizza() {
        this.ingredients = new ArrayList<>();
    }

    public Pizza(int id, String name, int basePrice, String dough) {
        this();
        this.id = id;
        this.name = name;
        this.basePrice = basePrice;
        this.dough = dough;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(int basePrice) {
        this.basePrice = basePrice;
    }

    public String getDough() {
        return dough;
    }

    public void setDough(String dough) {
        this.dough = dough;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    public void addIngredient(Ingredient ingredient) {
        this.ingredients.add(ingredient);
    }

    public void addIngredients(Ingredient ... ingredients) {
        for (Ingredient ingredient : ingredients) {
            addIngredient(ingredient);
        }
    }

    public void removeIngredient(Ingredient ingredient) {
        this.ingredients.remove(ingredient);
    }

    public int getPrice() {
        int price = basePrice;
        for (Ingredient ingredient : ingredients) {
            price += ingredient.getPrice();
        }
        return price;
    }

    public String toString() {
        return name + " (" + getPrice() + "€) : " + ingredients.toString();
    }

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }
        if (o.getClass() != this.getClass()) {
            return false;
        }
        Pizza p = (Pizza) o;
        return p.getId() == this.id;
    }
}
