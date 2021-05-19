package DB;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public abstract class DBConnection {
    Connection connection;
    private Logger LOG = LogManager.getLogger(DBConnection.class.getName());
    public void start(){
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(Configuration.DB_URL, Configuration.DB_USER, Configuration.DB_PASSWORD);
            LOG.trace("DB connected");
        } catch (ClassNotFoundException | SQLException e) {
            LOG.error(e.toString());
        }
    }

    public void stop() {
        try {
            connection.close();
            LOG.trace("DB disconnect");
        } catch (SQLException e) {
            LOG.error(e.toString());
            e.printStackTrace();
        }
    }
}
