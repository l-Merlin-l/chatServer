package DB;

import java.sql.*;

public class DBAuthService extends DBConnection implements AuthService {
    @Override
    public String getNickLoginPass(String login, String password) {
        try (PreparedStatement statement = connection.prepareStatement("select user_nick from users where user_login = ? and user_password = ?")) {
            statement.setString(1, login);
            statement.setString(2, password);
            try (ResultSet userNickResultSet = statement.executeQuery();) {
                if (userNickResultSet.next()) {
                    return userNickResultSet.getString("user_nick");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
