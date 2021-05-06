package DB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public abstract class DBConnection {
    Connection connection;
    public void start(){
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(Configuration.DB_HOST, Configuration.DB_USER, Configuration.DB_PASSWORD);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
