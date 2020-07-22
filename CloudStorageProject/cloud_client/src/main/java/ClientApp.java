import java.io.IOException;

public class ClientApp {
    public static void main(String[] args) throws IOException {
        try {
            ClientController clientController = new ClientController("localhost", 8189);
        } catch (IOException e) {
            System.err.println("Failed to connect to server! Please, check you network settings");
        }
    }
}
