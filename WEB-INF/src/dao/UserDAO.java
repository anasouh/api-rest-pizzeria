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
                String password = rs.getString("password");
                return new User(login, password);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
}
