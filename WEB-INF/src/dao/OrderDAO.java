package dao;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import database.DS;
import dto.Ingredient;
import dto.Order;
import dto.Pizza;

public class OrderDAO {
    private DS ds;

    public OrderDAO() {
        this.ds = new DS();
    }

    public Order[] findAll() {
        List<Order> orders = new ArrayList<>();
        try (Connection connection = ds.getConnection()) {
            Statement stat = connection.createStatement();
            ResultSet rs = stat.executeQuery("SELECT * FROM orders");
            while (rs.next()) {
                int id = rs.getInt("oid");
                String login = rs.getString("login");
                String date = rs.getDate("date").toString();
                Order order = new Order(id, login, date);
                fillPizzas(order);
                orders.add(order);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return orders.toArray(new Order[0]);
    }

    public Order findById(int id) {
        try (Connection connection = ds.getConnection()) {
            PreparedStatement stat = connection.prepareStatement("SELECT * FROM orders WHERE oid = ?");
            stat.setInt(1, id);
            ResultSet rs = stat.executeQuery();
            if (rs.next()) {
                String login = rs.getString("login");
                String date = rs.getDate("date").toString();
                Order order = new Order(id, login, date);
                fillPizzas(order);
                return order;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void save(Order order) {
        try (Connection connection = ds.getConnection()) {
            PreparedStatement stat;
            boolean exists = findById(order.getId()) != null;
            if (exists) {
                stat = connection.prepareStatement("UPDATE orders SET login = ?, date = ? WHERE oid = ?");
                stat.setString(1, order.getLogin());
                stat.setDate(2, Date.valueOf(order.getDate()));
                stat.setInt(3, order.getId());
            } else {
                stat = connection.prepareStatement("INSERT INTO orders (oid, login, date) VALUES (?, ?, ?)");
                stat.setInt(1, order.getId());
                stat.setString(2, order.getLogin());
                stat.setDate(3, Date.valueOf(order.getDate()));
            }
            stat.executeUpdate();
            savePizzas(order);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void delete(Order order) {
        try (Connection connection = ds.getConnection()) {
            PreparedStatement stat = connection.prepareStatement("DELETE FROM orders WHERE oid = ?");
            stat.setInt(1, order.getId());
            stat.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fillPizzas(Order order) {
        PizzaDAO pizzaDao = new PizzaDAO();
        try (Connection connection = ds.getConnection()) {
            PreparedStatement stat = connection.prepareStatement("SELECT * FROM contains WHERE oid = ?");
            stat.setInt(1, order.getId());
            ResultSet rs = stat.executeQuery();
            while (rs.next()) {
                int pid = rs.getInt("pid");
                int quantity = rs.getInt("quantity");
                Pizza pizza = pizzaDao.findById(pid);
                order.setPizzaQuantity(pizza, quantity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void savePizzas(Order order) {
        try (Connection connection = ds.getConnection()) {
            PreparedStatement stat = connection.prepareStatement("DELETE FROM contains WHERE oid = ?");
            stat.setInt(1, order.getId());
            stat.executeUpdate();
            stat = connection.prepareStatement("INSERT INTO contains (oid, pid, quantity) VALUES (?, ?, ?)");
            for (Map.Entry<Pizza, Integer> entry : order.getPizzasEntrySet()) {
                PizzaDAO pizzaDao = new PizzaDAO();
                Pizza pizza = entry.getKey();
                if (pizzaDao.findById(pizza.getId()) == null) {
                    pizzaDao.save(pizza);
                }

                stat.setInt(1, order.getId());
                stat.setInt(2, pizza.getId());
                stat.setInt(3, entry.getValue());
                stat.addBatch();
            }
            stat.executeBatch();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        OrderDAO dao = new OrderDAO();
        try (Connection connection = new DS().getConnection()) {
            Statement stat = connection.createStatement();
            stat.executeUpdate("TRUNCATE TABLE users, ingredients, pizzas, compose, orders, contains CASCADE");
        } catch (Exception e) {
            e.printStackTrace();
        }
        IngredientDAO ingredientDao = new IngredientDAO();
        Ingredient tomato = new Ingredient(1, "Tomato", 1);
        Ingredient cheese = new Ingredient(2, "Cheese", 2);
        ingredientDao.save(tomato);
        ingredientDao.save(cheese);

        PizzaDAO pizzaDao = new PizzaDAO();
        Pizza margherita = new Pizza(1, "Margherita", 5, "classic");
        margherita.addIngredient(tomato);
        margherita.addIngredient(cheese);
        pizzaDao.save(margherita);

        Pizza capricciosa = new Pizza(2, "Capricciosa", 7, "classic");
        capricciosa.addIngredient(tomato);
        capricciosa.addIngredient(cheese);
        pizzaDao.save(capricciosa);

        try (Connection connection = new DS().getConnection()) {
            Statement stat = connection.createStatement();
            stat.executeUpdate("INSERT INTO users (login, password) VALUES ('user', md5('user'))");
        } catch (Exception e) {
            e.printStackTrace();
        }

        Order order = new Order(1, "user", "2020-01-01");
        order.addPizza(margherita);
        order.addPizza(capricciosa);
        dao.save(order);

        order.removePizza(margherita);
        dao.save(order);

        Order[] orders = dao.findAll();
        for (Order o : orders) {
            System.out.println(o);
        }

    }
}
