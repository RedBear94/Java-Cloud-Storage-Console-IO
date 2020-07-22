import java.io.*;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientHandler {
    private final NetworkServer networkServer;
    private final ExecutorService executor;
    private final Socket clientSocket;
    private DataInputStream in;
    private DataOutputStream out;
    private String nickname;
    private String command;

    public ClientHandler(NetworkServer networkServer, Socket socket, String nickname) {
        this.networkServer = networkServer;
        this.clientSocket = socket;
        this.nickname = nickname;
        executor = Executors.newCachedThreadPool();
    }
    public void run() {
        doHandle(clientSocket);
    }

    private void doHandle(Socket socket) {
        try {
            out = new DataOutputStream(socket.getOutputStream());

            in = new DataInputStream(socket.getInputStream());

            out.writeUTF(nickname);
            createDirectory("./cloud_server/server/" + nickname);

            executor.execute(
                    new Thread(() -> {
                        try {
                            networkServer.subscribe(this);
                            while (true){
                                // Комманды
                                command = in.readUTF();
                                if(command.equals("exit")) {
                                    break;
                                }
                                else if(command.equals("info")) {
                                    sendInfo();
                                }
                                else if(command.equals("download")){
                                    System.out.println("Запрос на скачивание файла");
                                    sendFile(new File("./cloud_server/server/user-0/2.txt"));
                                }
                                else if(command.equals("send")){
                                    uploadFileFromClient();
                                }
                                continue;
                            }
                        } catch (IOException e){
                            System.out.println("Соеденинение с клиентом " + nickname + " было закрыто!");
                        } finally {
                            System.out.println("Клиент " + nickname + " отключился");
                            closeConnection();
                        }
                    })
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendInfo() throws IOException {
        System.out.println("Клиент " + nickname + " запросил список файлов у сервера");
        File dir = new File("./cloud_server/server/" + nickname); //path указывает на директорию
        /*List<File> filesArrayList = new ArrayList<>();*/
        for ( File file : dir.listFiles() ){
            if ( file.isFile() )
                out.writeUTF(file.getCanonicalPath());
        }
        out.writeUTF("fin");
    }

    public void sendFile(File file) throws IOException {
        FileInputStream fileIn = new FileInputStream(file);

        out.writeUTF(file.getName());

        byte[] buf = new byte[Short.MAX_VALUE];
        int bytesRead;
        while( (bytesRead = fileIn.read(buf)) != -1 ) {
            out.writeShort(bytesRead);
            out.write(buf,0,bytesRead);
        }
        out.writeShort(-1);
        fileIn.close();
        System.out.println("Файл отправлен");;
    }

    private void uploadFileFromClient() throws IOException {
        String fileName = in.readUTF();
        System.out.println("Скачивается " + fileName);

        File file = new File("./cloud_server/server/" + nickname + "/" + fileName);

        file.createNewFile();

        FileOutputStream os = new FileOutputStream(file);
        byte[] buf = new byte[Short.MAX_VALUE];
        int bytesSent;
        while( (bytesSent = in.readShort()) != -1 ) {
            in.readFully(buf,0,bytesSent);
            os.write(buf,0,bytesSent);
        }
        os.close();
        System.out.println("File uploaded!");
    }

    public static void createDirectory(String dirName) throws IOException {
        File file = new File(dirName);
        if (!file.exists()) {
            file.mkdir();
        }
    }

    private void closeConnection() {
        try {
            networkServer.unsubscribe(this);
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
