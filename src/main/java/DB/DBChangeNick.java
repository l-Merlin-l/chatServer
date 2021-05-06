package DB;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBChangeNick extends DBConnection{
    public boolean updateNick(String login, String newNick) throws SQLException{
        try(PreparedStatement statement = connection.prepareStatement("select * from users where user_nick = ?")){
            statement.setString(1, newNick);
            try (ResultSet resultSet = statement.executeQuery()){
                if(resultSet.next()){
                    return false;
                }
            }
        }
        try(PreparedStatement statement = connection.prepareStatement("update users set user_nick = ? where user_login = ?")){
            statement.setString(1, newNick);
            statement.setString(2, login);
            statement.executeUpdate();
            return true;
        }
    }
}
