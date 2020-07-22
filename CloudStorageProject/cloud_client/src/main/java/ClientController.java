import com.sun.istack.internal.Nullable;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ClientController {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String serverAnswer = "";
    private String nickname;

    public ClientController(String serverHost, int serverPort) throws IOException {
        socket = new Socket(serverHost, serverPort);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        Scanner sc = new Scanner(System.in);
        nickname = in.readUTF();
        System.out.println(nickname);

        new Thread(() -> {
            while (true) {
                try {
                    // Комманды
                    String command = sc.nextLine();

                    if(command.equals("exit")){
                        sc.close();
                        out.writeUTF(command);
                        System.exit(0);
                    }
                    else if(command.equals("info")){
                        out.writeUTF(command);
                        while(!serverAnswer.equals("fin")){
                            serverAnswer = in.readUTF();
                            System.out.println(serverAnswer);
                        }
                    }
                    else if(command.equals("download")){
                        out.writeUTF(command);
                        uploadFileFromServer();
                    }
                    else if(command.equals("send")){
                        out.writeUTF(command);
                        sendFile(new File("./cloud_client/client/user-0/1.txt"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void uploadFileFromServer() throws IOException {
        String fileName = in.readUTF();
        System.out.println("Скачивается " + fileName);

        File file = new File("./cloud_client/client/" + nickname + "/" + fileName);

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
}