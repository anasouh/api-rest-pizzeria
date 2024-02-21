package dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Order {
    private int id;
    private String login;
    private String date;
    private Map<Pizza, Integer> pizzas;

    public Order() {
        this.pizzas = new HashMap<>();
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
        return new ArrayList<>(pizzas.keySet());
    }

    public void addPizza(Pizza pizza) {
        if (pizzas.containsKey(pizza)) {
            pizzas.put(pizza, pizzas.get(pizza) + 1);
        } else {
            pizzas.put(pizza, 1);
        }
    }

    public void removePizza(Pizza pizza) {
        if (pizzas.containsKey(pizza)) {
            if (pizzas.get(pizza) > 1) {
                pizzas.put(pizza, pizzas.get(pizza) - 1);
            } else {
                pizzas.remove(pizza);
            }
        }
    }

    public void setPizzaQuantity(Pizza pizza, int quantity) {
        if (pizzas.containsKey(pizza)) {
            if (quantity > 0) {
                pizzas.put(pizza, quantity);
            } else {
                pizzas.remove(pizza);
            }
        } else {
            if (quantity > 0) {
                pizzas.put(pizza, quantity);
            }
        }
    }

    public int getPizzaQuantity(Pizza pizza) {
        return pizzas.get(pizza);
    }

    public int getPrice() {
        int price = 0;
        for (Map.Entry<Pizza, Integer> entry : pizzas.entrySet()) {
            price += entry.getKey().getPrice() * entry.getValue();
        }
        return price;
    }

    public String toString() {
        return "Order : " + pizzas;
    }
}