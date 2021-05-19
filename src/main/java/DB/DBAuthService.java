package DB;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;


public class DBAuthService extends DBConnection implements AuthService {

    Logger LOG = LogManager.getLogger(DBAuthService.class.getName());
    @Override
    public String getNickLoginPass(String login, String password) {
        try (PreparedStatement statement = connection.prepareStatement("select user_nick from users where user_login = ? and user_password = ?")) {
            statement.setString(1, login);
            statement.setString(2, password);
            try (ResultSet userNickResultSet = statement.executeQuery();) {
                if (userNickResultSet.next()) {
                    return userNickResultSet.getString("user_nick");
                }
                LOG.info("Попытка войти в чат с неверными данными логин/пароль");
            }
        } catch (SQLException e) {
            LOG.error(e);
        }
        return null;
    }
}
