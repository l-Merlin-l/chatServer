import DB.AuthService;
import DB.DBAuthService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MyServer {
    private static MyServer server;
    private final int PORT = 8080;
    private Map<String, ClientHandler> clients;
    private AuthService authService;

    private static final Logger LOG = LogManager.getLogger(MyServer.class.getName());

    public static MyServer getServer() {
        return server;
    }

    public MyServer() {
        server = this;
        LOG.trace("Сервер запущен");
        try (ServerSocket server = new ServerSocket(PORT)) {
            authService = new DBAuthService();
            authService.start();
            clients = new HashMap<>();
            ExecutorService clients = Executors.newCachedThreadPool();
            while (true) {
                Socket socket = server.accept();
                clients.execute(() -> new ClientHandler(socket));
            }
        } catch (IOException e) {
            LOG.error(e);
        }
    }

    public synchronized void unsubscribe(String nick) {
        clients.remove(nick);
    }

    public synchronized void subscribe(ClientHandler clientHandler) {
        clients.put(clientHandler.getName(), clientHandler);
    }

    public synchronized void broadcastMsg(String msg) {
        clients.forEach((k, client) -> client.sendMsg(msg));
    }

    public synchronized void privateMsg(String name, String fromName, String msg) {
        if (clients.containsKey(fromName)) {
            clients.get(fromName).sendMsg("Личное сообщение от " + name + ": " + msg);
            clients.get(name).sendMsg("Личное сообщение для " + fromName + ": " + msg);
        }
    }

    public synchronized void serverMsg(String fromName, String msg) {
        clients.get(fromName).sendMsg(msg);
    }


    public synchronized boolean isNickBusy(String nick) {
        return clients.containsKey(nick);
    }

    public AuthService getAuthService() {
        return authService;
    }
}

