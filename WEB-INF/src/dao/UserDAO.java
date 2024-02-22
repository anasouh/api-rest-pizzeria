package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import database.DS;
import dto.User;

public class UserDAO {
    private DS ds;

    public UserDAO() {
        this.ds = new DS();
    }

    public User findByLogin(String login) {
        try (Connection con = ds.getConnection()) {
            PreparedStatement stat = con.prepareStatement("SELECT * FROM users WHERE login = ?");
            stat.setString(1, login);
            ResultSet rs = stat.executeQuery();
            if (rs.next()) {
                String token = rs.getString("token");
                return new User(login, token);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
 
    public User authenticate(String login, String password) {
        try (Connection con = ds.getConnection()) {
            PreparedStatement stat = con.prepareStatement("SELECT * FROM users WHERE login = ? AND password = md5(?)");
            stat.setString(1, login);
            stat.setString(2, password);
            ResultSet rs = stat.executeQuery();
            if (rs.next()) {
                return new User(login, rs.getString("password"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void saveToken(User user, String token) {
        try (Connection con = ds.getConnection()) {
            PreparedStatement stat = con.prepareStatement("UPDATE users SET token = ? WHERE login = ?");
            stat.setString(1, token);
            stat.setString(2, user.getLogin());
            stat.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
