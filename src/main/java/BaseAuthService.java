import java.util.HashMap;
import java.util.Map;

public class BaseAuthService implements AuthService{
    Map<String, User> users;

    @Override
    public void start() {
        users = new HashMap<>();
        users.put("login1", new User("login1", "pass1", "nick1"));
        users.put("login2", new User("login2", "pass2", "nick2"));
        users.put("login3", new User("login3", "pass3", "nick3"));
    }

    @Override
    public String getNickLoginPass(String login, String password) {
        User user = users.get(login);
        if(user != null && user.getPassword().equals(password)){
            return user.getNick();
        }
        return null;
    }

    @Override
    public void stop() {
        System.out.println("Сервис остановился");
    }
}
