import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class NetworkServer {
    private final int port;
    private final List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    static int id = 0;

    public NetworkServer(int port) {
        this.port = port;
    }

    public void start() {
        try(ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Сервер был успешно запущен на порту " + port);

            while (true){
                System.out.println("Ожидание клиентского подключения...");

                Socket clientSocket = serverSocket.accept();

                System.out.println("Клиент подключился");
                // ClientHandler - Логика подключения клиентов с которыми работаем
                // clients.add(new ClientHandler(this, clientSocket));
                createClientHandler(clientSocket);
            }
        } catch (IOException e) {
            System.out.println("Ошибка при работе сервера");
            e.printStackTrace();
        }
    }

    private void createClientHandler(Socket clientSocket) {
        ClientHandler clientHandler = new ClientHandler(this, clientSocket, "user-" + id);
        id++;
        clientHandler.run();
    }

    public /*synchronized*/ void unsubscribe(ClientHandler clientHandler) throws IOException {
        clients.remove(clientHandler);
    }

    public /*synchronized*/ void subscribe(ClientHandler clientHandler) throws IOException {
        clients.add(clientHandler);
    }
}
