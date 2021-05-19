import DB.DBChangeNick;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;

public class ClientHandler {
    private MyServer server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String name = "";
    private String login = "";

    private Logger LOG = LogManager.getLogger(MyServer.class.getName());


    public ClientHandler(Socket socket) {
        try {
            this.server = MyServer.getServer();
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            try {
                auth();
                readMsg();
            } catch (IOException e) {
                LOG.error(e.toString());
            } finally {
                closeUser();
            }
        } catch (IOException e) {
            LOG.error(e.toString());
        }
    }

    private void auth() throws IOException {
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(120000);
                sendMsg("Вы отключены за превышение времени ожидания подключения к серверу");
                sendMsg("/end");
                closeConnection();
                LOG.info("Не авторизованный пользователь отключен из-за превышения времени ожидания");
            } catch (InterruptedException e) {
            }
        });
        thread.setDaemon(true);
        thread.start();
        while (socket.isConnected()) {
            String str = in.readUTF();
            if (str.startsWith("/auth")) {
                String[] parts = str.split(" ");
                login = parts[1];
                String password = parts[2];
                String nick = server.getAuthService().getNickLoginPass(login, password);
                if (nick != null) {
                    if (!server.isNickBusy(nick)) {
                        sendMsg("/authok " + nick);
                        name = nick;
                        server.broadcastMsg(name + " зашел в чат");
                        server.subscribe(this);
                        thread.interrupt();
                        LOG.info("Пользователь под ником {} вошел в в чат", name);
                        return;
                    } else {
                        LOG.warn("Повторная попытка войти в чат пользователя под ником {} ", name);
                        sendMsg("Данный пользователь уже в системе");
                    }
                } else {
                    LOG.warn("Неверный запрос на авторизацию");
                    sendMsg("Неверные логин/пароль");
                }
            } else {
                LOG.warn("Неверный запрос на авторизацию");
                sendMsg("Перед тем как отправлять сообщение  авторизуйтесь через команду </auth login pass>");
            }
        }
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            LOG.error(e.toString());
        }
    }

    public void readMsg() throws IOException {
        while (true) {
            String strFormClient = in.readUTF();

            if (strFormClient.startsWith("/w ")) {
                strFormClient = strFormClient.replace("/w ", "");
                if (strFormClient.indexOf(" ") > 0) {
                    String fromName = strFormClient.substring(0, strFormClient.indexOf(" "));
                    strFormClient = strFormClient.replace(fromName + " ", "");
                    server.privateMsg(name, fromName, strFormClient);
                }
            } else if (strFormClient.startsWith("/changeNick ")) {
                String[] parts = strFormClient.split(" ");
                if (parts.length == 2) {
                    DBChangeNick dbChangeNick = new DBChangeNick();
                    dbChangeNick.start();
                    try {
                        if (dbChangeNick.updateNick(login, parts[1])) {
                            server.unsubscribe(name);
                            server.subscribe(this);
                            LOG.info("Пользователь под ником {} сменил его на {}", name, parts[1]);
                            name = parts[1];

                        } else {
                            LOG.warn("Пользователь под ником {} пытался сменить его на уже занятый", name);
                            server.serverMsg(name, "Данный ник уже занят");
                        }
                    } catch (SQLException e) {
                        LOG.error(e.toString());
                        server.serverMsg(name, "Ошибка при обновлении ника, повторите попытку");
                    }
                } else {
                    LOG.warn("Пользователь под ником {} в неправильном формате ввел запрос для смены ника", name);
                    server.serverMsg(name, "Для смены ника нужна запись формата \"/changeNick newNick\" ");
                }
            } else {
                LOG.trace("{}: {}", name, strFormClient);
                if (strFormClient.equals("/end")) {
                    return;
                }
                server.broadcastMsg(name + ": " + strFormClient);
            }
        }
    }

    public void closeUser() {
        server.unsubscribe(name);
        LOG.info("{} вышел из чата", name);
        server.broadcastMsg(name + " вышел из чата");
        closeConnection();
    }

    public void closeConnection() {
        try {
            in.close();
        } catch (IOException e) {
            LOG.error(e.toString());
        }
        try {
            out.close();
        } catch (IOException e) {
            LOG.error(e.toString());
        }
        try {
            socket.close();
        } catch (IOException e) {
            LOG.error(e.toString());
        }
    }

    public String getName() {
        return name;
    }
}