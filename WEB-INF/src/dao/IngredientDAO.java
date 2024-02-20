package dao;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import database.DS;
import dto.Ingredient;

public class IngredientDAO {
    private DS ds;

    public IngredientDAO() {
        this.ds = new DS();
    }

    public Ingredient[] findAll() {
        List<Ingredient> ingredients = new ArrayList<Ingredient>();
        try (Connection connection = ds.getConnection()) {
            Statement stat = connection.createStatement();
            ResultSet rs = stat.executeQuery("SELECT * FROM ingredients");
            while (rs.next()) {
                int id = rs.getInt("iid");
                String name = rs.getString("name");
                int price = rs.getInt("price");
                ingredients.add(new Ingredient(id, name, price));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ingredients.toArray(new Ingredient[0]);
    }

    public Ingredient findById(int id) {
        try (Connection connection = ds.getConnection()) {
            PreparedStatement stat = connection.prepareStatement("SELECT * FROM ingredients WHERE iid = ?");
            stat.setInt(1, id);
            ResultSet rs = stat.executeQuery();
            if (rs.next()) {
                String name = rs.getString("name");
                int price = rs.getInt("price");
                return new Ingredient(id, name, price);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Ingredient[] findByIds(List<Integer> ids) {
        List<Ingredient> ingredients = new ArrayList<Ingredient>();
        for (int id : ids) {
            Ingredient ingredient = findById(id);
            if (ingredient != null) {
                ingredients.add(ingredient);
            }
        }
        return ingredients.toArray(new Ingredient[0]);
    }

    public void save(Ingredient ingredient) {
        try (Connection connection = ds.getConnection()) {
            PreparedStatement stat = connection.prepareStatement("INSERT INTO ingredients (iid, name, price) VALUES (?, ?, ?)");
            ingredient.setId(ingredient.getId());
            stat.setInt(1, ingredient.getId());
            stat.setString(2, ingredient.getName());
            stat.setInt(3, ingredient.getPrice());
            stat.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void delete(Ingredient ingredient) {
        try (Connection connection = ds.getConnection()) {
            PreparedStatement stat = connection.prepareStatement("DELETE FROM ingredients WHERE iid = ?");
            stat.setInt(1, ingredient.getId());
            stat.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
