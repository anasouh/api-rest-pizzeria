package database;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DS {
    private static final String CONFIG_PATH = "WEB-INF" + File.separator + "config.prop";
    Properties p = new Properties();

    public DS() {
        init();
    }

    public void init() {
        try {
            p.load(new FileInputStream(CONFIG_PATH));
            Class.forName(p.getProperty("driver"));
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(p.getProperty("url"), p.getProperty("login"), p.getProperty("password"));
    }
}