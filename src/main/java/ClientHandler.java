import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private MyServer server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String name = "";

    public ClientHandler(Socket socket){
        try {
            this.server = MyServer.getServer();
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            new Thread(() -> {
                try {
                    auth();
                    readMsg();
                }catch (IOException e){
                    e.printStackTrace();
                }finally {
                    closeUser();
                }
            }).start();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void auth() throws IOException{
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(120000);
                sendMsg("Вы отключены за превышение времени ожидания подключения к серверу");
                sendMsg("/end");
                closeConnection();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        thread.setDaemon(true);
        thread.start();
        while (socket.isConnected()){
            String str = in.readUTF();
            if(str.startsWith("/auth")){
                String [] parts = str.split(" ");
                String login = parts[1];
                String password = parts[2];
                String nick = server.getAuthService().getNickLoginPass(login, password);
                if (nick != null){
                    if (!server.isNickBusy(nick)){
                        sendMsg("/authok " + nick);
                        name = nick;
                        server.broadcastMsg(name + " зашел в чат");
                        server.subscribe(this);
                        thread.stop();
                        return;
                    }else {
                        sendMsg("Ник занят");
                    }
                }else {
                    sendMsg("Неверные логин/пароль");
                }
            }else {
                sendMsg("Перед тем как отправлять сообщение  авторизуйтесь через команду </auth login pass>");
            }
        }
    }

    public void sendMsg(String msg){
        try {
            out.writeUTF(msg);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void readMsg () throws IOException{
        while (true){
            String strFormClient = in.readUTF();

            if(strFormClient.startsWith("/w ")){
                strFormClient = strFormClient.replace("/w ", "");
                String fromName = strFormClient.substring(0, strFormClient.indexOf(" "));
                strFormClient = strFormClient.replace(fromName+" ", "");
                server.privateMsg(name, fromName, strFormClient );
            }else {
                System.out.println(name + ": " + strFormClient);
                if (strFormClient.equals("/end")) {
                    return;
                }
                server.broadcastMsg(name + ": " + strFormClient);
            }
        }
    }

    public void closeUser(){
        server.unsubscribe(this);
        server.broadcastMsg(name + " вышел из чата");
        closeConnection();
    }

    public void closeConnection() {
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getName() {
        return name;
    }
}