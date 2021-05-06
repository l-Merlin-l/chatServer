package DB;

import java.sql.*;

public class DBAuthService extends DBConnection implements AuthService{
//    Connection connection;
//
//    {
//        try {
//            Class.forName("org.postgresql.Driver");
//            connection = DriverManager.getConnection(Configuration.DB_HOST, Configuration.DB_USER, Configuration.DB_PASSWORD);
//        } catch (ClassNotFoundException | SQLException e) {
//            e.printStackTrace();
//        }
//    }
//    @Override
//    public void start() {
//    }

    @Override
    public String getNickLoginPass(String login, String password) {
        try ( PreparedStatement statement = connection.prepareStatement("select user_nick from users where user_login = ? and user_password = ?")){
            statement.setString(1, login);
            statement.setString(2, password);
            try (ResultSet userNickResultSet = statement.executeQuery();){
                if(userNickResultSet.next()){
                    return userNickResultSet.getString("user_nick");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

//    @Override
//    public void stop() {
//        try {
//            connection.close();
//        } catch (SQLException throwables) {
//            throwables.printStackTrace();
//        }
//    }
}
