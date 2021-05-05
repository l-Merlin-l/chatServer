public interface AuthService {
    void start();
    String getNickLoginPass(String login, String password);
    void stop();
}