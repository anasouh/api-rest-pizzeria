package dto;

import java.util.ArrayList;
import java.util.List;

public class Order {
    private int id;
    private String login;
    private String date;
    private List<Pizza> pizzas;

    public Order() {
        this.pizzas = new ArrayList<>();
    }

    public Order(int id, String login, String date) {
        this();
        this.id = id;
        this.login = login;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<Pizza> getPizzas() {
        return pizzas;
    }

    public void setPizzas(List<Pizza> pizzas) {
        this.pizzas = pizzas;
    }

    public void addPizza(Pizza pizza) {
        pizzas.add(pizza);
    }

    public void removePizza(Pizza pizza) {
        pizzas.remove(pizza);
    }

    public int getPrice() {
        int price = 0;
        for (Pizza pizza : pizzas) {
            price += pizza.getPrice();
        }
        return price;
    }

    public String toString() {
        return "Order : " + pizzas;
    }
}