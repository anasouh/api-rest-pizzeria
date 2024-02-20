package dao;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import database.DS;
import dto.Ingredient;
import dto.Pizza;

public class PizzaDAO {
    private DS ds;

    public PizzaDAO() {
        this.ds = new DS();
    }

    public Pizza[] findAll() {
        List<Pizza> pizzas = new ArrayList<>();
        try (Connection connection = ds.getConnection()) {
            Statement stat = connection.createStatement();
            ResultSet rs = stat.executeQuery("SELECT * FROM pizzas");
            while (rs.next()) {
                int id = rs.getInt("pid");
                String name = rs.getString("name");
                int basePrice = rs.getInt("basePrice");
                String dough = rs.getString("dough");
                Pizza pizza = new Pizza(id, name, basePrice, dough);
                fillIngredients(pizza);
                pizzas.add(pizza);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pizzas.toArray(new Pizza[0]);
    }

    public Pizza findById(int id) {
        try (Connection connection = ds.getConnection()) {
            PreparedStatement stat = connection.prepareStatement("SELECT * FROM pizzas WHERE pid = ?");
            stat.setInt(1, id);
            ResultSet rs = stat.executeQuery();
            if (rs.next()) {
                String name = rs.getString("name");
                int basePrice = rs.getInt("basePrice");
                String dough = rs.getString("dough");
                Pizza pizza = new Pizza(id, name, basePrice, dough);
                fillIngredients(pizza);
                return pizza;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void save(Pizza pizza) {
        try (Connection connection = ds.getConnection()) {
            PreparedStatement stat;
            boolean exists = findById(pizza.getId()) != null;
            if (exists) {
                stat = connection.prepareStatement("UPDATE pizzas SET name = ?, baseprice = ?, dough = ? WHERE pid = ?");
                stat.setString(1, pizza.getName());
                stat.setInt(2, pizza.getBasePrice());
                stat.setString(3, pizza.getDough());
                stat.setInt(4, pizza.getId());
            } else {
                stat = connection.prepareStatement("INSERT INTO pizzas (pid, name, baseprice, dough) VALUES (?, ?, ?, ?)");
                pizza.setId(pizza.getId());
                stat.setInt(1, pizza.getId());
                stat.setString(2, pizza.getName());
                stat.setInt(3, pizza.getBasePrice());
                stat.setString(4, pizza.getDough());
            }
            stat.executeUpdate();
            saveIngredients(pizza);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void delete(Pizza pizza) {
        try (Connection connection = ds.getConnection()) {
            PreparedStatement stat = connection.prepareStatement("DELETE FROM pizzas WHERE pid = ?");
            stat.setInt(1, pizza.getId());
            stat.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fillIngredients(Pizza pizza) {
        IngredientDAO ingredientDao = new IngredientDAO();
        try (Connection connection = ds.getConnection()) {
            PreparedStatement stat = connection.prepareStatement("SELECT * FROM compose WHERE pid = ?");
            stat.setInt(1, pizza.getId());
            ResultSet rs = stat.executeQuery();
            while (rs.next()) {
                int iid = rs.getInt("iid");
                Ingredient ingredient = ingredientDao.findById(iid);
                pizza.addIngredient(ingredient);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveIngredients(Pizza pizza) {
        try (Connection connection = ds.getConnection()) {
            PreparedStatement stat = connection.prepareStatement("DELETE FROM compose WHERE pid = ?");
            stat.setInt(1, pizza.getId());
            stat.executeUpdate();
            stat = connection.prepareStatement("INSERT INTO compose (pid, iid) VALUES (?, ?)");
            for (Ingredient ingredient : pizza.getIngredients()) {
                IngredientDAO ingredientDao = new IngredientDAO();
                if (ingredientDao.findById(ingredient.getId()) == null) {
                    ingredientDao.save(ingredient);
                }

                stat.setInt(1, pizza.getId());
                stat.setInt(2, ingredient.getId());
                stat.addBatch();
            }
            stat.executeBatch();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        PizzaDAO dao = new PizzaDAO();
        try (Connection connection = new DS().getConnection()) {
            Statement stat = connection.createStatement();
            stat.executeUpdate("TRUNCATE TABLE ingredients, pizzas CASCADE");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Ingredient cheese = new Ingredient(1, "Fromage", 2);
        Ingredient sauce = new Ingredient(2, "Sauce tomate", 1);
        Pizza pizza = new Pizza(1, "Margherita", 5, "classic");
        pizza.addIngredient(cheese);
        pizza.addIngredient(sauce);
        dao.save(pizza);
        System.out.println(dao.findById(1));
        pizza.removeIngredient(sauce);
        dao.save(pizza);
        System.out.println(dao.findById(1));
        pizza.setBasePrice(6);
        dao.save(pizza);
        System.out.println(dao.findById(1));
        dao.delete(pizza);
        System.out.println(dao.findById(1));
    }
}
