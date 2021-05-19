package DB;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public interface AuthService {
    void start();

    String getNickLoginPass(String login, String password);

    void stop();
}